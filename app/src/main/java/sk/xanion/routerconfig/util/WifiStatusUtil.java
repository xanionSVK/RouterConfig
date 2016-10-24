package sk.xanion.routerconfig.util;

import android.content.Context;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

/**
 *
 * Created by mkosik on 23. 10. 2016.
 */
public class WifiStatusUtil {
    public static boolean isConnectedToHomeWifi(Context ctx) {
        try {
            WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                return !TextUtils.isEmpty(wifiInfo.getBSSID()) && wifiInfo.getBSSID().equals(Settings.readSSID(ctx));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
