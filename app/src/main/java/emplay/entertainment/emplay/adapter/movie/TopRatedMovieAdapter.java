package emplay.entertainment.emplay.adapter.movie;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.models.movie.MovieModel;

public class TopRatedMovieAdapter extends RecyclerView.Adapter<TopRatedMovieAdapter.ViewHolder> {
    private final Context context;
    private final List<MovieModel> movies;
    private final OnMovieClickListener listener;

    public interface OnMovieClickListener {
        void onMovieClick(MovieModel movie);
    }

    public TopRatedMovieAdapter(Context context, List<MovieModel> movies, OnMovieClickListener listener) {
        this.context = context;
        this.movies = movies;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_filmstrip_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MovieModel movie = movies.get(position);

        Glide.with(context)
                .load(ImageUrl.POSTER + movie.getPosterPath())
                .placeholder(R.drawable.bg_poster_placeholder)
                .into(holder.ivPoster);

        holder.tvRank.setText(String.valueOf(position + 1));
        holder.tvTitle.setText(movie.getTitle());

        String year = (movie.getReleaseDate() != null && movie.getReleaseDate().length() >= 4)
                ? movie.getReleaseDate().substring(0, 4) : "";
        holder.tvMeta.setText(String.format(Locale.getDefault(), "%s · ★ %.1f", year, movie.getVoteAverage()));

        holder.itemView.setOnClickListener(v -> listener.onMovieClick(movie));
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<MovieModel> newMovies) {
        movies.clear();
        if (newMovies != null) movies.addAll(newMovies);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView tvRank, tvTitle, tvMeta;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            tvRank   = itemView.findViewById(R.id.tvRank);
            tvTitle  = itemView.findViewById(R.id.tvTitle);
            tvMeta   = itemView.findViewById(R.id.tvMeta);
        }
    }
}
