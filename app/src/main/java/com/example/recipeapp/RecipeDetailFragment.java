package com.example.recipeapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class RecipeDetailFragment extends Fragment {
    private Recipe recipe;
    private int recipeIndex;
    private TextView tvTitle, tvCategory, tvIngredients, tvInstructions;
    private Button btnEdit, btnDelete;
    private ImageView ivRecipeImage;

    public static RecipeDetailFragment newInstance(Recipe recipe, int index) {
        RecipeDetailFragment fragment = new RecipeDetailFragment();
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
        View view = inflater.inflate(R.layout.fragment_recipe_detail, container, false);

        tvTitle = view.findViewById(R.id.tvTitle);
        ivRecipeImage = view.findViewById(R.id.ivRecipeBackground);
        tvCategory = view.findViewById(R.id.tvCategory);
        tvIngredients = view.findViewById(R.id.tvIngredients);
        tvInstructions = view.findViewById(R.id.tvInstructions);
        btnEdit = view.findViewById(R.id.btnEdit);
        btnDelete = view.findViewById(R.id.btnDelete);

        if (getArguments() != null) {
            recipe = (Recipe) getArguments().getSerializable("recipe");
            recipeIndex = getArguments().getInt("index", -1);

            if (recipe != null) {
                tvTitle.setText(recipe.getTitle());
                tvCategory.setText(recipe.getCategory());
                tvIngredients.setText(recipe.getIngredients());
                tvInstructions.setText(recipe.getInstructions());
                ivRecipeImage.setImageResource(recipe.getImageResId());
            }
        }

        btnEdit.setOnClickListener(v -> {
            RecipeFormFragment formFragment = RecipeFormFragment.newInstance(recipe, recipeIndex);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, formFragment)
                    .addToBackStack(null)
                    .commit();
        });

        btnDelete.setOnClickListener(v -> {
            FileHelper.deleteRecipe(requireContext(), recipeIndex);
            getParentFragmentManager().popBackStack(); // Go back to list
        });

        return view;
    }
}

