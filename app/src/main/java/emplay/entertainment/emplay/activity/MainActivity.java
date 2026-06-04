package emplay.entertainment.emplay.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.fragment.layout.WatchlistFragment;
import emplay.entertainment.emplay.models.common.SharedViewModel;
import emplay.entertainment.emplay.fragment.layout.HomeFragment;
import emplay.entertainment.emplay.fragment.layout.ProfileFragment;
import emplay.entertainment.emplay.fragment.layout.SearchMoviesFragment;
import emplay.entertainment.emplay.fragment.layout.SearchTVShowsFragment;

public class MainActivity extends AppCompatActivity {

    private android.app.Dialog welcomeDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_fragment);

        SharedViewModel viewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        BottomNavigationView bottomNavigationView = findViewById(R.id.movie_bottom_navigation_view);
        if (bottomNavigationView == null) {
            Log.e("MovieOption", "BottomNavigationView is null");
        }

        // Only load the home fragment on first launch — skip on config changes
        // to avoid rebuilding the fragment stack unnecessarily.
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }

        assert bottomNavigationView != null;
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.menu_movie_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.menu_movie_search) {
                Boolean wasTVShowSearch = viewModel.getLastSearchWasTVShow().getValue();
                selectedFragment = (wasTVShowSearch != null && wasTVShowSearch)
                        ? new SearchTVShowsFragment()
                        : new SearchMoviesFragment();
            } else if (itemId == R.id.menu_movie_watchlist) {
                selectedFragment = new WatchlistFragment();
            } else if (itemId == R.id.menu_movie_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Show the welcome dialog if user isn't log in yet
        FirebaseUser currentUser =
                FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            @SuppressLint("InflateParams") View dialogView = getLayoutInflater().inflate(R.layout.dialog_welcome, null);

            welcomeDialog = new android.app.Dialog(this);
            welcomeDialog.setContentView(dialogView);
            welcomeDialog.setCancelable(false); // user must make a choice, can't dismiss by tapping outside

            if (welcomeDialog.getWindow() != null) {
                welcomeDialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                welcomeDialog.getWindow().setDimAmount(0.7f);
            }

            dialogView.findViewById(R.id.btn_login).setOnClickListener(v -> {
                if (welcomeDialog != null && welcomeDialog.isShowing()) {
                    welcomeDialog.dismiss();
                }
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finishAfterTransition();
            });

            dialogView.findViewById(R.id.btn_guest).setOnClickListener(v -> welcomeDialog.dismiss());

            welcomeDialog.show();
        }
    }

    @Override
    protected void onDestroy() {
        if (welcomeDialog != null && welcomeDialog.isShowing()) {
            welcomeDialog.dismiss();
        }
        super.onDestroy();
    }
}
