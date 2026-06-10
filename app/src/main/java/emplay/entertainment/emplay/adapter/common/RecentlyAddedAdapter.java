package emplay.entertainment.emplay.adapter.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.tool.BadgeHelper;
import emplay.entertainment.emplay.models.common.MediaItem;
import emplay.entertainment.emplay.models.movie.MovieModel;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;

public class RecentlyAddedAdapter extends RecyclerView.Adapter<RecentlyAddedAdapter.ViewHolder> {

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    private final Context context;
    private final List<MediaItem> items;
    private final OnItemClickListener listener;
    private final DatabaseHelper dbHelper;
    private final String userId;
    private OnItemRemovedListener onItemRemovedListener;

    public interface OnItemClickListener {
        void onItemClick(MediaItem item);
    }

    public interface OnItemRemovedListener {
        void onItemRemoved(int newCount);
    }

    public RecentlyAddedAdapter(Context context, List<MediaItem> items, OnItemClickListener listener,
                                DatabaseHelper dbHelper, String userId) {
        this.context = context;
        this.items = items;
        this.listener = listener;
        this.dbHelper = dbHelper;
        this.userId = userId;
    }

    public void setOnItemRemovedListener(OnItemRemovedListener listener) {
        this.onItemRemovedListener = listener;
    }

    public void removeItem(int position) {
        if (position < 0 || position >= items.size()) return;
        MediaItem item = items.remove(position);
        notifyItemRemoved(position);
        if (dbHelper != null && userId != null) {
            new Thread(() -> {
                if (item instanceof MovieModel) {
                    dbHelper.deleteMovie(item.getMediaId(), userId);
                } else if (item instanceof TVShowModel) {
                    dbHelper.deleteTV(item.getMediaId(), userId);
                }
            }).start();
        }
        if (onItemRemovedListener != null) onItemRemovedListener.onItemRemoved(items.size());
    }

    public void updateData(List<MediaItem> newItems) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return items.size(); }

            @Override
            public int getNewListSize() { return newItems.size(); }

            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                MediaItem o = items.get(oldPos);
                MediaItem n = newItems.get(newPos);
                return o.getMediaId() == n.getMediaId()
                        && o.getClass().equals(n.getClass());
            }

            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                MediaItem o = items.get(oldPos);
                MediaItem n = newItems.get(newPos);
                return Objects.equals(o.getTitle(), n.getTitle())
                        && Objects.equals(o.getPosterPath(), n.getPosterPath())
                        && Objects.equals(o.getSavedTimestamp(), n.getSavedTimestamp());
            }
        });
        items.clear();
        items.addAll(newItems);
        result.dispatchUpdatesTo(this);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recently_added, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediaItem item = items.get(position);

        holder.tvTitle.setText(item.getTitle());

        // Genre handling
        List<String> genres = null;
        if (item instanceof MovieModel) genres = ((MovieModel) item).getGenres();
        else if (item instanceof TVShowModel) genres = ((TVShowModel) item).getGenres();

        if (genres != null && !genres.isEmpty()) {
            holder.tvGenre.setText(genres.get(0));
        } else {
            holder.tvGenre.setText(R.string.saved_recently);
        }

        BadgeHelper.applyTypeBadge(context, holder.tvType, item instanceof MovieModel);

        // Timestamp formatting
        holder.tvSavedTime.setText(formatDate(item.getSavedTimestamp()));

        String posterPath = item.getPosterPath();
        String imageUrl = (posterPath != null && !posterPath.isEmpty()) 
                ? ImageUrl.POSTER + posterPath 
                : null;

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(holder.ivThumbnail);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    private String formatDate(String rawTimestamp) {
        if (rawTimestamp == null) return context.getString(R.string.saved_recently);
        try {
            Date date = DATE_FORMAT.parse(rawTimestamp);
            if (date == null) return context.getString(R.string.saved_recently);
            
            long diff = System.currentTimeMillis() - date.getTime();
            long minutes = diff / (1000 * 60);
            long hours = minutes / 60;
            long days = hours / 24;

            if (minutes < 1) return context.getString(R.string.just_now);
            if (minutes < 60) return context.getString(R.string.saved_ago, minutes + "m");
            if (hours < 24) return context.getString(R.string.saved_ago, hours + "h");
            return context.getString(R.string.saved_ago, days + "d");
        } catch (Exception e) {
            return context.getString(R.string.saved_recently);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvTitle, tvGenre, tvSavedTime, tvType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvGenre = itemView.findViewById(R.id.tvGenre);
            tvSavedTime = itemView.findViewById(R.id.tvSavedTime);
            tvType = itemView.findViewById(R.id.tvType);
        }
    }
}
