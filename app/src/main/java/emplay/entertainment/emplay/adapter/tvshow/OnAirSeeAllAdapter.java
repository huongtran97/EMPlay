package emplay.entertainment.emplay.adapter.tvshow;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.database.WatchlistHelper;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;

public class OnAirSeeAllAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ITEM   = 0;
    private static final int VIEW_TYPE_FOOTER = 1;

    public interface OnClickListener {
        void onClick(TVShowModel tv, View sharedElement);
    }

    private final Context context;
    private final List<TVShowModel> items;
    private final OnClickListener listener;
    private final DatabaseHelper dbHelper;
    private final String userId;
    private final Runnable onGuestMyListClick;
    private boolean showFooter = false;

    public OnAirSeeAllAdapter(Context context, List<TVShowModel> items, OnClickListener listener,
                               DatabaseHelper dbHelper, String userId, Runnable onGuestMyListClick) {
        this.context = context;
        this.items = items;
        this.listener = listener;
        this.dbHelper = dbHelper;
        this.userId = userId;
        this.onGuestMyListClick = onGuestMyListClick;
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
        return new ViewHolder(inflater.inflate(R.layout.item_on_air_seeall, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_FOOTER) return;
        ViewHolder h = (ViewHolder) holder;
        TVShowModel tv = items.get(position);

        h.tvTitle.setText(tv.getName());

        Glide.with(context)
                .load(ImageUrl.of(ImageUrl.THUMBNAIL, tv.getPosterPath()))
                .placeholder(R.drawable.bg_poster_placeholder)
                .into(h.ivPoster);
        h.ivPoster.setTransitionName("poster_" + tv.getTVShowId());

        int epNum = tv.getOnAirEpisodeNumber();
        if (epNum > 0) {
            h.tvBadge.setVisibility(View.VISIBLE);
            if (epNum == 1) {
                h.tvBadge.setText(R.string.badge_new_season);
                h.tvBadge.setTextColor(Color.parseColor("#5BA3D9"));
                h.tvBadge.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#0D2A4A")));
            } else {
                h.tvBadge.setText(R.string.badge_new_episode);
                h.tvBadge.setTextColor(Color.parseColor("#4DB87A"));
                h.tvBadge.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#0D2A1A")));
            }
            h.tvEpNum.setText(h.tvEpNum.getContext().getString(R.string.badge_episode_number, epNum));
            h.tvEpNum.setVisibility(View.VISIBLE);
        } else {
            h.tvBadge.setVisibility(View.GONE);
            h.tvEpNum.setVisibility(View.GONE);
        }

        String relDate = buildRelativeDate(tv.getOnAirEpisodeAirDate());
        if (!relDate.isEmpty()) {
            h.tvAirDate.setText(relDate);
            h.tvAirDate.setVisibility(View.VISIBLE);
        } else {
            h.tvAirDate.setVisibility(View.GONE);
        }

        double rating = tv.getVoteAverage();
        if (rating > 0) {
            h.tvRating.setText(String.format(Locale.US, "★ %.1f", rating));
            h.tvRating.setVisibility(View.VISIBLE);
        } else {
            h.tvRating.setVisibility(View.GONE);
        }

        boolean canSave = dbHelper != null && userId != null && !userId.isEmpty();
        if (canSave) {
            boolean saved = WatchlistHelper.isTVShowSaved(dbHelper, userId, tv.getTVShowId());
            applyMyListState(h.btnMyList, saved);
            h.btnMyList.setOnClickListener(v -> {
                boolean nowSaved = WatchlistHelper.isTVShowSaved(dbHelper, userId, tv.getTVShowId());
                if (nowSaved) {
                    WatchlistHelper.removeTVShow(dbHelper, userId, tv.getTVShowId());
                    applyMyListState(h.btnMyList, false);
                } else {
                    WatchlistHelper.saveTVShow(dbHelper, userId, tv.getTVShowId(),
                            tv.getTitle(), tv.getPosterPath(), "", tv.getVoteAverage());
                    applyMyListState(h.btnMyList, true);
                }
            });
        } else {
            applyMyListState(h.btnMyList, false);
            h.btnMyList.setOnClickListener(v -> {
                if (onGuestMyListClick != null) onGuestMyListClick.run();
            });
        }
        h.btnMyList.setVisibility(View.VISIBLE);

        h.itemView.setOnClickListener(v -> listener.onClick(tv, h.ivPoster));
        h.divider.setVisibility(position == items.size() - 1 ? View.GONE : View.VISIBLE);
    }

    private void applyMyListState(MaterialButton btn, boolean saved) {
        if (saved) {
            btn.setText(R.string.action_in_list);
            btn.setBackgroundTintList(ColorStateList.valueOf(0xFFE3B566));
            btn.setTextColor(0xFF3A2A0C);
            btn.setStrokeWidth(0);
        } else {
            btn.setText(R.string.action_my_list);
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            btn.setTextColor(0xFFE3B566);
            btn.setStrokeWidth(context.getResources().getDimensionPixelSize(R.dimen.hairline) * 2);
        }
    }

    private static String buildRelativeDate(String airDateStr) {
        if (airDateStr == null || airDateStr.length() < 10) return "";
        try {
            LocalDate airDate = LocalDate.parse(airDateStr.substring(0, 10));
            LocalDate today = LocalDate.now();
            long days = ChronoUnit.DAYS.between(airDate, today);
            if (days == 0) return "Today";
            if (days == 1) return "Yesterday";
            if (days > 1) return days + " days ago";
            if (days == -1) return "Tomorrow";
            return "in " + (-days) + " days";
        } catch (Exception ignored) {}
        return "";
    }

    @Override
    public int getItemCount() { return items.size() + (showFooter ? 1 : 0); }

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        FooterViewHolder(@NonNull View itemView) { super(itemView); }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivPoster;
        final TextView tvTitle, tvBadge, tvEpNum, tvAirDate, tvRating;
        final MaterialButton btnMyList;
        final View divider;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster  = itemView.findViewById(R.id.ivPoster);
            tvTitle   = itemView.findViewById(R.id.tvTitle);
            tvBadge   = itemView.findViewById(R.id.tvBadge);
            tvEpNum   = itemView.findViewById(R.id.tvEpNum);
            tvAirDate = itemView.findViewById(R.id.tvAirDate);
            tvRating  = itemView.findViewById(R.id.tvRating);
            btnMyList = itemView.findViewById(R.id.btnMyList);
            divider   = itemView.findViewById(R.id.divider);
        }
    }
}