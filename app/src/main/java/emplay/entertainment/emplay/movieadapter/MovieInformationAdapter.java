package emplay.entertainment.emplay.movieadapter;

import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.LanguageMapper;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.MovieDetailsResponse;
import emplay.entertainment.emplay.api.MovieDetailsResponse.Genre;

/**
 * RecyclerView.Adapter implementation for displaying detailed information of a movie.
 *
 * @version 1.0
 */
public class MovieInformationAdapter extends RecyclerView.Adapter<MovieInformationAdapter.MovieInformationViewHolder> {

    private List<MovieDetailsResponse> movieInformationList;

    /**
     * Constructor for the MovieInformationAdapter.
     *
     * @param movieInformationList The initial list of MovieDetailsResponse objects to be displayed.
     */
    public MovieInformationAdapter(List<MovieDetailsResponse> movieInformationList) {
        this.movieInformationList = movieInformationList;
    }



    @Override
    public MovieInformationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout for movie information item and create a ViewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_information, parent, false);
        return new MovieInformationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieInformationViewHolder holder, int position) {
        // Bind data to the ViewHolder
        MovieDetailsResponse movie = movieInformationList.get(position);
        holder.bind(movie);
    }

    @Override
    public int getItemCount() {
        // Return the number of items in the data set
        return movieInformationList.size();
    }

    /**
     * Updates the data in the adapter.
     *
     * @param newMovies The new list of MovieDetailsResponse objects.
     */
    public void updateData(List<MovieDetailsResponse> newMovies) {
        movieInformationList.clear();
        movieInformationList.addAll(newMovies);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for movie information items.
     */
    public static class MovieInformationViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView overView;
        private TextView language;
        private TextView releaseDate;
        private TextView genre;
        private TextView runtime;
        private ImageView poster;

        /**
         * Constructor for the ViewHolder.
         *
         * @param itemView The view for the movie information item.
         */
        public MovieInformationViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.search_result_title);
            overView = itemView.findViewById(R.id.search_result_overview);
            language = itemView.findViewById(R.id.search_result_language);
            releaseDate = itemView.findViewById(R.id.search_result_release_date);
            genre = itemView.findViewById(R.id.search_result_genres);
            runtime = itemView.findViewById(R.id.search_result_runtime);
            poster = itemView.findViewById(R.id.imageView);
        }

        /**
         * Binds the movie data to the ViewHolder.
         *
         * @param movie The MovieDetailsResponse object containing movie data.
         */
        public void bind(MovieDetailsResponse movie) {
            name.setText(movie.getTitle());
            overView.setText(movie.getOverview());
            if (VERSION.SDK_INT >= VERSION_CODES.N) {
                language.setText("\u2022    Language: " + LanguageMapper.getLanguageName(movie.getOriginalLanguage()));
            }
            releaseDate.setText("Release Date: " + movie.getReleaseDate());

            // Convert list of genres to a comma-separated string
            List<String> genres = new ArrayList<>();
            if (movie.getGenres() != null) {
                for (Genre genre : movie.getGenres()) {
                    genres.add(genre.getName());
                }
            }
            String genresString = String.join(" | ", genres);
            genre.setText(genresString);

            runtime.setText("\u2022    Runtime: " + movie.getRuntime() + " min");

            Glide.with(itemView.getContext())
                    .load("https://image.tmdb.org/t/p/w500/" + movie.getPosterPath())
                    .into(poster);

            // Set a click listener if needed
            itemView.setOnClickListener(v -> {
                // Handle click trailer event
            });
        }
    }
}
