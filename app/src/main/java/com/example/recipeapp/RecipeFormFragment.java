package com.example.recipeapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.json.JSONException;
import org.json.JSONObject;

public class RecipeFormFragment extends Fragment {
    private EditText etTitle, etCategory, etIngredients, etInstructions;
    private Button btnSave;
    private ImageView ivRecipeImage;
    private Recipe recipe;
    private int selectedImageResId = R.drawable.default_background;
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

        if (getArguments() != null) {
            recipe = (Recipe) getArguments().getSerializable("recipe");
            if (recipe != null) {
                etTitle.setText(recipe.getTitle());
                etCategory.setText(recipe.getCategory());
                etIngredients.setText(recipe.getIngredients());
                etInstructions.setText(recipe.getInstructions());
                selectedImageResId = recipe.getImageResId();
                ivRecipeImage.setImageResource(selectedImageResId);
                isPinned = recipe.isPinned();
                updatePinIcon();
            }
        }

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
                recipeObj.put("imageResId", selectedImageResId);
                recipeObj.put("pinned", isPinned);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (editingIndex >= 0) {
                FileHelper.updateRecipe(getContext(), editingIndex, recipeObj);
            } else {
                FileHelper.addRecipe(getContext(), recipeObj);
            }

            getParentFragmentManager().popBackStack(null, getParentFragmentManager().POP_BACK_STACK_INCLUSIVE);
            getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, new RecipeListFragment()).commit();
        });
        return view;
    }

    private void updatePinIcon() {
        if (isPinned) {
            btnPin.setImageResource(R.drawable.ic_heart_filled); // filled heart
        } else {
            btnPin.setImageResource(R.drawable.ic_heart); // empty heart
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
                    selectedImageResId = imageResIds[which];
                    ivRecipeImage.setImageResource(selectedImageResId);
                })
                .show();
    }
}

