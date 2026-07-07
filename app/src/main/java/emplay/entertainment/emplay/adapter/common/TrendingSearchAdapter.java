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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.database.WatchlistHelper;
import emplay.entertainment.emplay.models.common.MediaItem;
import emplay.entertainment.emplay.models.common.MultiSearchResult;
import emplay.entertainment.emplay.models.movie.MovieModel;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;

public class TrendingSearchAdapter<T extends MediaItem> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ITEM   = 0;
    private static final int VIEW_TYPE_FOOTER = 1;

    public interface OnItemClickListener {
        void onItemClick(MediaItem item, View sharedElement);
    }

    private final Context context;
    private final List<MediaItem> items;
    private final OnItemClickListener listener;
    private final DatabaseHelper dbHelper;
    private final String userId;
    private boolean showFooter = false;

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
        int oldSize = items.size();
        items.clear();
        if (newItems != null) items.addAll(newItems);
        int newSize = items.size();
        if (oldSize > 0) notifyItemRangeRemoved(0, oldSize);
        if (newSize > 0) notifyItemRangeInserted(0, newSize);
    }

    public void appendItems(List<? extends MediaItem> newItems) {
        if (newItems == null || newItems.isEmpty()) return;
        int start = items.size();
        items.addAll(newItems);
        notifyItemRangeInserted(start, newItems.size());
    }

    public int getDataItemCount() {
        return items.size();
    }

    public void setLoading(boolean loading) {
        boolean changed = showFooter != loading;
        showFooter = loading;
        if (changed) {
            if (loading) notifyItemInserted(items.size());
            else notifyItemRemoved(items.size());
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position < items.size() ? VIEW_TYPE_ITEM : VIEW_TYPE_FOOTER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == VIEW_TYPE_FOOTER) {
            return new FooterViewHolder(inflater.inflate(R.layout.item_on_air_footer, parent, false));
        }
        return new ViewHolder(inflater.inflate(R.layout.item_search_popular, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_FOOTER) return;
        ViewHolder h = (ViewHolder) holder;
        MediaItem item = items.get(position);
        h.tvTitle.setText(item.getTitle());
        h.tvRating.setText(String.format(Locale.getDefault(), "★ %.1f", item.getVoteAverage()));

        Glide.with(context)
                .load(ImageUrl.of(ImageUrl.THUMBNAIL, item.getPosterPath()))
                .placeholder(R.drawable.bg_poster_placeholder)
                .into(h.ivPoster);

        h.ivPoster.setTransitionName("poster_" + item.getMediaId());

        // Type chip + year + runtime/countdown
        if (item instanceof MovieModel) {
            MovieModel movie = (MovieModel) item;
            h.tvTypeChip.setText("Movie");
            h.tvYear.setText(extractYear(movie.getReleaseDate()));
            String countdown = formatCountdown(movie.getReleaseDate());
            if (countdown != null && h.tvCountdown != null) {
                h.tvRuntime.setVisibility(View.GONE);
                h.tvCountdown.setText(countdown);
                h.tvCountdown.setVisibility(View.VISIBLE);
            } else {
                if (h.tvCountdown != null) h.tvCountdown.setVisibility(View.GONE);
                int runtime = movie.getRuntime();
                if (runtime > 0) {
                    h.tvRuntime.setVisibility(View.VISIBLE);
                    h.tvRuntime.setText(formatRuntime(runtime));
                } else {
                    h.tvRuntime.setVisibility(View.GONE);
                }
            }
        } else if (item instanceof TVShowModel) {
            TVShowModel tv = (TVShowModel) item;
            h.tvTypeChip.setText("TV");
            h.tvYear.setText(extractYear(tv.getFirstAirDate()));
            String countdown = formatCountdown(tv.getFirstAirDate());
            if (h.tvCountdown != null) {
                if (countdown != null) {
                    h.tvCountdown.setText(countdown);
                    h.tvCountdown.setVisibility(View.VISIBLE);
                } else {
                    h.tvCountdown.setVisibility(View.GONE);
                }
            }
            h.tvRuntime.setVisibility(View.GONE);
        } else if (item instanceof MultiSearchResult) {
            MultiSearchResult multi = (MultiSearchResult) item;
            if ("movie".equals(multi.getMediaType())) {
                h.tvTypeChip.setText("Movie");
            } else if ("tv".equals(multi.getMediaType())) {
                h.tvTypeChip.setText("TV");
            } else {
                h.tvTypeChip.setText("");
            }
            h.tvYear.setText(multi.getDisplayYear());
            h.tvRuntime.setVisibility(View.GONE);
            if (h.tvCountdown != null) h.tvCountdown.setVisibility(View.GONE);
        } else {
            h.tvTypeChip.setText("");
            h.tvYear.setText("");
            h.tvRuntime.setVisibility(View.GONE);
            if (h.tvCountdown != null) h.tvCountdown.setVisibility(View.GONE);
        }

        // My List button
        boolean canSave = dbHelper != null && userId != null && !userId.isEmpty();
        if (canSave) {
            boolean saved = isSaved(item);
            applyMyListState(h.btnMyList, saved);
            h.btnMyList.setOnClickListener(v -> {
                boolean nowSaved = isSaved(item);
                if (nowSaved) {
                    removeFromList(item);
                    applyMyListState(h.btnMyList, false);
                } else {
                    addToList(item);
                    applyMyListState(h.btnMyList, true);
                }
            });
            h.btnMyList.setVisibility(View.VISIBLE);
        } else {
            h.btnMyList.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> listener.onItemClick(item, h.ivPoster));
        h.divider.setVisibility(position == items.size() - 1 ? View.GONE : View.VISIBLE);
    }

    private boolean isSaved(MediaItem item) {
        if (item instanceof MovieModel) {
            return WatchlistHelper.isMovieSaved(dbHelper, userId, item.getMediaId());
        } else if (item instanceof TVShowModel) {
            return WatchlistHelper.isTVShowSaved(dbHelper, userId, item.getMediaId());
        } else if (item instanceof MultiSearchResult) {
            MultiSearchResult multi = (MultiSearchResult) item;
            if ("movie".equals(multi.getMediaType())) {
                return WatchlistHelper.isMovieSaved(dbHelper, userId, multi.getMediaId());
            } else if ("tv".equals(multi.getMediaType())) {
                return WatchlistHelper.isTVShowSaved(dbHelper, userId, multi.getMediaId());
            }
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
        } else if (item instanceof MultiSearchResult) {
            MultiSearchResult multi = (MultiSearchResult) item;
            if ("movie".equals(multi.getMediaType())) {
                WatchlistHelper.saveMovie(dbHelper, userId, multi.getMediaId(),
                        multi.getTitle(), multi.getPosterPath(), "", multi.getVoteAverage());
            } else if ("tv".equals(multi.getMediaType())) {
                WatchlistHelper.saveTVShow(dbHelper, userId, multi.getMediaId(),
                        multi.getTitle(), multi.getPosterPath(), "", multi.getVoteAverage());
            }
        }
    }

    private void removeFromList(MediaItem item) {
        if (item instanceof MovieModel) {
            WatchlistHelper.removeMovie(dbHelper, userId, item.getMediaId());
        } else if (item instanceof TVShowModel) {
            WatchlistHelper.removeTVShow(dbHelper, userId, item.getMediaId());
        } else if (item instanceof MultiSearchResult) {
            MultiSearchResult multi = (MultiSearchResult) item;
            if ("movie".equals(multi.getMediaType())) {
                WatchlistHelper.removeMovie(dbHelper, userId, multi.getMediaId());
            } else if ("tv".equals(multi.getMediaType())) {
                WatchlistHelper.removeTVShow(dbHelper, userId, multi.getMediaId());
            }
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

    private static String formatCountdown(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = sdf.parse(dateStr);
            if (date == null) return null;
            long diffMs = date.getTime() - new Date().getTime();
            if (diffMs <= 0) return null;
            long days = (long) Math.ceil((double) diffMs / (1000L * 60 * 60 * 24));
            if (days == 1) return "· Tomorrow";
            if (days <= 30) return "· in " + days + " days";
            long months = Math.max(1, days / 30);
            return "· in " + months + (months == 1 ? " month" : " months");
        } catch (ParseException ignored) {
            return null;
        }
    }

    private static String formatRuntime(int minutes) {
        if (minutes <= 0) return "";
        int h = minutes / 60;
        int m = minutes % 60;
        return h > 0 ? h + "h " + m + "m" : m + "m";
    }

    @Override
    public int getItemCount() { return items.size() + (showFooter ? 1 : 0); }

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        FooterViewHolder(@NonNull View itemView) { super(itemView); }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        android.widget.ImageView ivPoster;
        TextView tvTitle, tvTypeChip, tvYear, tvRuntime, tvCountdown, tvRating;
        MaterialButton btnMyList;
        View divider;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster    = itemView.findViewById(R.id.ivPoster);
            tvTitle     = itemView.findViewById(R.id.trendNow);
            tvTypeChip  = itemView.findViewById(R.id.tvTypeChip);
            tvYear      = itemView.findViewById(R.id.tvYear);
            tvRuntime   = itemView.findViewById(R.id.tvRuntime);
            tvCountdown = itemView.findViewById(R.id.tvCountdown);
            tvRating    = itemView.findViewById(R.id.meta);
            btnMyList   = itemView.findViewById(R.id.btnMyList);
            divider     = itemView.findViewById(R.id.divider);
        }
    }
}