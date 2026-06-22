package emplay.entertainment.emplay.adapter.tvshow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.BasePosterAdapter;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;
import emplay.entertainment.emplay.tool.BadgeHelper;

public class OnAirTVAdapter extends BasePosterAdapter<TVShowModel> {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_MORE = 1;

    private boolean mShowMoreItem = false;
    private Runnable mOnMoreClick;

    public interface OnItemClickListener {
        void onItemClick(TVShowModel tvShow, View sharedElement);
    }

    public OnAirTVAdapter(Context context, List<TVShowModel> data, OnItemClickListener listener) {
        super(context, data, listener::onItemClick);
    }

    public void setShowMoreItem(boolean show, Runnable onMoreClick) {
        mShowMoreItem = show;
        mOnMoreClick = onMoreClick;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return (mShowMoreItem && position == mData.size()) ? VIEW_TYPE_MORE : VIEW_TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return mShowMoreItem ? mData.size() + 1 : mData.size();
    }

    @Override
    protected String getImageUrl(TVShowModel item) {
        return ImageUrl.of(ImageUrl.BACKDROP, item.getBackdropPath());
    }

    @Override
    protected int getLayoutRes() { return R.layout.tvshow_backdrop_item; }

    @Override
    protected int getImageViewId() { return R.id.header; }

    @Override
    protected void bindBadge(PosterViewHolder holder, TVShowModel item) {
        if (holder.badge == null) return;
        BadgeHelper.applyWhatsNewTVBadge(holder.badge, item.getFirstAirDate(), null);
    }

    @NonNull
    @Override
    public PosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_MORE) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.item_onair_more, parent, false);
            return new PosterViewHolder(v, R.id.header);
        }
        View view = LayoutInflater.from(mContext).inflate(getLayoutRes(), parent, false);
        return new OnAirViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PosterViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_MORE) {
            holder.itemView.setOnClickListener(v -> { if (mOnMoreClick != null) mOnMoreClick.run(); });
            return;
        }
        super.onBindViewHolder(holder, position);
        if (!(holder instanceof OnAirViewHolder h)) return;
        TVShowModel item = mData.get(position);

        h.tvTitle.setText(item.getName());

        String seasonName = item.getOnAirSeasonName();
        int epNum = item.getOnAirEpisodeNumber();
        if (seasonName != null || epNum > 0) {
            String info;
            if (seasonName != null && epNum > 0) info = seasonName + " · Ep " + epNum;
            else if (seasonName != null) info = seasonName;
            else info = "Ep " + epNum;
            h.tvSeasonInfo.setText(info);
            h.tvSeasonInfo.setVisibility(View.VISIBLE);
        } else {
            h.tvSeasonInfo.setVisibility(View.GONE);
        }

        String relDate = buildRelativeDate(item.getOnAirEpisodeAirDate());
        if (!relDate.isEmpty()) {
            h.tvAirDate.setText(relDate);
            h.tvAirDate.setVisibility(View.VISIBLE);
        } else {
            h.tvAirDate.setVisibility(View.GONE);
        }
    }

    private String buildRelativeDate(String airDateStr) {
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

    public static class OnAirViewHolder extends PosterViewHolder {
        final TextView tvTitle;
        final TextView tvSeasonInfo;
        final TextView tvAirDate;

        OnAirViewHolder(View view) {
            super(view, R.id.header);
            tvTitle = view.findViewById(R.id.tvOnAirTitle);
            tvSeasonInfo = view.findViewById(R.id.tvOnAirSeasonInfo);
            tvAirDate = view.findViewById(R.id.tvOnAirAirDate);
        }
    }
}