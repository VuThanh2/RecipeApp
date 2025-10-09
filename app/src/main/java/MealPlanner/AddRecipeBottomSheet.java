package MealPlanner;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.recipeapp.R;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

// Giả sử có RecipeManager.getAll() trả List<RecipeTag>
public class AddRecipeBottomSheet extends BottomSheetDialogFragment {
    public interface OnPick { void onPicked(RecipeTag tag); }
    private OnPick callback;

    public AddRecipeBottomSheet(OnPick cb) { this.callback = cb; }

    private ArrayAdapter<String> adapter;
    private List<RecipeTag> all = new ArrayList<>();
    private List<RecipeTag> filtered = new ArrayList<>();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle b) {
        View v = inf.inflate(R.layout.bottomsheet_add_recipe, c, false);
        EditText search = v.findViewById(R.id.edSearch);
        ListView list = v.findViewById(R.id.listRecipes);

        all = RecipeManager.getAllTags(); // bạn đã có sẵn dữ liệu recipe
        filtered.addAll(all);
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1,
                toTitles(filtered));
        list.setAdapter(adapter);

        list.setOnItemClickListener((parent, view, position, id) -> {
            RecipeTag tag = filtered.get(position);
            if (callback != null) callback.onPicked(tag);
            dismiss();
        });

        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int a,int b,int c){}
            public void afterTextChanged(Editable s){}
            public void onTextChanged(CharSequence s,int a,int b,int c){
                filter(s.toString());
            }
        });
        return v;
    }

    private void filter(String q){
        filtered.clear();
        q = q.toLowerCase().trim();
        for (RecipeTag t: all) if (t.title.toLowerCase().contains(q)) filtered.add(t);
        adapter.clear(); adapter.addAll(toTitles(filtered)); adapter.notifyDataSetChanged();
    }
    private List<String> toTitles(List<RecipeTag> list){
        ArrayList<String> r = new ArrayList<>();
        for (RecipeTag t: list) r.add(t.title);
        return r;
    }
}
