package emplay.entertainment.emplay.adapter.movie;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private final DatabaseHelper dbHelper;
    private final TMDBWatchlistApiService watchlistApiService;
    private final CardGestureListener gestureListener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    @Nullable private OnPaletteColorListener paletteColorListener;
    @Nullable private RecyclerView attachedRecyclerView;

    // Callback interface for palette color extraction per banner position
    public interface OnPaletteColorListener {
        void onColorReady(int position, int color);
    }

    public void setPaletteColorListener(@Nullable OnPaletteColorListener listener) {
        this.paletteColorListener = listener;
    }

    public interface CardGestureListener {
        void onSingleTap(MovieModel movie);
        void onDoubleTap(MovieModel movie);
    }

    public TrendingBannerAdapter(Context context, List<MovieModel> movies,
                                 DatabaseHelper dbHelper,
                                 CardGestureListener gestureListener) {
        this.context = context;
        this.movies = movies;
        this.dbHelper = dbHelper;
        this.gestureListener = gestureListener;
        this.watchlistApiService = ApiClient.getClient().create(TMDBWatchlistApiService.class);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trending_banner, parent, false);
        return new ViewHolder(view, context, gestureListener);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MovieModel movie = movies.get(position);

        // Update bound movie — gesture detector picks this up automatically
        holder.boundMovie = movie;

        String posterUrl = ImageUrl.POSTER + movie.getPosterPath();

        if (paletteColorListener != null) {
            // Load as Bitmap so we can run Palette color extraction on it
            Glide.with(context)
                    .asBitmap()
                    .load(posterUrl)
                    .placeholder(R.drawable.bg_poster_placeholder)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap bitmap,
                                                    @Nullable Transition<? super Bitmap> transition) {
                            holder.ivHeroPoster.setImageBitmap(bitmap);

                            // Extract dominant dark color from poster for animated background
                            Palette.from(bitmap).generate(palette -> {
                                if (palette == null || paletteColorListener == null) return;

                                // Use current adapter position instead of the captured lambda
                                // position — avoids reporting wrong color if holder was rebound
                                // while Glide/Palette was still running
                                int adapterPos = holder.getBindingAdapterPosition();
                                if (adapterPos == RecyclerView.NO_POSITION) return;

                                int fallback = 0xFF0D0D0D;
                                // Prefer dark muted, fall back to dark vibrant, then hard fallback
                                int dominant = palette.getDarkMutedColor(
                                        palette.getDarkVibrantColor(fallback));

                                // Darken by reducing brightness in HSV space
                                float[] hsv = new float[3];
                                Color.colorToHSV(dominant, hsv);
                                hsv[2] *= 0.55f;
                                paletteColorListener.onColorReady(adapterPos, Color.HSVToColor(hsv));
                            });
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            holder.ivHeroPoster.setImageDrawable(placeholder);
                        }
                    });
        } else {
            // No palette needed — load directly into ImageView
            Glide.with(context)
                    .load(posterUrl)
                    .placeholder(R.drawable.bg_poster_placeholder)
                    .into(holder.ivHeroPoster);
        }


        // Watchlist button (top-right corner of card) — single tap, same action as double tap
        holder.btnHeroWatchlist.setOnClickListener(v -> doubleTapWatchlist(holder, movie));

        // Load current watchlist state and update icon accordingly
        loadWatchlistState(holder, movie);
    }

    @Nullable
    public MovieModel getMovie(int position) {
        if (position < 0 || position >= movies.size()) return null;
        return movies.get(position);
    }

    // Reads watchlist state from local DB (Google) or TMDB API and sets the correct icon
    private void loadWatchlistState(ViewHolder holder, MovieModel movie) {
        AuthManager auth = AuthManager.getInstance(context);
        switch (auth.getAuthType()) {
            case GOOGLE: {
                String userId = auth.getUserId();
                int movieId = movie.getMovieId();
                new Thread(() -> {
                    boolean saved = WatchlistHelper.isMovieSaved(dbHelper, userId, movieId);
                    mainHandler.post(() -> {
                        if (holder.getBindingAdapterPosition() != RecyclerView.NO_POSITION) {
                            holder.icHeroWatchlist.setImageResource(
                                    saved ? R.drawable.ic_check : R.drawable.ic_watchlist);
                            holder.icHeroWatchlist.setTag(saved);
                        }
                    });
                }).start();
                break;
            }

            case TMDB:
                // Snapshot the ImageView reference before the async call.
                // The holder may be recycled by the time the network response arrives,
                // so we check getAdapterPosition() == NO_POSITION inside the callback
                // to avoid updating the wrong card's icon
                ImageView iconRef = holder.icHeroWatchlist;
                iconRef.setImageResource(R.drawable.ic_watchlist);
                watchlistApiService.getAccountStates(
                                TMDBpath.movieAccountStates(movie.getMovieId()), auth.getTMDBSessionId())
                        .enqueue(new Callback<TMDBAccountStatesResponse>() {
                            @Override
                            public void onResponse(@NonNull Call<TMDBAccountStatesResponse> call,
                                                   @NonNull Response<TMDBAccountStatesResponse> response) {
                                // Guard: skip update if holder has been recycled
                                if (holder.getBindingAdapterPosition() == RecyclerView.NO_POSITION) return;
                                boolean in = response.isSuccessful()
                                        && response.body() != null && response.body().watchlist;
                                iconRef.setImageResource(in ? R.drawable.ic_check : R.drawable.ic_watchlist);
                                // Store state as tag so doubleTapWatchlist can read it without another API call
                                iconRef.setTag(in);
                            }
                            @Override
                            public void onFailure(@NonNull Call<TMDBAccountStatesResponse> call,
                                                  @NonNull Throwable t) {}
                        });
                break;

            default:
                holder.icHeroWatchlist.setImageResource(R.drawable.ic_watchlist);
                break;
        }
    }

    // Handles watchlist add/remove for both Google and TMDB auth types
    private void doubleTapWatchlist(ViewHolder holder, MovieModel movie) {
        AuthManager auth = AuthManager.getInstance(context);
        switch (auth.getAuthType()) {

            case GOOGLE: {
                String userId = auth.getUserId();
                String genres = (movie.getGenres() != null && !movie.getGenres().isEmpty())
                        ? String.join(",", movie.getGenres()) : "";
                new Thread(() -> {
                    boolean wasSaved = WatchlistHelper.isMovieSaved(dbHelper, userId, movie.getMovieId());
                    String msg;
                    if (wasSaved) {
                        WatchlistHelper.removeMovie(dbHelper, userId, movie.getMovieId());
                        msg = "Removed from library";
                    } else {
                        long result = WatchlistHelper.saveMovie(dbHelper, userId, movie.getMovieId(),
                                movie.getTitle(), movie.getPosterPath(), genres, movie.getVoteAverage());
                        msg = result != -1 ? "Added to library" : "Failed to add";
                    }
                    boolean nowSaved = WatchlistHelper.isMovieSaved(dbHelper, userId, movie.getMovieId());
                    String finalMsg = msg;
                    mainHandler.post(() -> {
                        if (holder.getBindingAdapterPosition() != RecyclerView.NO_POSITION) {
                            holder.icHeroWatchlist.setImageResource(
                                    nowSaved ? R.drawable.ic_check : R.drawable.ic_watchlist);
                            holder.icHeroWatchlist.setTag(nowSaved);
                        }
                        Toast.makeText(context, finalMsg, Toast.LENGTH_SHORT).show();
                    });
                }).start();
                break;
            }

            case TMDB: {
                // Snapshot view references before async call — holder may be recycled
                // by the time the Retrofit callback fires
                ImageView iconRef = holder.icHeroWatchlist;
                LinearLayout btnRef = holder.btnHeroWatchlist;

                // Disable button to prevent duplicate requests while call is in flight
                btnRef.setEnabled(false);

                // Read current state from tag set by loadWatchlistState
                boolean currentlyIn = iconRef.getTag() != null && (boolean) iconRef.getTag();
                boolean addToWatchlist = !currentlyIn;

                watchlistApiService.updateWatchlist(
                                TMDBpath.accountAddToWatchlist(auth.getTMDBAccountId()),
                                auth.getTMDBSessionId(),
                                new TMDBWatchlistRequest("movie", movie.getMovieId(), addToWatchlist))
                        .enqueue(new Callback<TMDBWatchlistStatusResponse>() {
                            @Override
                            public void onResponse(@NonNull Call<TMDBWatchlistStatusResponse> call,
                                                   @NonNull Response<TMDBWatchlistStatusResponse> response) {
                                // Guard: skip update if holder has been recycled
                                if (holder.getBindingAdapterPosition() == RecyclerView.NO_POSITION) return;
                                boolean ok = response.isSuccessful() && response.body() != null;
                                // On failure, revert to previous state
                                boolean newState = ok ? addToWatchlist : currentlyIn;
                                iconRef.setImageResource(newState ? R.drawable.ic_check : R.drawable.ic_watchlist);
                                iconRef.setTag(newState);
                                btnRef.setEnabled(true);
                                Toast.makeText(context,
                                        ok ? (addToWatchlist ? "Added to TMDB watchlist"
                                                : "Removed from TMDB watchlist")
                                                : "Failed to update watchlist",
                                        Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onFailure(@NonNull Call<TMDBWatchlistStatusResponse> call,
                                                  @NonNull Throwable t) {
                                if (holder.getBindingAdapterPosition() == RecyclerView.NO_POSITION) return;
                                btnRef.setEnabled(true);
                                Toast.makeText(context, "Failed to update watchlist",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                break;
            }

            default:
                Toast.makeText(context, "Sign in to save movies", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public void updateData(List<MovieModel> newMovies) {
        List<MovieModel> oldMovies = new ArrayList<>(movies);
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return oldMovies.size(); }
            @Override public int getNewListSize() { return newMovies != null ? newMovies.size() : 0; }
            @Override public boolean areItemsTheSame(int o, int n) {
                return oldMovies.get(o).getMovieId() == newMovies.get(n).getMovieId();
            }
            @Override public boolean areContentsTheSame(int o, int n) {
                MovieModel a = oldMovies.get(o), b = newMovies.get(n);
                return Objects.equals(a.getPosterPath(), b.getPosterPath())
                        && Objects.equals(a.getTitle(), b.getTitle());
            }
        });
        movies.clear();
        if (newMovies != null) movies.addAll(newMovies);
        diff.dispatchUpdatesTo(this);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.attachedRecyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.attachedRecyclerView = null;
    }

    public void triggerWatchlist(MovieModel movie) {
        if (attachedRecyclerView == null) return;
        for (int i = 0; i < movies.size(); i++) {
            if (movies.get(i).getMovieId() == movie.getMovieId()) {
                RecyclerView.ViewHolder vh = attachedRecyclerView.findViewHolderForAdapterPosition(i);
                if (vh instanceof ViewHolder) {
                    doubleTapWatchlist((ViewHolder) vh, movie);
                }
                return;
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivHeroPoster;
        LinearLayout btnHeroWatchlist;
        ImageView icHeroWatchlist;

        // Mutable movie reference — updated each bind
        MovieModel boundMovie;
        final GestureDetector gestureDetector;

        @SuppressLint("ClickableViewAccessibility")
        public ViewHolder(@NonNull View itemView, Context context,
                          CardGestureListener gestureListener) {
            super(itemView);
            ivHeroPoster     = itemView.findViewById(R.id.ivHeroPoster);
            btnHeroWatchlist = itemView.findViewById(R.id.btnHeroWatchlist);
            icHeroWatchlist  = itemView.findViewById(R.id.icHeroWatchlist);

            gestureDetector = new GestureDetector(context,
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onDown(@NonNull MotionEvent e) { return true; }

                        @Override
                        public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                            if (boundMovie != null) gestureListener.onSingleTap(boundMovie);
                            return true;
                        }

                        @Override
                        public boolean onDoubleTap(@NonNull MotionEvent e) {
                            if (boundMovie != null) gestureListener.onDoubleTap(boundMovie);
                            return true;
                        }
                    });

            itemView.setClickable(true);
            itemView.setOnTouchListener((v, event) -> {
                boolean handled = gestureDetector.onTouchEvent(event);
                return handled || event.getActionMasked() == MotionEvent.ACTION_DOWN;
            });
        }
    }
}