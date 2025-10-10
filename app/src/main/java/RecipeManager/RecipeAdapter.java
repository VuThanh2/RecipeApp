package RecipeManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipeapp.R;

import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(Recipe recipe, int position);
    }

    private List<Recipe> recipeList;
    private OnItemClickListener listener;

    // Part E: Diet context lives here so callers don't need to change other classes
    private String dietMode = "normal";      // e.g., normal|vegan|keto|gluten_free
    private String filterPolicy = "warn";    // warn|hide

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
            holder.ivRecipeImage.setImageResource(R.drawable.default_background);
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
                holder.itemView.setLayoutParams(lp);
            }
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(recipe, position));
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
        if ("vegan".equals(d)) return new HashSet<>(Arrays.asList("meat","fish","dairy","egg"));
        if ("keto".equals(d)) return new HashSet<>(Arrays.asList("sugar","high-carb"));
        if ("gluten_free".equals(d)) return new HashSet<>(Arrays.asList("wheat","barley","rye","gluten"));
        return java.util.Collections.emptySet(); // normal/default
    }
}
