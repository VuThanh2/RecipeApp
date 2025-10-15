package MealPlanner;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.recipeapp.R;
import java.util.*;

import RecipeManager.Recipe;
import RecipeManager.RecipeDataManager;
import Login.SessionManager;
import android.content.Intent;

public class ShoppingListActivity extends AppCompatActivity {
    private static double round1(double x) {
        return Math.round(x * 10.0) / 10.0;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        // Kiểm tra session
        String currentUser = SessionManager.getCurrentUsername(this);
        if (currentUser == null || currentUser.isEmpty()) {
            Intent back = new Intent(this, Login.LoginActivity.class);
            back.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(back);
            finish();
            return;
        }

        // Lấy Week Id được truyền từ WeeklyPlanner
        String weekId = getIntent().getStringExtra("Week Id");
        if (weekId == null) {
            finish();
            return;
        }

        // Chuẩn hóa weekId để đảm bảo chỉ load dữ liệu của user hiện tại
        if (!weekId.contains("::")) {
            weekId = currentUser.toLowerCase() + "::" + weekId;
        }

        ListView listView = findViewById(R.id.listIngredients);

        // Lấy dữ liệu plan của tuần này theo user
        Map<Day, List<Recipe>> weekMap = MealPlanManager.getWeek(this, weekId);
        // Aggregate quantities per ingredient name + unit
        Map<String, Double> totals = new LinkedHashMap<>();
        Map<String, String> labels = new LinkedHashMap<>(); // key -> display label without qty

        // Gom tất cả nguyên liệu của các recipe trong tuần
        for (List<Recipe> dayRecipes : weekMap.values()) {
            for (Recipe r : dayRecipes) {
                Recipe fullRecipe = RecipeDataManager.GetRecipeById(this, r.getId());
                if (fullRecipe != null && fullRecipe.getItems() != null) {
                    for (Recipe.RecipeItem item : fullRecipe.getItems()) {
                        Recipe.Ingredient ing = item.getIngredient();
                        if (ing != null && ing.getName() != null) {
                            String name = ing.getName().trim();
                            String unit = null;
                            try { unit = ing.getUnit(); } catch (Exception ignore) { unit = null; }

                            // Best-effort parse numeric quantity from item.getQuantity()
                            double qty = 0.0;
                            try {
                                Object q = item.getQuantity();
                                if (q instanceof Number) {
                                    qty = ((Number) q).doubleValue();
                                } else if (q != null) {
                                    qty = Double.parseDouble(q.toString().trim());
                                } else {
                                    qty = 0.0;
                                }
                            } catch (Exception ex) {
                                // If cannot parse, count as 1 unit (fallback)
                                qty = 1.0;
                            }

                            // Optional: multiply by servings if the recipe carries servings information
                            try {
                                // If Recipe has getServings(), use it; otherwise default 1
                                java.lang.reflect.Method m = r.getClass().getMethod("getServings");
                                Object sv = m.invoke(r);
                                if (sv instanceof Number) {
                                    qty *= ((Number) sv).doubleValue();
                                } else if (sv != null) {
                                    qty *= Double.parseDouble(sv.toString());
                                }
                            } catch (Exception ignore) { /* no servings field, ignore */ }

                            String key = (unit == null || unit.isEmpty()) ? name.toLowerCase() : (name.toLowerCase() + "|" + unit.toLowerCase());
                            String label = (unit == null || unit.isEmpty()) ? name : (name + " (" + unit + ")");
                            labels.put(key, label);
                            totals.put(key, totals.getOrDefault(key, 0.0) + qty);
                        }
                    }
                }
            }
        }

        // Build display rows from aggregated totals (format: "<qty> <unit> — <name>")
        List<String> shoppingList = new ArrayList<>();
        for (Map.Entry<String, Double> e : totals.entrySet()) {
            String key = e.getKey();
            String label = labels.getOrDefault(key, key);
            double val = e.getValue();
            // Derive unit from key (name|unit) and clean display name from label
            String unitKey = "";
            int barIdx = key.indexOf('|');
            if (barIdx >= 0) {
                unitKey = key.substring(barIdx + 1);
            }
            String displayName = label;
            int unitParenIdx = label.lastIndexOf(" (");
            if (unitParenIdx > 0 && label.endsWith(")")) {
                displayName = label.substring(0, unitParenIdx);
            }
            // Quantity string (strip trailing .0 for integers)
            String qtyStr = (Math.abs(val - Math.rint(val)) < 1e-9)
                    ? String.valueOf((long) Math.rint(val))
                    : String.valueOf(round1(val));
            String prefix = unitKey.isEmpty() ? qtyStr : (qtyStr + " " + unitKey);
            shoppingList.add(prefix + " — " + displayName);
        }
        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, shoppingList));
    }
}