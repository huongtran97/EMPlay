package emplay.entertainment.emplay.adapter.movie;

import android.annotation.SuppressLint;
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
import java.util.Locale;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.models.movie.MovieModel;
import emplay.entertainment.emplay.tool.BadgeHelper;

public class  WhatsNewMovieAdapter extends RecyclerView.Adapter<WhatsNewMovieAdapter.ViewHolder> {

    private final Context context;
    private final List<MovieModel> movieList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(MovieModel movie);
    }

    public WhatsNewMovieAdapter(Context context, List<MovieModel> movieList, OnItemClickListener listener) {
        this.context = context;
        this.movieList = movieList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_whats_new_movie, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MovieModel movie = movieList.get(position);
        holder.tvTitle.setText(movie.getTitle());
        
        String releaseDateStr = movie.getReleaseDate();
        String year = (releaseDateStr != null && releaseDateStr.length() >= 4)
                ? releaseDateStr.substring(0, 4) : "";
        String lang = movie.getOriginalLanguage() != null
                ? movie.getOriginalLanguage().toUpperCase(Locale.ROOT) : "";
        String rel = BadgeHelper.formatRelativeDate(releaseDateStr);
        String meta = year + " · " + lang;
        if (!rel.isEmpty()) meta += " · " + rel;
        holder.tvMeta.setText(meta);

        BadgeHelper.applyWhatsNewBadge(holder.tvBadge, releaseDateStr, 14);

        Glide.with(context)
                .load(ImageUrl.THUMBNAIL + movie.getPosterPath())
                .placeholder(R.drawable.bg_poster_placeholder)
                .into(holder.ivThumb);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(movie));
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<MovieModel> newList) {
        movieList.clear();
        movieList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvTitle, tvMeta, tvBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.ivMovieThumb);
            tvTitle = itemView.findViewById(R.id.tvMovieTitle);
            tvMeta = itemView.findViewById(R.id.tvMovieMeta);
            tvBadge = itemView.findViewById(R.id.tvMovieBadge);
        }
    }
}
