package Login;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.recipeapp.R;

public class RegisterActivity extends AppCompatActivity {
    EditText usernameInput, passwordInput;
    Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameInput = findViewById(R.id.editUsername);
        passwordInput = findViewById(R.id.editPassword);
        registerButton = findViewById(R.id.btnRegister);

        registerButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString();
            String password = passwordInput.getText().toString();

            boolean success = UserDataManager.registerUser(this, username, password);
            if (success) {
                Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
                finish(); // return to login
            } else {
                Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
