package emplay.entertainment.emplay;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 *  @author Tran Ngoc Que Huong
 *  @version 1.0
 *
 * RecyclerView.Adapter implementation for displaying detailed information of a movie.
 */
public class MovieInformationAdapter extends RecyclerView.Adapter<MovieInformationAdapter.MovieInformationViewHolder> {

    private List<MovieModel> movieInformationList;

    /**
     * Constructor for the MovieInformationAdapter.
     *
     * @param movieInformationList The initial list of MovieModel objects to be displayed.
     */
    public MovieInformationAdapter(List<MovieModel> movieInformationList) {
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
        MovieModel movie = movieInformationList.get(position);
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
     * @param newMovies The new list of MovieModel objects.
     */
    public void updateData(List<MovieModel> newMovies) {
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
        private ImageButton poster;

        /**
         * Constructor for the ViewHolder.
         *
         * @param itemView The view for the movie information item.
         */
        public MovieInformationViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.movie_information_title);
            overView = itemView.findViewById(R.id.movie_information_overview);
            language = itemView.findViewById(R.id.movie_information_language);
            releaseDate = itemView.findViewById(R.id.movie_information_release_date);
            poster = itemView.findViewById(R.id.imageButton2);
        }

        /**
         * Binds the movie data to the ViewHolder.
         *
         * @param movie The MovieModel object containing movie data.
         */
        public void bind(MovieModel movie) {
            name.setText(movie.getName());
            overView.setText(movie.getOverview());
            language.setText(movie.getLanguage());
            releaseDate.setText(movie.getReleaseDate());

            Glide.with(itemView.getContext())
                    .load("https://image.tmdb.org/t/p/w500/" + movie.getPosterPath())
                    .into(poster);

            // Set a click listener if needed
            itemView.setOnClickListener(v -> {
                // Handle click event
            });
        }
    }
}
