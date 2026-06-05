package emplay.entertainment.emplay.adapter.movie;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.ApiClient;
import emplay.entertainment.emplay.api.auth.TMDBWatchlistApiService;
import emplay.entertainment.emplay.api.auth.model.TMDBAccountStatesResponse;
import emplay.entertainment.emplay.api.auth.model.TMDBWatchlistRequest;
import emplay.entertainment.emplay.api.auth.model.TMDBWatchlistStatusResponse;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.auth.AuthManager;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.database.WatchlistHelper;
import emplay.entertainment.emplay.models.movie.MovieModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrendingBannerAdapter extends RecyclerView.Adapter<TrendingBannerAdapter.ViewHolder> {

    private final Context context;
    private final List<MovieModel> movies;
    private final OnItemClickListener listener;
    private final DatabaseHelper dbHelper;
    private final TMDBWatchlistApiService watchlistApiService;

    @Nullable
    private OnPaletteColorListener paletteColorListener;

    public interface OnItemClickListener {
        void onItemClick(MovieModel movie);
    }

    public interface OnPaletteColorListener {
        void onColorReady(int position, int color);
    }

    public void setPaletteColorListener(@Nullable OnPaletteColorListener listener) {
        this.paletteColorListener = listener;
    }

    public TrendingBannerAdapter(Context context, List<MovieModel> movies,
                                 DatabaseHelper dbHelper, OnItemClickListener listener) {
        this.context = context;
        this.movies = movies;
        this.dbHelper = dbHelper;
        this.listener = listener;
        this.watchlistApiService = ApiClient.getClient().create(TMDBWatchlistApiService.class);
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

        String posterUrl = ImageUrl.POSTER + movie.getPosterPath();
        if (paletteColorListener != null) {
            Glide.with(context)
                    .asBitmap()
                    .load(posterUrl)
                    .placeholder(R.drawable.bg_poster_placeholder)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap bitmap,
                                                    @Nullable Transition<? super Bitmap> transition) {
                            holder.ivHeroPoster.setImageBitmap(bitmap);
                            Palette.from(bitmap).generate(palette -> {
                                if (palette == null || paletteColorListener == null) return;
                                int fallback = 0xFF0D0D0D;
                                int dominant = palette.getDarkMutedColor(
                                        palette.getDarkVibrantColor(fallback));
                                float[] hsv = new float[3];
                                Color.colorToHSV(dominant, hsv);
                                hsv[2] *= 0.55f;
                                paletteColorListener.onColorReady(position, Color.HSVToColor(hsv));
                            });
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            holder.ivHeroPoster.setImageDrawable(placeholder);
                        }
                    });
        } else {
            Glide.with(context)
                    .load(posterUrl)
                    .placeholder(R.drawable.bg_poster_placeholder)
                    .into(holder.ivHeroPoster);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(movie));
        setupWatchlistButton(holder, movie);
    }

    @Nullable
    public MovieModel getMovie(int position) {
        if (position < 0 || position >= movies.size()) return null;
        return movies.get(position);
    }

    private void setupWatchlistButton(ViewHolder holder, MovieModel movie) {
        holder.btnHeroWatchlist.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });
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
                setupTmdbWatchlistButton(holder, auth, movie);
                break;

            default:
                holder.icHeroWatchlist.setImageResource(R.drawable.ic_watchlist);
                holder.btnHeroWatchlist.setOnClickListener(v ->
                        Toast.makeText(context, "Sign in to save movies", Toast.LENGTH_SHORT).show());
                break;
        }
    }

    private void setupTmdbWatchlistButton(ViewHolder holder, AuthManager auth, MovieModel movie) {
        holder.icHeroWatchlist.setImageResource(R.drawable.ic_watchlist);
        holder.btnHeroWatchlist.setEnabled(false);

        watchlistApiService.getAccountStates(
                TMDBpath.movieAccountStates(movie.getMovieId()), auth.getTMDBSessionId())
                .enqueue(new Callback<TMDBAccountStatesResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TMDBAccountStatesResponse> call,
                                           @NonNull Response<TMDBAccountStatesResponse> response) {
                        boolean inWatchlist = response.isSuccessful()
                                && response.body() != null && response.body().watchlist;
                        applyTmdbState(holder, auth, movie, inWatchlist);
                    }

                    @Override
                    public void onFailure(@NonNull Call<TMDBAccountStatesResponse> call, @NonNull Throwable t) {
                        applyTmdbState(holder, auth, movie, false);
                    }
                });
    }

    private void applyTmdbState(ViewHolder holder, AuthManager auth, MovieModel movie, boolean inWatchlist) {
        holder.icHeroWatchlist.setImageResource(inWatchlist ? R.drawable.ic_check : R.drawable.ic_watchlist);
        holder.btnHeroWatchlist.setEnabled(true);
        holder.btnHeroWatchlist.setOnClickListener(v -> toggleTmdbWatchlist(holder, auth, movie, inWatchlist));
    }

    private void toggleTmdbWatchlist(ViewHolder holder, AuthManager auth, MovieModel movie, boolean currentlyInWatchlist) {
        holder.btnHeroWatchlist.setEnabled(false);
        boolean addToWatchlist = !currentlyInWatchlist;

        watchlistApiService.updateWatchlist(
                TMDBpath.accountAddToWatchlist(auth.getTMDBAccountId()),
                auth.getTMDBSessionId(),
                new TMDBWatchlistRequest("movie", movie.getMovieId(), addToWatchlist))
                .enqueue(new Callback<TMDBWatchlistStatusResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TMDBWatchlistStatusResponse> call,
                                           @NonNull Response<TMDBWatchlistStatusResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(context,
                                    addToWatchlist ? "Added to TMDB watchlist" : "Removed from TMDB watchlist",
                                    Toast.LENGTH_SHORT).show();
                            applyTmdbState(holder, auth, movie, addToWatchlist);
                        } else {
                            Toast.makeText(context, "Failed to update watchlist", Toast.LENGTH_SHORT).show();
                            applyTmdbState(holder, auth, movie, currentlyInWatchlist);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<TMDBWatchlistStatusResponse> call, @NonNull Throwable t) {
                        Toast.makeText(context, "Failed to update watchlist", Toast.LENGTH_SHORT).show();
                        applyTmdbState(holder, auth, movie, currentlyInWatchlist);
                    }
                });
    }

    private void updateWatchlistIcon(ViewHolder holder, String userId, int movieId) {
        int iconRes = WatchlistHelper.isMovieSaved(dbHelper, userId, movieId)
                ? R.drawable.ic_check : R.drawable.ic_watchlist;
        holder.icHeroWatchlist.setImageResource(iconRes);
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
        LinearLayout btnHeroWatchlist;
        ImageView icHeroWatchlist;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivHeroPoster = itemView.findViewById(R.id.ivHeroPoster);
            btnHeroWatchlist = itemView.findViewById(R.id.btnHeroWatchlist);
            icHeroWatchlist = itemView.findViewById(R.id.icHeroWatchlist);
        }
    }
}