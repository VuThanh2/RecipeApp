package MealPlanner;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.recipeapp.R;
import java.util.*;

import RecipeManager.Recipe;
import RecipeManager.RecipeDataManager;

public class ShoppingListActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        String weekId = getIntent().getStringExtra("Week Id");
        if (weekId == null) finish();

        ListView listView = findViewById(R.id.listIngredients);

        Map<Day, List<Recipe>> weekMap = MealPlanManager.getWeek(this, weekId);
        Set<String> ingredientsSet = new LinkedHashSet<>();

        for (List<Recipe> dayRecipes : weekMap.values()) {
            for (Recipe r : dayRecipes) {
                Recipe fullRecipe = RecipeDataManager.GetRecipeById(this, r.getId());
                if (fullRecipe != null && fullRecipe.getItems() != null) {
                    for (Recipe.RecipeItem item : fullRecipe.getItems()) {
                        Recipe.Ingredient ing = item.getIngredient();
                        if (ing != null && ing.getName() != null) {
                            ingredientsSet.add(ing.getName() + " - " + item.getQuantity());
                        }
                    }
                }
            }
        }

        List<String> shoppingList = new ArrayList<>(ingredientsSet);
        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, shoppingList));
    }
}
