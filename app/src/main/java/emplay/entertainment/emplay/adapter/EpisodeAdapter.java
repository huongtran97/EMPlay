package emplay.entertainment.emplay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.SeasonDetailResponse;

public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder> {

    private final List<SeasonDetailResponse.Episode> episodeList;
    private final Context context;

    public EpisodeAdapter(List<SeasonDetailResponse.Episode> episodeList, Context context) {
        this.episodeList = episodeList;
        this.context = context;
    }

    @NonNull
    @Override
    public EpisodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.episode_item, parent, false);
        return new EpisodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EpisodeViewHolder holder, int position) {
        SeasonDetailResponse.Episode episode = episodeList.get(position);

        holder.episodeNumber.setText("EP " + episode.getEpisodeNumber());
        holder.episodeTitle.setText(episode.getName());
        holder.episodeOverview.setText(episode.getOverview() != null
                && !episode.getOverview().isEmpty()
                ? episode.getOverview() : "No overview available.");
        holder.episodeAirDate.setText(episode.getAirDate() != null
                ? episode.getAirDate() : "");

        if (episode.getStillPath() != null) {
            Glide.with(context)
                    .load("https://image.tmdb.org/t/p/w500" + episode.getStillPath())
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
        TextView episodeNumber;
        TextView episodeTitle;
        TextView episodeOverview;
        TextView episodeAirDate;
        ImageView episodeStill;

        public EpisodeViewHolder(@NonNull View itemView) {
            super(itemView);
            episodeNumber = itemView.findViewById(R.id.episode_number);
            episodeTitle = itemView.findViewById(R.id.episode_title);
            episodeOverview = itemView.findViewById(R.id.episode_overview);
            episodeAirDate = itemView.findViewById(R.id.episode_air_date);
            episodeStill = itemView.findViewById(R.id.episode_still);
        }
    }
}