package MealPlanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.*;
import com.example.recipeapp.R;
import RecipeManager.Recipe;
import android.content.res.ColorStateList;
import android.graphics.Color;

public class WeeklyAdapter extends RecyclerView.Adapter<WeeklyAdapter.DayVH> {

    public interface Listener {
        void onAddClicked(Day day);
        void onRemoveTag(Day day, String recipeId, String title);
    }

    private final Context ctx;
    private final Listener listener;
    private String weekId;
    private final Day[] days = Day.values();
    private Map<Day, List<Recipe>> data = new HashMap<>();

    // Part F — diet context (self-contained; callers can set knobs)
    private String dietMode = "normal";   // normal | vegan | keto | gluten_free
    private String filterPolicy = "warn"; // warn | hide

    public void setDietMode(String diet) {
        if (diet == null) return;
        this.dietMode = diet.trim().toLowerCase();
        notifyDataSetChanged();
    }

    public void setFilterPolicy(String policy) {
        if (policy == null) return;
        this.filterPolicy = "hide".equalsIgnoreCase(policy) ? "hide" : "warn";
        notifyDataSetChanged();
    }

    public WeeklyAdapter(Context ctx, String weekId, Listener listener) {
        this.ctx = ctx; this.weekId = weekId; this.listener = listener;
        reload();
    }

    public void setWeekId(String weekId) { this.weekId = weekId; reload(); notifyDataSetChanged(); }
    public void reload() {
        data = MealPlanManager.getWeek(ctx, weekId);
    }


    @NonNull @Override public DayVH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_day_row, p, false);
        return new DayVH(view);
    }

    @Override public void onBindViewHolder(@NonNull DayVH h, int pos) {
        Day d = days[pos];
        h.dayLabel.setText(d.name().substring(0,1)); // S/M/T...
        h.chips.removeAllViews();
        List<Recipe> tags = data.get(d);
        if (tags != null) for (Recipe t: tags) {
            boolean allowed = isAllowedRecipe(t, dietMode);
            if (!allowed && "hide".equalsIgnoreCase(filterPolicy)) {
                // Skip adding this chip entirely
                continue;
            }
            Chip c = (Chip) LayoutInflater.from(ctx).inflate(R.layout.part_chip, h.chips, false);
            String title = t.getTitle() == null ? "(Untitled)" : t.getTitle();
            if (!allowed && "warn".equalsIgnoreCase(filterPolicy)) title += " \u26A0"; // ⚠
            c.setText(title);

            if (!allowed) {
                // Visual warn: red stroke + slight dim
                c.setAlpha(0.8f);
                c.setChipStrokeWidth(2f);
                c.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor("#FF4444")));
            }

            c.setOnLongClickListener(v -> { listener.onRemoveTag(d, t.getId(), t.getTitle()); return true; });
            h.chips.addView(c);
        }
        h.addBtn.setOnClickListener(v -> listener.onAddClicked(d));
    }

    @Override public int getItemCount() { return days.length; }

    static class DayVH extends RecyclerView.ViewHolder {
        TextView dayLabel; ChipGroup chips; ImageButton addBtn;
        DayVH(@NonNull View itemView) {
            super(itemView);
            dayLabel = itemView.findViewById(R.id.tvDay);
            chips = itemView.findViewById(R.id.chipGroup);
            addBtn = itemView.findViewById(R.id.btnAdd);
        }
    }

    // ===== Local diet helpers (kept inside adapter) =====
    private boolean isAllowedRecipe(Recipe r, String diet) {
        if (r == null || r.getItems() == null) return true;
        java.util.Set<String> forbidden = forbiddenFor(diet);
        for (Recipe.RecipeItem it : r.getItems()) {
            Recipe.Ingredient ing = it.getIngredient();
            if (ing == null || ing.getTags() == null) continue;
            for (String t : ing.getTags()) if (forbidden.contains(t)) return false;
        }
        return true;
    }

    private java.util.Set<String> forbiddenFor(String diet) {
        String d = diet == null ? "" : diet.trim().toLowerCase();
        if ("vegan".equals(d)) return new java.util.HashSet<>(java.util.Arrays.asList("meat","fish","dairy","egg"));
        if ("keto".equals(d)) return new java.util.HashSet<>(java.util.Arrays.asList("sugar","high-carb"));
        if ("gluten_free".equals(d)) return new java.util.HashSet<>(java.util.Arrays.asList("wheat","barley","rye","gluten"));
        return java.util.Collections.emptySet();
    }
}
