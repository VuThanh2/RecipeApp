package Login;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class User {
    // ---- JSON keys (kept literal to avoid coupling to UserDataManager internals)
    public static final String KEY_USERNAME     = "username";
    public static final String KEY_PASSWORD     = "password";
    public static final String KEY_SALT         = "salt";
    public static final String KEY_DIET_MODE    = "dietMode";

    // ---- Diet modes (same set used in UserDataManager)
    public static final String MODE_NORMAL       = "normal";
    public static final String MODE_VEGAN        = "vegan";
    public static final String MODE_KETO         = "keto";
    public static final String MODE_GLUTEN_FREE  = "gluten_free";

    private String username;
    private String passwordHash;
    private String salt;
    private String dietMode;

    public User() {}

    public User(String username, String passwordHash, String salt, String dietMode) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.dietMode = sanitizeDietMode(dietMode);
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = sanitizeUsername(username); }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getSalt() {
        return salt;
    }
    public void setSalt(String salt) {
        this.salt = salt;
    }
    public String getDietMode() { return dietMode; }
    public void setDietMode(String dietMode) { this.dietMode = sanitizeDietMode(dietMode); }

    private static String sanitizeUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "";
        }

        String cleaned = username.trim();

        // Remove any potentially dangerous characters
        // Only allow: letters, numbers, underscore, hyphen
        Pattern allowedPattern = Pattern.compile("[^a-zA-Z0-9_-]");
        cleaned = allowedPattern.matcher(cleaned).replaceAll("");

        // Limit length
        if (cleaned.length() > 20) {
            cleaned = cleaned.substring(0, 20);
        } else if (cleaned.length() < 3) {
            cleaned = "";
        }

        return cleaned;
    }
    public static boolean isValidUsername(String username) {
        if (username == null) return false;

        String u = username.trim();
        if (u.length() < 3 || u.length() > 20) return false;

        // Only alphanumeric, underscore, and hyphen
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9_-]+$");
        return pattern.matcher(u).matches();
    }

    // ---- JSON bridge
    public static User fromJson(JSONObject j) {
        User u = new User();
        u.username = j.optString(KEY_USERNAME, "");
        u.passwordHash = j.optString(KEY_PASSWORD, "");
        u.salt = j.optString(KEY_SALT, "");

        String dm = j.optString(KEY_DIET_MODE, null);
        if (!isValidDietMode(dm)) {
            dm = MapDietMode(dm);
        }
        u.dietMode = sanitizeDietMode(dm);
        return u;
    }

    public JSONObject toJson() {
        JSONObject j = new JSONObject();
        try {
            j.put(KEY_USERNAME, username == null ? "" : username);
            j.put(KEY_PASSWORD, passwordHash == null ? "" : passwordHash);
            j.put(KEY_SALT, salt == null ? "" : salt);
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

    private static String MapDietMode(String freeText) {
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

    public void clearSensitiveData() {
        if (passwordHash != null) {
            passwordHash = null;
        }
        if (salt != null) {
            salt = null;
        }
    }
}