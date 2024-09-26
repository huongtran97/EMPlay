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
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.models.MovieModel;

public class MovieLikedAdapter extends RecyclerView.Adapter<MovieLikedAdapter.MyViewHolder> {

    private final Context mContext;
    private final List<MovieModel> mData;
    private final OnItemClickListener onItemClickListener;
    private final DatabaseHelper databaseHelper;

    public MovieLikedAdapter(Context context, List<MovieModel> movies, OnItemClickListener onItemClickListener, DatabaseHelper databaseHelper) {
        this.mContext = context;
        this.mData = movies;
        this.onItemClickListener = onItemClickListener;
        this.databaseHelper = databaseHelper;
    }

    public void removeItem(int position) {
        // Remove the item from the database
        MovieModel movieToDelete = mData.get(position);
        databaseHelper.deleteMovie(movieToDelete.getMovieId());

        // Remove the item from the list and notify the adapter
        mData.remove(position);
        notifyItemRemoved(position);
    }

    public interface OnItemClickListener {
        void onItemClick(MovieModel movie);
    }

    public void updateData(List<MovieModel> newMovies) {
        mData.clear();
        mData.addAll(newMovies);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.liked_movie_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        MovieModel movie = mData.get(position);

        String fullUrl = movie.getPosterPath() != null ? "https://image.tmdb.org/t/p/w500" + movie.getPosterPath() : null;
        Glide.with(mContext)
                .load(fullUrl)
                .into(holder.img);
        holder.name.setText(movie.getTitle());

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(movie);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView name;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.liked_poster);
            name = itemView.findViewById(R.id.liked_name);
        }
    }
}