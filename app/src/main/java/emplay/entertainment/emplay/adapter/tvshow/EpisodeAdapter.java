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
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.api.tvshow.SeasonDetailResponse;

public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder> {

    private static final int MAX_VISIBLE = 4;

    private final List<SeasonDetailResponse.Episode> episodeList = new ArrayList<>();
    private final Context context;
    private boolean expanded = false;

    public EpisodeAdapter(Context context) {
        this.context = context;
    }

    public void updateData(List<SeasonDetailResponse.Episode> newEpisodes) {
        expanded = false;
        List<SeasonDetailResponse.Episode> old = new ArrayList<>(episodeList);
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return old.size(); }
            @Override public int getNewListSize() { return newEpisodes != null ? newEpisodes.size() : 0; }
            @Override public boolean areItemsTheSame(int o, int n) {
                return old.get(o).getId() == newEpisodes.get(n).getId();
            }
            @Override public boolean areContentsTheSame(int o, int n) {
                SeasonDetailResponse.Episode a = old.get(o), b = newEpisodes.get(n);
                return a.getEpisodeNumber() == b.getEpisodeNumber()
                        && Objects.equals(a.getName(), b.getName())
                        && Objects.equals(a.getStillPath(), b.getStillPath());
            }
        });
        episodeList.clear();
        if (newEpisodes != null) episodeList.addAll(newEpisodes);
        diff.dispatchUpdatesTo(this);
    }

    public void setExpanded(boolean expanded) {
        if (this.expanded == expanded) return;
        this.expanded = expanded;
        int total = episodeList.size();
        if (total <= MAX_VISIBLE) return;
        if (expanded) {
            notifyItemRangeInserted(MAX_VISIBLE, total - MAX_VISIBLE);
        } else {
            notifyItemRangeRemoved(MAX_VISIBLE, total - MAX_VISIBLE);
        }
    }

    public boolean isExpanded() { return expanded; }

    public int getTotalCount() { return episodeList.size(); }

    @NonNull
    @Override
    public EpisodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_episode, parent, false);
        return new EpisodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EpisodeViewHolder holder, int position) {
        SeasonDetailResponse.Episode ep = episodeList.get(position);

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

        String stillUrl = !TextUtils.isEmpty(ep.getStillPath()) ? ImageUrl.THUMBNAIL + ep.getStillPath() : null;
        Glide.with(context)
                .load(stillUrl)
                .placeholder(R.drawable.bg_poster_placeholder)
                .into(holder.episodeStill);
    }

    @Override
    public int getItemCount() {
        return expanded ? episodeList.size() : Math.min(episodeList.size(), MAX_VISIBLE);
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