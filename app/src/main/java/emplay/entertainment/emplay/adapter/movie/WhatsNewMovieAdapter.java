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
import java.util.Objects;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.models.movie.MovieModel;
import emplay.entertainment.emplay.tool.BadgeHelper;

public class WhatsNewMovieAdapter extends RecyclerView.Adapter<WhatsNewMovieAdapter.ViewHolder> {
    private final Context context;
    private final List<MovieModel> movieList;
    private final OnItemClickListener listener;
    private final int maxItems;

    public interface OnItemClickListener {
        void onItemClick(MovieModel movie, View sharedElement);
    }

    public WhatsNewMovieAdapter(Context context, List<MovieModel> movieList, OnItemClickListener listener) {
        this(context, movieList, listener, Integer.MAX_VALUE);
    }

    public WhatsNewMovieAdapter(Context context, List<MovieModel> movieList, OnItemClickListener listener, int maxItems) {
        this.context = context;
        this.movieList = movieList;
        this.listener = listener;
        this.maxItems = maxItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_whats_new_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MovieModel movie = movieList.get(position);

        Glide.with(context)
                .load(ImageUrl.of(ImageUrl.CARD, movie.getBackdropPath()))
                .placeholder(R.drawable.bg_poster_placeholder)
                .into(holder.ivThumb);

        holder.tvTitle.setText(movie.getTitle());
        holder.tvMeta.setText(movie.getReleaseDate());
        BadgeHelper.applyMovieBadge(holder.tvBadge, movie.getReleaseDate());

        holder.ivThumb.setTransitionName("poster_" + movie.getMovieId());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(movie, holder.ivThumb));
    }

    @Override
    public int getItemCount() {
        return Math.min(movieList.size(), maxItems);
    }

    public void updateData(List<MovieModel> newData) {
        List<MovieModel> oldData = new ArrayList<>(movieList);
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return oldData.size(); }
            @Override public int getNewListSize() { return newData != null ? newData.size() : 0; }
            @Override public boolean areItemsTheSame(int o, int n) {
                return oldData.get(o).getMovieId() == newData.get(n).getMovieId();
            }
            @Override public boolean areContentsTheSame(int o, int n) {
                return Objects.equals(oldData.get(o).getTitle(), newData.get(n).getTitle());
            }
        });
        movieList.clear();
        if (newData != null) movieList.addAll(newData);
        diff.dispatchUpdatesTo(this);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvTitle, tvMeta, tvBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.ivThumb);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMeta = itemView.findViewById(R.id.tvMeta);
            tvBadge = itemView.findViewById(R.id.tvBadge);
        }
    }
}
