package emplay.entertainment.emplay.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.ViewGroup;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.models.MovieModel;

/**
 * @author Tran Ngoc Que Huong
 * @version 1.0
 * RecyclerView.Adapter implementation for displaying a list of movies.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MyViewHolder> {

    private final Context mContext;
    private final List<MovieModel> mData;
    private final OnItemClickListener onItemClickListener;

    /**
     * Interface for handling click events on movie items.
     */
    public interface OnItemClickListener {
        /**
         * Called when a movie item is clicked.
         *
         * @param movie The MovieModel object associated with the clicked item.
         */
        void onItemClick(MovieModel movie);
    }

    /**
     * Constructor for the MovieAdapter.
     *
     * @param context             The context in which the adapter is operating.
     * @param data                The list of MovieModel objects to be displayed.
     * @param onItemClickListener Listener for handling item click events.
     */
    public MovieAdapter(Context context, List<MovieModel> data, OnItemClickListener onItemClickListener) {
        this.mContext = context;
        this.mData = data;
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * Updates the data in the adapter.
     *
     * @param newMovies The new list of MovieModel objects.
     */
    public void updateData(List<MovieModel> newMovies) {
        mData.clear();
        mData.addAll(newMovies);
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.movie_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        MovieModel movie = mData.get(position);

        // Handle null or empty poster path
        if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
            // Load the poster path
            Glide.with(mContext)
                    .load("https://image.tmdb.org/t/p/w500/" + movie.getPosterPath())
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.img);
        } else if (movie.getBackdropPath() != null && !movie.getBackdropPath().isEmpty()) {
            // Load the backdrop path
            Glide.with(mContext)
                    .load("https://image.tmdb.org/t/p/w500/" + movie.getBackdropPath())
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.img);
        } else {
            // Load a drawable resource as a fallback when both paths are null or empty
            Glide.with(mContext)
                    .load(R.drawable.placeholder_image)
                    .into(holder.img);
        }


        // Bind the item
        holder.bind(movie, onItemClickListener);
    }




    @Override
    public int getItemCount() {
        return mData.size();
    }

    /**
     * ViewHolder class for the movie items.
     */
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView img;

        /**
         * Constructor for the ViewHolder.
         *
         * @param itemView The view for the movie item.
         */
        public MyViewHolder(View itemView) {
            super(itemView);

            img = itemView.findViewById(R.id.header);
        }

        /**
         * Binds the item and click listener to the ViewHolder.
         *
         * @param item                The MovieModel object to bind.
         * @param onItemClickListener The listener for click events.
         */
        public void bind(MovieModel item, OnItemClickListener onItemClickListener) {
            itemView.setOnClickListener(v -> onItemClickListener.onItemClick(item));
        }
    }
}

