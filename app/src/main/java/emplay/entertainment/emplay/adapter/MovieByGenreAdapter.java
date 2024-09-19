package emplay.entertainment.emplay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.models.MovieModel;

public class MovieByGenreAdapter extends RecyclerView.Adapter<MovieByGenreAdapter.ViewHolder> {
    private List<MovieModel> movieByGenreList;

    private final Context mContext;
    private final OnItemClickListener onItemClickListener;

    public MovieByGenreAdapter(List<MovieModel> movieByGenreList,  Context mContext, OnItemClickListener onItemClickListener) {
        this.movieByGenreList = movieByGenreList;

        this.mContext = mContext;
        this.onItemClickListener = onItemClickListener;
    }

    public void updateData(List<MovieModel> newMovies) {
        movieByGenreList.clear();
        movieByGenreList.addAll(newMovies);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(MovieModel movie);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_by_genre_item, parent, false);
        View view1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_by_genres_view, parent, false);
        return new ViewHolder(view, view1);
        
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MovieModel movieModel = movieByGenreList.get(position);

        if (movieModel.getPosterPath() != null && !movieModel.getPosterPath().isEmpty()) {
            Glide.with(mContext)
                    .load("https://image.tmdb.org/t/p/w500/" + movieModel.getPosterPath())
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.poster);
        } else {
            Glide.with(mContext)
                    .load(R.drawable.placeholder_image)
                    .into(holder.poster);
        }

        holder.bind(movieModel, onItemClickListener);
    }

    @Override
    public int getItemCount() {
        // Ensure that both lists have the same size
        return (movieByGenreList.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView genreName;
        View view1; // Store reference to second view

        // Constructor for handling two views
        public ViewHolder(View view, View view1) {
            super(view); // Pass the main item view to the parent constructor
            this.view1 = view1;

            // Initialize views from the first layout (view)
            poster = view.findViewById(R.id.poster);
            genreName = view1.findViewById(R.id.movie_genres);

        }

        public void bind(MovieModel movieModel, OnItemClickListener onItemClickListener) {
            itemView.setOnClickListener(v -> onItemClickListener.onItemClick(movieModel));

        }
    }

}
