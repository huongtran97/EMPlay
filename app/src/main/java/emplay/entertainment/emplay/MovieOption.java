package emplay.entertainment.emplay;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import emplay.entertainment.emplay.models.SharedViewModel;
import emplay.entertainment.emplay.moviefragment.FavoriteFragment;
import emplay.entertainment.emplay.moviefragment.HomeFragment;
import emplay.entertainment.emplay.moviefragment.ProfileFragment;
import emplay.entertainment.emplay.moviefragment.SearchMoviesFragment;
import emplay.entertainment.emplay.moviefragment.SearchTVShowsFragment;

public class MovieOption extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_fragment);

        SharedViewModel viewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        BottomNavigationView bottomNavigationView = findViewById(R.id.movie_bottom_navigation_view);
        if (bottomNavigationView == null) {
            Log.e("MovieOption", "BottomNavigationView is null");
        }

        // Set initial fragment if there is no saved instance state
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
                } else if (itemId == R.id.menu_movie_favorite) {
                    selectedFragment = new FavoriteFragment();
                } else if (itemId == R.id.menu_movie_search) {
                    // Retrieve the last search type from ViewModel
                    Boolean wasTVShowSearch = viewModel.getLastSearchWasTVShow().getValue();

                    // Decide which fragment to show based on last search type
                    if (wasTVShowSearch != null && wasTVShowSearch) {
                        selectedFragment = new SearchTVShowsFragment();
                    } else {
                        selectedFragment = new SearchMoviesFragment();
                    }
                } else if (itemId == R.id.menu_movie_profile) {
                    selectedFragment = new ProfileFragment();
                }

                if (selectedFragment != null) {getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment)
                            .addToBackStack(null)  // Add this line if you want to keep the previous fragment in the back stack
                            .commit();
                }
                return true;
            }

        });
    }
}
