package MealPlanner;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recipeapp.R;


public class WeeklyPlannerActivity extends AppCompatActivity implements WeeklyAdapter.Listener {

    private String weekId;
    private WeeklyAdapter adapter;
    private TextView tvWeek;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_weekly_planner);

        tvWeek = findViewById(R.id.tvWeek);
        RecyclerView rv = findViewById(R.id.rvWeek);
        ImageButton prev = findViewById(R.id.btnPrev);
        ImageButton next = findViewById(R.id.btnNext);

        weekId = MealPlanManager.currentWeekId();
        tvWeek.setText(weekId);
        rv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this)); // <-- thêm dòng này

        adapter = new WeeklyAdapter(this, weekId, this);
        rv.setAdapter(adapter);

        prev.setOnClickListener(v -> {
            weekId = MealPlanManager.offsetWeekId(weekId, -1);
            tvWeek.setText(weekId);
            adapter.setWeekId(weekId);
        });
        next.setOnClickListener(v -> {
            weekId = MealPlanManager.offsetWeekId(weekId, +1);
            tvWeek.setText(weekId);
            adapter.setWeekId(weekId);
        });

        findViewById(R.id.btnStatistic).setOnClickListener(v ->
                Toast.makeText(this, "Statistic (stub)", Toast.LENGTH_SHORT).show()
        );
    }

    // === Adapter callbacks ===
    @Override public void onAddClicked(Day day) {
        new AddRecipeBottomSheet(tag -> {
            boolean ok = MealPlanManager.addRecipe(this, weekId, day, tag);
            if (!ok) Toast.makeText(this, "Đã có món trong ngày", Toast.LENGTH_SHORT).show();
            adapter.reload(); adapter.notifyDataSetChanged();
        }).show(getSupportFragmentManager(), "add-recipe");
    }

    @Override public void onRemoveTag(Day day, String recipeId, String title) {
        new AlertDialog.Builder(this)
                .setMessage("Xoá \"" + title + "\" khỏi " + day.name() + "?")
                .setPositiveButton("Xoá", (d, w) -> {
                    MealPlanManager.removeRecipe(this, weekId, day, recipeId);
                    adapter.reload(); adapter.notifyDataSetChanged();
                })
                .setNegativeButton("Huỷ", null).show();
    }


}
