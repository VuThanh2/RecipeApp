package Login;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.recipeapp.R;
import Login.SessionManager;
import RecipeManager.Recipe;
import RecipeManager.RecipeDataManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    TextInputLayout usernameLayout, passwordLayout, confirmPasswordLayout;
    TextInputEditText usernameInput, passwordInput, confirmPasswordInput;
    Button registerButton;
    private int normalColor, errorColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameLayout = findViewById(R.id.layoutUsername);
        passwordLayout = findViewById(R.id.layoutPassword);
        confirmPasswordLayout = findViewById(R.id.layoutConfirmPassword);

        usernameInput = findViewById(R.id.editUsername);
        passwordInput = findViewById(R.id.editPassword);
        confirmPasswordInput = findViewById(R.id.editConfirmPassword);
        registerButton = findViewById(R.id.btnRegister);

        normalColor = ContextCompat.getColor(this, R.color.white);
        errorColor = ContextCompat.getColor(this, R.color.red_light);

        registerButton.setOnClickListener(v -> ValidateAndRegister());

        ResetWhenTypingAgain(usernameLayout, usernameInput);
        ResetWhenTypingAgain(passwordLayout, passwordInput);
        ResetWhenTypingAgain(confirmPasswordLayout, confirmPasswordInput);
    }

    private void ValidateAndRegister() {
        String username = Objects.requireNonNull(usernameInput.getText()).toString().trim();
        String password = Objects.requireNonNull(passwordInput.getText()).toString();
        String confirmPassword = Objects.requireNonNull(confirmPasswordInput.getText()).toString();

        ResetLayoutOutlines();

        if (username.isEmpty()) {
            showError(usernameLayout, usernameInput, "Username cannot be empty");
            return;
        }
        if (password.isEmpty()) {
            showError(passwordLayout, passwordInput, "Password cannot be empty");
            return;
        }
        if (!isValidPassword(password)) {
            showError(passwordLayout, passwordInput, "Password must have 5+ chars, 1 number, 1 uppercase");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError(confirmPasswordLayout, confirmPasswordInput, "Passwords do not match");
            return;
        }


        boolean success = UserDataManager.registerUser(this, username, password);
        if (success) {
            addExampleRecipes(username);

            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
            SessionManager.setCurrentUsername(this, username);
            finish();
        } else {
            showError(usernameLayout, usernameInput, "Username already exists");
        }
    }

    private boolean isValidPassword(String password) {
        Pattern pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{5,}$");
        return pattern.matcher(password).matches();
    }

    private void showError(TextInputLayout layout, TextInputEditText input, String message) {
        layout.setBoxStrokeColor(errorColor);
        layout.setError(message);
        input.requestFocus();
    }

    private void ResetLayoutOutlines() {
        usernameLayout.setBoxStrokeColor(normalColor);
        usernameLayout.setError(null);
        passwordLayout.setBoxStrokeColor(normalColor);
        passwordLayout.setError(null);
        confirmPasswordLayout.setBoxStrokeColor(normalColor);
        confirmPasswordLayout.setError(null);
    }

    private void ResetWhenTypingAgain(TextInputLayout layout, TextInputEditText editText) {
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                layout.setBoxStrokeColor(normalColor);
                layout.setError(null);
            }
        });
    }

    private void addExampleRecipes(String username) {
        // Example Recipe 1: Simple Pasta
        Recipe pasta = new Recipe();
        pasta.setTitle("Simple Pasta");
        pasta.setCategory("Dinner");
        pasta.setInstructions("1. Boil water and cook pasta for 10 minutes\n2. Drain and add sauce\n3. Mix well and serve hot");
        pasta.setCalories(450);
        pasta.setProtein(12);
        pasta.setCarbs(75);
        pasta.setFat(8);
        pasta.setOwner(username);
        pasta.setPinned(false);

        List<Recipe.RecipeItem> pastaItems = new ArrayList<>();
        Recipe.Ingredient pastaIng = new Recipe.Ingredient(null, "Pasta");
        pastaIng.setUnit("grams");
        pastaIng.setTags(new ArrayList<>(Arrays.asList("wheat", "high-carb")));
        pastaItems.add(new Recipe.RecipeItem(pastaIng, "200"));

        Recipe.Ingredient sauceIng = new Recipe.Ingredient(null, "Tomato Sauce");
        sauceIng.setUnit("ml");
        sauceIng.setTags(new ArrayList<>());
        pastaItems.add(new Recipe.RecipeItem(sauceIng, "100"));

        pasta.setItems(pastaItems);

        // Example Recipe 2: Garden Salad
        Recipe salad = new Recipe();
        salad.setTitle("Garden Salad");
        salad.setCategory("Vegetarian");
        salad.setInstructions("1. Wash all vegetables thoroughly\n2. Chop lettuce, tomatoes, and cucumber\n3. Mix in a bowl\n4. Add olive oil and lemon juice\n5. Toss and serve fresh");
        salad.setCalories(150);
        salad.setProtein(3);
        salad.setCarbs(12);
        salad.setFat(10);
        salad.setOwner(username);
        salad.setPinned(true);

        List<Recipe.RecipeItem> saladItems = new ArrayList<>();
        Recipe.Ingredient lettuceIng = new Recipe.Ingredient(null, "Lettuce");
        lettuceIng.setUnit("grams");
        lettuceIng.setTags(new ArrayList<>());
        saladItems.add(new Recipe.RecipeItem(lettuceIng, "100"));

        Recipe.Ingredient tomatoIng = new Recipe.Ingredient(null, "Tomato");
        tomatoIng.setUnit("pieces");
        tomatoIng.setTags(new ArrayList<>());
        saladItems.add(new Recipe.RecipeItem(tomatoIng, "2"));

        Recipe.Ingredient cucumberIng = new Recipe.Ingredient(null, "Cucumber");
        cucumberIng.setUnit("pieces");
        cucumberIng.setTags(new ArrayList<>());
        saladItems.add(new Recipe.RecipeItem(cucumberIng, "1"));

        salad.setItems(saladItems);

        RecipeDataManager.AddRecipe(this, pasta);
        RecipeDataManager.AddRecipe(this, salad);
    }
}
