package emplay.entertainment.emplay.adapter.common;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.database.WatchlistHelper;
import emplay.entertainment.emplay.models.common.MediaItem;
import emplay.entertainment.emplay.models.movie.MovieModel;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;

public class TrendingSearchAdapter<T extends MediaItem> extends RecyclerView.Adapter<TrendingSearchAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(MediaItem item, View sharedElement);
    }

    private final Context context;
    private final List<MediaItem> items;
    private final OnItemClickListener listener;
    private final DatabaseHelper dbHelper;
    private final String userId;

    public TrendingSearchAdapter(Context context, List<? extends MediaItem> items,
                                  OnItemClickListener listener) {
        this(context, items, listener, null, null);
    }

    public TrendingSearchAdapter(Context context, List<? extends MediaItem> items,
                                  OnItemClickListener listener,
                                  DatabaseHelper dbHelper, String userId) {
        this.context  = context;
        this.items    = new ArrayList<>(items);
        this.listener = listener;
        this.dbHelper = dbHelper;
        this.userId   = userId;
    }

    public void updateData(List<? extends MediaItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_trending, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediaItem item = items.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvRating.setText(String.format(Locale.getDefault(), "★ %.1f", item.getVoteAverage()));

        Glide.with(context)
                .load(ImageUrl.of(ImageUrl.THUMBNAIL, item.getPosterPath()))
                .placeholder(R.drawable.bg_poster_placeholder)
                .into(holder.ivPoster);

        holder.ivPoster.setTransitionName("poster_" + item.getMediaId());

        // Type chip + year + runtime
        if (item instanceof MovieModel) {
            MovieModel movie = (MovieModel) item;
            holder.tvTypeChip.setText("Movie");
            holder.tvYear.setText(extractYear(movie.getReleaseDate()));
            int runtime = movie.getRuntime();
            if (runtime > 0) {
                holder.tvRuntime.setVisibility(View.VISIBLE);
                holder.tvRuntime.setText(formatRuntime(runtime));
            } else {
                holder.tvRuntime.setVisibility(View.GONE);
            }
        } else if (item instanceof TVShowModel) {
            TVShowModel tv = (TVShowModel) item;
            holder.tvTypeChip.setText("TV");
            holder.tvYear.setText(extractYear(tv.getFirstAirDate()));
            holder.tvRuntime.setVisibility(View.GONE);
        } else {
            holder.tvTypeChip.setText("");
            holder.tvYear.setText("");
            holder.tvRuntime.setVisibility(View.GONE);
        }

        // My List button
        boolean canSave = dbHelper != null && userId != null && !userId.isEmpty();
        if (canSave) {
            boolean saved = isSaved(item);
            applyMyListState(holder.btnMyList, saved);
            holder.btnMyList.setOnClickListener(v -> {
                boolean nowSaved = isSaved(item);
                if (nowSaved) {
                    removeFromList(item);
                    applyMyListState(holder.btnMyList, false);
                } else {
                    addToList(item);
                    applyMyListState(holder.btnMyList, true);
                }
            });
            holder.btnMyList.setVisibility(View.VISIBLE);
        } else {
            holder.btnMyList.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item, holder.ivPoster));
        holder.divider.setVisibility(position == getItemCount() - 1 ? View.GONE : View.VISIBLE);
    }

    private boolean isSaved(MediaItem item) {
        if (item instanceof MovieModel) {
            return WatchlistHelper.isMovieSaved(dbHelper, userId, item.getMediaId());
        } else if (item instanceof TVShowModel) {
            return WatchlistHelper.isTVShowSaved(dbHelper, userId, item.getMediaId());
        }
        return false;
    }

    private void addToList(MediaItem item) {
        if (item instanceof MovieModel) {
            MovieModel m = (MovieModel) item;
            WatchlistHelper.saveMovie(dbHelper, userId, m.getMovieId(),
                    m.getTitle(), m.getPosterPath(), "", m.getVoteAverage());
        } else if (item instanceof TVShowModel) {
            TVShowModel t = (TVShowModel) item;
            WatchlistHelper.saveTVShow(dbHelper, userId, t.getTVShowId(),
                    t.getTitle(), t.getPosterPath(), "", t.getVoteAverage());
        }
    }

    private void removeFromList(MediaItem item) {
        if (item instanceof MovieModel) {
            WatchlistHelper.removeMovie(dbHelper, userId, item.getMediaId());
        } else if (item instanceof TVShowModel) {
            WatchlistHelper.removeTVShow(dbHelper, userId, item.getMediaId());
        }
    }

    private void applyMyListState(MaterialButton btn, boolean saved) {
        if (saved) {
            btn.setText(R.string.action_in_list);
            btn.setBackgroundTintList(ColorStateList.valueOf(0xFFE3B566));
            btn.setTextColor(0xFF3A2A0C);
            btn.setStrokeWidth(0);
        } else {
            btn.setText(R.string.action_my_list);
            btn.setBackgroundTintList(ColorStateList.valueOf(android.graphics.Color.TRANSPARENT));
            btn.setTextColor(0xFFE3B566);
            btn.setStrokeWidth(context.getResources().getDimensionPixelSize(R.dimen.hairline) * 2);
        }
    }

    private static String extractYear(String date) {
        if (date == null || date.length() < 4) return "";
        return date.substring(0, 4);
    }

    private static String formatRuntime(int minutes) {
        if (minutes <= 0) return "";
        int h = minutes / 60;
        int m = minutes % 60;
        return h > 0 ? h + "h " + m + "m" : m + "m";
    }

    @Override
    public int getItemCount() { return items.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        android.widget.ImageView ivPoster;
        TextView tvTitle, tvTypeChip, tvYear, tvRuntime, tvRating;
        MaterialButton btnMyList;
        View divider;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster    = itemView.findViewById(R.id.ivPoster);
            tvTitle     = itemView.findViewById(R.id.trendNow);
            tvTypeChip  = itemView.findViewById(R.id.tvTypeChip);
            tvYear      = itemView.findViewById(R.id.tvYear);
            tvRuntime   = itemView.findViewById(R.id.tvRuntime);
            tvRating    = itemView.findViewById(R.id.meta);
            btnMyList   = itemView.findViewById(R.id.btnMyList);
            divider     = itemView.findViewById(R.id.divider);
        }
    }
}