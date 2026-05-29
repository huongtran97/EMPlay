package emplay.entertainment.emplay.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
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
import emplay.entertainment.emplay.models.MediaItem;

public class TrendingSearchAdapter extends RecyclerView.Adapter<TrendingSearchAdapter.ViewHolder> {
    private final Context context;
    private final List<? extends MediaItem> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(MediaItem item);
    }

    public TrendingSearchAdapter(Context context, List<? extends MediaItem> items, OnItemClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_trending, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediaItem item = items.get(position);

        // Fixed font size for rank numbers
        holder.tvNumber.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f);
        holder.tvNumber.setText(String.valueOf(position + 1));

        // Color based on rank position (rating-based thresholds cause all items to cluster same bucket)
        if (position == 0) {
            holder.tvNumber.setTextColor(Color.parseColor("#FFD700")); // Gold for #1
        } else if (position == 1) {
            holder.tvNumber.setTextColor(Color.parseColor("#E05C30")); // Orange-red for #2
        } else {
            holder.tvNumber.setTextColor(Color.parseColor("#888888")); // Gray for #3-5
        }

        holder.tvTitle.setText(item.getTitle());

        // Load thumbnail if available
        if (item.getBackdropPath() != null && !item.getBackdropPath().isEmpty()) {
            Glide.with(context)
                    .load("https://image.tmdb.org/t/p/w185" + item.getBackdropPath())
                    .placeholder(R.drawable.bg_poster_placeholder)
                    .centerCrop()
                    .into(holder.ivThumb);
            holder.ivThumb.setVisibility(View.VISIBLE);
        } else {
            holder.ivThumb.setVisibility(View.GONE);
        }

        // Show rating as meta info since MediaItem doesn't have release date
        @SuppressLint("DefaultLocale") String meta = String.format("Rating: %.1f", item.getVoteAverage());
        holder.tvMeta.setText(meta);

        // hide divider on last item
        holder.divider.setVisibility(position == items.size() - 1 ? View.GONE : View.VISIBLE);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumber, tvTitle, tvMeta;
        ImageView ivThumb;
        View divider;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tvNumber);
            tvTitle  = itemView.findViewById(R.id.trendNow);
            tvMeta   = itemView.findViewById(R.id.meta);
            ivThumb  = itemView.findViewById(R.id.ivThumb);
            divider  = itemView.findViewById(R.id.divider);
        }
    }
}
