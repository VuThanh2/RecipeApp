package Login;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import Login.User;
import static Login.User.*;

public class UserDataManager {
    private static final String USERS_FILE_NAME = "users.json";

    private static String mapFoodPreference(String freeText) {
        if (freeText == null) return MODE_NORMAL;
        String s = freeText.trim().toLowerCase();
        if (s.isEmpty()) return MODE_NORMAL;
        // simple normalization for common variants (VN + EN)
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

    // Ensure every user object has a valid dietMode (derived from legacy foodPreference if needed)
    private static void normalizeUsers(JSONArray users) {
        for (int i = 0; i < users.length(); i++) {
            JSONObject o = users.optJSONObject(i);
            if (o == null) continue;
            User u = User.fromJson(o); // applies legacy mapping & sanitize
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

    public static java.util.List<User> getAllUsers(Context context) {
        JSONArray arr = loadUsers(context);
        java.util.List<User> list = new java.util.ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o != null) list.add(User.fromJson(o));
        }
        return list;
    }

    public static User getUser(Context context, String username) {
        JSONArray arr = loadUsers(context);
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o != null && username.equalsIgnoreCase(o.optString(KEY_USERNAME))) {
                return User.fromJson(o);
            }
        }
        return null;
    }

    public static boolean registerUser(Context context, User u) {
        JSONArray arr = loadUsers(context);
        // prevent duplicates (case-insensitive)
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o != null && u.getUsername().equalsIgnoreCase(o.optString(KEY_USERNAME))) {
                return false;
            }
        }
        arr.put(u.toJson());
        saveUsers(context, arr);
        return true;
    }

    public static boolean registerUser(Context context, String username, String password) {
        JSONArray users = loadUsers(context);

        for (int i = 0; i < users.length(); i++) {
            try {
                JSONObject user = users.getJSONObject(i);
                if (username.equalsIgnoreCase(user.optString(KEY_USERNAME))) return false;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        JSONObject newUser = new JSONObject();
        try {
            newUser.put(KEY_USERNAME, username);
            newUser.put(KEY_PASSWORD, password);
            // legacy field kept for backward-compat (can be removed later)
            newUser.put(KEY_FOOD_PREF, "");
            // new normalized field
            newUser.put(KEY_DIET_MODE, MODE_NORMAL);
            users.put(newUser);
            saveUsers(context, users);
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean registerUser(Context context, String username, String password, String dietMode) {
        JSONArray users = loadUsers(context);

        for (int i = 0; i < users.length(); i++) {
            try {
                JSONObject user = users.getJSONObject(i);
                if (username.equalsIgnoreCase(user.optString(KEY_USERNAME))) return false;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        JSONObject newUser = new JSONObject();
        try {
            newUser.put(KEY_USERNAME, username);
            newUser.put(KEY_PASSWORD, password);
            // keep legacy field synchronized for old UI screens, optional
            newUser.put(KEY_FOOD_PREF, "");
            newUser.put(KEY_DIET_MODE, sanitizeDietMode(dietMode));
            users.put(newUser);
            saveUsers(context, users);
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean validateLogin(Context context, String username, String password) {
        JSONArray users = loadUsers(context);
        for (int i = 0; i < users.length(); i++) {
            try {
                JSONObject user = users.getJSONObject(i);
                if (username.equalsIgnoreCase(user.optString(KEY_USERNAME))
                        && password.equals(user.optString(KEY_PASSWORD))) {
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean updatePassword(Context context, String username, String newPassword) {
        JSONArray users = loadUsers(context);
        for (int i = 0; i < users.length(); i++) {
            try {
                JSONObject user = users.getJSONObject(i);
                if (username.equalsIgnoreCase(user.optString(KEY_USERNAME))) {
                    user.put(KEY_PASSWORD, newPassword);
                    saveUsers(context, users);
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static String getFoodPreference(Context context, String username) {
        JSONArray users = loadUsers(context);
        for (int i = 0; i < users.length(); i++) {
            try {
                JSONObject user = users.getJSONObject(i);
                if (username.equals(user.optString(KEY_USERNAME))) {
                    // Prefer dietMode going forward
                    String d = user.optString(KEY_DIET_MODE, null);
                    if (isValidDietMode(d)) return d;
                    // Fallback to legacy and map
                    String legacy = user.optString(KEY_FOOD_PREF, "");
                    return mapFoodPreference(legacy);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static void updateFoodPreference(Context context, String username, String preference) {
        JSONArray users = loadUsers(context);
        for (int i = 0; i < users.length(); i++) {
            try {
                JSONObject user = users.getJSONObject(i);
                if (username.equals(user.optString(KEY_USERNAME))) {
                    // Keep legacy field for compatibility
                    user.put(KEY_FOOD_PREF, preference == null ? "" : preference);
                    // And write normalized value into the new field
                    user.put(KEY_DIET_MODE, sanitizeDietMode(mapFoodPreference(preference)));
                    saveUsers(context, users);
                    return;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getDietMode(Context context, String username) {
        JSONArray users = loadUsers(context);
        for (int i = 0; i < users.length(); i++) {
            try {
                JSONObject user = users.getJSONObject(i);
                if (username.equals(user.optString(KEY_USERNAME))) {
                    // prefer new field
                    String d = user.optString(KEY_DIET_MODE, null);
                    if (!isValidDietMode(d)) {
                        // derive from legacy if needed, then persist
                        d = mapFoodPreference(user.optString(KEY_FOOD_PREF, ""));
                        d = sanitizeDietMode(d);
                        user.put(KEY_DIET_MODE, d);
                        saveUsers(context, users);
                    }
                    return d;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return MODE_NORMAL;
    }

    public static boolean updateDietMode(Context context, String username, String dietMode) {
        JSONArray users = loadUsers(context);
        for (int i = 0; i < users.length(); i++) {
            try {
                JSONObject user = users.getJSONObject(i);
                if (username.equals(user.optString(KEY_USERNAME))) {
                    String normalized = sanitizeDietMode(dietMode);
                    user.put(KEY_DIET_MODE, normalized);
                    // keep legacy field in sync (optional; safe to remove later)
                    user.put(KEY_FOOD_PREF, normalized);
                    saveUsers(context, users);
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static String getUsersFilePath(Context context) {
        File f = context.getFileStreamPath(USERS_FILE_NAME);
        return f != null ? f.getAbsolutePath() : "";
    }
}
