package emplay.entertainment.emplay.adapter.movie;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.view.ViewCompat;
import android.widget.TextView;
import com.google.android.material.imageview.ShapeableImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.models.movie.MovieModel;

public class TrendingBannerAdapter extends RecyclerView.Adapter<TrendingBannerAdapter.ViewHolder> {
    private static final Map<Integer, String> GENRE_NAMES = new HashMap<>();
    static {
        GENRE_NAMES.put(28,    "Action");
        GENRE_NAMES.put(12,    "Adventure");
        GENRE_NAMES.put(16,    "Animation");
        GENRE_NAMES.put(35,    "Comedy");
        GENRE_NAMES.put(80,    "Crime");
        GENRE_NAMES.put(99,    "Documentary");
        GENRE_NAMES.put(18,    "Drama");
        GENRE_NAMES.put(10751, "Family");
        GENRE_NAMES.put(14,    "Fantasy");
        GENRE_NAMES.put(36,    "History");
        GENRE_NAMES.put(27,    "Horror");
        GENRE_NAMES.put(10402, "Music");
        GENRE_NAMES.put(9648,  "Mystery");
        GENRE_NAMES.put(10749, "Romance");
        GENRE_NAMES.put(878,   "Sci-Fi");
        GENRE_NAMES.put(10770, "TV Movie");
        GENRE_NAMES.put(53,    "Thriller");
        GENRE_NAMES.put(10752, "War");
        GENRE_NAMES.put(37,    "Western");
    }

    private final Context context;
    private final List<MovieModel> movies;
    private final CardGestureListener gestureListener;

    public interface CardGestureListener {
        void onSingleTap(MovieModel movie);
    }

    public TrendingBannerAdapter(Context context, List<MovieModel> movies,
                                 CardGestureListener gestureListener) {
        this.context = context;
        this.movies = movies;
        this.gestureListener = gestureListener;
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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MovieModel movie = movies.get(position);
        holder.boundMovie = movie;

        // Poster image
        Glide.with(context)
                .load(ImageUrl.of(ImageUrl.BACKDROP, movie.getBackdropPath()))
                .placeholder(R.drawable.bg_poster_placeholder)
                .into(holder.ivHeroPoster);

        // Title
        holder.tvHeroTitle.setText(movie.getTitle());

        // Meta (Genres · Certification)
        String genreStr = resolveGenres(movie);
        String cert = movie.getCertification();
        if (cert != null && !cert.isEmpty()) {
            genreStr += " · " + cert;
        }
        holder.tvHeroMeta.setText(genreStr);

        // Rating
        holder.tvHeroRating.setText(String.format(Locale.getDefault(), "%.1f", movie.getVoteAverage()));

    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    private static String resolveGenres(MovieModel movie) {
        List<String> names = movie.getGenres();
        if (names != null && !names.isEmpty()) {
            StringBuilder sb = new StringBuilder(names.get(0));
            if (names.size() > 1) sb.append(" · ").append(names.get(1));
            return sb.toString();
        }
        List<Integer> ids = movie.getGenreIds();
        if (ids != null && !ids.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            int added = 0;
            for (int id : ids) {
                String name = GENRE_NAMES.get(id);
                if (name != null) {
                    if (added > 0) sb.append(" · ");
                    sb.append(name);
                    if (++added == 2) break;
                }
            }
            if (sb.length() > 0) return sb.toString();
        }
        return "Movie";
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivHeroPoster;
        TextView tvHeroTitle;
        TextView tvHeroMeta;
        TextView tvHeroRating;
        MovieModel boundMovie;
        final GestureDetector gestureDetector;

        @SuppressLint("ClickableViewAccessibility")
        public ViewHolder(@NonNull View itemView, Context context,
                          CardGestureListener gestureListener) {
            super(itemView);
            ivHeroPoster   = itemView.findViewById(R.id.ivHeroPoster);
            tvHeroTitle    = itemView.findViewById(R.id.tvHeroTitle);
            tvHeroMeta     = itemView.findViewById(R.id.tvHeroMeta);
            tvHeroRating   = itemView.findViewById(R.id.tvHeroRating);

            gestureDetector = new GestureDetector(context,
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onDown(@NonNull MotionEvent e) { return true; }

                        @Override
                        public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                            if (boundMovie != null) gestureListener.onSingleTap(boundMovie);
                            return true;
                        }
                    });

            itemView.setClickable(true);
            itemView.setOnTouchListener((v, event) -> {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(120).start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
                        break;
                }
                boolean handled = gestureDetector.onTouchEvent(event);
                return handled || event.getActionMasked() == MotionEvent.ACTION_DOWN;
            });
        }
    }
}
