package Login;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.recipeapp.R;

public class ForgotPasswordActivity extends AppCompatActivity {
    EditText input;
    Button recoverButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        input = findViewById(R.id.editUsernameOrEmail);
        recoverButton = findViewById(R.id.btnRecover);

        recoverButton.setOnClickListener(v -> {
            String query = input.getText().toString();
            String password = UserDataManager.getPasswordByUsername(this, query);

            if (password != null) {
                Toast.makeText(this, "Your password is: " + password, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
