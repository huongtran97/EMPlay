package emplay.entertainment.emplay.adapter.movie;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class TrendingBannerAdapter extends RecyclerView.Adapter<TrendingBannerAdapter.ViewHolder> {

    private final Context context;
    private final List<MovieModel> movies;
    private final OnItemClickListener listener;
    private final OnWatchlistClickListener watchlistListener;

    public interface OnItemClickListener {
        void onItemClick(MovieModel movie);
    }

    public interface OnWatchlistClickListener {
        void onWatchlistClick();
    }

    public TrendingBannerAdapter(Context context, List<MovieModel> movies, 
                                 OnItemClickListener listener, 
                                 OnWatchlistClickListener watchlistListener) {
        this.context = context;
        this.movies = movies;
        this.listener = listener;
        this.watchlistListener = watchlistListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trending_banner, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MovieModel movie = movies.get(position);

        holder.tvHeroTitle.setText(movie.getTitle());
        holder.tvHeroRating.setText(
                holder.itemView.getContext().getString(R.string.rating_format, movie.getVoteAverage())
        );

        if (movie.getReleaseDate() != null && movie.getReleaseDate().length() >= 4) {
            holder.tvHeroYear.setText(movie.getReleaseDate().substring(0, 4));
        }
        holder.tvHeroGenre.setText(movie.getOriginalLanguage().toUpperCase(Locale.ROOT));

        Glide.with(context)
                .load(ImageUrl.BACKDROP + movie.getBackdropPath())
                .placeholder(R.drawable.bg_poster_placeholder)
                .into(holder.ivHeroPoster);

        holder.btnHeroMoreInfo.setOnClickListener(v -> listener.onItemClick(movie));
        holder.btnHeroWatchlist.setOnClickListener(v -> watchlistListener.onWatchlistClick());
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<MovieModel> newMovies) {
        movies.clear();
        movies.addAll(newMovies);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivHeroPoster;
        TextView tvHeroTitle, tvHeroYear, tvHeroGenre, tvHeroRating, tvHeroBadge;
        Button btnHeroWatchlist, btnHeroMoreInfo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivHeroPoster = itemView.findViewById(R.id.ivHeroPoster);
            tvHeroTitle = itemView.findViewById(R.id.tvHeroTitle);
            tvHeroYear = itemView.findViewById(R.id.tvHeroYear);
            tvHeroGenre = itemView.findViewById(R.id.tvHeroGenre);
            tvHeroRating = itemView.findViewById(R.id.tvHeroRating);
            tvHeroBadge = itemView.findViewById(R.id.tvHeroBadge);
            btnHeroWatchlist = itemView.findViewById(R.id.btnHeroWatchlist);
            btnHeroMoreInfo = itemView.findViewById(R.id.btnHeroMoreInfo);
        }
    }
}
