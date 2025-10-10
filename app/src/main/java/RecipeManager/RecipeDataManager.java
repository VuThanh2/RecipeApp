package RecipeManager;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RecipeDataManager {

    private static final String RECIPES_FILE_NAME = "recipes.json";

    /* ---------------------------------------
     *  File helpers
     * --------------------------------------- */

    /**
     * Tạo file rỗng nếu chưa tồn tại.
     */
    public static void createJsonFileIfEmpty(Context context) {
        File file = new File(context.getFilesDir(), RECIPES_FILE_NAME);
        if (!file.exists()) {
            saveRaw(context, new JSONArray());
        }
    }

    /**
     * Đọc mảng JSON thô từ file. Nếu lỗi trả JSONArray rỗng.
     */
    private static JSONArray loadRaw(Context context) {
        StringBuilder sb = new StringBuilder();
        try (FileInputStream fis = context.openFileInput(RECIPES_FILE_NAME);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            String s = sb.toString().trim();
            return s.isEmpty() ? new JSONArray() : new JSONArray(s);
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    /**
     * Ghi mảng JSON thô ra file.
     */
    private static void saveRaw(Context context, JSONArray arr) {
        try (FileOutputStream fos = context.openFileOutput(RECIPES_FILE_NAME, Context.MODE_PRIVATE)) {
            fos.write(arr.toString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ---------------------------------------
     *  Public API làm việc với Recipe (khuyên dùng)
     * --------------------------------------- */

    /**
     * Trả toàn bộ danh sách Recipe.
     */
    public static List<Recipe> loadAll(Context context) {
        JSONArray raw = loadRaw(context);
        List<Recipe> out = new ArrayList<>();
        for (int i = 0; i < raw.length(); i++) {
            JSONObject o = raw.optJSONObject(i);
            Recipe r = jsonToRecipe(o);
            if (r != null) out.add(r);
        }
        return out;
    }

    /**
     * Ghi đè toàn bộ danh sách Recipe.
     */
    public static void saveAll(Context context, List<Recipe> recipes) {
        JSONArray arr = new JSONArray();
        for (Recipe r : recipes) arr.put(recipeToJson(r));
        saveRaw(context, arr);
    }

    /**
     * Lấy Recipe theo id.
     */
    public static Recipe getById(Context context, String id) {
        if (id == null) return null;
        JSONArray raw = loadRaw(context);
        for (int i = 0; i < raw.length(); i++) {
            JSONObject o = raw.optJSONObject(i);
            Recipe r = jsonToRecipe(o);
            if (r != null && id.equals(r.getId())) return r;
        }
        return null;
    }

    /**
     * Thêm Recipe (tự phát id nếu thiếu).
     */
    public static void add(Context context, Recipe recipe) {
        if (recipe == null) return;
        if (recipe.getId() == null || recipe.getId().isEmpty()) {
            recipe.setId(UUID.randomUUID().toString());
        }
        JSONArray arr = loadRaw(context);
        arr.put(recipeToJson(recipe));
        saveRaw(context, arr);
    }

    /**
     * Cập nhật Recipe theo id.
     */
    public static void updateById(Context context, String id, Recipe updated) {
        if (id == null || updated == null) return;
        JSONArray src = loadRaw(context);
        JSONArray dst = new JSONArray();
        for (int i = 0; i < src.length(); i++) {
            JSONObject o = src.optJSONObject(i);
            Recipe r = jsonToRecipe(o);
            if (r != null && id.equals(r.getId())) {
                if (updated.getId() == null || updated.getId().isEmpty()) {
                    updated.setId(id);
                }
                dst.put(recipeToJson(updated));
            } else {
                dst.put(o);
            }
        }
        saveRaw(context, dst);
    }

    /**
     * Xoá Recipe theo id.
     */
    public static void deleteById(Context context, String id) {
        if (id == null) return;
        JSONArray src = loadRaw(context);
        JSONArray dst = new JSONArray();
        for (int i = 0; i < src.length(); i++) {
            JSONObject o = src.optJSONObject(i);
            Recipe r = jsonToRecipe(o);
            if (r == null || !id.equals(r.getId())) dst.put(o);
        }
        saveRaw(context, dst);
    }

    /* ---------------------------------------
     *  JSON <-> Recipe helpers (thuần org.json)
     *  NOTE: chỉnh key/field cho khớp với class Recipe của bạn
     * --------------------------------------- */

    private static Recipe jsonToRecipe(JSONObject o) {
        if (o == null) return null;
        Recipe r = new Recipe();
        r.setId(o.optString("id", null));
        r.setTitle(o.optString("title", ""));
        r.setCategory(o.optString("category", ""));
        r.setIngredients(o.optString("ingredients", ""));
        r.setInstructions(o.optString("instructions", ""));
        // Tuỳ getter bạn đặt là getImage() hay getImageResId()
        r.setImage(o.optInt("imageResId", 0));
        r.setPinned(o.optBoolean("pinned", false));
        r.setGlobalIndex(o.optInt("globalIndex", -1));
        return r;
    }

    private static JSONObject recipeToJson(Recipe r) {
        JSONObject o = new JSONObject();
        if (r == null) return o;
        try {
            if (r.getId() != null) o.put("id", r.getId());
            o.put("title", safe(r.getTitle()));
            o.put("category", safe(r.getCategory()));
            o.put("ingredients", safe(r.getIngredients()));
            o.put("instructions", safe(r.getInstructions()));
            o.put("imageResId", r.getImage());     // hoặc r.getImageResId() nếu bạn đổi tên getter
            o.put("pinned", r.isPinned());
            o.put("globalIndex", r.getGlobalIndex());
        } catch (JSONException ignore) {
        }
        return o;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

}
