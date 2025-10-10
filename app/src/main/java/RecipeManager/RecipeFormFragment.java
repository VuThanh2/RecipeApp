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
import android.widget.EditText;
import android.widget.ImageView;
import androidx.fragment.app.FragmentManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import RecipeManager.Recipe;
import RecipeManager.RecipeDataManager;

import com.example.recipeapp.R;

import android.view.inputmethod.EditorInfo;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecipeFormFragment extends Fragment {
    private EditText etTitle, etInstructions;
    private AutoCompleteTextView etCategory;
    // Part C fields
    private AutoCompleteTextView actvIngredient;
    private TextInputEditText etQuantity;
    private ChipGroup chipGroupItems;

    private Button btnSave;
    private ImageView ivRecipeImage;
    private Recipe recipe;
    private int selectedImage = R.drawable.default_background;
    private boolean isPinned = false;
    private ImageView btnPin;

    private String currentDietMode = "normal";

    // Stage structured items while editing
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_form, container, false);

        etTitle = view.findViewById(R.id.etTitle);
        etCategory = view.findViewById(R.id.etCategory);
        etInstructions = view.findViewById(R.id.etInstructions);
        // Part C views (optional: assume they exist since we removed legacy field)
        actvIngredient = view.findViewById(R.id.actvIngredient);
        etQuantity     = view.findViewById(R.id.etQuantity);
        chipGroupItems = view.findViewById(R.id.chipGroupItems);

        btnSave = view.findViewById(R.id.btnSave);
        ivRecipeImage = view.findViewById(R.id.ivRecipeImage);
        btnPin = view.findViewById(R.id.btnPin);

        // Resolve current user's diet mode (best-effort; default = normal)
        String usernameArg = requireActivity().getIntent() != null
                ? requireActivity().getIntent().getStringExtra("username") : null;
        currentDietMode = UserDataManager.getDietMode(requireContext(), usernameArg == null ? "" : usernameArg);

        String[] categories = {"Breakfast", "Lunch", "Dinner", "Vegetarian", "Dessert"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line,categories);
        etCategory.setAdapter(adapter);
        etCategory.setThreshold(0);
        etCategory.setKeyListener(null);

        // ---- Part C: chip picker wiring ----
        if (actvIngredient != null && etQuantity != null && chipGroupItems != null) {
            List<String> suggestions = collectIngredientSuggestions();
            ArrayAdapter<String> ingAdapter = new ArrayAdapter<>(
                    requireContext(), android.R.layout.simple_list_item_1, suggestions);
            actvIngredient.setAdapter(ingAdapter);
            actvIngredient.setOnFocusChangeListener((vv, hasFocus) -> { if (hasFocus) actvIngredient.showDropDown(); });

            actvIngredient.setOnItemClickListener((parent, view1, position, id1) -> addCurrentChip());
            actvIngredient.setOnEditorActionListener((tv, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) { addCurrentChip(); return true; }
                return false;
            });
        }

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

        ivRecipeImage.setOnClickListener(v -> showImageSelectionDialog());

        btnSave.setOnClickListener(v -> {
            // 1) Lấy (hoặc tạo) Recipe đang chỉnh
            Recipe toSave = (recipe != null) ? recipe : new Recipe();  // cần ctor rỗng

            // 2) Đổ dữ liệu từ form
            toSave.setTitle(etTitle.getText().toString());
            toSave.setCategory(etCategory.getText().toString());
            toSave.setInstructions(etInstructions.getText().toString());
            toSave.setImage(selectedImage);
            toSave.setPinned(isPinned);

            // Build legacy text from chips for compatibility, and persist structured items
            List<Recipe.RecipeItem> itemsToSave = new ArrayList<>(stagedItems);
            String legacy = buildLegacyTextFromItems(itemsToSave);
            toSave.setItems(itemsToSave);
            toSave.setIngredients(legacy);

            // 3) Gọi API mới theo id
            if (toSave.getId() == null || toSave.getId().isEmpty()) {
                RecipeDataManager.add(requireContext(), toSave);
            } else {
                RecipeDataManager.updateById(requireContext(), toSave.getId(), toSave);
            }

            // 4) Điều hướng như cũ
            requireActivity().getSupportFragmentManager().popBackStack(
                    null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            requireActivity().getSupportFragmentManager().beginTransaction()
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
                etInstructions.setText(recipe.getInstructions());
                selectedImage = recipe.getImage();
                ivRecipeImage.setImageResource(selectedImage);
                isPinned = recipe.isPinned();
                updatePinIcon();

                // Hydrate chips from existing items (preferred), else from legacy text
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

    private void addCurrentChip() {
        String name = actvIngredient.getText() == null ? "" : actvIngredient.getText().toString().trim();
        String qty  = etQuantity.getText() == null ? "" : etQuantity.getText().toString().trim();
        if (name.isEmpty()) return;

        Recipe.Ingredient ing = new Recipe.Ingredient(null, name);
        ing.setTags(tagsForName(name));
        Recipe.RecipeItem item = new Recipe.RecipeItem(ing, qty);
        stagedItems.add(item);

        addChipFromItem(item);

        actvIngredient.setText("");
        etQuantity.setText("");
    }

    private void addChipFromItem(Recipe.RecipeItem item) {
        String name = item.getIngredient() != null ? item.getIngredient().getName() : "";
        String qty  = item.getQuantity() == null ? "" : item.getQuantity().trim();
        String label = name + (qty.isEmpty() ? "" : " — " + qty);

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
        for (Recipe r : RecipeDataManager.loadAll(requireContext())) {
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
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            Recipe.RecipeItem it = items.get(i);
            String name = (it.getIngredient() != null && it.getIngredient().getName() != null)
                    ? it.getIngredient().getName() : "";
            String qty  = it.getQuantity() == null ? "" : it.getQuantity().trim();
            if (!name.isEmpty()) sb.append(name);
            if (!qty.isEmpty()) sb.append(" — ").append(qty);
            if (i < items.size() - 1) sb.append("\n");
        }
        return sb.toString();
    }

    // Local heuristic: map ingredient name → tags (so we don't depend on other classes)
    private List<String> tagsForName(String rawName) {
        List<String> tags = new ArrayList<>();
        if (rawName == null) return tags;
        String n = rawName.trim().toLowerCase();
        if (n.isEmpty()) return tags;

        // --- meat / fish / dairy / egg ---
        if (n.matches(".*\\b(beef|steak|pork|bacon|ham|chicken|turkey|lamb|goat)\\b.*")) tags.add("meat");
        if (n.matches(".*\\b(fish|salmon|tuna|mackerel|sardine|anchovy|shrimp|prawn|crab|octopus|squid)\\b.*")) tags.add("fish");
        if (n.matches(".*\\b(milk|cheese|butter|yogurt|cream)\\b.*")) tags.add("dairy");
        if (n.matches(".*\\b(egg|eggs)\\b.*")) tags.add("egg");

        // --- gluten / grains ---
        if (n.matches(".*\\b(wheat|barley|rye|semolina|farina|spelt|malt)\\b.*")) tags.add("gluten");
        if (n.matches(".*\\b(bread|pasta|noodle|udon|spaghetti|flour)\\b.*")) tags.add("gluten");

        // --- high-carb / sugar ---
        if (n.matches(".*\\b(rice|potato|noodle|pasta|bread)\\b.*")) tags.add("high-carb");
        if (n.matches(".*\\b(sugar|honey|syrup|molasses)\\b.*")) tags.add("sugar");

        // --- vegan-friendly cues ---
        if (n.matches(".*\\b(tofu|tempeh|seitan|soy milk|almond milk|oat milk)\\b.*")) tags.add("vegan-friendly");

        // de-duplicate
        Set<String> uniq = new HashSet<>(tags);
        return new ArrayList<>(uniq);
    }


}
