package RecipeManager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
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

public class RecipeManagerActivity extends AppCompatActivity implements RecipeListFragment.OnRecipeSelectedListener {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ImageView profileIcon;
    private String currentUsername;
    private EditText etSearch;
    private RecipeListFragment recipeListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_manager);

        currentUsername = getIntent().getStringExtra("username");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        profileIcon = findViewById(R.id.ivProfileIcon);
        etSearch = findViewById(R.id.etSearch);

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
                PassDataToActivity(RecipeManagerActivity.class, 4);
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
            recipeListFragment = new RecipeListFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, recipeListFragment)
                    .commit();
        } else {
            recipeListFragment = (RecipeListFragment)
                    getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        }
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (recipeListFragment != null) {
                    recipeListFragment.FilterRecipesForSearching(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public void onRecipeSelected(Recipe recipe, int index) {
        RecipeDetailFragment detailFragment = RecipeDetailFragment.newInstance(recipe);

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
