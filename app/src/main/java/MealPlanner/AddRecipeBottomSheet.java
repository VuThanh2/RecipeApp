package MealPlanner;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup parent, @Nullable Bundle b) {
        View v = inf.inflate(R.layout.bottomsheet_add_recipe, parent, false);

        EditText search = v.findViewById(R.id.edSearch);
        ListView list   = v.findViewById(R.id.listRecipes);

        // Lấy dữ liệu thật từ RecipeManager
        List<Recipe> data = RecipeDataManager.loadAll(requireContext());
        all.clear(); all.addAll(data);
        filtered.clear(); filtered.addAll(all);

        adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1,
                toTitles(filtered));
        list.setAdapter(adapter);

        list.setOnItemClickListener((parent1, view, position, id) -> {
            Recipe picked = filtered.get(position);
            if (callback != null) callback.onPicked(picked);
            dismiss();
        });

        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int a,int b,int c) {}
            public void afterTextChanged(Editable s) {}
            public void onTextChanged(CharSequence s,int a,int b,int c) {
                filter(s == null ? "" : s.toString());
            }
        });

        return v;
    }

    private void filter(String q) {
        String query = q.toLowerCase().trim();
        filtered.clear();
        for (Recipe r : all) {
            String title = r.getTitle() == null ? "" : r.getTitle();
            if (title.toLowerCase().contains(query)) filtered.add(r);
        }
        adapter.clear();
        adapter.addAll(toTitles(filtered));
        adapter.notifyDataSetChanged();
    }

    private List<String> toTitles(List<Recipe> list) {
        ArrayList<String> titles = new ArrayList<>();
        for (Recipe r : list) {
            titles.add(r.getTitle() == null ? "(Untitled)" : r.getTitle());
        }
        return titles;
    }
}
