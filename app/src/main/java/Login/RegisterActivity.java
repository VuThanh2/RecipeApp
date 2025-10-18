package Login;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.recipeapp.R;
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
        if (!User.isValidUsername(username)) {
            showError(usernameLayout, usernameInput, "Invalid username format");
            Toast.makeText(this, "Username: 3-20 chars (letters, numbers, _ or -)", Toast.LENGTH_SHORT).show();
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
        if (confirmPassword.isEmpty()) {
            showError(confirmPasswordLayout, confirmPasswordInput, "Please confirm password");
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

    private void addExampleRecipes(String username) {
        // Example Recipe 1: Simple Pasta
        Recipe pasta = new Recipe();
        pasta.setTitle("Simple Pasta");
        pasta.setImage(R.drawable.image_pasta);
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
        pastaIng.setUnit("g");
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
        salad.setImage(R.drawable.image_salad);
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
        lettuceIng.setUnit("g");
        saladItems.add(new Recipe.RecipeItem(lettuceIng, "100"));

        Recipe.Ingredient tomatoIng = new Recipe.Ingredient(null, "Tomato");
        tomatoIng.setUnit("pieces");
        saladItems.add(new Recipe.RecipeItem(tomatoIng, "2"));

        Recipe.Ingredient cucumberIng = new Recipe.Ingredient(null, "Cucumber");
        cucumberIng.setUnit("pieces");
        saladItems.add(new Recipe.RecipeItem(cucumberIng, "1"));
        salad.setItems(saladItems);

        // Example Recipe 3: Bánh Mì
        Recipe banhMi = new Recipe();
        banhMi.setTitle("Bánh Mì");
        banhMi.setImage(R.drawable.image_banh_mi);
        banhMi.setCategory("Breakfast");
        banhMi.setInstructions("1. Toast baguette\n2. Add grilled pork, pickled veggies, cucumber\n3. Add mayo, soy sauce, herbs, chili\n4. Serve warm");
        banhMi.setCalories(420);
        banhMi.setProtein(18);
        banhMi.setCarbs(55);
        banhMi.setFat(12);
        banhMi.setOwner(username);
        banhMi.setPinned(false);

        List<Recipe.RecipeItem> banhMiItems = new ArrayList<>();
        Recipe.Ingredient breadIng = new Recipe.Ingredient(null, "Baguette");
        breadIng.setUnit("pieces");
        banhMiItems.add(new Recipe.RecipeItem(breadIng, "1"));

        Recipe.Ingredient porkIng = new Recipe.Ingredient(null, "Grilled Pork");
        porkIng.setUnit("g");
        banhMiItems.add(new Recipe.RecipeItem(porkIng, "100"));

        Recipe.Ingredient pickleIng = new Recipe.Ingredient(null, "Pickled Vegetables");
        pickleIng.setUnit("g");
        banhMiItems.add(new Recipe.RecipeItem(pickleIng, "50"));
        banhMi.setItems(banhMiItems);

        // Example Recipe 4: Vanilla Cake
        Recipe cake = new Recipe();
        cake.setTitle("Vanilla Cake");
        cake.setImage(R.drawable.image_cake);
        cake.setCategory("Dessert");
        cake.setInstructions("1. Mix flour, sugar, eggs, milk\n2. Bake 30 mins at 180°C\n3. Cool and serve");
        cake.setCalories(380);
        cake.setProtein(6);
        cake.setCarbs(60);
        cake.setFat(14);
        cake.setOwner(username);
        cake.setPinned(false);

        List<Recipe.RecipeItem> cakeItems = new ArrayList<>();
        Recipe.Ingredient flourIng = new Recipe.Ingredient(null, "Flour");
        flourIng.setUnit("g");
        cakeItems.add(new Recipe.RecipeItem(flourIng, "200"));

        Recipe.Ingredient sugarIng = new Recipe.Ingredient(null, "Sugar");
        sugarIng.setUnit("g");
        cakeItems.add(new Recipe.RecipeItem(sugarIng, "100"));

        Recipe.Ingredient eggIng = new Recipe.Ingredient(null, "Eggs");
        eggIng.setUnit("pieces");
        cakeItems.add(new Recipe.RecipeItem(eggIng, "2"));
        cake.setItems(cakeItems);

        // Example Recipe 5: Garlic Butter Steak
        Recipe steak = new Recipe();
        steak.setTitle("Garlic Butter Steak");
        steak.setImage(R.drawable.image_steak);
        steak.setCategory("Dinner");
        steak.setInstructions("1. Season steak\n2. Sear 3-4 min each side\n3. Add garlic butter and rest 5 min\n4. Serve hot");
        steak.setCalories(520);
        steak.setProtein(40);
        steak.setCarbs(0);
        steak.setFat(38);
        steak.setOwner(username);
        steak.setPinned(true);

        List<Recipe.RecipeItem> steakItems = new ArrayList<>();
        Recipe.Ingredient beefIng = new Recipe.Ingredient(null, "Ribeye Steak");
        beefIng.setUnit("pieces");
        steakItems.add(new Recipe.RecipeItem(beefIng, "1"));

        Recipe.Ingredient butterIng = new Recipe.Ingredient(null, "Butter");
        butterIng.setUnit("g");
        steakItems.add(new Recipe.RecipeItem(butterIng, "20"));

        Recipe.Ingredient garlicIng = new Recipe.Ingredient(null, "Garlic");
        garlicIng.setUnit("cloves");
        steakItems.add(new Recipe.RecipeItem(garlicIng, "2"));
        steak.setItems(steakItems);

        // Example Recipe 6: Sushi Roll
        Recipe sushi = new Recipe();
        sushi.setTitle("Sushi Roll");
        sushi.setImage(R.drawable.image_sushi);
        sushi.setCategory("Dinner");
        sushi.setInstructions("1. Cook sushi rice\n2. Place nori sheet and add rice\n3. Add fillings (salmon, cucumber)\n4. Roll tightly and slice");
        sushi.setCalories(310);
        sushi.setProtein(22);
        sushi.setCarbs(45);
        sushi.setFat(8);
        sushi.setOwner(username);
        sushi.setPinned(false);

        List<Recipe.RecipeItem> sushiItems = new ArrayList<>();
        Recipe.Ingredient riceIng = new Recipe.Ingredient(null, "Sushi Rice");
        riceIng.setUnit("g");
        sushiItems.add(new Recipe.RecipeItem(riceIng, "150"));

        Recipe.Ingredient noriIng = new Recipe.Ingredient(null, "Nori Sheet");
        noriIng.setUnit("pieces");
        sushiItems.add(new Recipe.RecipeItem(noriIng, "1"));

        Recipe.Ingredient salmonIng = new Recipe.Ingredient(null, "Salmon");
        salmonIng.setUnit("g");
        sushiItems.add(new Recipe.RecipeItem(salmonIng, "50"));
        sushi.setItems(sushiItems);

        RecipeDataManager.AddRecipe(this, pasta);
        RecipeDataManager.AddRecipe(this, salad);
        RecipeDataManager.AddRecipe(this, banhMi);
        RecipeDataManager.AddRecipe(this, cake);
        RecipeDataManager.AddRecipe(this, steak);
        RecipeDataManager.AddRecipe(this, sushi);
    }

}
