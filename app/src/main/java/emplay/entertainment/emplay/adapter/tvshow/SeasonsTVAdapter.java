package emplay.entertainment.emplay.adapter.tvshow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.BaseDiffUtilAdapter;
import emplay.entertainment.emplay.models.tvshow.SeasonsModel;

public class SeasonsTVAdapter extends BaseDiffUtilAdapter<SeasonsModel, SeasonsTVAdapter.SeasonTabViewHolder> {

    public interface OnSeasonSelectedListener {
        void onSeasonSelected(SeasonsModel season);
    }

    private final Context context;
    private final OnSeasonSelectedListener listener;
    private int selectedPosition = 0;

    public SeasonsTVAdapter(List<SeasonsModel> seasonsList, Context context,
                            OnSeasonSelectedListener listener) {
        this.context = context;
        this.listener = listener;
        if (seasonsList != null) mData.addAll(seasonsList);
    }

    @Override
    protected boolean areItemsTheSame(@NonNull SeasonsModel oldItem, @NonNull SeasonsModel newItem) {
        return oldItem.getId() == newItem.getId();
    }

    @Override
    protected boolean areContentsTheSame(@NonNull SeasonsModel oldItem, @NonNull SeasonsModel newItem) {
        return oldItem.getSeasonNumber() == newItem.getSeasonNumber()
                && Objects.equals(oldItem.getName(), newItem.getName());
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
                .inflate(R.layout.item_search_result_tv_season, parent, false);
        return new SeasonTabViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeasonTabViewHolder holder, int position) {
        SeasonsModel season = mData.get(position);
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

    public static class SeasonTabViewHolder extends RecyclerView.ViewHolder {
        TextView tvSeasonTab;

        public SeasonTabViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSeasonTab = itemView.findViewById(R.id.tvSeasonTab);
        }
    }
}