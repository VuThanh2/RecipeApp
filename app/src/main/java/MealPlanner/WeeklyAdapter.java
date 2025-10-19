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

import Login.SessionManager;

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

    private String normalizeWeekId(String id) {
        if (id == null) return null;
        if (id.contains("::")) return id; // already namespaced
        String user = SessionManager.getCurrentUsername(ctx);
        if (user == null || user.isEmpty()) return id; // best effort
        return user.trim().toLowerCase() + "::" + id;
    }

    public WeeklyAdapter(Context ctx, String weekId, Listener listener) {
        this.ctx = ctx; this.listener = listener;
        this.weekId = normalizeWeekId(weekId);
        reload();
    }

    public void setWeekId(String weekId) {
        this.weekId = normalizeWeekId(weekId);
        reload();
        notifyDataSetChanged();
    }
    public void reload() {
        String id = normalizeWeekId(this.weekId);
        if (id == null) {
            data = new HashMap<>();
        } else {
            data = MealPlanManager.getWeek(ctx, id);
        }
    }


    @NonNull @Override public DayVH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_day_row, p, false);
        return new DayVH(view);
    }

    @Override public void onBindViewHolder(@NonNull DayVH h, int pos) {
        Day d = days[pos];
        h.dayLabel.setText(d.name());
        h.chips.removeAllViews();
        List<Recipe> tags = data.get(d);
        if (tags != null) for (Recipe t: tags) {
            boolean allowed = isAllowedRecipe(t, dietMode);
            if (!allowed && "hide".equalsIgnoreCase(filterPolicy)) {
                // Skip adding this chip entirely
                continue;
            }
            Chip chip = (Chip) LayoutInflater.from(ctx).inflate(R.layout.part_chip, h.chips, false);
            String title = t.getTitle() == null ? "(Untitled)" : t.getTitle();
            if (!allowed && "warn".equalsIgnoreCase(filterPolicy)) title += " \u26A0"; // ⚠
            chip.setText(title);

            chip.setCloseIconVisible(true);
            chip.setCloseIconResource(android.R.drawable.ic_menu_close_clear_cancel);
            chip.setCloseIconTint(ColorStateList.valueOf(Color.BLACK));

            chip.setOnCloseIconClickListener(v -> {
                listener.onRemoveTag(d, t.getId(), t.getTitle()); // notify listener
                h.chips.removeView(chip);
            });

            if (!allowed) {
                // Visual warn: red stroke + slight dim
                chip.setAlpha(0.8f);
                chip.setChipStrokeWidth(2f);
                chip.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor("#FF4444")));
            }

            h.chips.addView(chip);
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
