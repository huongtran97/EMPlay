package emplay.entertainment.emplay.adapter;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.models.MovieModel;


public class SearchMovieAdapter extends RecyclerView.Adapter<SearchMovieAdapter.MovieViewHolder> {

    private final List<MovieModel> movieList;
    private final OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(MovieModel movie);
    }

    public SearchMovieAdapter(List<MovieModel> movieList, OnItemClickListener onItemClickListener) {
        this.movieList = movieList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        MovieModel movie = movieList.get(position);

        if (movie != null) {
            holder.title.setText(movie.getTitle());
            holder.releaseDate.setText("Release Date: " + movie.getReleaseDate());
            if (VERSION.SDK_INT >= VERSION_CODES.N) {
                holder.language.setText("Language: " + movie.getOriginalLanguage());
            }
            holder.ratingBar.setRating((float) (movie.getVoteAverage() / 2));

            Glide.with(holder.itemView.getContext())
                    .load("https://image.tmdb.org/t/p/w500" + movie.getPosterPath())
                    .into(holder.poster);

            holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(movie));
        }
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView releaseDate;
        TextView language;
        ImageView poster;
        RatingBar ratingBar;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.search_title);
            releaseDate = itemView.findViewById(R.id.search_release_date);
            language = itemView.findViewById(R.id.search_language);
            poster = itemView.findViewById(R.id.search_poster);
            ratingBar = itemView.findViewById(R.id.search_ratingBar);
        }
    }
}




