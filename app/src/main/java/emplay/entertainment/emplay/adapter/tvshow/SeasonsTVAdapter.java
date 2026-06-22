package emplay.entertainment.emplay.adapter.tvshow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.models.tvshow.SeasonsModel;

public class SeasonsTVAdapter extends RecyclerView.Adapter<SeasonsTVAdapter.SeasonTabViewHolder> {

    public interface OnSeasonSelectedListener {
        void onSeasonSelected(SeasonsModel season);
    }

    private final ArrayList<SeasonsModel> seasonsList;
    private final Context context;
    private final OnSeasonSelectedListener listener;
    private int selectedPosition = 0;

    public SeasonsTVAdapter(List<SeasonsModel> seasonsList, Context context,
                            OnSeasonSelectedListener listener) {
        this.seasonsList = new ArrayList<>(seasonsList);
        this.context = context;
        this.listener = listener;
    }

    public void updateData(List<SeasonsModel> newList) {
        List<SeasonsModel> old = new ArrayList<>(seasonsList);
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return old.size(); }
            @Override public int getNewListSize() { return newList != null ? newList.size() : 0; }
            @Override public boolean areItemsTheSame(int o, int n) {
                return old.get(o).getId() == newList.get(n).getId();
            }
            @Override public boolean areContentsTheSame(int o, int n) {
                SeasonsModel a = old.get(o), b = newList.get(n);
                return a.getSeasonNumber() == b.getSeasonNumber()
                        && Objects.equals(a.getName(), b.getName());
            }
        });
        seasonsList.clear();
        if (newList != null) seasonsList.addAll(newList);
        diff.dispatchUpdatesTo(this);
    }

    public void setSelectedPosition(int position) {
        int prev = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(prev);
        notifyItemChanged(position);
    }

    @NonNull
    @Override
    public SeasonTabViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.search_result_tv_season_item, parent, false);
        return new SeasonTabViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeasonTabViewHolder holder, int position) {
        SeasonsModel season = seasonsList.get(position);
        holder.tvSeasonTab.setText(season.getName());

        boolean selected = position == selectedPosition;
        holder.tvSeasonTab.setBackground(ContextCompat.getDrawable(context,
                selected ? R.drawable.bg_tab_selected : R.drawable.bg_chip));
        holder.tvSeasonTab.setTextColor(ContextCompat.getColor(context,
                selected ? R.color.on_accent : R.color.text_2));

        holder.tvSeasonTab.setOnClickListener(v -> {
            int prev = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(prev);
            notifyItemChanged(selectedPosition);
            if (listener != null) listener.onSeasonSelected(season);
        });
    }

    @Override
    public int getItemCount() { return seasonsList.size(); }

    public static class SeasonTabViewHolder extends RecyclerView.ViewHolder {
        TextView tvSeasonTab;

        public SeasonTabViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSeasonTab = itemView.findViewById(R.id.tvSeasonTab);
        }
    }
}