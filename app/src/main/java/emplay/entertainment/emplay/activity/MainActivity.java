package emplay.entertainment.emplay.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import emplay.entertainment.emplay.auth.AuthManager;

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
    private SharedViewModel viewModel;

    // Store current active navigation ID to handle back-navigation routing
    private int currentNavId = R.id.nav_home;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }

        setContentView(R.layout.bottom_nav_view);

        // Target the root layout of your included component bar
        LinearLayout customNavBar = findViewById(R.id.movie_bottom_navigation_container);

        ViewCompat.setOnApplyWindowInsetsListener(customNavBar, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.LayoutParams rawParams = view.getLayoutParams();

            if (rawParams instanceof ViewGroup.MarginLayoutParams params) {

                // 1. Grab your base design offset (the 24dp from your layout XML layout_marginBottom)
                int baseMargin = (int) (20 * view.getResources().getDisplayMetrics().density);

                // 2. Add the dynamic system nav bar thickness on top of the base margin
                params.bottomMargin = systemBars.bottom + baseMargin;

                view.setLayoutParams(params);
            }
            return insets;
        });


        // Window background glass blur tracking (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getWindow().setBackgroundBlurRadius((int) (24 * getResources().getDisplayMetrics().density));
        }

        viewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        // Safe fragment stack check
        if (savedInstanceState == null) {
            navigateToFragment(new HomeFragment(), R.id.nav_home);
        }

        // 2. Bind Direct Click Handlers to your custom XML Layout Views
        findViewById(R.id.nav_home).setOnClickListener(v -> navigateToFragment(new HomeFragment(), R.id.nav_home));
        findViewById(R.id.nav_search).setOnClickListener(v -> {
            Boolean wasTVShowSearch = viewModel.getLastSearchWasTVShow().getValue();
            Fragment searchFragment = (wasTVShowSearch != null && wasTVShowSearch)
                    ? new SearchTVShowsFragment()
                    : new SearchMoviesFragment();
            navigateToFragment(searchFragment, R.id.nav_search);
        });
        findViewById(R.id.nav_mylist).setOnClickListener(v -> navigateToFragment(new WatchlistFragment(), R.id.nav_mylist));
        findViewById(R.id.nav_profile).setOnClickListener(v -> navigateToFragment(new ProfileFragment(), R.id.nav_profile));

        // 3. Re-mapped Back Press Controller for custom layout tracking
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else if (currentNavId != R.id.nav_home) {
                    // If not on Home, force back to Home smoothly
                    navigateToFragment(new HomeFragment(), R.id.nav_home);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // 4. Welcome Dialog validation flow
        AuthManager authManager = AuthManager.getInstance(this);
        if (authManager.getAuthType() == AuthManager.AuthType.NONE) {
            @SuppressLint("InflateParams") View dialogView = getLayoutInflater().inflate(R.layout.dialog_welcome, null);

            welcomeDialog = new android.app.Dialog(this);
            welcomeDialog.setContentView(dialogView);
            welcomeDialog.setCancelable(false);

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

            dialogView.findViewById(R.id.btn_guest).setOnClickListener(v -> {
                AuthManager.getInstance(this).setGuest();
                welcomeDialog.dismiss();
            });

            welcomeDialog.show();
        }
    }

    /**
     * Replaces the fragment frame layout container and tracking state references.
     */
    private void navigateToFragment(Fragment fragment, int viewId) {
        currentNavId = viewId;
        getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();

        updateTabVisuals(viewId);
    }

    /**
     * Highlights the active icon by swapping color tints programmatically.
     */
    private void updateTabVisuals(int activeId) {
        // Direct conversion of classic Amber tint (#FFBF00)
        int colorActive = android.graphics.Color.parseColor("#8590AB");
        int colorInactive = getColor(R.color.text_dim); // Dark background contrast color

        // Target active highlights safely using your helper method
        setImageViewTint(R.id.nav_home, activeId == R.id.nav_home ? colorActive : colorInactive);
        setImageViewTint(R.id.nav_search, activeId == R.id.nav_search ? colorActive : colorInactive);
        setImageViewTint(R.id.nav_mylist, activeId == R.id.nav_mylist ? colorActive : colorInactive);
        setImageViewTint(R.id.nav_profile, activeId == R.id.nav_profile ? colorActive : colorInactive);
    }


    private void setImageViewTint(int layoutId, int color) {
        ViewGroup layout = findViewById(layoutId);
        if (layout != null && layout.getChildAt(0) instanceof ImageView) {
            ((ImageView) layout.getChildAt(0)).setColorFilter(color);
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
