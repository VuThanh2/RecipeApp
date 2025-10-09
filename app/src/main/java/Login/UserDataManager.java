package Login;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;

public class UserDataManager {
    private static final String USERS_FILE_NAME = "users.json";

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
            return new JSONArray(builder.toString());
        } catch (Exception e) {
            return new JSONArray();
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
        JSONArray users = loadUsers(context);

        for (int i = 0; i < users.length(); i++) {
            try {
                JSONObject user = users.getJSONObject(i);
                if (user.getString("username").equals(username)) return false;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        JSONObject newUser = new JSONObject();
        try {
            newUser.put("username", username);
            newUser.put("password", password);
            newUser.put("foodPreference", ""); // Default empty preference
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
                if (user.getString("username").equals(username)
                        && user.getString("password").equals(password)) {
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
                if (user.getString("username").equals(username)) {
                    user.put("password", newPassword);
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
                if (user.getString("username").equals(username)) {
                    return user.optString("foodPreference", "");
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
                if (user.getString("username").equals(username)) {
                    user.put("foodPreference", preference);
                    saveUsers(context, users);
                    return;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
