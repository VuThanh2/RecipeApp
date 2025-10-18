package RecipeManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipeapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(Recipe recipe, int position);

        void onSelectionChanged(int count);

        void onMultiSelectEntered();
    }

    private List<Recipe> recipeList;
    private OnItemClickListener listener;
    private String dietMode = "normal";
    private String filterPolicy = "warn";

    private boolean multiSelectMode = false;
    private final Set<Integer> selectedPositions = new HashSet<>();

    public void setDietMode(String dietMode) {
        if (dietMode == null) return;
        this.dietMode = dietMode.trim().toLowerCase();
    }

    public void setFilterPolicy(String policy) {
        if (policy == null) return;
        String p = policy.trim().toLowerCase();
        this.filterPolicy = ("hide".equals(p)) ? "hide" : "warn";
    }

    public RecipeAdapter(List<Recipe> recipeList, OnItemClickListener listener) {
        this.recipeList = recipeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeAdapter.ViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        holder.tvTitle.setText(recipe.getTitle());
        holder.tvCategory.setText(recipe.getCategory());

        if (recipe.getImage() != 0) {
            holder.ivRecipeImage.setImageResource(recipe.getImage());
        } else {
            holder.ivRecipeImage.setImageResource(R.drawable.image_default_background);
        }

        // Part E: apply diet policy purely inside adapter
        boolean allowed = recipeAllowed(recipe, dietMode);
        if (holder.tvDietWarn != null) {
            holder.tvDietWarn.setVisibility(allowed ? View.GONE : View.VISIBLE);
        }
        if (!allowed && "hide".equals(filterPolicy)) {
            // Collapse the row to visually hide it without changing dataset
            holder.itemView.setVisibility(View.GONE);
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp != null) {
                lp.height = 0;
                lp.width = 0;
                holder.itemView.setLayoutParams(lp);
            }
        } else {
            holder.itemView.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp != null) {
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                holder.itemView.setLayoutParams(lp);
            }
        }

        View checkbox = holder.itemView.findViewById(R.id.checkbox);
        if (checkbox != null) {
            checkbox.setVisibility(multiSelectMode ? View.VISIBLE : View.GONE);
            if (checkbox instanceof android.widget.CheckBox) {
                android.widget.CheckBox cb = (android.widget.CheckBox) checkbox;
                cb.setOnCheckedChangeListener(null);
                cb.setChecked(selectedPositions.contains(position));
                cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    toggleSelection(position);
                });
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (multiSelectMode) {
                toggleSelection(position);
            } else {
                if (listener != null) listener.onItemClick(recipe, position);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (!multiSelectMode) {
                enterMultiSelectMode(position);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvDietWarn;
        ImageView ivRecipeImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvCategory = itemView.findViewById(R.id.tvItemCategory);
            ivRecipeImage = itemView.findViewById(R.id.ivRecipeImage);
            tvDietWarn = itemView.findViewById(R.id.tvDietWarn); // may be null if not in layout
        }
    }

    public void updateList(List<Recipe> newList) {
        this.recipeList.clear();
        this.recipeList.addAll(newList);
        notifyDataSetChanged();
    }

    // ===== Local rule helpers (kept inside adapter as requested) =====
    private boolean recipeAllowed(Recipe r, String diet) {
        if (r == null || r.getItems() == null) return true;
        for (Recipe.RecipeItem it : r.getItems()) {
            Recipe.Ingredient ing = it.getIngredient();
            if (ing == null || ing.getTags() == null) continue;
            for (String t : ing.getTags()) {
                if (forbiddenFor(diet).contains(t)) return false;
            }
        }
        return true;
    }

    private Set<String> forbiddenFor(String diet) {
        if (diet == null) return java.util.Collections.emptySet();
        String d = diet.trim().toLowerCase();
        if ("vegan".equals(d)) return new HashSet<>(Arrays.asList("meat", "fish", "dairy", "egg"));
        if ("keto".equals(d)) return new HashSet<>(Arrays.asList("sugar", "high-carb"));
        if ("gluten_free".equals(d))
            return new HashSet<>(Arrays.asList("wheat", "barley", "rye", "gluten"));
        return java.util.Collections.emptySet(); // normal/default
    }

    //Multi-Select Mode
    public void enterMultiSelectMode(int position) {
        multiSelectMode = true;
        toggleSelection(position);
        if (listener != null) listener.onMultiSelectEntered();
    }

    public void setMultiSelectMode(boolean mode) {
        if (this.multiSelectMode == mode) return;
        this.multiSelectMode = mode;
        if (!mode) {
            selectedPositions.clear();
            if (listener != null) listener.onSelectionChanged(0);
        }
        notifyDataSetChanged();
    }

    public void toggleSelection(int position) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(position);
        } else {
            selectedPositions.add(position);
        }
        notifyItemChanged(position);
        if (listener != null) listener.onSelectionChanged(selectedPositions.size());
    }

    public boolean isMultiSelectMode() {
        return multiSelectMode;
    }

    public List<Recipe> getSelectedRecipes() {
        List<Recipe> selected = new ArrayList<>();
        for (int i = 0; i < recipeList.size(); i++) {
            if (selectedPositions.contains(i)) {
                selected.add(recipeList.get(i));
            }
        }

        return selected;
    }

    public void silentExitMultiSelectMode() {
        multiSelectMode = false;
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    public boolean isEmpty() {
        return recipeList.isEmpty();
    }
}
