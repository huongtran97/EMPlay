package emplay.entertainment.emplay;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;

/**
 * @author Tran Ngoc Que Huong
 * @version 1.0
 *
 * This class represents the main activity for the movie application.
 * It handles navigation between different fragments using a BottomNavigationView.
 */
public class MovieOption extends AppCompatActivity {

    /**
     * Called when the activity is first created.
     * Sets up the initial fragment and the BottomNavigationView for navigation.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                           Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_activity);

        BottomNavigationView bottomNavigationView = findViewById(R.id.movie_bottom_navigation_view);

        // Set initial fragment if there is no saved instance state
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            /**
             * Called when an item in the BottomNavigationView is selected.
             * Replaces the current fragment with the selected fragment.
             *
             * @param item The selected item.
             * @return true to display the selected item as the selected item,
             *         false to do nothing.
             */
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                int itemId = item.getItemId();

                // Determine which fragment to display based on the selected menu item
                if (itemId == R.id.menu_movie_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.menu_movie_favorite) {
                    selectedFragment = new FavoriteFragment();
                } else if (itemId == R.id.menu_movie_search) {
                    selectedFragment = new SearchFragment();
                } else if (itemId == R.id.menu_movie_profile) {
                    selectedFragment = new ProfileFragment();
                }

                // Replace the current fragment with the selected fragment
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                return true;
            }
        });

    }
}



