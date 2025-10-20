package Login;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import java.security.SecureRandom;

import static Login.User.*;

public class UserDataManager {
    private static final String USERS_FILE_NAME = "users.json";
    private static final String PREFS_NAME = "SecurityPrefs";
    private static final String PREFS_LOGIN_ATTEMPTS = "login_attempts_";
    private static final String PREFS_LOCKOUT_UNTIL = "lockout_until_";
    private static final String PREFS_LAST_LOGIN = "last_login_time_";
    private static final int MAX_LOGIN_ATTEMPTS = 6;
    private static final long LOCKOUT_DURATION_MS = 5 * 60 * 1000;  // 5 minutes
    private static final long ATTEMPT_RESET_TIME_MS = 15 * 60 * 1000; // 15 minutes
    private static final long MIN_LOGIN_INTERVAL_MS = 1000;

    // Rate limiting for registration (per IP/device)
    private static final String PREFS_LAST_REGISTER = "last_register_time";
    private static final long MIN_REGISTER_INTERVAL_MS = 5000; // 5 seconds between registrations

    // Password hashing with PBKDF2
    private static final int SALT_LENGTH = 16;
    private static final int HASH_ITERATIONS = 10000;
    private static final int HASH_LENGTH = 64;

    private static String MapDietMode(String freeText) {
        if (freeText == null) return MODE_NORMAL;
        String s = freeText.trim().toLowerCase();
        if (s.isEmpty()) return MODE_NORMAL;
        if (s.contains("vegan") || s.contains("thuần chay") || s.contains("ăn chay")) return MODE_VEGAN;
        if (s.contains("keto")) return MODE_KETO;
        if (s.contains("gluten")) return MODE_GLUTEN_FREE;
        return MODE_NORMAL;
    }

    private static boolean isValidDietMode(String dietMode) {
        if (dietMode == null) return false;
        String d = dietMode.trim().toLowerCase();
        return d.equals(MODE_NORMAL) || d.equals(MODE_VEGAN) || d.equals(MODE_KETO) || d.equals(MODE_GLUTEN_FREE);
    }

    private static String sanitizeDietMode(String dietMode) {
        if (isValidDietMode(dietMode)) return dietMode.trim().toLowerCase();
        return MODE_NORMAL;
    }

