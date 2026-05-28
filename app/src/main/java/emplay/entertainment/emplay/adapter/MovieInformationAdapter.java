package emplay.entertainment.emplay.adapter;

import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_GENRES;
import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_MOVIE_ID;
import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_POSTER_PATH;
import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_TITLE;
import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_USER_ID;
import static emplay.entertainment.emplay.database.DatabaseHelper.TABLE_USER_MOVIES;

import android.annotation.SuppressLint;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.ImageUrl;
import emplay.entertainment.emplay.activity.TrailerActivity;
import emplay.entertainment.emplay.api.MoviesTrailerResponses;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.models.MovieModel;
import emplay.entertainment.emplay.tool.ReadHelper;
import emplay.entertainment.emplay.fragment.ShowResultDetailsFragment;

/**
 *  Adapter for the movie detail card (single item — poster, metadata, overview).
 *  Handles the favourite/save button, trailer launch, and Read More/Less for the overview.
 */
public class MovieInformationAdapter extends RecyclerView.Adapter<MovieInformationAdapter.MovieResultViewHolder> {
    private final List<MovieModel> movieList;
    private List<MoviesTrailerResponses.TrailerModel> trailers = new ArrayList<>();
    private final FragmentActivity fragmentActivity;
    private final DatabaseHelper databaseHelper;
    private final FirebaseAuth mAuth;
    private final Set<Integer> expandedItems = new HashSet<>();

    public MovieInformationAdapter(List<MovieModel> movieList, FragmentActivity fragmentActivity) {
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

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MovieResultViewHolder holder, int position) {
        MovieModel movieModel = movieList.get(position);
        if (movieModel == null) {
            Log.e("MovieResultAdapter", "MovieModel at position " + position + " is null");
            return;
        }

        holder.resultTitle.setText(movieModel.getTitle());
        holder.resultReleaseDate.setText("Release Date: " + movieModel.getReleaseDate());
        holder.resultLanguage.setText("\u2022 Language: " + movieModel.getOriginalLanguage());
        holder.resultRatingBar.setRating((float) (movieModel.getVoteAverage() / 2));
        holder.resultOverview.setText(movieModel.getOverview());
        
        // Read More / Less 
        ReadHelper.setup(holder.resultOverview, holder.readMoreBtn, movieModel.getId(), expandedItems, holder, this);

        holder.resultRuntime.setText("\u2022 Runtime: " + movieModel.getRuntime() + " min");

        List<String> genres = movieModel.getGenres() != null ? movieModel.getGenres() : new ArrayList<>();
        StringBuilder genreNames = new StringBuilder();
        for (String genre : genres) {
            if (genreNames.length() > 0) genreNames.append(" | ");
            genreNames.append(genre);
        }
        holder.genreNames.setText(genreNames.toString());

        Glide.with(holder.itemView.getContext())
                .load(ImageUrl.POSTER + movieModel.getPosterPath())
                .error(R.drawable.placeholder_image)
                .into(holder.resultPoster);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            holder.addBtn.setImageResource(R.drawable.baseline_favorite_border_24);
            holder.addBtn.setOnClickListener(v ->
                    Toast.makeText(fragmentActivity, "You must be logged in to save it!", Toast.LENGTH_SHORT).show());
        } else {
            String userId = currentUser.getUid();
            updateSaveButtonState(holder, userId, movieModel.getId());

            holder.addBtn.setOnClickListener(v -> {
                if (isMovieSavedByUser(userId, movieModel.getId())) {
                    removeMovieFromUserMovies(userId, movieModel.getId());
                    holder.addBtn.setImageResource(R.drawable.baseline_favorite_border_24);
                    Toast.makeText(fragmentActivity, "Movie removed from library", Toast.LENGTH_SHORT).show();
                } else {
                    String genresString = "";
                    if (movieModel.getGenres() != null) {
                        genresString = String.join(",", movieModel.getGenres());
                    }
                    long result = saveMovieToUserMovies(userId, movieModel.getId(), movieModel.getTitle(), movieModel.getPosterPath(), genresString);
                    if (result != -1) {
                        holder.addBtn.setImageResource(R.drawable.baseline_favorite_24);
                        Toast.makeText(fragmentActivity, "Movie added to library", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(fragmentActivity, "Failed to add movie", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

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

        holder.itemView.setOnClickListener(v -> {
            ShowResultDetailsFragment fragment = ShowResultDetailsFragment.newInstance(movieModel.getId());
            FragmentTransaction transaction = fragmentActivity.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }

    private void updateSaveButtonState(MovieResultViewHolder holder, String userId, int movieId) {
        if (isMovieSavedByUser(userId, movieId)) {
            holder.addBtn.setImageResource(R.drawable.baseline_favorite_24);
        } else {
            holder.addBtn.setImageResource(R.drawable.baseline_favorite_border_24);
        }
    }

    public long saveMovieToUserMovies(String userId, int movieId, String title, String posterPath, String genres) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_MOVIE_ID, movieId);
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_POSTER_PATH, posterPath);
        values.put(COLUMN_GENRES, genres);
        long result = db.insert(TABLE_USER_MOVIES, null, values);
        Log.d("MovieSave", "Saved movie: " + title + ", result: " + result + ", userId: " + userId);
        return result;
    }

    public boolean isMovieSavedByUser(String userId, int movieId) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_USER_MOVIES +
                        " WHERE " + COLUMN_USER_ID + " = ? AND " + COLUMN_MOVIE_ID + " = ?",
                new String[]{userId, String.valueOf(movieId)}
        );
        boolean isSaved = false;
        if (cursor.moveToFirst()) {
            isSaved = cursor.getInt(0) > 0;
        }
        cursor.close();
        return isSaved;
    }

    private void removeMovieFromUserMovies(String userId, int movieId) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.delete(TABLE_USER_MOVIES,
                COLUMN_USER_ID + " = ? AND " + COLUMN_MOVIE_ID + " = ?",
                new String[]{userId, String.valueOf(movieId)}
        );
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setTeaserTrailers(List<MoviesTrailerResponses.TrailerModel> adapterTrailers) {
        this.trailers = adapterTrailers != null ? adapterTrailers : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class MovieResultViewHolder extends RecyclerView.ViewHolder {
        TextView resultTitle, resultReleaseDate, resultLanguage, genreNames, resultOverview, resultRuntime, readMoreBtn;
        RatingBar resultRatingBar;
        ImageView resultPoster;
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
            readMoreBtn = itemView.findViewById(R.id.read_more_text);
            addBtn = itemView.findViewById(R.id.add_to_library_btn);
            trailerBtn = itemView.findViewById(R.id.trailer_btn);
        }
    }
}