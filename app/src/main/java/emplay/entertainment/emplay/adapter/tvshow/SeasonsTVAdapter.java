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
import java.util.List;
import java.util.Objects;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.models.tvshow.SeasonsModel;

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

    public void updateData(List<SeasonsModel> newSeasonsList) {
        List<SeasonsModel> oldList = new ArrayList<>(seasonsList);
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return oldList.size(); }
            @Override public int getNewListSize() { return newSeasonsList != null ? newSeasonsList.size() : 0; }
            @Override public boolean areItemsTheSame(int o, int n) {
                return oldList.get(o).getId() == newSeasonsList.get(n).getId();
            }
            @Override public boolean areContentsTheSame(int o, int n) {
                SeasonsModel a = oldList.get(o), b = newSeasonsList.get(n);
                return a.getSeasonNumber() == b.getSeasonNumber()
                        && a.getNumberOfEpisodes() == b.getNumberOfEpisodes()
                        && Objects.equals(a.getPosterPath(), b.getPosterPath());
            }
        });
        seasonsList.clear();
        if (newSeasonsList != null) seasonsList.addAll(newSeasonsList);
        diff.dispatchUpdatesTo(this);
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
                    .centerCrop()
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