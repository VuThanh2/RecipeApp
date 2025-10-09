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

public class RecipeListFragment extends Fragment {
    private RecyclerView rvPinned, rvUnpinned;
    private RecipeAdapter adapterPinned, adapterUnpinned;
    private final List<Recipe> pinnedList = new ArrayList<>();
    private final List<Recipe> unpinnedList = new ArrayList<>();
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

        if (adapterPinned != null) adapterPinned.notifyDataSetChanged();
        if (adapterUnpinned != null) adapterUnpinned.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadData();
    }
}
