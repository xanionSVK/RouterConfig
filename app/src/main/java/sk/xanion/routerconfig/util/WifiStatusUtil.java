package sk.xanion.routerconfig.util;

import android.content.Context;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * Created by mkosik on 23. 10. 2016.
 */

public class WifiStatusUtil {

    private static final String WIFI_SSID = "tplnk";

    public static boolean isConnectedToHomeWifi(Context ctx) {
        try {
            WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                return WIFI_SSID.equals(wifiInfo.getBSSID());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
