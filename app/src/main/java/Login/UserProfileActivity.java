package Login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.recipeapp.R;

public class UserProfileActivity extends AppCompatActivity {
    private TextView textUsername;
    private EditText editFoodPreference;
    private ImageView imageAvatar;
    private Button buttonSaveProfile, buttonLogout;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        textUsername = findViewById(R.id.textUsername);
        editFoodPreference = findViewById(R.id.editFoodPreference);
        imageAvatar = findViewById(R.id.imageAvatar);
        buttonSaveProfile = findViewById(R.id.buttonSaveProfile);
        buttonLogout = findViewById(R.id.buttonLogout);

        Intent intent = getIntent();
        currentUsername = intent.getStringExtra("username");
        textUsername.setText(currentUsername);

        String foodPref = UserDataManager.getFoodPreference(this, currentUsername);
        editFoodPreference.setText(foodPref);

        buttonSaveProfile.setOnClickListener(v -> {
            String newPref = editFoodPreference.getText().toString().trim();
            UserDataManager.updateFoodPreference(this, currentUsername, newPref);
            Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
        });

        buttonLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            Intent logoutIntent = new Intent(this, LoginActivity.class);
            logoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(logoutIntent);
            finish();
        });

        imageAvatar.setImageResource(R.drawable.default_avatar);
    }
}
