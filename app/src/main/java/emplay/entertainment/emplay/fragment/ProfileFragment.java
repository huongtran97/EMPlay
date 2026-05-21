package emplay.entertainment.emplay.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.activity.AboutActivity;
import emplay.entertainment.emplay.activity.MainActivity;
import emplay.entertainment.emplay.tool.SwipeToDeleteCallback;
import emplay.entertainment.emplay.activity.LoginActivity;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.models.MovieModel;
import emplay.entertainment.emplay.models.TVShowModel;
import emplay.entertainment.emplay.adapter.MovieLikedAdapter;
import emplay.entertainment.emplay.adapter.TVLikedAdapter;

public class ProfileFragment extends Fragment {
    private static final String ARG_ID = "arg_id";
    private FirebaseAuth mAuth;
    private Button deleteAccountBtn;
    private Button logOutBtn;
    private RecyclerView moviesRecyclerView;
    private RecyclerView tvShowsRecyclerView;
    private List<MovieModel> likedMoviesList;
    private List<TVShowModel> likedTVShowsList;
    private MovieLikedAdapter movieLikedAdapter;
    private TVLikedAdapter tvShowLikedAdapter;
    private DatabaseHelper databaseHelper;
    private TextView usernameTextView;
    private TextView emailTextView;

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
        databaseHelper = new DatabaseHelper(getContext());
        mAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.customer_profile_view, container, false);

        likedMoviesList = new ArrayList<>();
        likedTVShowsList = new ArrayList<>();

        moviesRecyclerView = view.findViewById(R.id.liked_movie_recyclerview);
        tvShowsRecyclerView = view.findViewById(R.id.liked_tv_recyclerview);
        usernameTextView = view.findViewById(R.id.profile_username);
        emailTextView = view.findViewById(R.id.profile_email);

        movieLikedAdapter = new MovieLikedAdapter(getContext(), likedMoviesList, this::onItemClicked, databaseHelper);
        tvShowLikedAdapter = new TVLikedAdapter(getContext(), likedTVShowsList, this::onItemClicked, databaseHelper);

        moviesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        tvShowsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        moviesRecyclerView.setAdapter(movieLikedAdapter);
        tvShowsRecyclerView.setAdapter(tvShowLikedAdapter);

        ItemTouchHelper movieItemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(moviesRecyclerView));
        movieItemTouchHelper.attachToRecyclerView(moviesRecyclerView);

        ItemTouchHelper tvShowItemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(tvShowsRecyclerView));
        tvShowItemTouchHelper.attachToRecyclerView(tvShowsRecyclerView);

        view.findViewById(R.id.about_btn).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AboutActivity.class)));

        logOutBtn = view.findViewById(R.id.logout_account_btn);
        logOutBtn.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(requireContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        deleteAccountBtn = view.findViewById(R.id.delete_account_btn);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            deleteAccountBtn.setText("Delete Account");
            deleteAccountBtn.setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Account")
                        .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            currentUser.delete().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(requireContext(), LoginActivity.class));
                                    requireActivity().finish();
                                } else {
                                    Toast.makeText(requireContext(), "Failed to delete account", Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("No", null)
                        .show();
            });
        } else {
            logOutBtn.setText("Login / Register");
            logOutBtn.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });
            deleteAccountBtn.setText("Continue Browsing");
            deleteAccountBtn.setOnClickListener(v -> replaceFragment(new HomeFragment()));
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String username = user.getDisplayName();
            String email = user.getEmail();

            usernameTextView.setText(username != null ? username : "No username set");
            emailTextView.setText(email != null ? email : "No email set");
        }
        else {
            usernameTextView.setText("Hi there!");
            emailTextView.setText(" ");
        }

        new Thread(() -> {
            List<MovieModel> likedMovies = getAllMoviesFromDatabase();
            List<TVShowModel> likedTVShows = getSavedTVShows();
            requireActivity().runOnUiThread(() -> {
                movieLikedAdapter.updateData(likedMovies);
                tvShowLikedAdapter.updateData(likedTVShows);
            });
        }).start();
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
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public List<MovieModel> getAllMoviesFromDatabase() {
        List<MovieModel> movies = new ArrayList<>();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return movies;

        String userId = user.getUid();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + DatabaseHelper.TABLE_USER_MOVIES +
                        " WHERE " + DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{userId}
        );

        if (cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_MOVIE_ID);
                int titleIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TITLE);
                int posterPathIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_POSTER_PATH);
                if (idIndex == -1 || titleIndex == -1 || posterPathIndex == -1) {
                    Log.e("Database", "Column missing in user_movies table");
                    break;
                }
                int id = cursor.getInt(idIndex);
                String title = cursor.getString(titleIndex);
                String posterPath = cursor.getString(posterPathIndex);
                movies.add(new MovieModel(id, title, posterPath));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return movies;
    }

    public List<TVShowModel> getSavedTVShows() {
        List<TVShowModel> tvShows = new ArrayList<>();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return tvShows;

        String userId = user.getUid();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + DatabaseHelper.TABLE_USER_TVSHOWS +
                        " WHERE " + DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{userId}
        );

        if (cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TVSHOW_ID);
                int titleIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TITLE);
                int posterPathIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_POSTER_PATH);
                if (idIndex == -1 || titleIndex == -1 || posterPathIndex == -1) {
                    Log.e("Database", "Column missing in user_tvshows table");
                    break;
                }
                int id = cursor.getInt(idIndex);
                String title = cursor.getString(titleIndex);
                String posterPath = cursor.getString(posterPathIndex);
                tvShows.add(new TVShowModel(id, title, posterPath));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tvShows;
    }
}

