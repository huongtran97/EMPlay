package emplay.entertainment.emplay.activity;

import static emplay.entertainment.emplay.R.*;
import static emplay.entertainment.emplay.R.id.*;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import emplay.entertainment.emplay.MovieOption;
import emplay.entertainment.emplay.MusicOption;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.R.id;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("NonConstantResourceId")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == login) {
            Intent loginPage = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginPage);
        } else if (itemId == about) {
            Intent aboutPage = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(aboutPage);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.popupmenu, menu);
        return false;
    }

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);

        TextView movieBtn = findViewById(id.movieBtn);
        TextView musicBtn = findViewById(id.musicBtn);
        Log.w("MainActivity", "In onCreate() - Load Widgets");

        movieBtn.setOnClickListener(click -> {
            Intent moviePage = new Intent(MainActivity.this, MovieOption.class);
            startActivity(moviePage);
        });

        musicBtn.setOnClickListener(click -> {
            Intent musicPage = new Intent(MainActivity.this, MusicOption.class);
            startActivity(musicPage);
        });

    }


}


