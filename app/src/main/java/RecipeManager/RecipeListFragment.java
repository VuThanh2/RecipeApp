package RecipeManager;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipeapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import Login.UserDataManager;
import Login.SessionManager;

public class RecipeListFragment extends Fragment {
    private RecyclerView rvPinned, rvUnpinned;
    private RecipeAdapter adapterPinned, adapterUnpinned;
    private final List<Recipe> pinnedList = new ArrayList<>();
    private final List<Recipe> unpinnedList = new ArrayList<>();
    private OnRecipeSelectedListener listener;
    private String dietMode = "normal";
    private String filterPolicy = "warn";
    private androidx.appcompat.view.ActionMode actionMode;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnRecipeSelectedListener) {
            listener = (OnRecipeSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnRecipeSelectedListener");
        }
    }

    // Giữ nguyên chữ ký để không phá chỗ khác.
    public interface OnRecipeSelectedListener {
        void onRecipeSelected(Recipe recipe, int index /*deprecated: luôn -1*/);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_list, container, false);

        rvPinned = view.findViewById(R.id.rvPinned);
        rvUnpinned = view.findViewById(R.id.rvUnpinned);
        FloatingActionButton fab = view.findViewById(R.id.fab_add);

        // Vẫn dùng helper hiện có để đảm bảo file tồn tại
        RecipeDataManager.createJsonFileIfEmpty(requireContext());

        setupAdapters();

        // Resolve diet & policy from Intent (fallbacks keep app working if nothing provided)
        resolveDietContext();
        applyDietContextToAdapters();

        reloadData();

        fab.setOnClickListener(v -> {
            // Tạo mới
            RecipeFormFragment formFragment = RecipeFormFragment.newInstance(null);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, formFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void setupAdapters() {
        adapterPinned = new RecipeAdapter(pinnedList, new RecipeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Recipe recipe, int position) {
                if (!adapterPinned.isMultiSelectMode()) {
                    if (listener != null) listener.onRecipeSelected(recipe, -1);
                }
            }

            @Override
            public void onSelectionChanged(int count) {
                updateActionMode(count);
            }

            @Override
            public void onMultiSelectEntered() {
                // Synchronize multi-select mode with unpinned adapter
                adapterUnpinned.setMultiSelectMode(true);
            }
        });
        rvPinned.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvPinned.setAdapter(adapterPinned);
        rvPinned.setNestedScrollingEnabled(false);

        adapterUnpinned = new RecipeAdapter(unpinnedList, new RecipeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Recipe recipe, int position) {
                if (!adapterUnpinned.isMultiSelectMode()) {
                    if (listener != null) listener.onRecipeSelected(recipe, -1);
                }
            }

            @Override
            public void onSelectionChanged(int count) {
                updateActionMode(count);
            }

            @Override
            public void onMultiSelectEntered() {
                // Synchronize multi-select mode with pinned adapter
                adapterPinned.setMultiSelectMode(true);
            }
        });
        rvUnpinned.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvUnpinned.setAdapter(adapterUnpinned);
        rvUnpinned.setNestedScrollingEnabled(false);
    }

    private void reloadData() {
        adapterPinned.silentExitMultiSelectMode();
        adapterUnpinned.silentExitMultiSelectMode();
        pinnedList.clear();
        unpinnedList.clear();

        List<Recipe> all = RecipeDataManager.LoadAllRecipe(requireContext());
        for (Recipe r : all) {
            if (r.isPinned()) pinnedList.add(r);
            else unpinnedList.add(r);
        }

        // Part F: apply hide policy by filtering data BEFORE notifying adapters
        if ("hide".equalsIgnoreCase(filterPolicy)) {
            filterListInPlace(pinnedList, dietMode);
            filterListInPlace(unpinnedList, dietMode);
        }

        if (adapterPinned != null) adapterPinned.notifyDataSetChanged();
        if (adapterUnpinned != null) adapterUnpinned.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadData();
    }

    private void resolveDietContext() {
        // Read optional diet policy from Intent for backward compatibility
        if (requireActivity().getIntent() != null) {
            String policyExtra = requireActivity().getIntent().getStringExtra("dietPolicy");
            if (policyExtra != null) {
                filterPolicy = "hide".equalsIgnoreCase(policyExtra) ? "hide" : "warn";
            }
        }

        // Always resolve current user from SessionManager
        String username = SessionManager.getCurrentUsername(requireContext());
        if (username != null && !username.isEmpty()) {
            dietMode = UserDataManager.getDietMode(requireContext(), username);
        } else {
            dietMode = "normal"; // fallback when no session (shouldn't happen after login)
        }
    }

    private void applyDietContextToAdapters() {
        if (adapterPinned != null) {
            adapterPinned.setDietMode(dietMode);
            adapterPinned.setFilterPolicy(filterPolicy);
        }
        if (adapterUnpinned != null) {
            adapterUnpinned.setDietMode(dietMode);
            adapterUnpinned.setFilterPolicy(filterPolicy);
        }
    }

    private void filterListInPlace(List<Recipe> list, String diet) {
        java.util.Iterator<Recipe> it = list.iterator();
        while (it.hasNext()) {
            if (!isAllowedRecipe(it.next(), diet)) it.remove();
        }
    }

    private boolean isAllowedRecipe(Recipe r, String diet) {
        if (r == null || r.getItems() == null) return true;
        java.util.Set<String> forbidden;
        String d = (diet == null ? "" : diet.trim().toLowerCase());
        switch (d) {
            case "vegan":
                forbidden = new java.util.HashSet<>(java.util.Arrays.asList("meat","fish","dairy","egg"));
                break;
            case "keto":
                forbidden = new java.util.HashSet<>(java.util.Arrays.asList("sugar","high-carb"));
                break;
            case "gluten_free":
                forbidden = new java.util.HashSet<>(java.util.Arrays.asList("wheat","barley","rye","gluten"));
                break;
            default:
                forbidden = java.util.Collections.emptySet();
        }
        for (Recipe.RecipeItem it : r.getItems()) {
            Recipe.Ingredient ing = it.getIngredient();
            if (ing == null || ing.getTags() == null) continue;
            for (String t : ing.getTags()) if (forbidden.contains(t)) return false;
        }
        return true;
    }

    public void FilterRecipesForSearching(String query) {
        if (query == null) query = "";
        String q = query.toLowerCase().trim();

        List<Recipe> all = RecipeDataManager.LoadAllRecipe(requireContext());
        pinnedList.clear();
        unpinnedList.clear();

        for (Recipe r : all) {
            boolean match = false;

            if (r.getTitle() != null && r.getTitle().toLowerCase().contains(q)) {
                match = true;
            } else if (r.getIngredients() != null && r.getIngredients().toLowerCase().contains(q)) {
                match = true;
            } else if (r.getInstructions() != null && r.getInstructions().toLowerCase().contains(q)) {
                match = true;
            } else if (r.getItems() != null) {
                for (Recipe.RecipeItem item : r.getItems()) {
                    if (item.getIngredient() != null &&
                            item.getIngredient().getName() != null &&
                            item.getIngredient().getName().toLowerCase().contains(q)) {
                        match = true;
                        break;
                    }
                }
            }

            if (match) {
                if (r.isPinned()) pinnedList.add(r);
                else unpinnedList.add(r);
            }
        }

        if ("hide".equalsIgnoreCase(filterPolicy)) {
            filterListInPlace(pinnedList, dietMode);
            filterListInPlace(unpinnedList, dietMode);
        }

        adapterPinned.notifyDataSetChanged();
        adapterUnpinned.notifyDataSetChanged();
    }

    public void applyFilter(RecipeFilterItem filter) {
        List<Recipe> all = RecipeDataManager.LoadAllRecipe(requireContext());

        pinnedList.clear();
        unpinnedList.clear();

        for (Recipe r : all) {
            // Category filter
            if (!filter.categories.isEmpty() && !filter.categories.contains(r.getCategory())) continue;

            // Nutrition filter
            if (r.getCalories() < filter.minCalories || r.getCalories() > filter.maxCalories) continue;
            if (r.getProtein() < filter.minProtein || r.getProtein() > filter.maxProtein) continue;
            if (r.getCarbs() < filter.minCarbs || r.getCarbs() > filter.maxCarbs) continue;
            if (r.getFat() < filter.minFat || r.getFat() > filter.maxFat) continue;

            // Add to pinned or unpinned
            if (r.isPinned()) pinnedList.add(r);
            else unpinnedList.add(r);
        }
        applyDietContextToAdapters();

        adapterPinned.notifyDataSetChanged();
        adapterUnpinned.notifyDataSetChanged();
    }

    private void updateActionMode(int count) {
        int selectedPinnedCount = adapterPinned.getSelectedRecipes().size();
        int selectedUnpinnedCount = adapterUnpinned.getSelectedRecipes().size();
        int totalSelected = selectedPinnedCount + selectedUnpinnedCount;

        // Start action mode if selections exist
        if (totalSelected > 0 && actionMode == null) {
            actionMode = ((AppCompatActivity)getActivity()).startSupportActionMode(actionModeCallback);
        }
        // Finish action mode if no selections
        else if (totalSelected == 0 && actionMode != null) {
            actionMode.finish();
        }

        // Update the title with total count
        if (actionMode != null) {
            actionMode.setTitle(totalSelected + " selected");
        }
    }

    private final androidx.appcompat.view.ActionMode.Callback actionModeCallback = new androidx.appcompat.view.ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_action_mode, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) { return false; }

        @Override
        public boolean onActionItemClicked(androidx.appcompat.view.ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.action_delete) {
                MultiSelectRecipeDelete();
                mode.finish();
                return true;
            } else if (item.getItemId() == R.id.action_pin) {
                pinSelected();
                mode.finish();
                return true;
            } else if (item.getItemId() == R.id.action_copy) {
                MultiSelectRecipeCopy();
                mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(androidx.appcompat.view.ActionMode mode) {
            // Exit multi-select mode in both adapters
            adapterPinned.silentExitMultiSelectMode();
            adapterUnpinned.silentExitMultiSelectMode();
            actionMode = null;
        }
    };

    private void MultiSelectRecipeDelete() {
        List<Recipe> toDelete = new ArrayList<>();
        toDelete.addAll(adapterPinned.getSelectedRecipes());
        toDelete.addAll(adapterUnpinned.getSelectedRecipes());

        if (toDelete.isEmpty()) return;

        List<Recipe> all = RecipeDataManager.LoadAllRecipe(requireContext());

        for (Recipe deleteMe : toDelete) {
            for (int i = 0; i < all.size(); i++) {
                Recipe r = all.get(i);
                if (r.getId() != null && r.getId().equals(deleteMe.getId())) {
                    all.remove(i);
                    break;
                }
            }
        }

        RecipeDataManager.saveAll(requireContext(), all);

        MealPlanner.MealPlanManager.cleanupDeletedRecipes(requireContext());

        reloadData();

        adapterPinned.silentExitMultiSelectMode();
        adapterUnpinned.silentExitMultiSelectMode();
    }

    private void pinSelected() {
        List<Recipe> selectedPinned = adapterPinned.getSelectedRecipes();
        List<Recipe> selectedUnpinned = adapterUnpinned.getSelectedRecipes();
        List<Recipe> allSelected = new ArrayList<>();
        allSelected.addAll(selectedPinned);
        allSelected.addAll(selectedUnpinned);

        if (allSelected.isEmpty()) return;

        boolean allPinned = true;
        for (Recipe selected : allSelected) {
            if (!selected.isPinned()) {
                allPinned = false;
                break;
            }
        }

        List<Recipe> all = RecipeDataManager.LoadAllRecipe(requireContext());
        boolean targetPinState = !allPinned;
        for (Recipe selected : allSelected) {
            for (Recipe r : all) {
                if (r.getId() != null && r.getId().equals(selected.getId())) {
                    r.setPinned(targetPinState);
                    break;
                }
            }
        }

        RecipeDataManager.saveAll(requireContext(), all);
        adapterPinned.silentExitMultiSelectMode();
        adapterUnpinned.silentExitMultiSelectMode();
        reloadData();
    }

    private void MultiSelectRecipeCopy() {
        List<Recipe> toCopy = new ArrayList<>();
        toCopy.addAll(adapterPinned.getSelectedRecipes());
        toCopy.addAll(adapterUnpinned.getSelectedRecipes());

        if (toCopy.isEmpty()) {
            Toast.makeText(requireContext(), "No recipes selected", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Recipe> copiedRecipes = new ArrayList<>();
        for (Recipe original : toCopy) {
            Recipe copy = createRecipeCopy(original);
            copiedRecipes.add(copy);
        }

        for (Recipe copy : copiedRecipes) {
            RecipeDataManager.AddRecipe(requireContext(), copy);
        }

        String message = copiedRecipes.size() == 1
                ? "1 recipe copied"
                : copiedRecipes.size() + " recipes copied";
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

        reloadData();
        adapterPinned.silentExitMultiSelectMode();
        adapterUnpinned.silentExitMultiSelectMode();
    }

    private Recipe createRecipeCopy(Recipe original) {
        Recipe copy = new Recipe();

        copy.setTitle(original.getTitle() + " (Copy)");
        copy.setCategory(original.getCategory());
        copy.setInstructions(original.getInstructions());
        copy.setImage(original.getImage());
        copy.setPinned(original.isPinned());

        copy.setCalories(original.getCalories());
        copy.setCarbs(original.getCarbs());
        copy.setFat(original.getFat());
        copy.setProtein(original.getProtein());

        if (original.getItems() != null && !original.getItems().isEmpty()) {
            List<Recipe.RecipeItem> copiedItems = new ArrayList<>();
            for (Recipe.RecipeItem item : original.getItems()) {
                Recipe.Ingredient originalIng = item.getIngredient();

                Recipe.Ingredient copiedIng = new Recipe.Ingredient(
                        null, // New ingredient will get new ID
                        originalIng != null ? originalIng.getName() : ""
                );

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
            copy.setItems(copiedItems);
        }

        if (original.getIngredients() != null && !original.getIngredients().isEmpty()) {
            copy.setIngredients(original.getIngredients());
        }

        copy.setId(null);
        copy.setOwner(null);
        return copy;
    }
}