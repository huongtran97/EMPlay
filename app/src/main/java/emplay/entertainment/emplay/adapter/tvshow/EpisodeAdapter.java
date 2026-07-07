package emplay.entertainment.emplay.adapter.tvshow;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.BaseDiffUtilAdapter;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.api.tvshow.SeasonDetailResponse;
import emplay.entertainment.emplay.tool.GlideImageLoader;

public class EpisodeAdapter extends BaseDiffUtilAdapter<SeasonDetailResponse.Episode, EpisodeAdapter.EpisodeViewHolder> {

    private static final int MAX_VISIBLE = 4;

    private final Context context;
    private boolean expanded = false;

    public EpisodeAdapter(Context context) {
        this.context = context;
    }

    @Override
    protected boolean areItemsTheSame(@NonNull SeasonDetailResponse.Episode oldItem,
                                      @NonNull SeasonDetailResponse.Episode newItem) {
        return oldItem.getId() == newItem.getId();
    }

    @Override
    protected boolean areContentsTheSame(@NonNull SeasonDetailResponse.Episode oldItem,
                                         @NonNull SeasonDetailResponse.Episode newItem) {
        return oldItem.getEpisodeNumber() == newItem.getEpisodeNumber()
                && Objects.equals(oldItem.getName(), newItem.getName())
                && Objects.equals(oldItem.getStillPath(), newItem.getStillPath());
    }

    @Override
    public void updateData(List<SeasonDetailResponse.Episode> newEpisodes) {
        expanded = false;
        super.updateData(newEpisodes);
    }

    // Overrides base getItemCount() to respect the expand/collapse state.
    @Override
    public int getItemCount() {
        return expanded ? mData.size() : Math.min(mData.size(), MAX_VISIBLE);
    }

    public void setExpanded(boolean expanded) {
        if (this.expanded == expanded) return;
        this.expanded = expanded;
        int total = mData.size();
        if (total <= MAX_VISIBLE) return;
        if (expanded) {
            notifyItemRangeInserted(MAX_VISIBLE, total - MAX_VISIBLE);
        } else {
            notifyItemRangeRemoved(MAX_VISIBLE, total - MAX_VISIBLE);
        }
    }

    public boolean isExpanded() { return expanded; }

    public int getTotalCount() { return mData.size(); }

    @NonNull
    @Override
    public EpisodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_episode, parent, false);
        return new EpisodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EpisodeViewHolder holder, int position) {
        SeasonDetailResponse.Episode ep = mData.get(position);

        holder.episodeTitle.setText(ep.getName());
        holder.episodeNumber.setText(String.valueOf(ep.getEpisodeNumber()));

        StringBuilder meta = new StringBuilder();
        if (ep.getRuntime() > 0) meta.append(String.format(Locale.getDefault(), "%dm", ep.getRuntime()));
        if (!TextUtils.isEmpty(ep.getAirDate())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                if (!meta.isEmpty()) meta.append(" · ");
            }
            meta.append(ep.getAirDate());
        }
        holder.episodeAirDate.setText(meta.toString());

        String overview = ep.getOverview();
        if (!TextUtils.isEmpty(overview)) {
            holder.episodeOverview.setVisibility(View.VISIBLE);
            holder.episodeOverview.setMaxLines(1);
            holder.episodeOverview.setText(overview);
            holder.readMore.setVisibility(View.GONE);
            holder.episodeOverview.post(() -> {
                android.text.Layout layout = holder.episodeOverview.getLayout();
                if (layout == null) return;
                boolean truncated = layout.getEllipsisCount(layout.getLineCount() - 1) > 0;
                holder.readMore.setVisibility(truncated ? View.VISIBLE : View.GONE);
            });
            holder.readMore.setOnClickListener(v -> {
                boolean expanded = holder.episodeOverview.getMaxLines() == Integer.MAX_VALUE;
                holder.episodeOverview.setMaxLines(expanded ? 1 : Integer.MAX_VALUE);
                holder.readMore.setText(expanded ? R.string.read_more : R.string.read_less);
            });
        } else {
            holder.episodeOverview.setVisibility(View.GONE);
            holder.readMore.setVisibility(View.GONE);
        }

        GlideImageLoader.load(context,
                ImageUrl.of(ImageUrl.THUMBNAIL, ep.getStillPath()),
                holder.episodeStill, R.drawable.bg_poster_placeholder);
    }

    public static class EpisodeViewHolder extends RecyclerView.ViewHolder {
        ImageView episodeStill;
        TextView episodeNumber, episodeTitle, episodeAirDate, episodeOverview, readMore;

        public EpisodeViewHolder(@NonNull View itemView) {
            super(itemView);
            episodeStill    = itemView.findViewById(R.id.episode_still);
            episodeNumber   = itemView.findViewById(R.id.episode_number);
            episodeTitle    = itemView.findViewById(R.id.episode_title);
            episodeAirDate  = itemView.findViewById(R.id.episode_air_date);
            episodeOverview = itemView.findViewById(R.id.episode_overview);
            readMore        = itemView.findViewById(R.id.read_more_text);
        }
    }
}