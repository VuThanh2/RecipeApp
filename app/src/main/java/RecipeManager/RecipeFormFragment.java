package RecipeManager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import RecipeManager.Recipe;
import RecipeManager.RecipeDataManager;

import com.example.recipeapp.R;

import org.json.JSONException;
import org.json.JSONObject;

public class RecipeFormFragment extends Fragment {
    private EditText etTitle, etIngredients, etInstructions;
    private AutoCompleteTextView etCategory;
    private Button btnSave;
    private ImageView ivRecipeImage;
    private Recipe recipe;
    private int selectedImage = R.drawable.default_background;
    private boolean isPinned = false;
    private ImageView btnPin;

    public static RecipeFormFragment newInstance(@Nullable Recipe recipe) {
        RecipeFormFragment f = new RecipeFormFragment();
        Bundle args = new Bundle();
        args.putSerializable("recipe", recipe);
        f.setArguments(args);
        return f;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_form, container, false);

        etTitle = view.findViewById(R.id.etTitle);
        etCategory = view.findViewById(R.id.etCategory);
        etIngredients = view.findViewById(R.id.etIngredients);
        etInstructions = view.findViewById(R.id.etInstructions);
        btnSave = view.findViewById(R.id.btnSave);
        ivRecipeImage = view.findViewById(R.id.ivRecipeImage);
        btnPin = view.findViewById(R.id.btnPin);

        String[] categories = {"Breakfast", "Lunch", "Dinner", "Vegetarian", "Dessert"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line,categories);
        etCategory.setAdapter(adapter);
        etCategory.setThreshold(0);
        etCategory.setKeyListener(null);

        LoadRecipeFromDetail();

        etCategory.setOnClickListener(v -> {
            if (!etCategory.isPopupShowing()) {
                String previousText = etCategory.getText().toString();
                etCategory.setTag(previousText);
                etCategory.setText("");
                etCategory.showDropDown();
            }
        });

        etCategory.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String previousText = (String) etCategory.getTag();
                if (etCategory.getText().toString().isEmpty() && previousText != null) {
                    etCategory.setText(previousText);
                }
            }
        });

        btnPin.setOnClickListener(v -> {
            isPinned = !isPinned;
            updatePinIcon();
        });

        int editingIndex = getArguments() != null ? getArguments().getInt("index", -1) : -1;
        ivRecipeImage.setOnClickListener(v -> showImageSelectionDialog());

        btnSave.setOnClickListener(v -> {
            // 1) Lấy (hoặc tạo) Recipe đang chỉnh
            Recipe toSave = (recipe != null) ? recipe : new Recipe();  // cần ctor rỗng

            // 2) Đổ dữ liệu từ form
            toSave.setTitle(etTitle.getText().toString());
            toSave.setCategory(etCategory.getText().toString());
            toSave.setIngredients(etIngredients.getText().toString());
            toSave.setInstructions(etInstructions.getText().toString());
            toSave.setImage(selectedImage);
            toSave.setPinned(isPinned);

            // 3) Gọi API mới theo id
            if (toSave.getId() == null || toSave.getId().isEmpty()) {
                // Trường hợp tạo mới (id sẽ tự phát trong add(...))
                RecipeDataManager.add(requireContext(), toSave);
            } else {
                // Trường hợp cập nhật
                RecipeDataManager.updateById(requireContext(), toSave.getId(), toSave);
            }

            // 4) Điều hướng như cũ
            requireActivity().getSupportFragmentManager().popBackStack(null,
                    getParentFragmentManager().POP_BACK_STACK_INCLUSIVE);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new RecipeListFragment())
                    .commit();
        });

        return view;
    }

    private void updatePinIcon() {
        if (isPinned) {
            btnPin.setImageResource(R.drawable.ic_heart_filled);
        } else {
            btnPin.setImageResource(R.drawable.ic_heart);
        }
    }

    private void showImageSelectionDialog() {
        final int[] imageResIds = {
                R.drawable.pho,
                R.drawable.steak,
                R.drawable.salad,
        };

        String[] imageNames = {"Pho", "Steak", "Salad"};

        new AlertDialog.Builder(getContext())
                .setTitle("Select an image")
                .setItems(imageNames, (dialog, which) -> {
                    selectedImage = imageResIds[which];
                    ivRecipeImage.setImageResource(selectedImage);
                })
                .show();
    }

    private void LoadRecipeFromDetail() {
        if (getArguments() != null) {
            recipe = (Recipe) getArguments().getSerializable("recipe");
            if (recipe != null) {
                etTitle.setText(recipe.getTitle());
                etCategory.setText(recipe.getCategory());
                etIngredients.setText(recipe.getIngredients());
                etInstructions.setText(recipe.getInstructions());
                selectedImage = recipe.getImage();
                ivRecipeImage.setImageResource(selectedImage);
                isPinned = recipe.isPinned();
                updatePinIcon();
            }
        }
    }
}
