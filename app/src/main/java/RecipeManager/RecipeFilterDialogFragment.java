package RecipeManager;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.recipeapp.R;

import java.util.ArrayList;
import java.util.List;

public class RecipeFilterDialogFragment extends DialogFragment {
    private static final String ARG_FILTER = "arg_filter";

    public interface OnFilterAppliedListener {
        void onFilterApplied(RecipeFilterItem filterItem);
    }

    private OnFilterAppliedListener listener;
    private RecipeFilterItem currentFilter;

    public static RecipeFilterDialogFragment newInstance(RecipeFilterItem filter) {
        RecipeFilterDialogFragment fragment = new RecipeFilterDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_FILTER, filter != null ? filter : new RecipeFilterItem());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof OnFilterAppliedListener) {
            listener = (OnFilterAppliedListener) getParentFragment();
        } else if (context instanceof OnFilterAppliedListener) {
            listener = (OnFilterAppliedListener) context;
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentFilter = (RecipeFilterItem) getArguments().getSerializable(ARG_FILTER);
        }
        if (currentFilter == null) currentFilter = new RecipeFilterItem();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.dialog_recipe_filter, null);

        CheckBox cbBreakfast = view.findViewById(R.id.cbBreakfast);
        CheckBox cbLunch = view.findViewById(R.id.cbLunch);
        CheckBox cbDinner = view.findViewById(R.id.cbDinner);
        CheckBox cbDessert = view.findViewById(R.id.cbDessert);
        CheckBox cbVegetarian = view.findViewById(R.id.cbVegetarian);

        EditText etMinCalories = view.findViewById(R.id.etMinCalories);
        EditText etMaxCalories = view.findViewById(R.id.etMaxCalories);
        EditText etMinFat = view.findViewById(R.id.etMinFat);
        EditText etMaxFat = view.findViewById(R.id.etMaxFat);
        EditText etMinCarbs = view.findViewById(R.id.etMinCarbs);
        EditText etMaxCarbs = view.findViewById(R.id.etMaxCarbs);
        EditText etMinProtein = view.findViewById(R.id.etMinProtein);
        EditText etMaxProtein = view.findViewById(R.id.etMaxProtein);

        Button btnApply = view.findViewById(R.id.btnApplyFilter);
        Button btnReset = view.findViewById(R.id.btnReset);

        LoadFilterWhenOpen(currentFilter, cbBreakfast, cbLunch, cbDinner, cbDessert, cbVegetarian,
                etMinCalories, etMaxCalories, etMinProtein, etMaxProtein, etMinFat, etMaxFat, etMinCarbs, etMaxCarbs);

        btnApply.setOnClickListener(v -> {
            RecipeFilterItem filterItem = CollectFilter(cbBreakfast, cbLunch, cbDinner, cbDessert, cbVegetarian,
                    etMinCalories, etMaxCalories, etMinProtein, etMaxProtein, etMinFat, etMaxFat, etMinCarbs, etMaxCarbs);

            currentFilter = filterItem;
            if (listener != null) listener.onFilterApplied(filterItem);
            dismiss();
        });

        btnReset.setOnClickListener(v -> {
            cbBreakfast.setChecked(false);
            cbLunch.setChecked(false);
            cbDinner.setChecked(false);
            cbDessert.setChecked(false);
            cbVegetarian.setChecked(false);

            etMinCalories.setText("");
            etMaxCalories.setText("");
            etMinProtein.setText("");
            etMaxProtein.setText("");
            etMinFat.setText("");
            etMaxFat.setText("");
            etMinCarbs.setText("");
            etMaxCarbs.setText("");

            currentFilter = new RecipeFilterItem();
            if (listener != null && isAdded()) listener.onFilterApplied(new RecipeFilterItem());
            dismiss();
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(view);
        builder.setTitle("Filter Recipes");

        return builder.create();
    }

    private RecipeFilterItem CollectFilter(CheckBox cbBreakfast, CheckBox cbLunch, CheckBox cbDinner,
                                           CheckBox cbDessert, CheckBox cbVegetarian,
                                           EditText etMinCalories, EditText etMaxCalories,
                                           EditText etMinProtein, EditText etMaxProtein,
                                           EditText etMinFat, EditText etMaxFat,
                                           EditText etMinCarbs, EditText etMaxCarbs) {
        RecipeFilterItem filterItem = new RecipeFilterItem();
        List<String> categories = new ArrayList<>();
        if (cbBreakfast.isChecked()) categories.add("Breakfast");
        if (cbLunch.isChecked()) categories.add("Lunch");
        if (cbDinner.isChecked()) categories.add("Dinner");
        if (cbDessert.isChecked()) categories.add("Dessert");
        if (cbVegetarian.isChecked()) categories.add("Vegetarian");
        filterItem.categories = categories;

        try { filterItem.minCalories = Integer.parseInt(etMinCalories.getText().toString()); } catch (Exception ignored) {}
        try { filterItem.maxCalories = Integer.parseInt(etMaxCalories.getText().toString()); } catch (Exception ignored) {}
        try { filterItem.minProtein = Integer.parseInt(etMinProtein.getText().toString()); } catch (Exception ignored) {}
        try { filterItem.maxProtein = Integer.parseInt(etMaxProtein.getText().toString()); } catch (Exception ignored) {}
        try { filterItem.minFat = Integer.parseInt(etMinFat.getText().toString()); } catch (Exception ignored) {}
        try { filterItem.maxFat = Integer.parseInt(etMaxFat.getText().toString()); } catch (Exception ignored) {}
        try { filterItem.minCarbs = Integer.parseInt(etMinCarbs.getText().toString()); } catch (Exception ignored) {}
        try { filterItem.maxCarbs = Integer.parseInt(etMaxCarbs.getText().toString()); } catch (Exception ignored) {}

        return filterItem;
    }

    private void LoadFilterWhenOpen(RecipeFilterItem filter,
                                    CheckBox cbBreakfast, CheckBox cbLunch, CheckBox cbDinner,
                                    CheckBox cbDessert, CheckBox cbVegetarian,
                                    EditText etMinCalories, EditText etMaxCalories,
                                    EditText etMinProtein, EditText etMaxProtein,
                                    EditText etMinFat, EditText etMaxFat,
                                    EditText etMinCarbs, EditText etMaxCarbs) {
        if (filter == null) return;

        cbBreakfast.setChecked(filter.categories.contains("Breakfast"));
        cbLunch.setChecked(filter.categories.contains("Lunch"));
        cbDinner.setChecked(filter.categories.contains("Dinner"));
        cbDessert.setChecked(filter.categories.contains("Dessert"));
        cbVegetarian.setChecked(filter.categories.contains("Vegetarian"));

        etMinCalories.setText(filter.minCalories > 0 ? String.valueOf(filter.minCalories) : "");
        etMaxCalories.setText(filter.maxCalories < Integer.MAX_VALUE ? String.valueOf(filter.maxCalories) : "");
        etMinProtein.setText(filter.minProtein > 0 ? String.valueOf(filter.minProtein) : "");
        etMaxProtein.setText(filter.maxProtein < Integer.MAX_VALUE ? String.valueOf(filter.maxProtein) : "");
        etMinFat.setText(filter.minFat > 0 ? String.valueOf(filter.minFat) : "");
        etMaxFat.setText(filter.maxFat < Integer.MAX_VALUE ? String.valueOf(filter.maxFat) : "");
        etMinCarbs.setText(filter.minCarbs > 0 ? String.valueOf(filter.minCarbs) : "");
        etMaxCarbs.setText(filter.maxCarbs < Integer.MAX_VALUE ? String.valueOf(filter.maxCarbs) : "");
    }
}
