package emplay.entertainment.emplay.adapter;

import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_ID;
import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_POSTER_PATH;
import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_TITLE;
import static emplay.entertainment.emplay.database.DatabaseHelper.TABLE_MOVIES;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.activity.TrailerActivity;
import emplay.entertainment.emplay.api.MoviesTrailerResponses;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.models.MovieModel;
import emplay.entertainment.emplay.fragment.ProfileFragment;

public class MovieResultAdapter extends RecyclerView.Adapter<MovieResultAdapter.MovieResultViewHolder> {

    private List<MovieModel> movieList;
    private List<MoviesTrailerResponses.TrailerModel> trailers = new ArrayList<>();
    private FragmentActivity fragmentActivity;
    private DatabaseHelper databaseHelper;
    private FirebaseAuth mAuth;

    // Constructor
    public MovieResultAdapter(List<MovieModel> movieList, FragmentActivity fragmentActivity) {
        this.movieList = movieList;
        this.fragmentActivity = fragmentActivity;
        this.databaseHelper = new DatabaseHelper(fragmentActivity);
        this.mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public MovieResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_information, parent, false);
        return new MovieResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieResultViewHolder holder, int position) {
        MovieModel movieModel = movieList.get(position);
        if (movieModel != null) {
            holder.resultTitle.setText(movieModel.getTitle());
            holder.resultReleaseDate.setText("Release Date: " + movieModel.getReleaseDate());
            holder.resultLanguage.setText("\u2022 Language: " + movieModel.getOriginalLanguage());
            holder.resultRatingBar.setRating((float) (movieModel.getVoteAverage() / 2));
            holder.resultOverview.setText(movieModel.getOverview());
            holder.resultRuntime.setText("\u2022 Runtime: " + movieModel.getRuntime() + " min");

            List<String> genres = movieModel.getGenres() != null ? movieModel.getGenres() : new ArrayList<>();
            StringBuilder genreNames = new StringBuilder();
            for (String genre : genres) {
                if (genreNames.length() > 0) {
                    genreNames.append(" | ");
                }
                genreNames.append(genre);
            }
            holder.genreNames.setText(genreNames.toString());

            Glide.with(holder.itemView.getContext())
                    .load("https://image.tmdb.org/t/p/w500" + movieModel.getPosterPath())
                    .error(R.drawable.placeholder_image)
                    .into(holder.resultPoster);

            // Check if the movie is already saved and update the icon
            if (isMovieSaved(movieModel.getId())) {
                holder.addBtn.setImageResource(R.drawable.baseline_favorite_24);
            } else {
                holder.addBtn.setImageResource(R.drawable.baseline_favorite_border_24);
            }

            // Trailer button logic
            holder.trailerBtn.setOnClickListener(v -> {
                if (!trailers.isEmpty()) {
                    String videoKey = trailers.get(0).getKey();
                    Intent intent = new Intent(holder.itemView.getContext(), TrailerActivity.class);
                    intent.putExtra("MOVIE_ID", videoKey);
                    holder.itemView.getContext().startActivity(intent);
                } else {
                    Toast.makeText(fragmentActivity, "No trailer available", Toast.LENGTH_SHORT).show();
                }
            });

            // Add/Remove movie logic with authentication check
            holder.addBtn.setOnClickListener(v -> {
                FirebaseUser currentUser = mAuth.getCurrentUser(); // Check if user is logged in
                if (currentUser != null) {
                    // User is logged in
                    if (isMovieSaved(movieModel.getId())) {
                        removeMovieFromDatabase(movieModel.getId());
                        holder.addBtn.setImageResource(R.drawable.baseline_favorite_border_24);
                        Toast.makeText(fragmentActivity, "Movie removed from library", Toast.LENGTH_SHORT).show();
                    } else {
                        long result = saveMovieToDatabase(movieModel);
                        if (result != -1) {
                            holder.addBtn.setImageResource(R.drawable.baseline_favorite_24);
                            Toast.makeText(fragmentActivity, "Added to library", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(fragmentActivity, "Failed to add movie", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(fragmentActivity, "You must be logged in to save it!", Toast.LENGTH_SHORT).show();
                }
            });

            // Item click logic for displaying movie details
            holder.itemView.setOnClickListener(v -> onItemClick(movieModel));
        } else {
            Log.e("MovieResultAdapter", "MovieModel at position " + position + " is null");
        }
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    // Check if the movie is already saved in the database
    private boolean isMovieSaved(long movieId) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_MOVIES + " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(movieId)});
        boolean isSaved = false;
        if (cursor.moveToFirst()) {
            isSaved = cursor.getInt(0) > 0;
        }
        cursor.close();
        return isSaved;
    }

    // Remove a movie from the database
    private void removeMovieFromDatabase(long movieId) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.delete(TABLE_MOVIES, COLUMN_ID + " = ?", new String[]{String.valueOf(movieId)});
    }

    // Save a movie to the database
    public long saveMovieToDatabase(MovieModel movie) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, movie.getId());
        values.put(COLUMN_TITLE, movie.getTitle());
        values.put(COLUMN_POSTER_PATH, movie.getPosterPath());

        return db.insertWithOnConflict(TABLE_MOVIES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    // Handle item click for navigating to movie details
    private void onItemClick(MovieModel movieModel) {
        ProfileFragment profileFragment = ProfileFragment.newInstance(movieModel.getId());

        FragmentTransaction transaction = fragmentActivity.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, profileFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void setTeaserTrailers(List<MoviesTrailerResponses.TrailerModel> adapterTrailers) {
        this.trailers = adapterTrailers != null ? adapterTrailers : new ArrayList<>();
        notifyDataSetChanged();
    }

    // ViewHolder class for the RecyclerView
    public static class MovieResultViewHolder extends RecyclerView.ViewHolder {
        TextView resultTitle;
        TextView resultReleaseDate;
        TextView resultLanguage;
        RatingBar resultRatingBar;
        TextView genreNames;
        ImageView resultPoster;
        TextView resultOverview;
        TextView resultRuntime;
        ImageButton addBtn;
        Button trailerBtn;

        public MovieResultViewHolder(@NonNull View itemView) {
            super(itemView);
            resultTitle = itemView.findViewById(R.id.search_result_title);
            resultReleaseDate = itemView.findViewById(R.id.search_result_release_date);
            resultLanguage = itemView.findViewById(R.id.search_result_language);
            resultRatingBar = itemView.findViewById(R.id.ratingBar);
            genreNames = itemView.findViewById(R.id.search_result_genres);
            resultPoster = itemView.findViewById(R.id.imageView);
            resultOverview = itemView.findViewById(R.id.search_result_overview);
            resultRuntime = itemView.findViewById(R.id.search_result_runtime);
            addBtn = itemView.findViewById(R.id.add_to_library_btn);
            trailerBtn = itemView.findViewById(R.id.trailer_btn);
        }
    }
}

