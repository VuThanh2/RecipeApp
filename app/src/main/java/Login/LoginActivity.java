package Login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.recipeapp.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import Login.SessionManager;

import RecipeManager.RecipeManagerActivity;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    TextInputLayout usernameLayout, passwordLayout;
    TextInputEditText usernameInput, passwordInput;
    Button loginButton;
    TextView registerLink, forgotPasswordLink;
    private int normalColor, errorColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SessionManager.isLoggedIn(this)) {
            Intent intent = new Intent(this, RecipeManagerActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        usernameLayout = findViewById(R.id.layoutUsername);
        passwordLayout = findViewById(R.id.layoutPassword);
        usernameInput = findViewById(R.id.editUsername);
        passwordInput = findViewById(R.id.editPassword);
        loginButton = findViewById(R.id.btnLogin);
        registerLink = findViewById(R.id.txtRegister);
        forgotPasswordLink = findViewById(R.id.txtForgotPassword);

        normalColor = ContextCompat.getColor(this, android.R.color.white);
        errorColor = ContextCompat.getColor(this, android.R.color.holo_red_dark);

        int accentColor = ContextCompat.getColor(this, android.R.color.white);
        registerLink.setTextColor(accentColor);
        forgotPasswordLink.setTextColor(accentColor);

        ResetWhenTypingAgain(usernameLayout, usernameInput);
        ResetWhenTypingAgain(passwordLayout, passwordInput);

        loginButton.setOnClickListener(v -> ValidateLogin());

        registerLink.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        forgotPasswordLink.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));
    }

    private void ValidateLogin() {
        String username = Objects.requireNonNull(usernameInput.getText()).toString().trim();
        String password = Objects.requireNonNull(passwordInput.getText()).toString().trim();

        ResetLayoutOutlines();

        if (username.isEmpty()) {
            showError(usernameLayout, usernameInput, "Username cannot be empty");
            return;
        }
        if (password.isEmpty()) {
            showError(passwordLayout, passwordInput, "Password cannot be empty");
            return;
        }

        if (UserDataManager.validateLogin(this, username, password)) {
            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
            SessionManager.setCurrentUsername(this, username);
            SessionManager.setLoggedIn(this, true);
            Intent intent = new Intent(LoginActivity.this, RecipeManagerActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
            finish();
        } else {
            showError(passwordLayout, passwordInput, "Invalid username or password");
        }
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
    }

    private void ResetWhenTypingAgain(TextInputLayout layout, TextInputEditText editText) {
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                layout.setBoxStrokeColor(normalColor);
                layout.setError(null);
            }
        });
    }


}
