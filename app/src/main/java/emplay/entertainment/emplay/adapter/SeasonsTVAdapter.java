package emplay.entertainment.emplay.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.ImageUrl;
import emplay.entertainment.emplay.models.SeasonsModel;

/**
 *  Horizontal seasons row on the TV show detail screen.
 *  Tapping a season opens SeasonDetailFragment via the OnSeasonClickListener callback.
 */
public class SeasonsTVAdapter extends RecyclerView.Adapter<SeasonsTVAdapter.SeasonsViewHolder> {

    private final ArrayList<SeasonsModel> seasonsList;
    private final Context context;
    private OnSeasonClickListener onSeasonClickListener;

    public interface OnSeasonClickListener {
        void onSeasonClick(SeasonsModel season);
    }

    public SeasonsTVAdapter(List<SeasonsModel> seasonsList, Context context) {
        this.seasonsList = (ArrayList<SeasonsModel>) seasonsList;
        this.context = context;
    }

    public SeasonsTVAdapter(List<SeasonsModel> seasonsList, Context context,
                            OnSeasonClickListener listener) {
        this.seasonsList = (ArrayList<SeasonsModel>) seasonsList;
        this.context = context;
        this.onSeasonClickListener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<SeasonsModel> newSeasonsList) {
        seasonsList.clear();
        seasonsList.addAll(newSeasonsList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SeasonsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_result_tv_season_item, parent, false);
        return new SeasonsViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SeasonsViewHolder holder, int position) {
        SeasonsModel seasonsModel = seasonsList.get(position);
        if (seasonsModel != null) {
            holder.name.setText(seasonsModel.getName());
            holder.episode.setText("Episodes: " + seasonsModel.getNumberOfEpisodes());

            String posterUrl = null;
            if (seasonsModel.getPosterPath() != null && !seasonsModel.getPosterPath().isEmpty()) {
                posterUrl = ImageUrl.THUMBNAIL + seasonsModel.getPosterPath();
            }

            Glide.with(context)
                    .load(posterUrl != null ? posterUrl : R.drawable.placeholder_image)
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.poster);

            holder.itemView.setOnClickListener(v -> {
                if (onSeasonClickListener != null) {
                    onSeasonClickListener.onSeasonClick(seasonsModel);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return seasonsList.size();
    }

    public static class SeasonsViewHolder extends RecyclerView.ViewHolder {
        TextView name, episode;
        ImageView poster;

        public SeasonsViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            episode = itemView.findViewById(R.id.episode);
            poster = itemView.findViewById(R.id.header);
        }
    }
}