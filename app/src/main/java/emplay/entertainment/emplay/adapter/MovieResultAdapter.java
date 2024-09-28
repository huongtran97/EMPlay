package emplay.entertainment.emplay.adapter;

import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_MOVIE_ID;
import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_POSTER_PATH;
import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_TITLE;
import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_TVSHOW_ID;
import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_USER_ID;
import static emplay.entertainment.emplay.database.DatabaseHelper.TABLE_USER_MOVIES;

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
            populateMovieData(holder, movieModel);
            setUpTrailerButton(holder);
            handleAddToLibrary(holder, movieModel);

        } else {
            Log.e("MovieResultAdapter", "MovieModel at position " + position + " is null");
        }
    }

    private void populateMovieData(@NonNull MovieResultViewHolder holder, MovieModel movieModel) {
        holder.resultTitle.setText(movieModel.getTitle());
        holder.resultReleaseDate.setText("Release Date: " + movieModel.getReleaseDate());
        holder.resultLanguage.setText("\u2022 Language: " + movieModel.getOriginalLanguage());
        holder.resultRatingBar.setRating((float) (movieModel.getVoteAverage() / 2));
        holder.resultOverview.setText(movieModel.getOverview());
        holder.resultRuntime.setText("\u2022 Runtime: " + movieModel.getRuntime() + " min");

        StringBuilder genreNames = new StringBuilder();
        List<String> genres = movieModel.getGenres();
        if (genres != null) {
            for (String genre : genres) {
                if (genreNames.length() > 0) {
                    genreNames.append(" | ");
                }
                genreNames.append(genre);
            }
        }
        holder.genreNames.setText(genreNames.toString());

        Glide.with(holder.itemView.getContext())
                .load("https://image.tmdb.org/t/p/w500" + movieModel.getPosterPath())
                .error(R.drawable.placeholder_image)
                .into(holder.resultPoster);
    }

    private void setUpTrailerButton(@NonNull MovieResultViewHolder holder) {
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
    }

    private void handleAddToLibrary(@NonNull MovieResultViewHolder holder, MovieModel movieModel) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            holder.addBtn.setOnClickListener(v ->
                    Toast.makeText(fragmentActivity, "You must be logged in to save it!", Toast.LENGTH_SHORT).show());
            return;
        }

        String userId = currentUser.getUid();
        updateSaveButtonState(holder, userId, movieModel);

        holder.addBtn.setOnClickListener(v -> {
            int movieId = movieModel.getMovieId();
            String title = movieModel.getTitle();
            String posterPath = movieModel.getPosterPath();

            if (isMovieSavedByUser(userId, movieId)) {
                removeMovieFromUserMovies(userId, movieId);
                holder.addBtn.setImageResource(R.drawable.baseline_favorite_border_24);
                Toast.makeText(fragmentActivity, "Movie removed from library", Toast.LENGTH_SHORT).show();
            } else {
                long result = saveMovieToUserMovies(userId, movieId, title, posterPath);
                if (result != -1) {
                    holder.addBtn.setImageResource(R.drawable.baseline_favorite_24);
                    Toast.makeText(fragmentActivity, "Added to library", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(fragmentActivity, "Failed to add movie", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateSaveButtonState(@NonNull MovieResultViewHolder holder, String userId, MovieModel movieModel) {
        if (isMovieSavedByUser(userId, movieModel.getMovieId())) {
            holder.addBtn.setImageResource(R.drawable.baseline_favorite_24);
        } else {
            holder.addBtn.setImageResource(R.drawable.baseline_favorite_border_24);
        }
    }




    public long saveMovieToUserMovies(String userId, int movieId, String title, String posterPath) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_MOVIE_ID, movieId);
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_POSTER_PATH, posterPath);
        return db.insert(TABLE_USER_MOVIES, null, values);
    }

    private void removeMovieFromUserMovies(String userId, int movieId) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.delete(TABLE_USER_MOVIES, COLUMN_USER_ID + " = ? AND " + COLUMN_MOVIE_ID + " = ?", new String[]{userId, String.valueOf(movieId)});
    }

    private boolean isMovieSavedByUser(String userId, int movieId) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER_MOVIES + " WHERE " + COLUMN_USER_ID + " = ? AND " + COLUMN_MOVIE_ID + " = ?",
                new String[]{userId, String.valueOf(movieId)});

        boolean isSaved = cursor.getCount() > 0;
        cursor.close();
        return isSaved;
    }

    public void setTeaserTrailers(List<MoviesTrailerResponses.TrailerModel> adapterTrailers) {
        this.trailers = adapterTrailers != null ? adapterTrailers : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return movieList.size();
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

