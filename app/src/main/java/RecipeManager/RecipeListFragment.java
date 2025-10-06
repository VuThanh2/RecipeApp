package RecipeManager;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipeapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import androidx.recyclerview.widget.GridLayoutManager;

public class RecipeListFragment extends Fragment {
    private RecyclerView rvPinned, rvUnpinned;
    private RecipeAdapter adapterPinned, adapterUnpinned;
    private List<Recipe> pinnedList = new ArrayList<>();
    private List<Recipe> unpinnedList = new ArrayList<>();
    private FloatingActionButton fab;
    private OnRecipeSelectedListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnRecipeSelectedListener) {
            listener = (OnRecipeSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnRecipeSelectedListener");
        }
    }

    public interface OnRecipeSelectedListener {
        void onRecipeSelected(Recipe recipe, int index);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_list, container, false);

        rvPinned = view.findViewById(R.id.rvPinned);
        rvUnpinned = view.findViewById(R.id.rvUnpinned);
        fab = view.findViewById(R.id.fab_add);

        RecipeDataManager.CreateJsonFileIfEmpty(getContext());
        loadRecipesFromFile();

        adapterPinned = new RecipeAdapter(pinnedList, (recipe, pos) ->
                listener.onRecipeSelected(recipe, recipe.getGlobalIndex()));
        rvPinned.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvPinned.setAdapter(adapterPinned);
        rvPinned.setNestedScrollingEnabled(false);

        adapterUnpinned = new RecipeAdapter(unpinnedList, (recipe, pos) ->
                listener.onRecipeSelected(recipe, recipe.getGlobalIndex()));
        rvUnpinned.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvUnpinned.setAdapter(adapterUnpinned);
        rvUnpinned.setNestedScrollingEnabled(false);

        fab.setOnClickListener(v -> {
            RecipeFormFragment formFragment = RecipeFormFragment.newInstance(null, -1);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, formFragment)
                    .addToBackStack(null)
                    .commit();
        });
        return view;
    }

    private void loadRecipesFromFile() {
        pinnedList.clear();
        unpinnedList.clear();
        JSONArray array = RecipeDataManager.loadRecipes(getContext());

        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject obj = array.getJSONObject(i);
                Recipe recipe = new Recipe(
                        obj.getString("title"),
                        obj.getString("category"),
                        obj.getString("ingredients"),
                        obj.getString("instructions"),
                        obj.optInt("imageResId", 0)
                );
                recipe.setPinned(obj.optBoolean("pinned", false));
                recipe.setGlobalIndex(i); // ← ADD THIS LINE - Store the real JSON index

                if (recipe.isPinned()) pinnedList.add(recipe);
                else unpinnedList.add(recipe);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecipesFromFile();
        if (adapterPinned != null) {
            adapterPinned.notifyDataSetChanged();
        }
        if (adapterUnpinned != null) {
            adapterUnpinned.notifyDataSetChanged();
        }
    }
}



