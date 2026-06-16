package emplay.entertainment.emplay.adapter.tvshow;

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
import emplay.entertainment.emplay.api.common.MovieApiService;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;

public class WhatsNewTVAdapter extends RecyclerView.Adapter<WhatsNewTVAdapter.ViewHolder> {
    private final Context context;
    private final List<TVShowModel> tvShowList;
    private final OnItemClickListener listener;
    private final int maxItems;

    public interface OnItemClickListener {
        void onItemClick(TVShowModel tvShow, View sharedElement);
    }

    public WhatsNewTVAdapter(Context context, List<TVShowModel> tvShowList, MovieApiService apiService, OnItemClickListener listener) {
        this(context, tvShowList, apiService, listener, Integer.MAX_VALUE);
    }

    public WhatsNewTVAdapter(Context context, List<TVShowModel> tvShowList, MovieApiService apiService, OnItemClickListener listener, int maxItems) {
        this.context = context;
        this.tvShowList = tvShowList;
        this.listener = listener;
        this.maxItems = maxItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_whats_new_tvshow, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TVShowModel tvShow = tvShowList.get(position);

        Glide.with(context)
                .load(ImageUrl.of(ImageUrl.CARD, tvShow.getBackdropPath()))
                .placeholder(R.drawable.bg_poster_placeholder)
                .into(holder.ivThumb);

        holder.tvTitle.setText(tvShow.getName());
        holder.tvMeta.setText(tvShow.getFirstAirDate());

        holder.ivThumb.setTransitionName("poster_" + tvShow.getTVShowId());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(tvShow, holder.ivThumb));
    }

    @Override
    public int getItemCount() {
        return Math.min(tvShowList.size(), maxItems);
    }

    public void updateData(List<TVShowModel> newData) {
        List<TVShowModel> oldData = new ArrayList<>(tvShowList);
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return oldData.size(); }
            @Override public int getNewListSize() { return newData != null ? newData.size() : 0; }
            @Override public boolean areItemsTheSame(int o, int n) {
                return oldData.get(o).getTVShowId() == newData.get(n).getTVShowId();
            }
            @Override public boolean areContentsTheSame(int o, int n) {
                return Objects.equals(oldData.get(o).getName(), newData.get(n).getName());
            }
        });
        tvShowList.clear();
        if (newData != null) tvShowList.addAll(newData);
        diff.dispatchUpdatesTo(this);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvTitle, tvMeta, tvBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.ivThumb);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMeta = itemView.findViewById(R.id.tvMeta);
            tvBadge = itemView.findViewById(R.id.tvBadge);
        }
    }
}
