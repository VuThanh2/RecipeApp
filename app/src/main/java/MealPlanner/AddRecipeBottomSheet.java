package MealPlanner;

import androidx.appcompat.app.AlertDialog;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import Login.UserDataManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.recipeapp.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.List;
import RecipeManager.Recipe;
import RecipeManager.RecipeDataManager;

public class AddRecipeBottomSheet extends BottomSheetDialogFragment {
    public interface OnPick { void onPicked(Recipe recipe); }
    private final OnPick callback;

    public AddRecipeBottomSheet(OnPick cb) { this.callback = cb; }

    private ArrayAdapter<String> adapter;
    private final List<Recipe> all = new ArrayList<>();
    private final List<Recipe> filtered = new ArrayList<>();

    private String dietMode = "normal";   // normal | vegan | keto | gluten_free
    private String filterPolicy = "warn"; // warn | hide

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup parent, @Nullable Bundle b) {
        View v = inf.inflate(R.layout.bottomsheet_add_recipe, parent, false);

        EditText search = v.findViewById(R.id.edSearch);
        ListView list   = v.findViewById(R.id.listRecipes);

        List<Recipe> data = RecipeDataManager.LoadAllRecipe(requireContext());
        all.clear(); all.addAll(data);

        resolveDietContext();

        filtered.clear();
        if ("hide".equalsIgnoreCase(filterPolicy)) {
            for (Recipe r : all) if (isAllowedRecipe(r, dietMode)) filtered.add(r);
        } else {
            filtered.addAll(all);
        }

        adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1,
                toTitles(filtered));
        list.setAdapter(adapter);

        list.setOnItemClickListener((parent1, view, position, id) -> {
            Recipe picked = filtered.get(position);
            boolean allowed = isAllowedRecipe(picked, dietMode);
            if (!allowed && "warn".equalsIgnoreCase(filterPolicy)) {
                String why = violationSummary(picked, dietMode);
                new AlertDialog.Builder(requireContext())
                        .setTitle("Contains restricted items")
                        .setMessage(why.isEmpty() ? "This recipe doesn't match your diet. Continue?"
                                : ("This recipe has: " + why + "\nContinue?"))
                        .setPositiveButton("Add anyway", (d, w) -> { if (callback != null) callback.onPicked(picked); dismiss(); })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                if (callback != null) callback.onPicked(picked);
                dismiss();
            }
        });

        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int a,int b,int c) {}
            public void afterTextChanged(Editable s) {}
            public void onTextChanged(CharSequence s,int a,int b,int c) {
                FilterToFindRecipeName(s == null ? "" : s.toString());
            }
        });

        return v;
    }

    private void FilterToFindRecipeName(String q) {
        String query = q == null ? "" : q.toLowerCase().trim();
        filtered.clear();
        for (Recipe r : all) {
            String title = r.getTitle() == null ? "" : r.getTitle();
            boolean match = title.toLowerCase().contains(query);
            if (!match) continue;
            if ("hide".equalsIgnoreCase(filterPolicy)) {
                if (isAllowedRecipe(r, dietMode)) filtered.add(r);
            } else {
                filtered.add(r);
            }
        }
        adapter.clear();
        adapter.addAll(toTitles(filtered));
        adapter.notifyDataSetChanged();
    }

    private List<String> toTitles(List<Recipe> list) {
        ArrayList<String> titles = new ArrayList<>();
        for (Recipe r : list) {
            String base = r.getTitle() == null ? "(Untitled)" : r.getTitle();
            if ("warn".equalsIgnoreCase(filterPolicy) && !isAllowedRecipe(r, dietMode)) {
                titles.add(base + "  ⚠");
            } else {
                titles.add(base);
            }
        }
        return titles;
    }

    private void resolveDietContext() {
        String username = null;
        if (getActivity() != null && getActivity().getIntent() != null) {
            username = getActivity().getIntent().getStringExtra("username");
            String pol = getActivity().getIntent().getStringExtra("dietPolicy");
            if (pol != null) filterPolicy = "hide".equalsIgnoreCase(pol) ? "hide" : "warn";
        }
        try {
            dietMode = UserDataManager.getDietMode(requireContext(), username == null ? "" : username);
            if (dietMode == null || dietMode.isEmpty()) dietMode = "normal";
        } catch (Throwable t) {
            dietMode = "normal";
        }
    }

    private boolean isAllowedRecipe(Recipe r, String diet) {
        if (r == null || r.getItems() == null) return true;
        Set<String> forbidden = forbiddenFor(diet);
        for (Recipe.RecipeItem it : r.getItems()) {
            Recipe.Ingredient ing = it.getIngredient();
            if (ing == null || ing.getTags() == null) continue;
            for (String t : ing.getTags()) if (forbidden.contains(t)) return false;
        }
        return true;
    }

    private String violationSummary(Recipe r, String diet) {
        if (r == null || r.getItems() == null) return "";
        Set<String> forbidden = forbiddenFor(diet);
        Set<String> hit = new HashSet<>();
        for (Recipe.RecipeItem it : r.getItems()) {
            Recipe.Ingredient ing = it.getIngredient();
            if (ing == null || ing.getTags() == null) continue;
            for (String t : ing.getTags()) if (forbidden.contains(t)) hit.add(t);
        }
        return String.join(", ", hit); // e.g., "meat, dairy"
    }

    private Set<String> forbiddenFor(String diet) {
        String d = diet == null ? "" : diet.trim().toLowerCase();
        if ("vegan".equals(d)) return new HashSet<>(Arrays.asList("meat","fish","dairy","egg"));
        if ("keto".equals(d)) return new HashSet<>(Arrays.asList("sugar","high-carb"));
        if ("gluten_free".equals(d)) return new HashSet<>(Arrays.asList("wheat","barley","rye","gluten"));
        return java.util.Collections.emptySet();
    }
}
