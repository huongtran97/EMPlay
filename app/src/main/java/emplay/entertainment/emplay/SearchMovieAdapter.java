package emplay.entertainment.emplay;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;


/**
 *  * @author Tran Ngoc Que Huong
 *  * @version 1.0
 *
 * Adapter for displaying a list of movies in a Search Movie RecyclerView.
 */
public class SearchMovieAdapter extends RecyclerView.Adapter<SearchMovieAdapter.MovieViewHolder> {

    private final List<MovieModel> movieList;
    private final OnItemClickListener onItemClickListener;

    /**
     * Interface for handling item click events.
     */
    public interface OnItemClickListener {
        /**
         * Called when a movie item is clicked.
         *
         * @param movie The movie that was clicked.
         */
        void onItemClick(MovieModel movie);
    }

    /**
     * Constructor for the SearchMovieAdapter.
     *
     * @param movieList         The list of movies to be displayed.
     * @param onItemClickListener The listener for item click events.
     */
    public SearchMovieAdapter(List<MovieModel> movieList, OnItemClickListener onItemClickListener) {
        this.movieList = movieList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_movie_item, parent, false); // Ensure this matches your XML filename
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        MovieModel movie = movieList.get(position);

        holder.title.setText("  "+ movie.getName());
        holder.releaseDate.setText("    Release Date: " + movie.getReleaseDate());
        holder.language.setText("   Language: " + movie.getLanguage());
        holder.overview.setText("   Overview: " + movie.getOverview());

        Glide.with(holder.itemView.getContext())
                .load("https://image.tmdb.org/t/p/w500" + movie.getPosterPath())
                .into(holder.poster);

        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(movie));
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    /**
     * ViewHolder for movie items in the RecyclerView.
     */
    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView releaseDate;
        TextView language;
        TextView overview;
        ImageView poster;

        /**
         * Constructor for the MovieViewHolder.
         *
         * @param itemView The view for each movie item.
         */
        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.search_title);
            releaseDate = itemView.findViewById(R.id.search_release_date);
            language = itemView.findViewById(R.id.search_language);
            overview = itemView.findViewById(R.id.search_overview);
            poster = itemView.findViewById(R.id.search_poster);
        }
    }
}


