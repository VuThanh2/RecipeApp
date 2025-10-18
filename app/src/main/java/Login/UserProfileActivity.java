package Login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.recipeapp.R;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;

public class UserProfileActivity extends AppCompatActivity {
    private TextView textUsername;
    private AutoCompleteTextView dietInput;
    private final String[] dietLabels = new String[] { "Normal", "Vegan", "Keto", "Gluten-free" };
    private final String[] dietValues = new String[] { "normal", "vegan", "keto", "gluten_free" };
    private ImageView imageAvatar;
    private Button buttonSaveProfile, buttonLogout;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        textUsername = findViewById(R.id.textUsername);
        imageAvatar = findViewById(R.id.imageAvatar);
        buttonSaveProfile = findViewById(R.id.buttonSaveProfile);
        buttonLogout = findViewById(R.id.buttonLogout);
        dietInput = findViewById(R.id.etDietMode);

        if (dietInput != null) {
            ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dietLabels);
            dietInput.setAdapter(dietAdapter);
            dietInput.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) dietInput.showDropDown();
            });
            // Default selection
            dietInput.setText(dietLabels[0], false);
        }

        currentUsername = SessionManager.getCurrentUsername(this);
        if (currentUsername == null || currentUsername.isEmpty()) {
            Intent back = new Intent(this, LoginActivity.class);
            back.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(back);
            finish();
            return;
        }
        textUsername.setText(currentUsername);

        String currentDiet = UserDataManager.getDietMode(this, currentUsername);
        String display = mapDietValueToLabel(currentDiet);
        dietInput.setText(display, false);

        buttonSaveProfile.setOnClickListener(v -> {
            String selectedLabel = dietInput.getText().toString().trim();
            String diet = mapLabelToDietValue(selectedLabel);
            UserDataManager.updateDietMode(this, currentUsername, diet);
            Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
        });

        buttonLogout.setOnClickListener(v -> {
            SessionManager.clear(this);
            SessionManager.setLoggedIn(this, false);
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            Intent logoutIntent = new Intent(this, LoginActivity.class);
            logoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(logoutIntent);
            finish();
        });

        imageAvatar.setImageResource(R.drawable.image_default_avatar);
    }

    private String mapDietValueToLabel(String value) {
        if (value == null) return "Normal";
        String v = value.trim().toLowerCase();
        for (int i = 0; i < dietValues.length; i++) {
            if (dietValues[i].equals(v)) return dietLabels[i];
        }
        return "Normal";
    }

    private String mapLabelToDietValue(String label) {
        if (label == null) return "normal";
        String s = label.trim();
        for (int i = 0; i < dietLabels.length; i++) {
            if (dietLabels[i].equalsIgnoreCase(s)) return dietValues[i];
        }
        String l = s.toLowerCase();
        if (l.contains("vegan") || l.contains("thuần chay") || l.contains("ăn chay")) return "vegan";
        if (l.contains("keto")) return "keto";
        if (l.contains("gluten")) return "gluten_free";
        return "normal";
    }
}
