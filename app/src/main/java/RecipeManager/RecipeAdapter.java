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

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(Recipe recipe, int position);
    }

    private List<Recipe> recipeList;
    private OnItemClickListener listener;

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

        holder.itemView.setOnClickListener(v -> listener.onItemClick(recipe, position));
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory;
        ImageView ivRecipeImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvCategory = itemView.findViewById(R.id.tvItemCategory);
            ivRecipeImage = itemView.findViewById(R.id.ivRecipeImage);
        }
    }
    public void updateList(List<Recipe> newList) {
        this.recipeList.clear();
        this.recipeList.addAll(newList);
        notifyDataSetChanged();
    }
}

