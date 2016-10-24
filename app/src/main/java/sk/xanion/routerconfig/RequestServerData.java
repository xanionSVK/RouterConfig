package sk.xanion.routerconfig;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;

import sk.xanion.routerconfig.model.WirelessStatus;
import sk.xanion.routerconfig.util.WifiStatusUtil;

/**
 * Created by mkosik on 21. 10. 2016.
 */

public class RequestServerData extends AsyncTask<Void, Void, Bundle> {

    public static final String KEY_WIRELESS_STATUS = "sk.xanion.routerconfig.KEY_WIRELESS_STATUS";
    public static final String KEY_CHANGE_STATUS = "sk.xanion.routerconfig.KEY_CHANGE_STATUS";

    public static final int METHOD_BLOCK = 1;
    public static final int METHOD_UNBLOCK = 2;
    public static final int METHOD_STATUS = 3;

    private static final int CONNECTION_TIMEOUT = 30000;
    private static final int READ_TIMEOUT = 30000;
    private RequestServerDataListener mListener;
    private Context mCtx;
    private int mMethodType;

    /**
     * Async task pre volanie routra
     *
     * @param ctx        context
     * @param methodType 1 - pre zapnutie blokovania, 2 - pre vypnutie blokovania, 3 - vrati aktualny status blokovania
     */
    public RequestServerData(Context ctx, int methodType) {
        if (ctx == null) {
            throw new IllegalArgumentException("Context can not be null.");
        }
        this.mCtx = ctx;
        if (ctx instanceof RequestServerDataListener) {
            this.mListener = (RequestServerDataListener) ctx;
        }
        this.mMethodType = methodType;

    }


    /**
     * Async task pre volanie routra
     *
     * @param listener   listener
     * @param ctx        context
     * @param methodType 1 - pre zapnutie blokovania, 2 - pre vypnutie blokovania, 3 - vrati aktualny status blokovania
     */
    public RequestServerData(Context ctx, RequestServerDataListener listener, int methodType) {
        if (ctx == null) {
            throw new IllegalArgumentException("Context can not be null.");
        }
        this.mCtx = ctx;
        this.mListener = listener;
        this.mMethodType = methodType;

    }

    public interface RequestServerDataListener {
        /**
         * Called after requst is done.
         *
         * @param ctx        - context that called
         * @param result     result bundle
         * @param methodType method type that was called
         */
        public void onPostExecute(Context ctx, Bundle result, int methodType);

        public void onPreexecute(int methodType);
    }

