package emplay.entertainment.emplay.movieadapter;

import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_ID;
import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_POSTER_PATH;
import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_TITLE;
import static emplay.entertainment.emplay.database.DatabaseHelper.TABLE_MOVIES;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.widget.Toast;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.models.MovieModel;
import emplay.entertainment.emplay.moviefragment.ProfileFragment;

public class MovieResultAdapter extends RecyclerView.Adapter<MovieResultAdapter.MovieResultViewHolder> {

    private List<MovieModel> movieList;
    private FragmentActivity fragmentActivity;
    private DatabaseHelper databaseHelper;
    private Context context;

    // Constructor
    public MovieResultAdapter(List<MovieModel> movieList, FragmentActivity fragmentActivity) {
        this.movieList = movieList;
        this.fragmentActivity = fragmentActivity;
        this.context = fragmentActivity; // Initialize context
        this.databaseHelper = new DatabaseHelper(context); // Initialize DatabaseHelper with context
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
            // Bind data as before
            holder.resultTitle.setText(movieModel.getTitle());
            holder.resultReleaseDate.setText("Release Date: " + movieModel.getReleaseDate());
            holder.resultLanguage.setText("\u2022    Language: " + movieModel.getOriginalLanguage());
            holder.resultRatingBar.setRating((float) (movieModel.getVoteAverage() / 2));
            holder.resultOverview.setText("Overview: " + movieModel.getOverview());
            holder.resultRuntime.setText("\u2022    Runtime: " + movieModel.getRuntime() + " min");

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
                    .into(holder.resultPoster);

            // Check if the movie is already saved and update the icon
            if (isMovieSaved(movieModel.getId())) {
                holder.addBtn.setImageResource(R.drawable.baseline_favorite_24);
            } else {
                holder.addBtn.setImageResource(R.drawable.baseline_favorite_border_24);
            }

            holder.addBtn.setOnClickListener(v -> {
                if (isMovieSaved(movieModel.getId())) {
                    removeMovieFromDatabase(movieModel.getId());
                    holder.addBtn.setImageResource(R.drawable.baseline_favorite_border_24);
                    Toast.makeText(context, "Movie removed from library", Toast.LENGTH_SHORT).show();
                } else {
                    long result = saveMovieToDatabase(movieModel);
                    if (result != -1) {
                        holder.addBtn.setImageResource(R.drawable.baseline_favorite_24);
                        Toast.makeText(context, "Movie added to library", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to add movie", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            holder.itemView.setOnClickListener(v -> onItemClick(movieModel));
        }
    }

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

    private void removeMovieFromDatabase(long movieId) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.delete(TABLE_MOVIES, COLUMN_ID + " = ?", new String[]{String.valueOf(movieId)});
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public long saveMovieToDatabase(MovieModel movie) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase(); // Use databaseHelper instance
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, movie.getId());
        values.put(COLUMN_TITLE, movie.getTitle());
        values.put(COLUMN_POSTER_PATH, movie.getPosterPath());

        long result = db.insertWithOnConflict(TABLE_MOVIES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return result;
    }

    private void onItemClick(MovieModel movieModel) {
        // Create a new instance of the movie details fragment with the selected movie's ID
        ProfileFragment profileFragment = ProfileFragment.newInstance(movieModel.getId());

        // Begin a fragment transaction to replace the current fragment with the movie details fragment
        FragmentTransaction transaction = fragmentActivity.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, profileFragment);
        transaction.addToBackStack(null); // Add the transaction to the back stack to allow the user to navigate back
        transaction.commit();
    }

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
        }
    }
}
