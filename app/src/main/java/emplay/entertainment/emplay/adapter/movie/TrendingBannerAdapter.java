package emplay.entertainment.emplay.adapter.movie;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.auth.AuthManager;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.database.WatchlistHelper;
import emplay.entertainment.emplay.models.movie.MovieModel;

public class TrendingBannerAdapter extends RecyclerView.Adapter<TrendingBannerAdapter.ViewHolder> {

    private final Context context;
    private final List<MovieModel> movies;
    private final OnItemClickListener listener;
    private final DatabaseHelper dbHelper;

    public interface OnItemClickListener {
        void onItemClick(MovieModel movie);
    }

    public TrendingBannerAdapter(Context context, List<MovieModel> movies,
                                 DatabaseHelper dbHelper, OnItemClickListener listener) {
        this.context = context;
        this.movies = movies;
        this.dbHelper = dbHelper;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trending_banner, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("StringFormatInvalid")
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
        setupWatchlistButton(holder, movie);
    }

    private void setupWatchlistButton(ViewHolder holder, MovieModel movie) {
        AuthManager auth = AuthManager.getInstance(context);
        switch (auth.getAuthType()) {
            case GOOGLE:
                String userId = auth.getUserId();
                updateWatchlistIcon(holder, userId, movie.getMovieId());
                holder.btnHeroWatchlist.setOnClickListener(v -> {
                    if (WatchlistHelper.isMovieSaved(dbHelper, userId, movie.getMovieId())) {
                        WatchlistHelper.removeMovie(dbHelper, userId, movie.getMovieId());
                        updateWatchlistIcon(holder, userId, movie.getMovieId());
                        Toast.makeText(context, "Movie removed from library", Toast.LENGTH_SHORT).show();
                    } else {
                        String genres = (movie.getGenres() != null && !movie.getGenres().isEmpty())
                                ? String.join(",", movie.getGenres()) : "";
                        long result = WatchlistHelper.saveMovie(dbHelper, userId, movie.getMovieId(),
                                movie.getTitle(), movie.getPosterPath(), genres, movie.getVoteAverage());
                        if (result != -1) {
                            updateWatchlistIcon(holder, userId, movie.getMovieId());
                            Toast.makeText(context, "Movie added to library", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to add Movie", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;

            case TMDB:
                // Banner doesn't support per-item account_states fetch; direct to detail page.
                holder.btnHeroWatchlist.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_watchlist));
                holder.btnHeroWatchlist.setOnClickListener(v -> {
                    Toast.makeText(context, "Open movie details to save to your TMDB watchlist", Toast.LENGTH_SHORT).show();
                    listener.onItemClick(movie);
                });
                break;

            default:
                holder.btnHeroWatchlist.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_watchlist));
                holder.btnHeroWatchlist.setOnClickListener(v ->
                        Toast.makeText(context, "Sign in to save movies", Toast.LENGTH_SHORT).show());
                break;
        }
    }

    private void updateWatchlistIcon(ViewHolder holder, String userId, int movieId) {
        int iconRes = WatchlistHelper.isMovieSaved(dbHelper, userId, movieId)
                ? R.drawable.ic_check : R.drawable.ic_watchlist;
        holder.btnHeroWatchlist.setIcon(ContextCompat.getDrawable(context, iconRes));
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
        MaterialButton btnHeroWatchlist;
        Button btnHeroMoreInfo;

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
