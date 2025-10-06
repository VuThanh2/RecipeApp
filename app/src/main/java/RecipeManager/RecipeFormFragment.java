package RecipeManager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.recipeapp.R;

import org.json.JSONException;
import org.json.JSONObject;

public class RecipeFormFragment extends Fragment {
    private EditText etTitle, etIngredients, etInstructions;
    private AutoCompleteTextView etCategory;
    private Button btnSave;
    private ImageView ivRecipeImage;
    private Recipe recipe;
    private int selectedImage = R.drawable.default_background;
    private boolean isPinned = false;
    private ImageView btnPin;

    public static RecipeFormFragment newInstance(@Nullable Recipe recipe, int index) {
        RecipeFormFragment fragment = new RecipeFormFragment();
        Bundle args = new Bundle();
        args.putSerializable("recipe", recipe);
        args.putInt("index", index);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_form, container, false);

        etTitle = view.findViewById(R.id.etTitle);
        etCategory = view.findViewById(R.id.etCategory);
        etIngredients = view.findViewById(R.id.etIngredients);
        etInstructions = view.findViewById(R.id.etInstructions);
        btnSave = view.findViewById(R.id.btnSave);
        ivRecipeImage = view.findViewById(R.id.ivRecipeImage);
        btnPin = view.findViewById(R.id.btnPin);

        String[] categories = {"Breakfast", "Lunch", "Dinner", "Vegetarian", "Dessert"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line,categories);
        etCategory.setAdapter(adapter);
        etCategory.setThreshold(0);
        etCategory.setKeyListener(null);

        LoadRecipeFromDetail();

        etCategory.setOnClickListener(v -> {
            if (!etCategory.isPopupShowing()) {
                String previousText = etCategory.getText().toString();
                etCategory.setTag(previousText);
                etCategory.setText("");
                etCategory.showDropDown();
            }
        });

        etCategory.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String previousText = (String) etCategory.getTag();
                if (etCategory.getText().toString().isEmpty() && previousText != null) {
                    etCategory.setText(previousText);
                }
            }
        });

        btnPin.setOnClickListener(v -> {
            isPinned = !isPinned;
            updatePinIcon();
        });

        int editingIndex = getArguments() != null ? getArguments().getInt("index", -1) : -1;
        ivRecipeImage.setOnClickListener(v -> showImageSelectionDialog());

        btnSave.setOnClickListener(v -> {
            JSONObject recipeObj = new JSONObject();
            try {
                recipeObj.put("title", etTitle.getText().toString());
                recipeObj.put("category", etCategory.getText().toString());
                recipeObj.put("ingredients", etIngredients.getText().toString());
                recipeObj.put("instructions", etInstructions.getText().toString());
                recipeObj.put("imageResId", selectedImage);
                recipeObj.put("pinned", isPinned);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (editingIndex >= 0) {
                RecipeDataManager.updateRecipe(getContext(), editingIndex, recipeObj);
            } else {
                RecipeDataManager.addRecipe(getContext(), recipeObj);
            }

            getParentFragmentManager().popBackStack(null, getParentFragmentManager().POP_BACK_STACK_INCLUSIVE);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new RecipeListFragment())
                    .commit();
        });
        return view;
    }

    private void updatePinIcon() {
        if (isPinned) {
            btnPin.setImageResource(R.drawable.ic_heart_filled);
        } else {
            btnPin.setImageResource(R.drawable.ic_heart);
        }
    }

    private void showImageSelectionDialog() {
        final int[] imageResIds = {
                R.drawable.pho,
                R.drawable.steak,
                R.drawable.salad,
        };

        String[] imageNames = {"Pho", "Steak", "Salad"};

        new AlertDialog.Builder(getContext())
                .setTitle("Select an image")
                .setItems(imageNames, (dialog, which) -> {
                    selectedImage = imageResIds[which];
                    ivRecipeImage.setImageResource(selectedImage);
                })
                .show();
    }

    private void LoadRecipeFromDetail() {
        if (getArguments() != null) {
            recipe = (Recipe) getArguments().getSerializable("recipe");
            if (recipe != null) {
                etTitle.setText(recipe.getTitle());
                etCategory.setText(recipe.getCategory());
                etIngredients.setText(recipe.getIngredients());
                etInstructions.setText(recipe.getInstructions());
                selectedImage = recipe.getImage();
                ivRecipeImage.setImageResource(selectedImage);
                isPinned = recipe.isPinned();
                updatePinIcon();
            }
        }
    }
}
