package RecipeManager;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipeapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import Login.UserDataManager;

public class RecipeListFragment extends Fragment {
    private RecyclerView rvPinned, rvUnpinned;
    private RecipeAdapter adapterPinned, adapterUnpinned;
    private final List<Recipe> pinnedList = new ArrayList<>();
    private final List<Recipe> unpinnedList = new ArrayList<>();
    private FloatingActionButton fab;
    private OnRecipeSelectedListener listener;

    // Part F: diet context (defaults keep legacy behavior if nothing is passed)
    private String dietMode = "normal";      // normal | vegan | keto | gluten_free
    private String filterPolicy = "warn";    // warn | hide

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
        fab = view.findViewById(R.id.fab_add);

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
        adapterPinned = new RecipeAdapter(pinnedList, (recipe, pos) -> {
            if (listener != null) listener.onRecipeSelected(recipe, -1);
        });
        rvPinned.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvPinned.setAdapter(adapterPinned);
        rvPinned.setNestedScrollingEnabled(false);

        adapterUnpinned = new RecipeAdapter(unpinnedList, (recipe, pos) -> {
            if (listener != null) listener.onRecipeSelected(recipe, -1);
        });
        rvUnpinned.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvUnpinned.setAdapter(adapterUnpinned);
        rvUnpinned.setNestedScrollingEnabled(false);
    }

    private void reloadData() {
        pinnedList.clear();
        unpinnedList.clear();

        // ✅ Dùng API mới dạng domain objects
        List<Recipe> all = RecipeDataManager.loadAll(requireContext());
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
        // Get username/policy from Intent if provided; defaults keep app working
        String username = null;
        if (requireActivity().getIntent() != null) {
            username = requireActivity().getIntent().getStringExtra("username");
            String policyExtra = requireActivity().getIntent().getStringExtra("dietPolicy");
            if (policyExtra != null) filterPolicy = "hide".equalsIgnoreCase(policyExtra) ? "hide" : "warn";
        }
        // Diet mode comes from UserDataManager if username is available; else default "normal"
        try {
            dietMode = UserDataManager.getDietMode(requireContext(), username == null ? "" : username);
            if (dietMode == null || dietMode.trim().isEmpty()) dietMode = "normal";
        } catch (Throwable t) {
            dietMode = "normal";
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
}
