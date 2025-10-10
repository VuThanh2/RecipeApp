package Login;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
    // ---- JSON keys (kept literal to avoid coupling to UserDataManager internals)
    public static final String KEY_USERNAME     = "username";
    public static final String KEY_PASSWORD     = "password";
    public static final String KEY_FOOD_PREF    = "foodPreference"; // legacy
    public static final String KEY_DIET_MODE    = "dietMode";       // new

    // ---- Diet modes (same set used in UserDataManager)
    public static final String MODE_NORMAL       = "normal";
    public static final String MODE_VEGAN        = "vegan";
    public static final String MODE_KETO         = "keto";
    public static final String MODE_GLUTEN_FREE  = "gluten_free";

    private String username;
    private String password;       // NOTE: kept for compatibility; consider hashing later
    private String dietMode;       // normalized value
    private String foodPreference; // legacy/raw text (optional; can be empty)

    public User() {}

    public User(String username, String password, String dietMode) {
        this.username = username;
        this.password = password;
        this.dietMode = sanitizeDietMode(dietMode);
        this.foodPreference = "";
    }

    // ---- Getters/Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getDietMode() { return dietMode; }
    public void setDietMode(String dietMode) { this.dietMode = sanitizeDietMode(dietMode); }

    public String getFoodPreference() { return foodPreference; }
    public void setFoodPreference(String foodPreference) { this.foodPreference = foodPreference; }

    // ---- JSON bridge
    public static User fromJson(JSONObject j) {
        User u = new User();
        u.username       = j.optString(KEY_USERNAME, "");
        u.password       = j.optString(KEY_PASSWORD, "");
        u.foodPreference = j.optString(KEY_FOOD_PREF, "");

        String dm = j.optString(KEY_DIET_MODE, null);
        if (!isValidDietMode(dm)) {
            dm = mapFoodPreference(u.foodPreference);
        }
        u.dietMode = sanitizeDietMode(dm);
        return u;
    }

    public JSONObject toJson() {
        JSONObject j = new JSONObject();
        try {
            j.put(KEY_USERNAME, username == null ? "" : username);
            j.put(KEY_PASSWORD, password == null ? "" : password);
            j.put(KEY_FOOD_PREF, foodPreference == null ? "" : foodPreference);
            j.put(KEY_DIET_MODE, sanitizeDietMode(dietMode));
        } catch (JSONException e) {
            // swallow or log as needed
        }
        return j;
    }

    // ---- Helpers (same logic as in UserDataManager; duplicated here for independence)
    private static boolean isValidDietMode(String dietMode) {
        if (dietMode == null) return false;
        String d = dietMode.trim().toLowerCase();
        return d.equals(MODE_NORMAL) || d.equals(MODE_VEGAN)
                || d.equals(MODE_KETO)   || d.equals(MODE_GLUTEN_FREE);
    }

    private static String sanitizeDietMode(String dietMode) {
        return isValidDietMode(dietMode) ? dietMode.trim().toLowerCase() : MODE_NORMAL;
    }

    private static String mapFoodPreference(String freeText) {
        if (freeText == null) return MODE_NORMAL;
        String s = freeText.trim().toLowerCase();
        if (s.isEmpty()) return MODE_NORMAL;
        if (s.contains("vegan") || s.contains("thuần chay") || s.contains("ăn chay")) return MODE_VEGAN;
        if (s.contains("keto")) return MODE_KETO;
        if (s.contains("gluten")) return MODE_GLUTEN_FREE;
        return MODE_NORMAL;
    }

    // Optional quality-of-life
    @Override public String toString() {
        return "User{" + username + ", diet=" + dietMode + "}";
    }
}