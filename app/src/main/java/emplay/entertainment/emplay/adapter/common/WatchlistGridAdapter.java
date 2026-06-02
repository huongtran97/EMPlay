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

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.models.common.MediaItem;

public class WatchlistGridAdapter extends RecyclerView.Adapter<WatchlistGridAdapter.GridViewHolder> {

    private final Context context;
    private List<MediaItem> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(MediaItem item);
    }

    public WatchlistGridAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.items = new ArrayList<>();
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<MediaItem> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
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
        
        holder.title.setText(item.getTitle());
        holder.rating.setText(
                holder.itemView.getContext().getString(R.string.rating_format, item.getVoteAverage())
        );
        
        holder.upcomingDate.setVisibility(View.GONE);

        String url = ImageUrl.THUMBNAIL + item.getPosterPath();
        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.placeholder_image)
                .into(holder.poster);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
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

            // Set fixed aspect ratio for poster if needed or let XML handle it
            itemView.post(() -> {
                int width = itemView.getWidth();
                if (width > 0) {
                    ViewGroup.LayoutParams lp = poster.getLayoutParams();
                    lp.height = (int) (width * 1.5);
                    poster.setLayoutParams(lp);
                }
            });
        }
    }
}
