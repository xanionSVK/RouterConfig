package sk.xanion.routerconfig.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.jsoup.helper.Validate;

/**
 * Created by Michal Košík on 24. 10. 2016.
 * misko.kosik@gmail.com
 */

public class Settings {

    private static final Long DEFAULT_LONG = -1L;
    private static final String PREFS_FILE = "sk.iternal.mais.studentapp.PREFES_FILE_KEY";

    private static final String KEY_URL = "sk.xanion.routerConfig.KEY_URL";
    private static final String KEY_SSID = "sk.xanion.routerConfig.key_studium";
    private static final String KEY_BLOCKED_MAC_ = "sk.xanion.routerConfig.KEY_BLOCKED_MAC_";

    /**
     * @param ctx context
     * @return current token stored in settings
     */
    public static String readUrl(Context ctx) {
        return read(ctx, KEY_URL);
    }

    public static void saveUrl(Context ctx, String token) {
        save(ctx, KEY_URL, token);
    }

    /**
     * @param ctx context
     * @return current token stored in settings
     */
    public static String readSSID(Context ctx) {
        return read(ctx, KEY_SSID);
    }

    public static void saveSSID(Context ctx, String token) {
        save(ctx, KEY_SSID, token);
    }

    /**
     * @param ctx context
     * @return current token stored in settings
     */
    public static String readBlockedMac(Context ctx, int idx) {
        return read(ctx, KEY_SSID + idx);
    }

    public static void saveBlockedMac(Context ctx, String macAdress, int idx) {
        save(ctx, KEY_SSID + idx, macAdress);
    }

    /**
     * @param ctx   context
     * @param key   key of the property
     * @param value value of the property to store
     */
    private static void save(Context ctx, String key, String value) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * @param ctx context
     * @param key key of the property
     * @return current value for the given key stored in settings
     */
    private static String read(Context ctx, String key) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        return prefs.getString(key, null);
    }

    /**
     * Saves value of the property to settings
     *
     * @param ctx   context
     * @param key   key of the property
     * @param value value of the property to store
     */
    private static void saveLong(Context ctx, String key, Long value) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(key, value == null ? DEFAULT_LONG : value);
        editor.apply();
    }

    /**
     * @param ctx context
     * @param key key of the property
     * @return current value for the given key stored in settings
     */
    private static Long readLong(Context ctx, String key) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        long result = prefs.getLong(key, DEFAULT_LONG);
        return result == DEFAULT_LONG ? null : result;
    }
}
