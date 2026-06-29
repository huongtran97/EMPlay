package emplay.entertainment.emplay.fragment.layout;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import de.hdodenhof.circleimageview.CircleImageView;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.activity.AboutActivity;
import emplay.entertainment.emplay.activity.LoginActivity;
import emplay.entertainment.emplay.activity.MainActivity;
import emplay.entertainment.emplay.adapter.common.RecentlySavedAdapter;
import emplay.entertainment.emplay.api.auth.TMDBWatchlistApiService;
import emplay.entertainment.emplay.api.auth.model.TMDBMovieWatchlistResponse;
import emplay.entertainment.emplay.api.auth.model.TMDBTVWatchlistResponse;
import emplay.entertainment.emplay.api.common.ApiClient;
import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.auth.AuthManager;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.fragment.common.BaseFragment;
import emplay.entertainment.emplay.fragment.common.RecentlyAddedFragment;
import emplay.entertainment.emplay.fragment.details.MovieResultDetailsFragment;
import emplay.entertainment.emplay.fragment.details.TVShowResultDetailsFragment;
import emplay.entertainment.emplay.models.common.MediaItem;
import emplay.entertainment.emplay.models.movie.MovieModel;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;
import emplay.entertainment.emplay.tool.BadgeHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends BaseFragment {
    private static final String ARG_ID = "arg_id";
    private FirebaseAuth mAuth;
    private DatabaseHelper databaseHelper;
    private TMDBWatchlistApiService watchlistApiService;
    private CircleImageView civAvatar;
    private TextView tvProfileName, tvProfileEmail, tvMemberSince;
    private TextView tvMoviesCount, tvTvShowsCount, tvTopGenre;
    private LinearLayout llTasteProfile, guestBanner;
    private View llTasteProfileHeader;
    private final List<MediaItem> recentlySavedList = new ArrayList<>();
    private RecentlySavedAdapter recentlySavedAdapter;
    private AlertDialog deleteAccountDialog;

    public static ProfileFragment newInstance(int id) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = DatabaseHelper.getInstance(requireContext());
        mAuth = FirebaseAuth.getInstance();
        watchlistApiService = ApiClient.getClient().create(TMDBWatchlistApiService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.customer_profile_view, container, false);

        civAvatar = view.findViewById(R.id.civAvatar);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvMemberSince = view.findViewById(R.id.tvMemberSince);
        tvMoviesCount = view.findViewById(R.id.tvMoviesCount);
        tvTvShowsCount = view.findViewById(R.id.tvTvShowsCount);
        tvTopGenre = view.findViewById(R.id.tvTopGenre);
        llTasteProfile = view.findViewById(R.id.llTasteProfile);
        llTasteProfileHeader = view.findViewById(R.id.llTasteProfileHeader);
        guestBanner = view.findViewById(R.id.guestBanner);

        RecyclerView rvRecentlySaved = view.findViewById(R.id.rvRecentlySaved);
        recentlySavedAdapter = new RecentlySavedAdapter(requireContext(), recentlySavedList, this::onItemClicked);
        rvRecentlySaved.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvRecentlySaved.setAdapter(recentlySavedAdapter);

        view.findViewById(R.id.llAbout).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AboutActivity.class)));

        return view;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AuthManager auth = AuthManager.getInstance(requireContext());
        TextView tvLogoutLabel = view.findViewById(R.id.tvLogoutLabel);
        TextView tvRecentlySavedSeeAll = view.findViewById(R.id.tvRecentlySavedSeeAll);

        switch (auth.getAuthType()) {
            case GOOGLE:
                FirebaseUser user = mAuth.getCurrentUser();
                if (user == null) break;
                tvLogoutLabel.setText(R.string.log_out);
                bindGoogleProfile(user);
                setupGoogleLogout();
                setupDeleteAccount(view, user);
                if (tvRecentlySavedSeeAll != null) {
                    tvRecentlySavedSeeAll.setPaintFlags(
                            tvRecentlySavedSeeAll.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                    tvRecentlySavedSeeAll.setOnClickListener(v -> navigateTo(new RecentlyAddedFragment()));
                }
                loadGoogleUserData(user.getUid());
                break;

            case TMDB:
                tvLogoutLabel.setText(R.string.disconnect_tmdb);
                bindTmdbProfile(auth);
                setupTmdbLogout(auth);
                view.findViewById(R.id.dividerDeleteAccount).setVisibility(View.GONE);
                view.findViewById(R.id.llDeleteAccount).setVisibility(View.GONE);
                // Taste profile needs genre names not available in watchlist responses
                llTasteProfileHeader.setVisibility(View.GONE);
                llTasteProfile.setVisibility(View.GONE);
                if (tvRecentlySavedSeeAll != null) tvRecentlySavedSeeAll.setVisibility(View.GONE);
                loadTmdbUserData(auth);
                break;

            default: // NONE / GUEST
                tvLogoutLabel.setText(R.string.log_in);
                bindGuestProfile();
                view.findViewById(R.id.llLogout).setOnClickListener(v ->
                        startActivity(new Intent(requireContext(), LoginActivity.class)));
                view.findViewById(R.id.dividerDeleteAccount).setVisibility(View.GONE);
                view.findViewById(R.id.llDeleteAccount).setVisibility(View.GONE);
                if (tvRecentlySavedSeeAll != null) tvRecentlySavedSeeAll.setVisibility(View.GONE);
                break;
        }

        setupThemePicker(view);
    }

    private void setupThemePicker(View view) {
        TextView tvThemeValue = view.findViewById(R.id.tv_theme_value);
        int currentMode = PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getInt(emplay.entertainment.emplay.EMPlayApplication.PREF_NIGHT_MODE,
                        AppCompatDelegate.MODE_NIGHT_YES);
        tvThemeValue.setText(currentMode == AppCompatDelegate.MODE_NIGHT_NO
                ? R.string.theme_parchment : R.string.theme_deep_blue);

        view.findViewById(R.id.llTheme).setOnClickListener(v -> {
            String[] options = {
                    getString(R.string.theme_deep_blue),
                    getString(R.string.theme_parchment)
            };
            int checkedItem = currentMode == AppCompatDelegate.MODE_NIGHT_NO ? 1 : 0;
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.settings_theme)
                    .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                        int mode = (which == 1)
                                ? AppCompatDelegate.MODE_NIGHT_NO
                                : AppCompatDelegate.MODE_NIGHT_YES;
                        PreferenceManager.getDefaultSharedPreferences(requireContext())
                                .edit()
                                .putInt(emplay.entertainment.emplay.EMPlayApplication.PREF_NIGHT_MODE, mode)
                                .apply();
                        AppCompatDelegate.setDefaultNightMode(mode);
                        dialog.dismiss();
                        requireActivity().recreate();
                    })
                    .show();
        });
    }

    // Google user

    private void bindGoogleProfile(FirebaseUser user) {
        tvProfileName.setText(user.getDisplayName() != null ? user.getDisplayName() : "User");
        tvProfileEmail.setText(user.getEmail() != null ? user.getEmail() : "");

        if (user.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .circleCrop()
                    .placeholder(R.drawable.avatar)
                    .into(civAvatar);
        } else {
            civAvatar.setImageResource(R.drawable.avatar);
        }
        displayGoogleJoinDate();
    }

    @SuppressLint("SetTextI18n")
    private void displayGoogleJoinDate() {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences("app_prefs", MODE_PRIVATE);
        long joinDate = prefs.getLong("join_date", -1);
        if (joinDate != -1) {
            String formatted = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    .format(new Date(joinDate));
            tvMemberSince.setText("Member since " + formatted);
        } else {
            tvMemberSince.setText("");
        }
    }

    private void loadGoogleUserData(String userId) {
        new Thread(() -> {
            List<MovieModel> movies = databaseHelper.getAllMoviesFromDatabase(userId);
            List<TVShowModel> tvShows = databaseHelper.getSavedTVShows(userId);

            List<MediaItem> combined = new ArrayList<>();
            combined.addAll(movies);
            combined.addAll(tvShows);
            Collections.reverse(combined);

            safeRunOnUiThread(() -> {
                tvMoviesCount.setText(String.valueOf(movies.size()));
                tvTvShowsCount.setText(String.valueOf(tvShows.size()));
                recentlySavedAdapter.updateData(combined);
                buildTasteProfile(movies, tvShows);
                displayGoogleJoinDate();
            });
        }).start();
    }

    private void setupGoogleLogout() {
        requireView().findViewById(R.id.llLogout).setOnClickListener(v -> {
            AuthManager.getInstance(requireContext()).signOut();
            Intent intent = new Intent(requireContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void setupDeleteAccount(View view, FirebaseUser user) {
        view.findViewById(R.id.llDeleteAccount).setOnClickListener(v -> {
            deleteAccountDialog = new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        String email = user.getEmail();
                        user.delete().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                AuthManager.getInstance(requireContext()).signOut();
                                if (email != null) databaseHelper.deleteUserProfile(email);
                                if (isAdded()) {
                                    Toast.makeText(requireContext(),
                                            "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(requireContext(), LoginActivity.class));
                                    requireActivity().finish();
                                }
                            } else if (isAdded()) {
                                Toast.makeText(requireContext(),
                                        "Failed to delete account", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    //  TMDB user

    private void bindTmdbProfile(AuthManager auth) {
        String username = auth.getTMDBUsername();
        tvProfileName.setText(!username.isEmpty() ? username : "TMDB User");
        tvProfileEmail.setText( username);
        tvProfileEmail.setTextColor(0xFF01B4E4);
        tvProfileEmail.setVisibility(View.VISIBLE);
        tvMemberSince.setText(R.string.tmdb_account_badge);
        civAvatar.setImageResource(R.drawable.avatar);
    }

    private void loadTmdbUserData(AuthManager auth) {
        String accountId = auth.getTMDBAccountId();
        String sessionId = auth.getTMDBSessionId();
        if (accountId == null || sessionId == null) return;

        AtomicInteger pending = new AtomicInteger(2);
        List<MediaItem> recentMovies = new ArrayList<>();
        List<MediaItem> recentTV = new ArrayList<>();
        int[] counts = {0, 0}; // [movies, tv]

        safeEnqueue(watchlistApiService.getWatchlistMovies(
                        TMDBpath.accountWatchlistMovies(accountId), sessionId, 1),
                new Callback<TMDBMovieWatchlistResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TMDBMovieWatchlistResponse> call,
                                           @NonNull Response<TMDBMovieWatchlistResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            counts[0] = response.body().totalResults;
                            if (response.body().results != null) recentMovies.addAll(response.body().results);
                        }
                        if (pending.decrementAndGet() == 0) onTmdbDataReady(counts, recentMovies, recentTV);
                    }

                    @Override
                    public void onFailure(@NonNull Call<TMDBMovieWatchlistResponse> call, @NonNull Throwable t) {
                        if (pending.decrementAndGet() == 0) onTmdbDataReady(counts, recentMovies, recentTV);
                    }
                });

        safeEnqueue(watchlistApiService.getWatchlistTV(
                        TMDBpath.accountWatchlistTVShows(accountId), sessionId, 1),
                new Callback<TMDBTVWatchlistResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TMDBTVWatchlistResponse> call,
                                           @NonNull Response<TMDBTVWatchlistResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            counts[1] = response.body().totalResults;
                            if (response.body().results != null) recentTV.addAll(response.body().results);
                        }
                        if (pending.decrementAndGet() == 0) onTmdbDataReady(counts, recentMovies, recentTV);
                    }

                    @Override
                    public void onFailure(@NonNull Call<TMDBTVWatchlistResponse> call, @NonNull Throwable t) {
                        if (pending.decrementAndGet() == 0) onTmdbDataReady(counts, recentMovies, recentTV);
                    }
                });
    }

    private void onTmdbDataReady(int[] counts, List<MediaItem> movies, List<MediaItem> tv) {
        List<MediaItem> combined = new ArrayList<>();
        combined.addAll(movies);
        combined.addAll(tv);
        safeRunOnUiThread(() -> {
            tvMoviesCount.setText(String.valueOf(counts[0]));
            tvTvShowsCount.setText(String.valueOf(counts[1]));
            tvTopGenre.setText("—");
            recentlySavedAdapter.updateData(combined);
        });
    }

    private void setupTmdbLogout(AuthManager auth) {
        requireView().findViewById(R.id.llLogout).setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.disconnect_tmdb)
                        .setMessage("You will be signed out of your TMDB account.")
                        .setPositiveButton(R.string.disconnect_tmdb, (d, w) -> {
                            auth.signOut();
                            Intent intent = new Intent(requireContext(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show());
    }

    // Guest session

    private void bindGuestProfile() {
        tvProfileName.setText("Hi there!");
        tvProfileEmail.setText("Do you want to login?");
        tvMemberSince.setText("");
        guestBanner.setVisibility(View.VISIBLE);
    }

    //  Taste profile (Google only)

    private void buildTasteProfile(List<MovieModel> movies, List<TVShowModel> tvShows) {
        Map<String, Integer> genreCounts = new HashMap<>();
        for (MovieModel m : movies) {
            if (m.getGenres() != null) {
                for (String g : m.getGenres()) {
                    Integer c = genreCounts.get(g);
                    genreCounts.put(g, (c == null ? 0 : c) + 1);
                }
            }
        }
        for (TVShowModel t : tvShows) {
            if (t.getGenres() != null) {
                for (String g : t.getGenres()) {
                    Integer c = genreCounts.get(g);
                    genreCounts.put(g, (c == null ? 0 : c) + 1);
                }
            }
        }

        if (genreCounts.isEmpty()) {
            tvTopGenre.setText("—");
            llTasteProfile.removeAllViews();
            return;
        }

        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(genreCounts.entrySet());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        }
        tvTopGenre.setText(sorted.get(0).getKey());

        llTasteProfile.removeAllViews();
        int max = sorted.get(0).getValue();
        int displayCount = Math.min(sorted.size(), 4);

        for (int i = 0; i < displayCount; i++) {
            Map.Entry<String, Integer> entry = sorted.get(i);
            View row = getLayoutInflater().inflate(R.layout.taste_row, llTasteProfile, false);
            TextView name = row.findViewById(R.id.tvGenreName);
            TextView count = row.findViewById(R.id.tvGenreCount);
            View bar = row.findViewById(R.id.viewTasteBar);

            name.setText(entry.getKey());
            count.setText(String.valueOf(entry.getValue()));

            int color = BadgeHelper.getGenreColorByName(requireContext(), entry.getKey());
            bar.setBackgroundTintList(ColorStateList.valueOf(color));

            float ratio = (float) entry.getValue() / max;
            row.post(() -> {
                int fullWidth = bar.getParent() instanceof View ? ((View) bar.getParent()).getWidth() : 0;
                if (fullWidth > 0) {
                    ViewGroup.LayoutParams lp = bar.getLayoutParams();
                    lp.width = (int) (fullWidth * ratio);
                    bar.setLayoutParams(lp);
                }
            });
            llTasteProfile.addView(row);
        }
    }

    // Shared

    private void onItemClicked(MediaItem item, View view) {
        if (item instanceof MovieModel) {
            navigateTo(MovieResultDetailsFragment.newInstance(((MovieModel) item).getMovieId()));
        } else if (item instanceof TVShowModel) {
            navigateTo(TVShowResultDetailsFragment.newInstance(((TVShowModel) item).getTVShowId()));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (deleteAccountDialog != null && deleteAccountDialog.isShowing()) {
            deleteAccountDialog.dismiss();
        }
    }
}