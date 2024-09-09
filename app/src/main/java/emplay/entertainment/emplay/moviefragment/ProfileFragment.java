package emplay.entertainment.emplay.moviefragment;

import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_ID;
import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_POSTER_PATH;
import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_TITLE;
import static emplay.entertainment.emplay.database.DatabaseHelper.TABLE_TVSHOWS;

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
import emplay.entertainment.emplay.SwipeToDeleteCallback;
import emplay.entertainment.emplay.activity.LoginActivity;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.models.MovieModel;
import emplay.entertainment.emplay.models.TVShowModel;
import emplay.entertainment.emplay.movieadapter.MovieLikedAdapter;
import emplay.entertainment.emplay.movieadapter.TVLikedAdapter;

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

        // Initialize lists
        likedMoviesList = new ArrayList<>();
        likedTVShowsList = new ArrayList<>();

        // Initialize RecyclerViews and adapters
        moviesRecyclerView = view.findViewById(R.id.liked_movie_recyclerview);
        tvShowsRecyclerView = view.findViewById(R.id.liked_tv_recyclerview);
        usernameTextView = view.findViewById(R.id.profile_username);
        emailTextView = view.findViewById(R.id.profile_email);

        movieLikedAdapter = new MovieLikedAdapter(getContext(), likedMoviesList, this::onItemClicked, databaseHelper);
        tvShowLikedAdapter = new TVLikedAdapter(getContext(), likedTVShowsList, this::onItemClicked, databaseHelper);

        moviesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tvShowsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        moviesRecyclerView.setAdapter(movieLikedAdapter);
        tvShowsRecyclerView.setAdapter(tvShowLikedAdapter);

        ItemTouchHelper movieItemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(moviesRecyclerView));
        movieItemTouchHelper.attachToRecyclerView(moviesRecyclerView);

        ItemTouchHelper tvShowItemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(tvShowsRecyclerView));
        tvShowItemTouchHelper.attachToRecyclerView(tvShowsRecyclerView);

        logOutBtn = view.findViewById(R.id.logout_account_btn);
        logOutBtn.setOnClickListener(v -> {
            // Sign out the user
            mAuth.signOut();

            // Redirect to login activity or another activity
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish(); // Optional: Finish the current activity
        });


        deleteAccountBtn = view.findViewById(R.id.delete_account_btn);
        deleteAccountBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.delete().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(requireContext(), LoginActivity.class));
                                    requireActivity().finish();
                                } else {
                                    Toast.makeText(requireContext(), "Failed to delete account", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(requireContext(), "No user is signed in", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Display user information
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String username = user.getDisplayName();  // Assuming display name is set
            String email = user.getEmail();

            usernameTextView.setText(username != null ? username : "No username set");
            emailTextView.setText(email != null ? email : "No email set");
        } else {
            usernameTextView.setText("No user signed in");
            emailTextView.setText("No user signed in");
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
            ShowResultTVShowDetailsFragment fragment = ShowResultTVShowDetailsFragment.newInstance(tvShow.getId());
            replaceFragment(fragment);
        } else {
            Toast.makeText(getContext(), "Unknown item type", Toast.LENGTH_SHORT).show();
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public List<MovieModel> getAllMoviesFromDatabase() {
        List<MovieModel> movies = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM movies", null);

        if (cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex(COLUMN_ID);
                int posterPathIndex = cursor.getColumnIndex(COLUMN_POSTER_PATH);
                int titleIndex = cursor.getColumnIndex(COLUMN_TITLE);
                if (idIndex == -1 || posterPathIndex == -1 || titleIndex == -1) {
                    Log.e("Database", "Column missing in database table");
                    return movies;
                }
                int id = cursor.getInt(idIndex);
                String title = cursor.getString(titleIndex);
                String posterPath = cursor.getString(posterPathIndex);
                Log.d("Database", "Movie ID: " + id + ", Poster Path: " + posterPath);
                MovieModel movie = new MovieModel(id, title, posterPath);
                movies.add(movie);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return movies;
    }

    public List<TVShowModel> getSavedTVShows() {
        List<TVShowModel> tvShows = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TVSHOWS, null);

        if (cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex(COLUMN_ID);
                int posterPathIndex = cursor.getColumnIndex(COLUMN_POSTER_PATH);
                int titleIndex = cursor.getColumnIndex(COLUMN_TITLE);
                if (idIndex == -1 || posterPathIndex == -1 || titleIndex == -1) {
                    Log.e("Database", "Column missing in database table");
                    return tvShows;
                }
                int id = cursor.getInt(idIndex);
                String title = cursor.getString(titleIndex);
                String posterPath = cursor.getString(posterPathIndex);
                Log.d("Database", "TV Show ID: " + id + ", Poster Path: " + posterPath);
                TVShowModel tvShow = new TVShowModel(id, title, posterPath);
                tvShows.add(tvShow);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return tvShows;
    }
}

