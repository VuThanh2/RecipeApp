package Login;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.recipeapp.R;

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

        normalColor = ContextCompat.getColor(this, R.color.purple);
        errorColor = ContextCompat.getColor(this, android.R.color.holo_red_dark);

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
            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
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
}
