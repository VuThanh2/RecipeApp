package RecipeManager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.recipeapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class RecipeDetailFragment extends Fragment {
    private Recipe recipe;
    private TextView tvTitle, tvCategory, tvIngredients, tvInstructions;
    private TextView tvCalories, tvCarbs, tvFat, tvProtein;
    private ImageView ivRecipeImage;
    private FloatingActionButton fabMenu;

    /**
     * Khởi tạo theo Recipe (không còn index).
     */
    public static RecipeDetailFragment newInstance(Recipe recipe) {
        RecipeDetailFragment fragment = new RecipeDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("recipe", recipe);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_detail, container, false);

        tvTitle = view.findViewById(R.id.tvTitle);
        ivRecipeImage = view.findViewById(R.id.ivRecipeBackground);
        tvCategory = view.findViewById(R.id.tvCategory);
        tvIngredients = view.findViewById(R.id.tvIngredients);
        tvInstructions = view.findViewById(R.id.tvInstructions);
        tvCalories = view.findViewById(R.id.tvCalories);
        tvCarbs = view.findViewById(R.id.tvCarb);
        tvFat = view.findViewById(R.id.tvFat);
        tvProtein = view.findViewById(R.id.tvProtein);
        fabMenu = view.findViewById(R.id.fabMenu);

        loadRecipeFromArgs();
        BindRecipeInfoToUi();

        fabMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), v);
            popup.getMenuInflater().inflate(R.menu.menu_recipe_action, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.action_edit) {
                    if (recipe == null) {
                        Toast.makeText(requireContext(), "Recipe not found", Toast.LENGTH_SHORT).show();
                    } else {
                        // Mở form ở chế độ EDIT theo Recipe (id nằm trong recipe)
                        RecipeFormFragment formFragment = RecipeFormFragment.newInstance(recipe);
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, formFragment)
                                .addToBackStack(null)
                                .commit();
                    }
                    return true;

                } else if (itemId == R.id.action_copy) {
                    if (recipe == null) {
                        Toast.makeText(requireContext(), "Recipe not found", Toast.LENGTH_SHORT).show();
                    } else {
                        copyRecipe();
                    }
                    return true;

                } else if (itemId == R.id.action_delete) {
                    if (recipe == null || recipe.getId() == null || recipe.getId().isEmpty()) {
                        Toast.makeText(requireContext(), "Cannot delete: invalid recipe id", Toast.LENGTH_SHORT).show();
                    } else {
                        RecipeDataManager.DeleteRecipeById(requireContext(), recipe.getId());
                        // Quay lại danh sách; RecipeListFragment sẽ reload ở onResume()
                        getParentFragmentManager().popBackStack();
                    }
                    return true;
                }

                return false;
            });

            popup.show();
        });
        return view;
    }

    private void loadRecipeFromArgs() {
        Bundle args = getArguments();
        if (args != null) {
            recipe = (Recipe) args.getSerializable("recipe");
            // Nếu bạn muốn “chắc cú” dữ liệu mới nhất theo id:
            // if (recipe != null && recipe.getId() != null) {
            //     Recipe fresh = RecipeDataManager.getById(requireContext(), recipe.getId());
            //     if (fresh != null) recipe = fresh;
            // }
        }
    }

    private void BindRecipeInfoToUi() {
        if (recipe == null) return;

        tvTitle.setText(recipe.getTitle());
        tvCategory.setText(recipe.getCategory());
        tvIngredients.setText(recipe.getIngredients());
        tvInstructions.setText(recipe.getInstructions());
        ivRecipeImage.setImageResource(recipe.getImage());
        tvCalories.setText(getString(R.string.label_calories, recipe.getCalories()));
        tvCarbs.setText(getString(R.string.label_carbs, recipe.getCarbs()));
        tvFat.setText(getString(R.string.label_fat, recipe.getFat()));
        tvProtein.setText(getString(R.string.label_protein, recipe.getProtein()));
    }

    private void copyRecipe() {
        Recipe copyRecipe = new Recipe();

        copyRecipe.setTitle(recipe.getTitle() + " (Copy)");
        copyRecipe.setCategory(recipe.getCategory());
        copyRecipe.setInstructions(recipe.getInstructions());
        copyRecipe.setImage(recipe.getImage());
        copyRecipe.setPinned(recipe.isPinned());

        copyRecipe.setCalories(recipe.getCalories());
        copyRecipe.setCarbs(recipe.getCarbs());
        copyRecipe.setFat(recipe.getFat());
        copyRecipe.setProtein(recipe.getProtein());

        if (recipe.getItems() != null && !recipe.getItems().isEmpty()) {
            List<Recipe.RecipeItem> copiedItems = new ArrayList<>();
            for (Recipe.RecipeItem item : recipe.getItems()) {
                Recipe.Ingredient originalIng = item.getIngredient();
                Recipe.Ingredient copiedIng = new Recipe.Ingredient(null, originalIng != null ? originalIng.getName() : "");

                if (originalIng != null && originalIng.getUnit() != null) {
                    copiedIng.setUnit(originalIng.getUnit());
                }

                if (originalIng != null && originalIng.getTags() != null) {
                    copiedIng.setTags(new ArrayList<>(originalIng.getTags()));
                }

                Recipe.RecipeItem copiedItem = new Recipe.RecipeItem(
                        copiedIng,
                        item.getQuantity()
                );
                copiedItems.add(copiedItem);
            }
            copyRecipe.setItems(copiedItems);
        }

        if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
            copyRecipe.setIngredients(recipe.getIngredients());
        }

        copyRecipe.setId(null);
        copyRecipe.setOwner(null);

        RecipeDataManager.AddRecipe(requireContext(), copyRecipe);

        Toast.makeText(requireContext(), "Recipe copied successfully", Toast.LENGTH_SHORT).show();
        getParentFragmentManager().popBackStack();
    }
}