    private static void normalizeUsers(JSONArray users) {
        for (int i = 0; i < users.length(); i++) {
            JSONObject o = users.optJSONObject(i);
            if (o == null) continue;
            User u = User.fromJson(o);
            try {
                users.put(i, u.toJson());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static JSONArray loadUsers(Context context) {
        try {
            FileInputStream fileInputStream = context.openFileInput(USERS_FILE_NAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();
            fileInputStream.close();
            JSONArray arr = new JSONArray(builder.toString());
            normalizeUsers(arr);
            return arr;
        } catch (Exception e) {
            JSONArray empty = new JSONArray();
            normalizeUsers(empty);
            return empty;
        }
    }

    private static void saveUsers(Context context, JSONArray users) {
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(USERS_FILE_NAME, Context.MODE_PRIVATE);
            fileOutputStream.write(users.toString().getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean registerUser(Context context, String username, String password) {
        if (isRegistrationRateLimited(context)) {
            return false;
        }

        if (!isValidUsername(username)) {
            return false;
        }

        JSONArray users = loadUsers(context);

        for (int i = 0; i < users.length(); i++) {
            try {
                JSONObject user = users.getJSONObject(i);
                if (username.equalsIgnoreCase(user.optString(KEY_USERNAME))) return false;
            } catch (JSONException e) {
                Log.e("UserDataManager", "Error checking username: " + e.getMessage());
            }
        }

        JSONObject newUser = new JSONObject();
        try {
            String salt = generateSalt();
            String hashedPassword = hashPassword(password, salt);
            if (hashedPassword == null) return false;
            newUser.put(KEY_USERNAME, username);
            newUser.put(KEY_PASSWORD, hashedPassword);
            newUser.put("salt", salt);
            newUser.put(KEY_DIET_MODE, MODE_NORMAL);
            users.put(newUser);
            saveUsers(context, users);
            recordRegistration(context);
            return true;
        } catch (JSONException e) {
            Log.e("UserDataManager", "Error registering user: " + e.getMessage());
            return false;
        }
    }

    public static boolean validateLogin(Context context, String username, String password) {
        if (isAccountLockedOut(context, username)) {
            return false;
        }

        if (isLoginRateLimited(context, username)) {
            recordFailedAttempt(context, username);
            return false;
        }

        JSONArray users = loadUsers(context);
        for (int i = 0; i < users.length(); i++) {
            try {
                JSONObject user = users.getJSONObject(i);
                if (username.equalsIgnoreCase(user.optString(KEY_USERNAME))) {
                    String storedHash = user.optString(KEY_PASSWORD);
                    String salt = user.optString("salt");
                    if (verifyPassword(password, storedHash, salt)) {
                        resetFailedAttempts(context, username);
                        recordLoginAttempt(context, username);
                        return true;
                    } else {
                        recordFailedAttempt(context, username);
                        return false;
                    }
                }
            } catch (JSONException e) {
                Log.e("UserDataManager", "Error validating login: " + e.getMessage());
            }
        }
        recordFailedAttempt(context, username);
        return false;
    }

    public static boolean updatePassword(Context context, String username, String newPassword) {
        JSONArray users = loadUsers(context);
        for (int i = 0; i < users.length(); i++) {
            try {
                JSONObject user = users.getJSONObject(i);
                if (username.equalsIgnoreCase(user.optString(KEY_USERNAME))) {
                    String salt = generateSalt();
                    String hashedPassword = hashPassword(newPassword, salt);
                    if (hashedPassword == null) return false; // Handle hashing failure
                    user.put(KEY_PASSWORD, hashedPassword);
                    user.put("salt", salt);
                    saveUsers(context, users);
                    return true;
                }
            } catch (JSONException e) {
                Log.e("UserDataManager", "Error updating password: " + e.getMessage());
            }
        }
        return false;
    }

    public static String getDietMode(Context context, String username) {
        if (username == null || username.trim().isEmpty()) {
            return MODE_NORMAL;
        }

        JSONArray users = loadUsers(context);
        for (int i = 0; i < users.length(); i++) {
            try {
                JSONObject user = users.getJSONObject(i);
                String storedUsername = user.optString(KEY_USERNAME);

                // Case-insensitive comparison to match SessionManager behavior
                if (username.trim().equalsIgnoreCase(storedUsername)) {
                    String d = user.optString(KEY_DIET_MODE, null);
                    if (!isValidDietMode(d)) {
                        d = MapDietMode(user.optString(KEY_DIET_MODE, ""));
                        d = sanitizeDietMode(d);
                        user.put(KEY_DIET_MODE, d);
                        saveUsers(context, users);
                    }
                    return d;
                }
            } catch (JSONException e) {
                Log.e("UserDataManager", "Error getting diet mode: " + e.getMessage());
            }
        }
        return MODE_NORMAL;
    }

    public static boolean updateDietMode(Context context, String username, String dietMode) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        JSONArray users = loadUsers(context);
        for (int i = 0; i < users.length(); i++) {
            try {
                JSONObject user = users.getJSONObject(i);
                String storedUsername = user.optString(KEY_USERNAME);

                // Case-insensitive comparison to match SessionManager behavior
                if (username.trim().equalsIgnoreCase(storedUsername)) {
                    String normalized = sanitizeDietMode(dietMode);
                    user.put(KEY_DIET_MODE, normalized);
                    saveUsers(context, users);
                    return true;
                }
            } catch (JSONException e) {
                Log.e("UserDataManager", "Error updating diet mode: " + e.getMessage());
            }
        }
        return false;
    }

    // ============== PASSWORD HASHING ==============

    /**
     * Generate a random salt for password hashing
     */
    private static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.encodeToString(salt, Base64.NO_WRAP);
    }

    /**
     * Hash password with PBKDF2 algorithm
     */
    private static String hashPassword(String password, String salt) {
        try {
            byte[] saltBytes = Base64.decode(salt, Base64.NO_WRAP);
            javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(
                    password.toCharArray(),
                    saltBytes,
                    HASH_ITERATIONS,
                    HASH_LENGTH * 8
            );
            javax.crypto.SecretKeyFactory factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e("UserDataManager", "Error hashing password: " + e.getMessage());
            return null;
        }
    }

    /**
     * Verify password against stored hash
     */
    private static boolean verifyPassword(String password, String storedHash, String salt) {
        String computedHash = hashPassword(password, salt);
        return computedHash != null && computedHash.equals(storedHash);
    }

    // ============== BRUTE FORCE PROTECTION ==============

    private static SharedPreferences getSecurityPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Check if user is locked out due to too many failed attempts
     */
    public static boolean isAccountLockedOut(Context context, String username) {
        SharedPreferences prefs = getSecurityPrefs(context);
        long lockoutUntil = prefs.getLong(PREFS_LOCKOUT_UNTIL + username, 0);
        return System.currentTimeMillis() < lockoutUntil;
    }

    /**
     * Get remaining lockout time in seconds
     */
    public static long getRemainingLockoutTime(Context context, String username) {
        SharedPreferences prefs = getSecurityPrefs(context);
        long lockoutUntil = prefs.getLong(PREFS_LOCKOUT_UNTIL + username, 0);
        long remaining = lockoutUntil - System.currentTimeMillis();
        return remaining > 0 ? remaining / 1000 : 0;
    }

    /**
     * Record failed login attempt
     */
    private static void recordFailedAttempt(Context context, String username) {
        SharedPreferences prefs = getSecurityPrefs(context);
        SharedPreferences.Editor editor = prefs.edit();

        long lastAttemptTime = prefs.getLong(PREFS_LAST_LOGIN + username, 0);
        if (System.currentTimeMillis() - lastAttemptTime > ATTEMPT_RESET_TIME_MS) {
            editor.putInt(PREFS_LOGIN_ATTEMPTS + username, 0); // Reset attempts after 30 minutes
        }

        int attempts = prefs.getInt(PREFS_LOGIN_ATTEMPTS + username, 0) + 1;
        editor.putInt(PREFS_LOGIN_ATTEMPTS + username, attempts);

        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            long lockoutUntil = System.currentTimeMillis() + LOCKOUT_DURATION_MS;
            editor.putLong(PREFS_LOCKOUT_UNTIL + username, lockoutUntil);
        }

        editor.apply();
    }

    /**
     * Reset failed login attempts on successful login
     */
    private static void resetFailedAttempts(Context context, String username) {
        SharedPreferences.Editor editor = getSecurityPrefs(context).edit();
        editor.remove(PREFS_LOGIN_ATTEMPTS + username);
        editor.remove(PREFS_LOCKOUT_UNTIL + username);
        editor.apply();
    }

    // ============== RATE LIMITING ==============

    /**
     * Check if registration rate limit is exceeded
     */
    private static boolean isRegistrationRateLimited(Context context) {
        SharedPreferences prefs = getSecurityPrefs(context);
        long lastRegister = prefs.getLong(PREFS_LAST_REGISTER, 0);
        return (System.currentTimeMillis() - lastRegister) < MIN_REGISTER_INTERVAL_MS;
    }

    /**
     * Record registration timestamp
     */
    private static void recordRegistration(Context context) {
        SharedPreferences.Editor editor = getSecurityPrefs(context).edit();
        editor.putLong(PREFS_LAST_REGISTER, System.currentTimeMillis());
        editor.apply();
    }

    /**
     * Check if login rate limit is exceeded
     */
    public static boolean isLoginRateLimited(Context context, String username) {
        SharedPreferences prefs = getSecurityPrefs(context);
        long lastLogin = prefs.getLong(PREFS_LAST_LOGIN + username, 0);
        return (System.currentTimeMillis() - lastLogin) < MIN_LOGIN_INTERVAL_MS;
    }

    /**
     * Record login attempt timestamp
     */
    private static void recordLoginAttempt(Context context, String username) {
        SharedPreferences.Editor editor = getSecurityPrefs(context).edit();
        editor.putLong(PREFS_LAST_LOGIN + username, System.currentTimeMillis());
        editor.apply();
    }
}
