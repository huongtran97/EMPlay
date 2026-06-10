package emplay.entertainment.emplay.adapter.tvshow;

import android.annotation.SuppressLint;
import android.content.Context;
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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.api.tvshow.SeasonDetailResponse;
import emplay.entertainment.emplay.tool.ReadHelper;

/**
 * Adapter for the list of episodes within a season. Each row shows the still image,
 * episode number, title, air date, and a collapsible overview (Read More/Less).
 */
public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder> {
    private final List<SeasonDetailResponse.Episode> episodeList = new ArrayList<>();
    private final Context context;
    private final Set<Integer> expandedItems = new HashSet<>();

    public EpisodeAdapter(Context context) {
        this.context = context;
    }

    public void updateData(List<SeasonDetailResponse.Episode> newEpisodes) {
        List<SeasonDetailResponse.Episode> oldList = new ArrayList<>(episodeList);
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return oldList.size(); }
            @Override public int getNewListSize() { return newEpisodes != null ? newEpisodes.size() : 0; }
            @Override public boolean areItemsTheSame(int o, int n) {
                return oldList.get(o).getId() == newEpisodes.get(n).getId();
            }
            @Override public boolean areContentsTheSame(int o, int n) {
                SeasonDetailResponse.Episode a = oldList.get(o), b = newEpisodes.get(n);
                return a.getEpisodeNumber() == b.getEpisodeNumber()
                        && Objects.equals(a.getName(), b.getName())
                        && Objects.equals(a.getStillPath(), b.getStillPath());
            }
        });
        episodeList.clear();
        if (newEpisodes != null) episodeList.addAll(newEpisodes);
        diff.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public EpisodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.episode_item, parent, false);
        return new EpisodeViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull EpisodeViewHolder holder, int position) {
        SeasonDetailResponse.Episode episode = episodeList.get(position);

        holder.episodeNumber.setText("EP " + episode.getEpisodeNumber());
        holder.episodeTitle.setText(episode.getName());
        holder.episodeOverview.setText(episode.getOverview() != null
                && !episode.getOverview().isEmpty()
                ? episode.getOverview() : "No overview available.");

        // Read More / Less 
        ReadHelper.setup(holder.episodeOverview, holder.readMoreBtn, episode.getEpisodeNumber(), expandedItems, holder, this);

        holder.episodeAirDate.setText(episode.getAirDate() != null
                ? episode.getAirDate() : "");

        if (episode.getStillPath() != null) {
            Glide.with(context)
                    .load(ImageUrl.THUMBNAIL + episode.getStillPath())
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.episodeStill);
        } else {
            holder.episodeStill.setImageResource(R.drawable.placeholder_image);
        }
    }

    @Override
    public int getItemCount() {
        return episodeList.size();
    }

    public static class EpisodeViewHolder extends RecyclerView.ViewHolder {
        TextView episodeNumber, episodeTitle, episodeOverview, episodeAirDate, readMoreBtn;
        ImageView episodeStill;

        public EpisodeViewHolder(@NonNull View itemView) {
            super(itemView);
            episodeNumber = itemView.findViewById(R.id.episode_number);
            episodeTitle = itemView.findViewById(R.id.episode_title);
            episodeOverview = itemView.findViewById(R.id.episode_overview);
            episodeAirDate = itemView.findViewById(R.id.episode_air_date);
            readMoreBtn = itemView.findViewById(R.id.read_more_text);
            episodeStill = itemView.findViewById(R.id.episode_still);
        }
    }
}