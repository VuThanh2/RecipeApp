package Login;

import android.content.Context;
import android.content.SharedPreferences;

public final class SessionManager {
    private static final String PREF = "app_session";
    private static final String KEY_USERNAME = "current_username";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private SessionManager(){}

    public static void setCurrentUsername(Context ctx, String username) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit()
                .putString(KEY_USERNAME, username == null ? "" : username.trim().toLowerCase())
                .apply();
    }

    public static String getCurrentUsername(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String u = sp.getString(KEY_USERNAME, "");
        return u == null ? "" : u.trim().toLowerCase();
    }

    public static void clear(Context ctx){
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().clear().apply();
    }

    public static void setLoggedIn(Context ctx, boolean value) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_IS_LOGGED_IN, value)
                .apply();
    }

    public static boolean isLoggedIn(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getBoolean(KEY_IS_LOGGED_IN, false);
    }
}