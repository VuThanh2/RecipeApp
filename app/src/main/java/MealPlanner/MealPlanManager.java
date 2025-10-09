package MealPlanner;

import android.content.Context;

import org.json.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import java.util.Locale;

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

    // core

    private static JSONObject ensureWeek(JSONObject root, String weekId) {
        return root;
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

    public static List<RecipeTag> getDayPlan(Context ctx, String weekId, Day day) {
        JSONArray weeks = load(ctx);
        ArrayList<RecipeTag> list = new ArrayList<>();
        try {
            JSONObject w = getWeekObj(ctx, weekId, weeks);
            JSONArray arr = w.getJSONObject("days").optJSONArray(day.name());
            for (int i=0;i<arr.length();i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new RecipeTag(o.optString("id"), o.optString("title")));
            }
        } catch (Exception ignored) {}
        return list;
    }

    public static Map<Day, List<RecipeTag>> getWeek(Context ctx, String weekId) {
        HashMap<Day, List<RecipeTag>> map = new HashMap<>();
        for (Day d: Day.values()) map.put(d, getDayPlan(ctx, weekId, d));
        return map;
    }

    public static boolean addRecipe(Context ctx, String weekId, Day day, RecipeTag tag) {
        JSONArray weeks = load(ctx);
        try {
            JSONObject w = getWeekObj(ctx, weekId, weeks);
            JSONArray arr = w.getJSONObject("days").getJSONArray(day.name());
            // no duplicate id per day
            for (int i=0;i<arr.length();i++)
                if (tag.id.equals(arr.getJSONObject(i).optString("id"))) return false;
            JSONObject o = new JSONObject().put("id", tag.id).put("title", tag.title);
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
    // Temporary: dummy test data
    public static Map<Day, List<RecipeTag>> getDummyWeek() {
        Map<Day, List<RecipeTag>> map = new HashMap<>();

        map.put(Day.MON, Arrays.asList(
                new RecipeTag("r1", "Pho"),
                new RecipeTag("r2", "Spring Rolls")
        ));
        map.put(Day.TUE, Arrays.asList(
                new RecipeTag("r3", "Banh Mi")
        ));
        map.put(Day.WED, Arrays.asList(
                new RecipeTag("r4", "Fried Rice"),
                new RecipeTag("r5", "Salad")
        ));
        map.put(Day.THU, Collections.singletonList(
                new RecipeTag("r6", "Bun Cha")
        ));
        map.put(Day.FRI, Arrays.asList(
                new RecipeTag("r7", "Grilled Chicken"),
                new RecipeTag("r8", "Sushi")
        ));
        map.put(Day.SAT, Arrays.asList(
                new RecipeTag("r9", "Pancakes")
        ));
        map.put(Day.SUN, Arrays.asList(
                new RecipeTag("r10", "Steak")
        ));

        return map;
    }

}