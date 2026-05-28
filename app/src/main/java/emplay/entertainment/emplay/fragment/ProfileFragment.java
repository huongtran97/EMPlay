package emplay.entertainment.emplay.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.Uri;
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
import androidx.fragment.app.Fragment;
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

import de.hdodenhof.circleimageview.CircleImageView;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.activity.AboutActivity;
import emplay.entertainment.emplay.tool.BadgeHelper;
import emplay.entertainment.emplay.activity.LoginActivity;
import emplay.entertainment.emplay.activity.MainActivity;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.models.MediaItem;
import emplay.entertainment.emplay.models.MovieModel;
import emplay.entertainment.emplay.models.TVShowModel;
import emplay.entertainment.emplay.adapter.RecentlySavedAdapter;

/**
 *  Profile screen — shows the current user's name, avatar, and their liked movies/TV shows.
 *  Liked items are read from SQLite on a background thread so the UI doesn't freeze.
 *  Guest users see a "Login / Register" prompt instead of real data.
 */
public class ProfileFragment extends BaseFragment {
    private static final String ARG_ID = "arg_id";
    private FirebaseAuth mAuth;
    private DatabaseHelper databaseHelper;
    private CircleImageView civAvatar;
    private TextView tvProfileName, tvProfileEmail, tvMemberSince;
    private TextView tvMoviesCount, tvTvShowsCount, tvTopGenre;
    private LinearLayout llTasteProfile;
    private RecyclerView rvRecentlySaved;
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
        databaseHelper = DatabaseHelper.getInstance(getContext());
        mAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.customer_profile_view, container, false);

        civAvatar = view.findViewById(R.id.civAvatar);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvMemberSince = view.findViewById(R.id.tvMemberSince);
        tvMoviesCount = view.findViewById(R.id.tvMoviesCount);
        tvTvShowsCount = view.findViewById(R.id.tvTvShowsCount);
        tvTopGenre = view.findViewById(R.id.tvTopGenre);
        
        llTasteProfile = view.findViewById(R.id.llTasteProfile);
        rvRecentlySaved = view.findViewById(R.id.rvRecentlySaved);

        TextView tvRecentlySavedSeeAll = view.findViewById(R.id.tvRecentlySavedSeeAll);
        if (tvRecentlySavedSeeAll != null) {
            tvRecentlySavedSeeAll.setPaintFlags(tvRecentlySavedSeeAll.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            tvRecentlySavedSeeAll.setOnClickListener(v -> {
                Fragment fragment = new RecentlyAddedFragment();
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            });
        }

        recentlySavedAdapter = new RecentlySavedAdapter(getContext(), recentlySavedList, this::onItemClicked);
        rvRecentlySaved.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvRecentlySaved.setAdapter(recentlySavedAdapter);

        view.findViewById(R.id.llAbout).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AboutActivity.class)));

        view.findViewById(R.id.llLogout).setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                mAuth.signOut();
                Intent intent = new Intent(requireContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                startActivity(new Intent(requireContext(), LoginActivity.class));
            }
        });

        view.findViewById(R.id.llDeleteAccount).setOnClickListener(v -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) return;
            
            deleteAccountDialog = new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        String email = currentUser.getEmail();
                        currentUser.delete().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (email != null) {
                                    databaseHelper.deleteUserProfile(email);
                                }
                                if (isAdded()) {
                                    Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(requireContext(), LoginActivity.class));
                                    requireActivity().finish();
                                }
                            } else {
                                if (isAdded()) {
                                    Toast.makeText(requireContext(), "Failed to delete account", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (deleteAccountDialog != null && deleteAccountDialog.isShowing()) {
            deleteAccountDialog.dismiss();
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser user = mAuth.getCurrentUser();

        TextView tvLogoutLabel = view.findViewById(R.id.tvLogoutLabel);
        tvLogoutLabel.setText(user != null ? R.string.log_out : R.string.log_in);

        if (user != null) {
            String username = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            tvProfileName.setText(username != null ? username : "No username set");
            tvProfileEmail.setText(email != null ? email : "No email set");

            if (photoUrl != null) {
                Glide.with(this)
                        .load(photoUrl)
                        .circleCrop()
                        .placeholder(R.drawable.avatar)
                        .into(civAvatar);
            } else {
                civAvatar.setImageResource(R.drawable.avatar);
            }

            // Show join date immediately if available
            displayJoinDate();
        }
        else {
            tvProfileName.setText("Hi there!");
            tvProfileEmail.setText("Do you want to login?");
            tvMemberSince.setText("");
            view.findViewById(R.id.llDeleteAccount).setVisibility(View.GONE);
        }

        loadUserData();
    }

    @SuppressLint("SetTextI18n")
    private void displayJoinDate() {
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

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        String userId = user.getUid();

        new Thread(() -> {
            List<MovieModel> likedMovies = databaseHelper.getAllMoviesFromDatabase(userId);
            List<TVShowModel> likedTVShows = databaseHelper.getSavedTVShows(userId);
            
            List<MediaItem> combined = new ArrayList<>();
            combined.addAll(likedMovies);
            combined.addAll(likedTVShows);
            
            // Sort by recent
            Collections.reverse(combined);

            safeRunOnUiThread(() -> {
                updateStats(likedMovies, likedTVShows);
                recentlySavedAdapter.updateData(combined);
                buildTasteProfile(likedMovies, likedTVShows);
            });
        }).start();
    }

    private void updateStats(List<MovieModel> movies, List<TVShowModel> tvShows) {
        tvMoviesCount.setText(String.valueOf(movies.size()));
        tvTvShowsCount.setText(String.valueOf(tvShows.size()));
        displayJoinDate();
    }

    private void buildTasteProfile(List<MovieModel> movies, List<TVShowModel> tvShows) {
        Map<String, Integer> genreCounts = getStringIntegerMap(movies, tvShows);

        if (genreCounts.isEmpty()) {
            tvTopGenre.setText("—");
            llTasteProfile.removeAllViews();
            return;
        }

        List<Map.Entry<String, Integer>> sortedGenres = new ArrayList<>(genreCounts.entrySet());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sortedGenres.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        }

        tvTopGenre.setText(sortedGenres.get(0).getKey());

        llTasteProfile.removeAllViews();
        int max = sortedGenres.get(0).getValue();
        int displayCount = Math.min(sortedGenres.size(), 4);

        for (int i = 0; i < displayCount; i++) {
            Map.Entry<String, Integer> entry = sortedGenres.get(i);
            View row = getLayoutInflater().inflate(R.layout.taste_row, llTasteProfile, false);
            TextView name = row.findViewById(R.id.tvGenreName);
            TextView count = row.findViewById(R.id.tvGenreCount);
            View bar = row.findViewById(R.id.viewTasteBar);

            name.setText(entry.getKey());
            count.setText(String.valueOf(entry.getValue()));

            int genreColor = BadgeHelper.getGenreColorByName(requireContext(), entry.getKey());
            bar.setBackgroundTintList(ColorStateList.valueOf(genreColor));

            // Set bar width based on ratio
            float ratio = (float) entry.getValue() / max;
            row.post(() -> {
                int fullWidth = bar.getParent() instanceof View ? ((View)bar.getParent()).getWidth() : 0;
                if (fullWidth > 0) {
                    ViewGroup.LayoutParams lp = bar.getLayoutParams();
                    lp.width = (int) (fullWidth * ratio);
                    bar.setLayoutParams(lp);
                }
            });

            llTasteProfile.addView(row);
        }
    }

    @NonNull
    private static Map<String, Integer> getStringIntegerMap(List<MovieModel> movies, List<TVShowModel> tvShows) {
        Map<String, Integer> genreCounts = new HashMap<>();
        for (MovieModel m : movies) {
            if (m.getGenres() != null) {
                for (String g : m.getGenres()) {
                    Integer current = genreCounts.get(g);
                    genreCounts.put(g, (current == null ? 0 : current) + 1);
                }
            }
        }
        for (TVShowModel t : tvShows) {
            if (t.getGenres() != null) {
                for (String g : t.getGenres()) {
                    Integer current = genreCounts.get(g);
                    genreCounts.put(g, (current == null ? 0 : current) + 1);
                }
            }
        }
        return genreCounts;
    }

    private void onItemClicked(Object item) {
        if (item instanceof MovieModel) {
            MovieModel movie = (MovieModel) item;
            ShowResultDetailsFragment fragment = ShowResultDetailsFragment.newInstance(movie.getId());
            replaceFragment(fragment);
        } else if (item instanceof TVShowModel) {
            TVShowModel tvShow = (TVShowModel) item;
            ShowResultTVShowDetailsFragment fragment = ShowResultTVShowDetailsFragment.newInstance(tvShow.getTVShowId());
            replaceFragment(fragment);
        }
    }

    private void replaceFragment(Fragment fragment) {
        navigateTo(fragment);
    }
}