    private static boolean activated = true;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.i("RequestServerData", mMethodType + " onPreExecute");
        if (mListener != null) {
            mListener.onPreexecute(mMethodType);
        }
    }

    @Override
    protected Bundle doInBackground(Void... voids) {
        Bundle result = null;
        boolean isHomeWifi = WifiStatusUtil.isConnectedToHomeWifi(mCtx);
        if (!isHomeWifi) {
            try {
                Thread.sleep(new Random().nextInt(5) * 1000L);
            } catch (InterruptedException e) {
            }
        }
        switch (mMethodType) {
            case METHOD_BLOCK:
            case METHOD_UNBLOCK: {
                if (isHomeWifi) {
                    result = changeStatus();
                } else {
                    activated = !activated;
                    result = new Bundle();
                    result.putString("result", "Volanie bolo úspešné");
                    result.putBoolean(KEY_CHANGE_STATUS, activated);
                }
                break;
            }
            case 3: {
                result = new Bundle();
                WirelessStatus status = null;
                if (isHomeWifi) {
                    status = getWirelessStatus();
                } else {
                    status = new WirelessStatus();
                    status.active = activated;
                    status.macAdresses = new ArrayList<>();
                    status.macAdresses.add("bez wifi pridana mcadresa");
                    result.putBoolean(KEY_CHANGE_STATUS, activated);
                }
                result.putSerializable(KEY_WIRELESS_STATUS, status);
                break;
            }
        }//result.clear();
        return result;
    }

    private Bundle changeStatus() {
        final String TAG = "RequestServerData";
        String serverUrl = "http://192.168.1.1/";
        String requestUrl = serverUrl + getMethod();
        URL url;
        Bundle result = new Bundle();
        HttpURLConnection conn = null;
        try {
            url = new URL(requestUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            String postData = generateParams();
            byte[] data = postData.getBytes("UTF-8");
            //stream headers
            conn.setRequestProperty("Content-Length", data.length + "");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0");
            conn.setRequestProperty("Accept-Language", "sk,cs;q=0.8,en-US;q=0.5,en;q=0.3");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            conn.setRequestProperty("DNT", "1");
            conn.setRequestProperty("Referer", "http://192.168.1.1/basic/home_wlan.htm");
            conn.setRequestProperty("Authorization", "Basic YWRtaW46d0g3QkRM");
            conn.setRequestProperty("Connection", "keep-alive");

            OutputStream os = conn.getOutputStream();
            os.write(data);

            int responseCode = -1;
            try {
                responseCode = conn.getResponseCode();
            } catch (IOException io) {
                //router vie hodit hodit connection timeout exception
                //TODO je to skutocne preto?
                if (!io.getMessage().contains("ETIMEDOUT")) {
                    throw io;
                }
            }

            switch (responseCode) {
                //didnt get response code, but change is succesful
                case -1: {
                    result.putString("result", "Volanie bolo úspešné");
                    result.putBoolean(KEY_CHANGE_STATUS, true);
                }
                case HttpsURLConnection.HTTP_UNAUTHORIZED:
                case HttpURLConnection.HTTP_SEE_OTHER: {
                    //OK
                    switch (mMethodType) {
                        case METHOD_UNBLOCK: {
                            result.putString("result", "blokovanie bolo vypnuté");
                        }
                        case METHOD_BLOCK:
                            result.putString("result", "blokovanie bolo zapnuté");
                            break;
                    }
                    break;
                }
                default: {
                    String line;
                    BufferedReader br = null;
                    if (conn.getErrorStream() != null) {
                        String response = "";
                        try {
                            br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));

                            while ((line = br.readLine()) != null) {
                                response += line;
                            }
                            Log.d(TAG, TextUtils.isEmpty(response) ? "empty error" : response);
                        } catch (Exception e) {
                            response = e.getMessage();
                            response = TextUtils.isEmpty(response) ? "empty error" : response;
                            Log.e(TAG, response);
                            result.putString("error", response);
                        }
                    }
                }
            }
        } catch (MalformedURLException e) {
            // should not happen
            Log.e(TAG, e.getMessage());
        } catch (SocketTimeoutException e) {
            Log.e(TAG, "SocketTimeoutException Occured");
            result.putString("error", "Nepodarilo sa spojit so serverom");
        } catch (SSLHandshakeException sslEx) {
            Log.e(TAG, "SSLHandshakeException Occured");
            result.putString("error", "HTTPS pripojenie nie je podporovane");
        } catch (IOException e) {
            if (e.getMessage() != null) {
                Log.e(TAG, "IOException Occured. " + e.getMessage());
            } else {
                Log.e(TAG, "IOException Occured.");
            }
            try {
                if (conn != null && conn.getResponseCode() == 401) {
                    result.putString("error", "Neplatné prihlasovacie údaje");
                }
            } catch (IOException e1) {
                if (e.getMessage() != null) {
                    Log.e(TAG, e.getMessage());
                }
            }
        } catch (Exception e) {
            if (e.getMessage() != null) {
                Log.e(TAG, "IOException Occured. " + e.getMessage());
            } else {
                Log.e(TAG, "IOException Occured.");
            }
            result.putString("error", "Dáka iná chyba");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return result;
    }

    public static WirelessStatus getWirelessStatus() {
        WirelessStatus status = new WirelessStatus();
        try {
            Document doc = Jsoup.connect("http://192.168.1.1/basic/home_wlan.htm").header("Authorization", "Basic YWRtaW46d0g3QkRM").get();
            Elements macs = doc.getElementsByAttributeValue("NAME", "WLANFLT_MAC");
            Elements radios = doc.getElementsByAttributeValue("NAME", "WLAN_FltActive");
            if (radios != null) {
                Boolean active = null;
                List<String> adreses = new ArrayList<>();
                for (Element radio : radios) {
                    if (radio.val().equals("1") && radio.hasAttr("checked")) {
                        active = true;
                        break;
                    } else {
                        if (radio.val().equals("0") && radio.hasAttr("checked")) {
                            active = false;
                            break;
                        }
                    }
                }
                if (macs != null) {
                    for (Element mac : macs) {
                        if (!TextUtils.isEmpty(mac.val())) {
                            adreses.add(mac.val());
                        }
                    }
                }
                status.active = active;
                status.macAdresses = adreses;
            }
        } catch (Exception e) {
            e.printStackTrace();
            status.exception = e.getMessage();
        }

        return status;
    }

    private String getMethod() {
        String result = null;
        switch (mMethodType) {
            case METHOD_BLOCK:
            case METHOD_UNBLOCK: {
                result = "Forms/home_wlan_1";
                break;
            }
            case 3: {
                break;
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(Bundle bundle) {
        Log.i("RequestServerData", mMethodType + " onPostExecute");
        if (mListener != null) {
            mListener.onPostExecute(this.mCtx, bundle, mMethodType);
        }
    }

    /**
     * Vygeneruje parametre podla typu volania
     * 1 - pre zapnutie blokovania
     * 2 - pre vypnutie blokovania
     *
     * @return
     */
    private String generateParams() {
        String result = null;
        switch (mMethodType) {
            case METHOD_BLOCK: {
                result = "wlanWEBFlag=0&AccessFlag=0&wlan_APenable=1&Countries_Channels=SLOVAKIA&Channel_ID=00000000&AdvWlan_slPower" +
                        "=High&BeaconInterval=100&RTSThreshold=2347&FragmentThreshold=2346&DTIM=1&WirelessMode=802.11b%2Bg%2Bn" +
                        "&WLANChannelBandwidth=20%2F40+MHz&WLANGuardInterval=AUTO&WLANMCS=AUTO&WLSSIDIndex=1&ESSID_HIDE_Selection" +
                        "=0&UseWPS_Selection=0&WPSMode_Selection=1&ESSID=tplnk&WEP_Selection=WPA-PSK%2FWPA2-PSK&TKIP_Selection" +
                        "=TKIP%2FAES&PreSharedKey=55preskakujucichpanacikov&WDSMode_Selection=0&WLAN_FltActive=1&WLAN_FltAction" +
                        "=00000001&WLANFLT_MAC=a4%3Aa4%3Aa4%3Aa4%3Aa4%3Aa4&WLANFLT_MAC=00%3A00%3A00%3A00%3A00%3A00&WLANFLT_MAC" +
                        "=00%3A00%3A00%3A00%3A00%3A00&WLANFLT_MAC=00%3A00%3A00%3A00%3A00%3A00&WLANFLT_MAC=00%3A00%3A00%3A00%3A00" +
                        "%3A00&WLANFLT_MAC=00%3A00%3A00%3A00%3A00%3A00&WLANFLT_MAC=00%3A00%3A00%3A00%3A00%3A00&WLANFLT_MAC=00" +
                        "%3A00%3A00%3A00%3A00%3A00&wlanRadiusWEPFlag=0";
                break;
            }
            case METHOD_UNBLOCK: {
                result = "wlanWEBFlag=0&AccessFlag=0&wlan_APenable=1&Countries_Channels=SLOVAKIA&Channel_ID=00000000&AdvWlan_slPower" +
                        "=High&BeaconInterval=100&RTSThreshold=2347&FragmentThreshold=2346&DTIM=1&WirelessMode=802.11b%2Bg%2Bn" +
                        "&WLANChannelBandwidth=20%2F40+MHz&WLANGuardInterval=AUTO&WLANMCS=AUTO&WLSSIDIndex=1&ESSID_HIDE_Selection" +
                        "=0&UseWPS_Selection=0&WPSMode_Selection=1&ESSID=tplnk&WEP_Selection=WPA-PSK%2FWPA2-PSK&TKIP_Selection" +
                        "=TKIP%2FAES&PreSharedKey=55preskakujucichpanacikov&WDSMode_Selection=0&WLAN_FltActive=0&wlanRadiusWEPFlag" +
                        "=0";
                break;
            }
        }
        return result;
    }
}
