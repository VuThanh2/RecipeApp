package RecipeManager;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import Login.SessionManager;

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
    private static final String KEY_OWNER = "owner";
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

    /**
     * Trả toàn bộ danh sách Recipe.
     */
    public static List<Recipe> LoadAllRecipe(Context context) {
        String current = SessionManager.getCurrentUsername(context);
        JSONArray raw = loadRaw(context);
        List<Recipe> out = new ArrayList<>();
        for (int i = 0; i < raw.length(); i++) {
            JSONObject o = raw.optJSONObject(i);
            Recipe r = jsonToRecipe(o);
            if (r != null && current.equals(r.getOwner())) out.add(r);
        }
        return out;
    }

    /**
     * Ghi đè toàn bộ danh sách Recipe.
     */
    public static void saveAll(Context context, List<Recipe> recipes) {
        String current = SessionManager.getCurrentUsername(context);
        JSONArray src = loadRaw(context);
        JSONArray dst = new JSONArray();
        // Keep recipes of other users
        for (int i = 0; i < src.length(); i++) {
            JSONObject o = src.optJSONObject(i);
            Recipe r = jsonToRecipe(o);
            if (r == null || !current.equals(r.getOwner())) {
                dst.put(o);
            }
        }
        // Write current user's recipes
        if (recipes != null) {
            for (Recipe r : recipes) {
                if (r == null) continue;
                if (r.getOwner() == null || r.getOwner().isEmpty()) r.setOwner(current);
                if (r.getId() == null || r.getId().isEmpty()) r.setId(UUID.randomUUID().toString());
                dst.put(recipeToJson(r));
            }
        }
        saveRaw(context, dst);
    }

    /**
     * Lấy Recipe theo id.
     */
    public static Recipe GetRecipeById(Context context, String id) {
        if (id == null) return null;
        String current = SessionManager.getCurrentUsername(context);
        JSONArray raw = loadRaw(context);
        for (int i = 0; i < raw.length(); i++) {
            JSONObject o = raw.optJSONObject(i);
            Recipe r = jsonToRecipe(o);
            if (r != null && id.equals(r.getId()) && current.equals(r.getOwner())) return r;
        }
        return null;
    }

    /**
     * Thêm Recipe (tự phát id nếu thiếu).
     */
    public static void AddRecipe(Context context, Recipe recipe) {
        if (recipe == null) return;
        String current = SessionManager.getCurrentUsername(context);
        if (recipe.getId() == null || recipe.getId().isEmpty()) {
            recipe.setId(UUID.randomUUID().toString());
        }
        if (recipe.getOwner() == null || recipe.getOwner().isEmpty()) {
            recipe.setOwner(current);
        }
        JSONArray arr = loadRaw(context);
        arr.put(recipeToJson(recipe));
        saveRaw(context, arr);
    }

    /**
     * Cập nhật Recipe theo id.
     */
    public static void UpdateRecipeById(Context context, String id, Recipe updated) {
        if (id == null || updated == null) return;
        String current = SessionManager.getCurrentUsername(context);
        JSONArray src = loadRaw(context);
        JSONArray dst = new JSONArray();
        for (int i = 0; i < src.length(); i++) {
            JSONObject o = src.optJSONObject(i);
            Recipe r = jsonToRecipe(o);
            if (r != null && id.equals(r.getId()) && current.equals(r.getOwner())) {
                // Keep id and owner stable
                if (updated.getId() == null || updated.getId().isEmpty()) updated.setId(id);
                updated.setOwner(current);
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
    public static void DeleteRecipeById(Context context, String id) {
        if (id == null) return;
        String current = SessionManager.getCurrentUsername(context);
        JSONArray src = loadRaw(context);
        JSONArray dst = new JSONArray();
        for (int i = 0; i < src.length(); i++) {
            JSONObject o = src.optJSONObject(i);
            Recipe r = jsonToRecipe(o);
            if (r == null) { dst.put(o); continue; }
            if (id.equals(r.getId()) && current.equals(r.getOwner())) {
                // skip to delete
            } else {
                dst.put(o);
            }
        }
        saveRaw(context, dst);
    }

    private static Recipe jsonToRecipe(JSONObject o) {
        if (o == null) return null;
        Recipe r = new Recipe();
        // ---- basic fields (legacy-safe) ----
        r.setId(o.optString("id", null));
        r.setTitle(o.optString("title", ""));
        r.setCategory(o.optString("category", ""));
        r.setIngredients(o.optString("ingredients", "")); // legacy text kept
        r.setInstructions(o.optString("instructions", ""));
        r.setImage(o.optInt("imageResId", 0)); // or getImageResId()
        r.setPinned(o.optBoolean("pinned", false));
        r.setGlobalIndex(o.optInt("globalIndex", -1));
        r.setCalories(o.optInt("calories", 0));
        r.setProtein(o.optInt("protein", 0));
        r.setCarbs(o.optInt("carbs", 0));
        r.setFat(o.optInt("fat", 0));
        r.setOwner(o.optString(KEY_OWNER, ""));

        // ---- NEW: structured items ----
        List<Recipe.RecipeItem> items = new ArrayList<>();
        JSONArray itemsArr = o.optJSONArray("items");
        if (itemsArr != null) {
            for (int i = 0; i < itemsArr.length(); i++) {
                JSONObject it = itemsArr.optJSONObject(i);
                if (it == null) continue;

                // ingredient object (preferred)
                JSONObject ingJson = it.optJSONObject("ingredient");
                Recipe.Ingredient ing = new Recipe.Ingredient();
                if (ingJson != null) {
                    ing.setId(ingJson.optString("id", null));
                    ing.setName(ingJson.optString("name", ""));
                    ing.setUnit(ingJson.optString("unit", ""));
                    JSONArray tagsArr = ingJson.optJSONArray("tags");
                    if (tagsArr != null) {
                        List<String> tags = new ArrayList<>();
                        for (int t = 0; t < tagsArr.length(); t++) {
                            String tag = tagsArr.optString(t, null);
                            if (tag != null) tags.add(tag);
                        }
                        ing.setTags(tags);
                    }
                } else {
                    // tolerate minimal format with name/quantity at top-level
                    String name = it.optString("name", "");
                    ing = new Recipe.Ingredient(stableIngredientId(name), name);
                }

                String qty = it.optString("quantity", "");
                items.add(new Recipe.RecipeItem(ing, qty));
            }
        } else {
            // ---- MIGRATION: derive items from legacy text if needed ----
            String legacy = r.getIngredients();
            if (legacy != null && !legacy.trim().isEmpty()) {
                items = parseLegacyIngredients(legacy);
            }
        }
        r.setItems(items);

        // If legacy text is empty but items exist, synthesize legacy text for old UI
        if ((r.getIngredients() == null || r.getIngredients().isEmpty()) && r.getItems() != null && !r.getItems().isEmpty()) {
            r.setIngredients(buildLegacyTextFromItems(r.getItems()));
        }
        return r;
    }

    private static JSONObject recipeToJson(Recipe r) {
        JSONObject o = new JSONObject();
        if (r == null) return o;
        try {
            if (r.getId() != null) o.put("id", r.getId());
            o.put("title", safe(r.getTitle()));
            o.put("category", safe(r.getCategory()));
            o.put("instructions", safe(r.getInstructions()));
            o.put("imageResId", r.getImage());
            o.put("pinned", r.isPinned());
            o.put("globalIndex", r.getGlobalIndex());
            o.put("calories", r.getCalories());
            o.put("protein", r.getProtein());
            o.put("carbs", r.getCarbs());
            o.put("fat", r.getFat());
            o.put(KEY_OWNER, r.getOwner() == null ? "" : r.getOwner());

            // ---- NEW: write structured items ----
            JSONArray itemsArr = new JSONArray();
            List<Recipe.RecipeItem> items = r.getItems();
            if (items != null) {
                for (Recipe.RecipeItem it : items) {
                    JSONObject itJson = new JSONObject();
                    itJson.put("quantity", it == null || it.getQuantity() == null ? "" : it.getQuantity());

                    Recipe.Ingredient ing = (it == null ? null : it.getIngredient());
                    JSONObject ingJson = new JSONObject();
                    if (ing == null) {
                        ingJson.put("id", "");
                        ingJson.put("name", "");
                        ingJson.put("unit", "");
                        ingJson.put("tags", new JSONArray());
                    } else {
                        String name = ing.getName() == null ? "" : ing.getName();
                        String id = ing.getId() == null || ing.getId().isEmpty() ? stableIngredientId(name) : ing.getId();
                        ingJson.put("id", id);
                        ingJson.put("name", name);
                        ingJson.put("unit", ing.getUnit() == null ? "" : ing.getUnit());
                        JSONArray tagsArr = new JSONArray();
                        if (ing.getTags() != null)
                            for (String tag : ing.getTags()) tagsArr.put(tag);
                        ingJson.put("tags", tagsArr);
                    }
                    itJson.put("ingredient", ingJson);
                    itemsArr.put(itJson);
                }
            }
            o.put("items", itemsArr);

            // ---- Keep legacy text in sync for current UI ----
            String legacy = r.getIngredients();
            if (legacy == null || legacy.trim().isEmpty()) {
                legacy = buildLegacyTextFromItems(items);
            }
            o.put("ingredients", safe(legacy));

        } catch (JSONException ignore) {
        }
        return o;
    }

    // ===== Migration helpers =====
    // Make a stable-ish id from name until we have a real catalog
    private static String stableIngredientId(String name) {
        if (name == null) name = "";
        String norm = name.trim().toLowerCase().replaceAll("\\s+", "-");
        if (norm.isEmpty()) return "ing-" + UUID.randomUUID();
        return "ing-" + norm;
    }

    // PUBLIC so the form can reuse it when saving from multiline text (Part C ready)
    public static List<Recipe.RecipeItem> parseLegacyIngredients(String multiline) {
        List<Recipe.RecipeItem> list = new ArrayList<>();
        if (multiline == null) return list;
        String[] lines = multiline.split("\\r?\\n");
        Pattern trailingQty = Pattern.compile("(\\d+\\s*[a-zA-Z%]+)$"); // e.g., 200g, 2tbsp, 100 ml, 5%
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty()) continue;

            // Try "name — qty" or "name - qty"
            String name;
            String qty;
            String[] dashSplit = line.split("\\s[—-]\\s", 2);
            if (dashSplit.length == 2) {
                name = dashSplit[0].trim();
                qty = dashSplit[1].trim();
            } else {
                Matcher m = trailingQty.matcher(line);
                if (m.find()) {
                    qty = m.group(1).trim();
                    name = line.substring(0, m.start()).trim();
                } else {
                    name = line;
                    qty = "";
                }
            }

            Recipe.Ingredient ing = new Recipe.Ingredient(stableIngredientId(name), name);
            // tags empty for now; Part D will enrich from catalog
            list.add(new Recipe.RecipeItem(ing, qty));
        }
        return list;
    }

    private static String buildLegacyTextFromItems(List<Recipe.RecipeItem> items) {
        if (items == null || items.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            Recipe.RecipeItem it = items.get(i);
            String name = (it.getIngredient() != null && it.getIngredient().getName() != null)
                    ? it.getIngredient().getName() : "";
            String qty = it.getQuantity() == null ? "" : it.getQuantity().trim();
            String unit = (it.getIngredient() != null && it.getIngredient().getUnit() != null)
                    ? it.getIngredient().getUnit().trim() : "";
            if (!name.isEmpty()) sb.append(name);
            if (!qty.isEmpty()) {
                sb.append(" — ").append(qty);
                if (!unit.isEmpty()) sb.append(" ").append(unit);
            }
            if (i < items.size() - 1) sb.append("\n");
        }
        return sb.toString();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    /* ---------------------------------------
     *  Legacy API giữ lại để không vỡ chỗ cũ (có thể xoá sau)
     * --------------------------------------- */

    /**
     * @deprecated dùng {@link #LoadAllRecipe(Context)} thay vì JSONArray thô.
     */
    @Deprecated
    public static JSONArray loadRecipes(Context context) {
        return loadRaw(context);
    }

    /**
     * @deprecated dùng {@link #saveAll(Context, List)}.
     */
    @Deprecated
    public static void saveData(Context context, JSONArray jsonArray) {
        saveRaw(context, jsonArray);
    }

    /**
     * @deprecated dùng {@link #AddRecipe(Context, Recipe)}.
     */
    @Deprecated
    public static void addRecipe(Context context, JSONObject recipeJson) {
        if (recipeJson == null) return;
        try {
            String current = SessionManager.getCurrentUsername(context);
            // Ensure id
            if (!recipeJson.has("id") || recipeJson.optString("id", "").trim().isEmpty()) {
                recipeJson.put("id", UUID.randomUUID().toString());
            }
            // Ensure owner
            recipeJson.put(KEY_OWNER, current == null ? "" : current);
        } catch (JSONException ignore) {}
        JSONArray recipes = loadRaw(context);
        recipes.put(recipeJson);
        saveRaw(context, recipes);
    }

    /**
     * @deprecated dùng {@link #DeleteRecipeById(Context, String)}.
     */
    @Deprecated
    public static void deleteRecipe(Context context, int index) {
        String current = SessionManager.getCurrentUsername(context);
        JSONArray src = loadRaw(context);
        JSONArray dst = new JSONArray();
        int currentUserIdx = 0; // counts only recipes of current user
        for (int i = 0; i < src.length(); i++) {
            JSONObject o = src.optJSONObject(i);
            Recipe r = jsonToRecipe(o);
            if (r != null && current.equals(r.getOwner())) {
                if (currentUserIdx == index) {
                    // skip to delete this user's index-th recipe
                } else {
                    dst.put(o);
                }
                currentUserIdx++;
            } else {
                // other users' recipes stay untouched
                dst.put(o);
            }
        }
        saveRaw(context, dst);
    }

    /**
     * @deprecated dùng {@link #UpdateRecipeById(Context, String, Recipe)}.
     */
    @Deprecated
    public static void updateRecipe(Context context, int index, JSONObject updatedRecipe) {
        String current = SessionManager.getCurrentUsername(context);
        JSONArray src = loadRaw(context);
        JSONArray dst = new JSONArray();
        int currentUserIdx = 0; // counts only recipes of current user
        for (int i = 0; i < src.length(); i++) {
            JSONObject o = src.optJSONObject(i);
            Recipe r = jsonToRecipe(o);
            if (r != null && current.equals(r.getOwner())) {
                if (currentUserIdx == index) {
                    try {
                        // Preserve id and enforce owner
                        String id = r.getId();
                        if (id == null || id.isEmpty()) id = UUID.randomUUID().toString();
                        if (updatedRecipe == null) updatedRecipe = new JSONObject();
                        updatedRecipe.put("id", id);
                        updatedRecipe.put(KEY_OWNER, current);
                    } catch (JSONException ignore) {}
                    dst.put(updatedRecipe);
                } else {
                    dst.put(o);
                }
                currentUserIdx++;
            } else {
                dst.put(o);
            }
        }
        saveRaw(context, dst);
    }
}
