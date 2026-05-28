package emplay.entertainment.emplay.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TrailerActivity extends AppCompatActivity {

    private static final String ARG_MOVIE_ID = "MOVIE_ID";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String videoId = getIntent().getStringExtra(ARG_MOVIE_ID);

        if (videoId == null || videoId.isEmpty()) {
            Toast.makeText(this, "No trailer available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Try the native YouTube app first, fall back to the browser if it's not installed.
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com/watch?v=" + videoId));

        try {
            startActivity(appIntent);
        } catch (Exception e) {
            startActivity(webIntent);
        }
        finish();
    }
}