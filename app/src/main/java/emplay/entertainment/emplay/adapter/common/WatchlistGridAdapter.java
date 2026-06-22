package emplay.entertainment.emplay.adapter.common;

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
import java.util.Locale;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.models.common.MediaItem;

public class WatchlistGridAdapter extends RecyclerView.Adapter<WatchlistGridAdapter.GridViewHolder> {

    private final Context context;
    private final List<MediaItem> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(MediaItem item, View sharedElement);
    }

    public WatchlistGridAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<MediaItem> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    public MediaItem getItem(int position) {
        return items.get(position);
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void restoreItem(int position, MediaItem item) {
        int insertAt = Math.min(position, items.size());
        items.add(insertAt, item);
        notifyItemInserted(insertAt);
    }

    @NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_watchlist_poster, parent, false);
        return new GridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GridViewHolder holder, int position) {
        MediaItem item = items.get(position);

        Glide.with(context)
                .load(ImageUrl.of(ImageUrl.THUMBNAIL, item.getPosterPath()))
                .placeholder(R.drawable.bg_poster_placeholder)
                .into(holder.poster);

        holder.title.setText(item.getTitle());
        holder.rating.setText(String.format(Locale.getDefault(), "%.1f", item.getVoteAverage()));
        
        holder.poster.setTransitionName("poster_" + item.getMediaId());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item, holder.poster));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class GridViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title, rating, upcomingDate;

        public GridViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.ivPoster);
            title = itemView.findViewById(R.id.tvPosterTitle);
            rating = itemView.findViewById(R.id.tvPosterRating);
            upcomingDate = itemView.findViewById(R.id.tvUpcomingDate);
        }
    }
}
