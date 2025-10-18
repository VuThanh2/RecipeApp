package MealPlanner;

import android.content.Context;

import org.json.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Locale;
import RecipeManager.Recipe;
import RecipeManager.RecipeDataManager;

public class MealPlanManager {
    private static final String FILE = "meal_plans.json";
    private static final String TMP = "meal_plans.json.tmp";
    private static final Object LOCK = new Object();

    //week helper
    public static String currentWeekId() {
        Calendar calendar = Calendar.getInstance(Locale.US);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        int year = calendar.get(Calendar.YEAR);
        int week = calendar.get(Calendar.WEEK_OF_YEAR);
        return String.format(Locale.US, "%04d-W%02d", year, week);
    }

    public static String offsetWeekId(String weekId, int offset) {
        Calendar calendar = Calendar.getInstance(Locale.US);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        String[] parts = weekId.split("-W");
        int year = Integer.parseInt(parts[0]);
        int week = Integer.parseInt(parts[1]);
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.WEEK_OF_YEAR, week);
        calendar.add(Calendar.WEEK_OF_YEAR, offset);
        int y = calendar.get(Calendar.YEAR);
        int w = calendar.get(Calendar.WEEK_OF_YEAR);
        return String.format(Locale.US, "%04d-W%02d", y, w);

    }

    //load/save json
    private static JSONArray load(Context context)  {
        synchronized (LOCK) {
            File file = new File(context.getFilesDir(), FILE);
            if (!file.exists()) return new JSONArray();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                return new JSONArray(sb.toString());

            } catch (Exception e) {
                return new JSONArray();
            }

        }
    }

    private static void save(Context context, JSONArray weeks) {
        synchronized (LOCK) {
            File dir = context.getFilesDir();
            File temp = new File(dir, TMP), dst = new File(dir, FILE);
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(temp), StandardCharsets.UTF_8))) {
                writer.write(weeks.toString());
            } catch (IOException e) {
                return;
            }
            if (!temp.renameTo(dst)) {
                try (FileInputStream in = new FileInputStream(temp);
                     FileOutputStream out = new FileOutputStream(dst)) {
                    byte[] buffer = new byte[8192];
                    int n;
                    while ((n = in.read(buffer)) >= 0) {
                        out.write(buffer, 0, n);
                    }
                } catch (IOException ignore) {
                }
                temp.delete();
            }
        }
    }

    private static JSONObject getWeekObj(Context context, String weekId, JSONArray weeks) throws JSONException {
        for (int i = 0; i < weeks.length(); i++) {
            JSONObject week = weeks.getJSONObject(i);
            if (weekId.equals(week.optString("weekId"))) return week;
        }
        //create new
        JSONObject week = new JSONObject().put("weekId", weekId);
        JSONObject days = new JSONObject();
        for (Day day : Day.values()) {
            days.put(day.name(), new JSONArray());
        }
        week.put("days", days);
        weeks.put(week);
        return week;
    }

    /**
     * Get day plan with automatic cleanup of deleted recipes
     * FIXED: Now validates recipe existence and auto-removes orphaned entries
     */
    public static List<Recipe> getDayPlan(Context ctx, String weekId, Day day) {
        JSONArray weeks = load(ctx);
        ArrayList<Recipe> list = new ArrayList<>();
        ArrayList<String> validIds = new ArrayList<>();
        boolean needsCleanup = false;

        try {
            JSONObject w = getWeekObj(ctx, weekId, weeks);
            JSONArray arr = w.getJSONObject("days").optJSONArray(day.name());

            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                String recipeId = o.optString("id", null);

                // Validate recipe still exists
                Recipe fullRecipe = RecipeDataManager.GetRecipeById(ctx, recipeId);
                if (fullRecipe != null) {
                    // Recipe exists - create lightweight reference
                    Recipe r = new Recipe();
                    r.setId(fullRecipe.getId());
                    r.setTitle(fullRecipe.getTitle());
                    list.add(r);
                    validIds.add(recipeId);
                } else {
                    // Recipe was deleted - mark for cleanup
                    needsCleanup = true;
                }
            }

            // Auto-cleanup: remove deleted recipe references
            if (needsCleanup) {
                cleanupDay(ctx, weekId, day, validIds, weeks);
            }

        } catch (Exception ignored) {}
        return list;
    }

    /**
     * Clean up deleted recipe references from a specific day
     */
    private static void cleanupDay(Context ctx, String weekId, Day day, List<String> validIds, JSONArray weeks) {
        try {
            JSONObject w = getWeekObj(ctx, weekId, weeks);
            JSONArray cleanArray = new JSONArray();

            // Rebuild array with only valid recipes
            for (String validId : validIds) {
                Recipe recipe = RecipeDataManager.GetRecipeById(ctx, validId);
                if (recipe != null) {
                    JSONObject o = new JSONObject()
                            .put("id", recipe.getId())
                            .put("title", recipe.getTitle());
                    cleanArray.put(o);
                }
            }

            w.getJSONObject("days").put(day.name(), cleanArray);
            save(ctx, weeks);
        } catch (Exception e) {
            // Silent fail - cleanup is best-effort
        }
    }

    public static Map<Day, List<Recipe>> getWeek(Context ctx, String weekId) {
        HashMap<Day, List<Recipe>> map = new HashMap<>();
        for (Day d: Day.values()) map.put(d, getDayPlan(ctx, weekId, d));
        return map;
    }

    public static boolean addRecipe(Context ctx, String weekId, Day day, Recipe tag) {
        JSONArray weeks = load(ctx);
        try {
            Recipe fullRecipe = RecipeDataManager.GetRecipeById(ctx, tag.getId());
            if (fullRecipe == null) {
                return false; // Recipe doesn't exist
            }

            JSONObject w = getWeekObj(ctx, weekId, weeks);
            JSONArray arr = w.getJSONObject("days").getJSONArray(day.name());

            // no duplicate id per day
            for (int i = 0; i < arr.length(); i++) {
                if (tag.getId().equals(arr.getJSONObject(i).optString("id"))) {
                    return false;
                }
            }

            JSONObject o = new JSONObject()
                    .put("id", fullRecipe.getId())
                    .put("title", fullRecipe.getTitle());
            arr.put(o);
            save(ctx, weeks);
            return true;
        } catch (Exception e) { return false; }
    }

    public static boolean removeRecipe(Context ctx, String weekId, Day day, String recipeId) {
        JSONArray weeks = load(ctx);
        try {
            JSONObject w = getWeekObj(ctx, weekId, weeks);
            JSONArray arr = w.getJSONObject("days").getJSONArray(day.name());
            JSONArray out = new JSONArray();
            boolean removed = false;
            for (int i=0;i<arr.length();i++) {
                JSONObject it = arr.getJSONObject(i);
                if (recipeId.equals(it.optString("id"))) { removed = true; continue; }
                out.put(it);
            }
            w.getJSONObject("days").put(day.name(), out);
            if (removed) save(ctx, weeks);
            return removed;
        } catch (Exception e) { return false; }
    }

    /**
     * Clean up all deleted recipe references across entire meal plan
     * Call this after bulk recipe deletions
     */
    public static void cleanupDeletedRecipes(Context ctx) {
        JSONArray weeks = load(ctx);
        boolean modified = false;

        try {
            for (int w = 0; w < weeks.length(); w++) {
                JSONObject week = weeks.getJSONObject(w);
                JSONObject days = week.getJSONObject("days");

                for (Day day : Day.values()) {
                    JSONArray arr = days.optJSONArray(day.name());
                    if (arr == null) continue;

                    JSONArray cleanArray = new JSONArray();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.getJSONObject(i);
                        String recipeId = o.optString("id", null);

                        // Only keep recipes that still exist
                        if (RecipeDataManager.GetRecipeById(ctx, recipeId) != null) {
                            cleanArray.put(o);
                        } else {
                            modified = true;
                        }
                    }

                    days.put(day.name(), cleanArray);
                }
            }

            if (modified) {
                save(ctx, weeks);
            }
        } catch (Exception e) {
            // Silent fail - cleanup is best-effort
        }
    }
}