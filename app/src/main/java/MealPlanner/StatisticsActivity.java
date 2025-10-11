package MealPlanner;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.recipeapp.R;
import java.util.*;
import RecipeManager.Recipe;
import RecipeManager.RecipeDataManager;

public class StatisticsActivity extends AppCompatActivity {
    private TextView tvCalories, tvProtein, tvCarbs, tvFat;
    private LinearLayout layoutRecipeCount;
    private String weekId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        weekId = getIntent().getStringExtra("Week Id");
        if (weekId == null) weekId = MealPlanManager.currentWeekId();

        layoutRecipeCount = findViewById(R.id.layoutRecipeCount);
        tvCalories = findViewById(R.id.tvCalories);
        tvProtein = findViewById(R.id.tvProtein);
        tvCarbs = findViewById(R.id.tvCarbs);
        tvFat = findViewById(R.id.tvFat);

        loadStatistic();
    }

    private void loadStatistic() {
        Map<Day, List<Recipe>> weekPlan = MealPlanManager.getWeek(this, weekId);
        List<Recipe> allRecipes = new ArrayList<>();

        // Gather all recipes in the week
        for (List<Recipe> recipes : weekPlan.values()) {
            for (Recipe r : recipes) {
                Recipe full = RecipeDataManager.GetRecipeById(this, r.getId());
                if (full != null) allRecipes.add(full);
            }
        }

        // Count how many times each recipe appears
        Map<String, Integer> recipeCount = new LinkedHashMap<>();
        int totalCalories = 0, totalProtein = 0, totalCarbs = 0, totalFat = 0;

        for (Recipe r : allRecipes) {
            String title = r.getTitle();
            recipeCount.put(title, recipeCount.getOrDefault(title, 0) + 1);

            totalCalories += r.getCalories();
            totalProtein += r.getProtein();
            totalCarbs += r.getCarbs();
            totalFat += r.getFat();
        }

        // Show total nutrition
        tvCalories.setText(totalCalories + " kcal");
        tvProtein.setText(totalProtein + " g");
        tvCarbs.setText(totalCarbs + " g");
        tvFat.setText(totalFat + " g");

        // Show each recipe title and count
        layoutRecipeCount.removeAllViews();
        for (Map.Entry<String, Integer> entry : recipeCount.entrySet()) {
            TextView tv = new TextView(this);
            tv.setText(entry.getKey() + ": " + entry.getValue() + " time" + (entry.getValue() > 1 ? "s" : ""));
            tv.setTextSize(16);
            tv.setPadding(0, 8, 0, 8);
            layoutRecipeCount.addView(tv);
        }

        if (recipeCount.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No recipes selected for this week");
            empty.setTextSize(16);
            layoutRecipeCount.addView(empty);
        }
    }
}
