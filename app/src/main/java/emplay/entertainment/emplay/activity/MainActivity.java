package emplay.entertainment.emplay.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.models.SharedViewModel;
import emplay.entertainment.emplay.fragment.HomeFragment;
import emplay.entertainment.emplay.fragment.ProfileFragment;
import emplay.entertainment.emplay.fragment.SearchMoviesFragment;
import emplay.entertainment.emplay.fragment.SearchTVShowsFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_fragment);

        SharedViewModel viewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        BottomNavigationView bottomNavigationView = findViewById(R.id.movie_bottom_navigation_view);
        if (bottomNavigationView == null) {
            Log.e("MovieOption", "BottomNavigationView is null");
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.menu_movie_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.menu_movie_search) {
                    Boolean wasTVShowSearch = viewModel.getLastSearchWasTVShow().getValue();
                    if (wasTVShowSearch != null && wasTVShowSearch) {
                        selectedFragment = new SearchTVShowsFragment();
                    } else {
                        selectedFragment = new SearchMoviesFragment();
                    }
                } else if (itemId == R.id.menu_movie_profile) {
                    selectedFragment = new ProfileFragment();
                }

                if (selectedFragment != null) {getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment)
                            .addToBackStack(null)
                            .commit();
                }
                return true;
            }

        });

        com.google.firebase.auth.FirebaseUser currentUser =
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_welcome, null);

            android.app.Dialog dialog = new android.app.Dialog(this);
            dialog.setContentView(dialogView);
            dialog.setCancelable(false);

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.getWindow().setDimAmount(0.7f);
            }

            dialogView.findViewById(R.id.btn_login).setOnClickListener(v -> {
                dialog.dismiss();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            });

            dialogView.findViewById(R.id.btn_guest).setOnClickListener(v -> {
                dialog.dismiss();
            });

            dialog.show();
        }

    }

}
