package RecipeManager;

import android.content.res.ColorStateList;
import android.graphics.Color;
import Login.UserDataManager;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.recipeapp.R;
import android.view.inputmethod.EditorInfo;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecipeFormFragment extends Fragment {
    private TextInputLayout layoutIngredient, layoutQuantity;
    private TextInputEditText etTitle, etInstructions, etQuantity, etCalories, etCarbs, etFat, etProtein;
    private AutoCompleteTextView actvCategory, actvIngredient;
    private ChipGroup chipGroupItems;
    private Button btnSave;
    private ImageView ivRecipeImage, btnPin;
    private Recipe recipe;
    private int selectedImage = R.drawable.default_background;
    private boolean isPinned = false;
    private String currentDietMode = "normal";
    private int normalColor, errorColor;

    private static final String[] UNITS = {"g", "kg", "ml", "l", "pcs"};

    private final List<Recipe.RecipeItem> stagedItems = new ArrayList<>();

    public static RecipeFormFragment newInstance(@Nullable Recipe recipe) {
        RecipeFormFragment f = new RecipeFormFragment();
        Bundle args = new Bundle();
        args.putSerializable("recipe", recipe);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_form, container, false);

        etTitle = view.findViewById(R.id.etTitle);
        actvCategory = view.findViewById(R.id.etCategory);
        etInstructions = view.findViewById(R.id.etInstructions);
        etCalories = view.findViewById(R.id.etCalories);
        etCarbs = view.findViewById(R.id.etCarbs);
        etFat = view.findViewById(R.id.etFat);
        etProtein = view.findViewById(R.id.etProtein);
        actvIngredient = view.findViewById(R.id.actvIngredient);
        etQuantity = view.findViewById(R.id.etQuantity);
        chipGroupItems = view.findViewById(R.id.chipGroupItems);

        layoutIngredient = view.findViewById(R.id.layoutIngredient);
        layoutQuantity = view.findViewById(R.id.layoutQuantity);

        btnSave = view.findViewById(R.id.btnSave);
        ivRecipeImage = view.findViewById(R.id.ivRecipeImage);
        btnPin = view.findViewById(R.id.btnPin);

        normalColor = ContextCompat.getColor(requireContext(), R.color.green_primary);
        errorColor = ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark);

        String usernameArg = requireActivity().getIntent() != null
                ? requireActivity().getIntent().getStringExtra("username") : null;
        currentDietMode = UserDataManager.getDietMode(requireContext(), usernameArg == null ? "" : usernameArg);

        String[] categories = {"Breakfast", "Lunch", "Dinner", "Vegetarian", "Dessert"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, categories);
        actvCategory.setAdapter(adapter);
        actvCategory.setThreshold(0);
        actvCategory.setKeyListener(null);

        // Chip picker wiring
        if (actvIngredient != null && etQuantity != null && chipGroupItems != null) {
            List<String> suggestions = collectIngredientSuggestions();
            ArrayAdapter<String> ingAdapter = new ArrayAdapter<>(
                    requireContext(), android.R.layout.simple_list_item_1, suggestions);
            actvIngredient.setAdapter(ingAdapter);

            actvIngredient.setOnFocusChangeListener((vv, hasFocus) -> {
                if (hasFocus) {
                    actvIngredient.showDropDown();
                }
            });

            actvIngredient.setOnItemClickListener((parent, view1, position, id1) -> {
                etQuantity.requestFocus();
            });

            actvIngredient.setOnEditorActionListener((tv, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    layoutIngredient.setBoxStrokeColor(normalColor);
                    layoutIngredient.setError(null);
                    etQuantity.requestFocus();
                    return true;
                }
                return false;
            });

            etQuantity.setOnEditorActionListener((tv, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    ValidateAndAddChip();
                    return true;
                }
                return false;
            });

            etQuantity.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    layoutQuantity.setBoxStrokeColor(normalColor);
                    layoutQuantity.setError(null);
                }
            });
        }

        LoadRecipeFromDetail();

        actvCategory.setOnClickListener(v -> {
            if (!actvCategory.isPopupShowing()) {
                String previousText = actvCategory.getText().toString();
                actvCategory.setTag(previousText);
                actvCategory.setText("");
                actvCategory.showDropDown();
            }
        });

        actvCategory.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String previousText = (String) actvCategory.getTag();
                if (actvCategory.getText().toString().isEmpty() && previousText != null) {
                    actvCategory.setText(previousText);
                }
            }
        });

        btnPin.setOnClickListener(v -> {
            isPinned = !isPinned;
            updatePinIcon();
        });

        ivRecipeImage.setOnClickListener(v -> showImageSelectionDialog());

        btnSave.setOnClickListener(v -> {
            Recipe toSave = (recipe != null) ? recipe : new Recipe();

            toSave.setTitle(etTitle.getText().toString());
            toSave.setCategory(actvCategory.getText().toString());
            toSave.setInstructions(etInstructions.getText().toString());
            toSave.setImage(selectedImage);
            toSave.setPinned(isPinned);

            int calories = etCalories.getText().toString().isEmpty() ? 0 : Integer.parseInt(etCalories.getText().toString());
            int carbs = etCarbs.getText().toString().isEmpty() ? 0 : Integer.parseInt(etCarbs.getText().toString());
            int fat = etFat.getText().toString().isEmpty() ? 0 : Integer.parseInt(etFat.getText().toString());
            int protein = etProtein.getText().toString().isEmpty() ? 0 : Integer.parseInt(etProtein.getText().toString());

            toSave.setCalories(calories);
            toSave.setCarbs(carbs);
            toSave.setFat(fat);
            toSave.setProtein(protein);

            List<Recipe.RecipeItem> itemsToSave = new ArrayList<>(stagedItems);
            String legacy = buildLegacyTextFromItems(itemsToSave);
            toSave.setItems(itemsToSave);
            toSave.setIngredients(legacy);

            if (toSave.getId() == null || toSave.getId().isEmpty()) {
                RecipeDataManager.AddRecipe(requireContext(), toSave);
            } else {
                RecipeDataManager.UpdateRecipeById(requireContext(), toSave.getId(), toSave);
            }

            requireActivity().getSupportFragmentManager().popBackStack(
                    null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new RecipeListFragment())
                    .commit();
        });

        return view;
    }

    private void ValidateAndAddChip() {
        String ingredient = actvIngredient.getText() == null ? "" : actvIngredient.getText().toString().trim();
        String quantity = etQuantity.getText() == null ? "" : etQuantity.getText().toString().trim();

        ResetLayoutOutlines();

        if (ingredient.isEmpty()) {
            showError(layoutIngredient, actvIngredient, "Please add an ingredient");
            return;
        }
        if (quantity.isEmpty()) {
            showError(layoutQuantity, etQuantity, "Please add the quantity");
            return;
        }

        addCurrentChipWithUnitPicker();
    }

    private void addCurrentChip(String name, String qty, String unit) {
        if (name == null || name.trim().isEmpty()) return;
        String safeQty = qty == null ? "" : qty.trim();
        String safeUnit = unit == null ? "" : unit.trim();

        Recipe.Ingredient ing = new Recipe.Ingredient(null, name);
        ing.setUnit(safeUnit);
        ing.setTags(tagsForName(name));
        Recipe.RecipeItem item = new Recipe.RecipeItem(ing, safeQty);
        stagedItems.add(item);

        addChipFromItem(item);
        actvIngredient.setText("");
        etQuantity.setText("");
    }

    private void addCurrentChipWithUnitPicker() {
        final String name = actvIngredient.getText() == null ? "" : actvIngredient.getText().toString().trim();
        final String qty = etQuantity.getText() == null ? "" : etQuantity.getText().toString().trim();
        if (name.isEmpty()) return;

        final int[] chosenIndex = {0};
        new AlertDialog.Builder(requireContext())
                .setTitle("Select unit")
                .setSingleChoiceItems(UNITS, 0, (d, which) -> chosenIndex[0] = which)
                .setPositiveButton("OK", (d, w) -> {
                    String unit = UNITS[chosenIndex[0]];
                    addCurrentChip(name, qty, unit);
                })
                .setNegativeButton("Cancel", (d, w) -> {
                    // default to pcs if user cancels
                    addCurrentChip(name, qty, "pcs");
                })
                .show();
    }

    private void addChipFromItem(Recipe.RecipeItem item) {
        String name = item.getIngredient() != null ? item.getIngredient().getName() : "";
        String qty = item.getQuantity() == null ? "" : item.getQuantity().trim();
        String unit = (item.getIngredient() != null && item.getIngredient().getUnit() != null) ? item.getIngredient().getUnit().trim() : "";
        String qtyUnit = qty.isEmpty() ? "" : (unit.isEmpty() ? qty : (qty + " " + unit));
        String label = name + (qtyUnit.isEmpty() ? "" : " — " + qtyUnit);

        Chip chip = new Chip(requireContext());
        boolean allowed = DietRules.isAllowed(item.getIngredient(), currentDietMode);
        if (!allowed) {
            chip.setAlpha(0.7f);
            chip.setChipStrokeWidth(2f);
            chip.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor("#FF4444")));
        }
        chip.setText(label);
        chip.setCloseIconVisible(true);
        chip.setCheckable(false);
        chip.setOnCloseIconClickListener(v -> {
            chipGroupItems.removeView(chip);
            stagedItems.remove(item);
        });
        chipGroupItems.addView(chip);
    }

    private List<String> collectIngredientSuggestions() {
        Set<String> set = new HashSet<>();
        for (Recipe r : RecipeDataManager.LoadAllRecipe(requireContext())) {
            if (r.getItems() != null) {
                for (Recipe.RecipeItem it : r.getItems()) {
                    if (it.getIngredient() != null && it.getIngredient().getName() != null) {
                        String n = it.getIngredient().getName().trim();
                        if (!n.isEmpty()) set.add(n);
                    }
                }
            }
            String legacy = r.getIngredients();
            if (legacy != null && !legacy.trim().isEmpty()) {
                for (Recipe.RecipeItem it : RecipeDataManager.parseLegacyIngredients(legacy)) {
                    if (it.getIngredient() != null && it.getIngredient().getName() != null) {
                        String n = it.getIngredient().getName().trim();
                        if (!n.isEmpty()) set.add(n);
                    }
                }
            }
        }
        return new ArrayList<>(set);
    }

    private String buildLegacyTextFromItems(List<Recipe.RecipeItem> items) {
        if (items == null || items.isEmpty()) return "";
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            Recipe.RecipeItem recipeItem = items.get(i);
            String nameIngredient = (recipeItem.getIngredient() != null && recipeItem.getIngredient().getName() != null)
                    ? recipeItem.getIngredient().getName() : "";
            String quantity = recipeItem.getQuantity() == null ? "" : recipeItem.getQuantity().trim();
            if (!nameIngredient.isEmpty()) stringBuilder.append(nameIngredient);
            if (!quantity.isEmpty()) stringBuilder.append(" — ").append(quantity);
            if (i < items.size() - 1) stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    private List<String> tagsForName(String rawName) {
        List<String> tags = new ArrayList<>();
        if (rawName == null) return tags;
        String n = rawName.trim().toLowerCase();
        if (n.isEmpty()) return tags;

        if (n.matches(".*\\b(beef|steak|pork|bacon|ham|chicken|turkey|lamb|goat)\\b.*")) tags.add("meat");
        if (n.matches(".*\\b(fish|salmon|tuna|mackerel|sardine|anchovy|shrimp|prawn|crab|octopus|squid)\\b.*")) tags.add("fish");
        if (n.matches(".*\\b(milk|cheese|butter|yogurt|cream)\\b.*")) tags.add("dairy");
        if (n.matches(".*\\b(egg|eggs)\\b.*")) tags.add("egg");

        if (n.matches(".*\\b(wheat|barley|rye|semolina|farina|spelt|malt)\\b.*")) tags.add("gluten");
        if (n.matches(".*\\b(bread|pasta|noodle|udon|spaghetti|flour)\\b.*")) tags.add("gluten");

        if (n.matches(".*\\b(rice|potato|noodle|pasta|bread)\\b.*")) tags.add("high-carb");
        if (n.matches(".*\\b(sugar|honey|syrup|molasses)\\b.*")) tags.add("sugar");

        if (n.matches(".*\\b(tofu|tempeh|seitan|soy milk|almond milk|oat milk)\\b.*")) tags.add("vegan-friendly");

        Set<String> uniq = new HashSet<>(tags);
        return new ArrayList<>(uniq);
    }

    private void showError(TextInputLayout layout, TextInputEditText input, String message) {
        layout.setBoxStrokeColor(errorColor);
        layout.setError(message);
        input.requestFocus();
    }

    private void showError(TextInputLayout layout, AutoCompleteTextView input, String message) {
        layout.setBoxStrokeColor(errorColor);
        layout.setError(message);
        input.requestFocus();
    }

    private void ResetLayoutOutlines() {
        layoutIngredient.setBoxStrokeColor(normalColor);
        layoutIngredient.setError(null);
        layoutQuantity.setBoxStrokeColor(normalColor);
        layoutQuantity.setError(null);
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
                actvCategory.setText(recipe.getCategory());
                etInstructions.setText(recipe.getInstructions());
                selectedImage = recipe.getImage();
                ivRecipeImage.setImageResource(selectedImage);
                isPinned = recipe.isPinned();
                etCalories.setText(String.valueOf(recipe.getCalories()));
                etCarbs.setText(String.valueOf(recipe.getCarbs()));
                etFat.setText(String.valueOf(recipe.getFat()));
                etProtein.setText(String.valueOf(recipe.getProtein()));
                updatePinIcon();

                stagedItems.clear();
                List<Recipe.RecipeItem> source = (recipe.getItems() != null && !recipe.getItems().isEmpty())
                        ? recipe.getItems()
                        : RecipeDataManager.parseLegacyIngredients(recipe.getIngredients());
                if (chipGroupItems != null) {
                    chipGroupItems.removeAllViews();
                    if (source != null) {
                        for (Recipe.RecipeItem it : source) {
                            stagedItems.add(it);
                            addChipFromItem(it);
                        }
                    }
                }
            }
        }
    }
}