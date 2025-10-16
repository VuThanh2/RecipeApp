package Login;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.recipeapp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Objects;
import java.util.regex.Pattern;

public class ForgotPasswordActivity extends AppCompatActivity {
    TextInputLayout usernameLayout, newPasswordLayout, confirmPasswordLayout;
    TextInputEditText usernameInput, newPasswordInput, confirmPasswordInput;
    Button resetPasswordButton;
    private int normalColor, errorColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        usernameLayout = findViewById(R.id.layoutUsername);
        newPasswordLayout = findViewById(R.id.layoutNewPassword);
        confirmPasswordLayout = findViewById(R.id.layoutConfirmPassword);
        usernameInput = findViewById(R.id.editUsernameOrEmail);
        newPasswordInput = findViewById(R.id.editNewPassword);
        confirmPasswordInput = findViewById(R.id.editConfirmPassword);
        resetPasswordButton = findViewById(R.id.btnResetPassword);

        normalColor = ContextCompat.getColor(this, R.color.white);
        errorColor = ContextCompat.getColor(this, R.color.red_light);

        resetPasswordButton.setOnClickListener(v -> ValidateAndResetPassword());

        ResetWhenTypingAgain(usernameLayout, usernameInput);
        ResetWhenTypingAgain(newPasswordLayout, newPasswordInput);
        ResetWhenTypingAgain(confirmPasswordLayout, confirmPasswordInput);
    }

    private void ValidateAndResetPassword() {
        String username = Objects.requireNonNull(usernameInput.getText()).toString().trim();
        String newPassword = Objects.requireNonNull(newPasswordInput.getText()).toString();
        String confirmNewPassword = Objects.requireNonNull(confirmPasswordInput.getText()).toString();

        ResetLayoutOutline();

        if (username.isEmpty()) {
            showError(usernameLayout, usernameInput, "Username cannot be empty");
            return;
        }
        if (newPassword.isEmpty()) {
            showError(newPasswordLayout, newPasswordInput, "New password cannot be empty");
            return;
        }
        if (!isValidPassword(newPassword)) {
            showError(newPasswordLayout, newPasswordInput, "Password must have 5+ chars, 1 number, 1 uppercase");
            return;
        }
        if (!newPassword.equals(confirmNewPassword)) {
            showError(confirmPasswordLayout, confirmPasswordInput, "Passwords do not match");
            return;
        }

        boolean success = UserDataManager.updatePassword(this, username, newPassword);
        if (success) {
            Toast.makeText(this, "Password reset successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            showError(usernameLayout, usernameInput, "User not found");
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

    private void ResetLayoutOutline() {
        usernameLayout.setBoxStrokeColor(normalColor);
        usernameLayout.setError(null);
        newPasswordLayout.setBoxStrokeColor(normalColor);
        newPasswordLayout.setError(null);
        confirmPasswordLayout.setBoxStrokeColor(normalColor);
        confirmPasswordLayout.setError(null);
    }

    private void ResetWhenTypingAgain(TextInputLayout layout, TextInputEditText input) {
        input.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                layout.setBoxStrokeColor(normalColor);
                layout.setError(null);
            }
        });
    }
}
