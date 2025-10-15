package RecipeManager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.recipeapp.R;

public class RecipeDetailFragment extends Fragment {
    private Recipe recipe;

    private TextView tvTitle, tvCategory, tvIngredients, tvInstructions;
    private Button btnEdit, btnDelete;
    private ImageView ivRecipeImage;

    /**
     * Khởi tạo theo Recipe (không còn index).
     */
    public static RecipeDetailFragment newInstance(Recipe recipe) {
        RecipeDetailFragment fragment = new RecipeDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("recipe", recipe); // có thể chuyển sang Parcelable sau
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_detail, container, false);

        tvTitle = view.findViewById(R.id.tvTitle);
        ivRecipeImage = view.findViewById(R.id.ivRecipeBackground);
        tvCategory = view.findViewById(R.id.tvCategory);
        tvIngredients = view.findViewById(R.id.tvIngredients);
        tvInstructions = view.findViewById(R.id.tvInstructions);
        btnEdit = view.findViewById(R.id.btnEdit);
        btnDelete = view.findViewById(R.id.btnDelete);

        loadRecipeFromArgs();
        bindRecipeToUi();

        btnEdit.setOnClickListener(v -> {
            if (recipe == null) {
                Toast.makeText(requireContext(), "Recipe not found", Toast.LENGTH_SHORT).show();
                return;
            }
            // Mở form ở chế độ EDIT theo Recipe (id nằm trong recipe)
            RecipeFormFragment formFragment = RecipeFormFragment.newInstance(recipe);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, formFragment)
                    .addToBackStack(null)
                    .commit();
        });

        btnDelete.setOnClickListener(v -> {
            if (recipe == null || recipe.getId() == null || recipe.getId().isEmpty()) {
                Toast.makeText(requireContext(), "Cannot delete: invalid recipe id", Toast.LENGTH_SHORT).show();
                return;
            }
            // ✅ API mới theo id
            RecipeDataManager.DeleteRecipeById(requireContext(), recipe.getId());
            // Quay lại danh sách; RecipeListFragment sẽ reload ở onResume()
            getParentFragmentManager().popBackStack();
        });

        return view;
    }

    private void loadRecipeFromArgs() {
        Bundle args = getArguments();
        if (args != null) {
            recipe = (Recipe) args.getSerializable("recipe");
            // Nếu bạn muốn “chắc cú” dữ liệu mới nhất theo id:
            // if (recipe != null && recipe.getId() != null) {
            //     Recipe fresh = RecipeDataManager.getById(requireContext(), recipe.getId());
            //     if (fresh != null) recipe = fresh;
            // }
        }
    }

    private void bindRecipeToUi() {
        if (recipe == null) return;
        tvTitle.setText(recipe.getTitle());
        tvCategory.setText(recipe.getCategory());
        tvIngredients.setText(recipe.getIngredients());
        tvInstructions.setText(recipe.getInstructions());
        ivRecipeImage.setImageResource(recipe.getImage());
    }
}
