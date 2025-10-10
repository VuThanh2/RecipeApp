package RecipeManager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.recipeapp.R;
import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

import Login.LoginActivity;
import Login.UserProfileActivity;
import MealPlanner.WeeklyPlannerActivity;

public class RecipeManagerActivity extends AppCompatActivity
        implements RecipeListFragment.OnRecipeSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView toolbarTitle;
    private ImageView profileIcon;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_manager);

        currentUsername = getIntent().getStringExtra("username");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        toolbarTitle = findViewById(R.id.tvToolbarTitle);
        profileIcon = findViewById(R.id.ivProfileIcon);

        toolbarTitle.setOnClickListener(v -> {
            PassDataToActivity(RecipeManagerActivity.class, 2);
        });

        profileIcon.setOnClickListener(v -> {
            PassDataToActivity(UserProfileActivity.class, 1);
        });

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(android.R.color.white));

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                PassDataToActivity(RecipeManagerActivity.class, 1);
            } else if (id == R.id.nav_logout) {
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
                PassDataToActivity(LoginActivity.class, 4);
            } else if (id == R.id.nav_planner) {
                PassDataToActivity(WeeklyPlannerActivity.class, 1);
            }

            drawerLayout.closeDrawers();
            return true;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new RecipeListFragment())
                    .commit();
        }
    }

    /**
     * Callback từ RecipeListFragment.
     * index đã lỗi thời (list giờ theo id); bỏ qua và mở Detail bằng recipe.
     */
    @Override
    public void onRecipeSelected(Recipe recipe, int index /*deprecated*/) {
        // Nếu bạn đã refactor RecipeDetailFragment như mình đề xuất:
        RecipeDetailFragment detailFragment = RecipeDetailFragment.newInstance(recipe);

        // Nếu muốn luôn lấy bản mới nhất từ storage theo id, bạn có thể:
        // Recipe fresh = (recipe != null && recipe.getId() != null)
        //     ? RecipeDataManager.getById(this, recipe.getId()) : null;
        // RecipeDetailFragment detailFragment = RecipeDetailFragment.newInstance(fresh != null ? fresh : recipe);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();
    }

    private void PassDataToActivity(Class<?> destinationActivity, int flagType) {
        Intent intent = new Intent(this, destinationActivity);
        intent.putExtra("username", currentUsername);
        switch (flagType) {
            case 1:
                break;
            case 2:
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                break;
            case 3:
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                break;
            case 4:
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                break;
        }
        startActivity(intent);

        if (flagType == 2 || flagType == 4) {
            finish();
        }
    }
}
