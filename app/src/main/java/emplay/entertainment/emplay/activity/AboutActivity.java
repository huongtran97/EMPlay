package emplay.entertainment.emplay.activity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import emplay.entertainment.emplay.BuildConfig;
import emplay.entertainment.emplay.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView versionText = findViewById(R.id.app_version);
        versionText.setText("Version " + BuildConfig.VERSION_NAME);

        ImageButton backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(v -> finish());

        findViewById(R.id.privacy_policy_row).setOnClickListener(v -> showPrivacyPolicy());
    }

    private void showPrivacyPolicy() {
        String policy =
                "Last updated: May 2025\n\n" +
                "EMPlay does not collect or share personal data with third parties.\n\n" +
                "Account & Authentication\n" +
                "If you create an account, your email and display name are stored via Firebase Authentication. This data is used solely to identify your session and is not shared.\n\n" +
                "Local Storage\n" +
                "Saved movies and TV shows are stored locally on your device using SQLite. This data never leaves your device.\n\n" +
                "Third-Party Services\n" +
                "Movie and TV show data is fetched from The Movie Database (TMDB) API. Please refer to TMDB's privacy policy for details on their data practices.\n\n" +
                "Contact\n" +
                "For any privacy concerns, contact us through the app's GitHub repository.";

        new AlertDialog.Builder(this)
                .setTitle("Privacy Policy")
                .setMessage(policy)
                .setPositiveButton("OK", null)
                .show();
    }
}