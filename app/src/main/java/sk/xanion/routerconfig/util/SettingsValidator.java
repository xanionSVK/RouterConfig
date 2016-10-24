package sk.xanion.routerconfig.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Patterns;

import java.util.regex.Pattern;

import sk.xanion.routerconfig.R;

/**
 * Created by Michal Košík on 24. 10. 2016.
 * misko.kosik@gmail.com
 */

public class SettingsValidator {

    private static final Pattern MAC_ADRESS_PATERN = Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");

    public static boolean isRouterUrlValid(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        if (!url.startsWith("http://")) {
            return false;
        }
        try {
            return Patterns.IP_ADDRESS.matcher(url.substring(7)).matches();
        } catch (Exception e) {
           e.printStackTrace();
            return false;
        }
    }

    public static boolean isMacAdressValid(String macAdress) {
        if (TextUtils.isEmpty(macAdress)) {
            return false;
        }
        if (!MAC_ADRESS_PATERN.matcher(macAdress).matches()) {
            return false;
        }
        return true;
    }


    public static String validate(Context ctx, String url, String login, String password, String blockedMac, String ssid) {
        if (!isRouterUrlValid(url)) {
            return ctx.getString(R.string.errorInvalidRouterUrl);
        }
        if (TextUtils.isEmpty(login)) {
            return ctx.getString(R.string.errorInvalidLogin);
        }

        if (TextUtils.isEmpty(password)) {
            return ctx.getString(R.string.errorInvalidPassword);
        }

        if (!isMacAdressValid(blockedMac)) {
            return ctx.getString(R.string.errorInvalidMacAdress);
        }

        if (TextUtils.isEmpty(ssid)) {
            return ctx.getString(R.string.errorInvalidSSID);
        }
        return null;
    }

    public static String validate(Context ctx) {
        if (!isRouterUrlValid(Settings.readUrl(ctx))) {
            return ctx.getString(R.string.errorInvalidRouterUrl);
        }

        if (TextUtils.isEmpty(Settings.readPassword(ctx))) {
            return ctx.getString(R.string.errorInvalidPassword);
        }

        if (!isMacAdressValid(Settings.readBlockedMac(ctx, 1))) {
            return ctx.getString(R.string.errorInvalidMacAdress);
        }

        if (TextUtils.isEmpty(Settings.readSSID(ctx))) {
            return ctx.getString(R.string.errorInvalidSSID);
        }
        return null;
    }
}
