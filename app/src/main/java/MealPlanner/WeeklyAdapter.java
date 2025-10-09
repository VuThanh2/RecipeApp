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

public class WeeklyAdapter extends RecyclerView.Adapter<WeeklyAdapter.DayVH> {

    public interface Listener {
        void onAddClicked(Day day);
        void onRemoveTag(Day day, String recipeId, String title);
    }

    private final Context ctx;
    private final Listener listener;
    private String weekId;
    private final Day[] days = Day.values();
    private Map<Day, List<RecipeTag>> data = new HashMap<>();

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
        List<RecipeTag> tags = data.get(d);
        if (tags != null) for (RecipeTag t: tags) {
            Chip c = (Chip) LayoutInflater.from(ctx).inflate(R.layout.part_chip, h.chips, false);
            c.setText(t.title);
            c.setOnLongClickListener(v -> { listener.onRemoveTag(d, t.id, t.title); return true; });
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
}
