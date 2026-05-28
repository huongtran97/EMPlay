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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.ImageUrl;
import emplay.entertainment.emplay.tool.BadgeHelper;
import emplay.entertainment.emplay.models.MediaItem;
import emplay.entertainment.emplay.models.MovieModel;
import emplay.entertainment.emplay.models.TVShowModel;

public class RecentlyAddedAdapter extends RecyclerView.Adapter<RecentlyAddedAdapter.ViewHolder> {

    private final Context context;
    private final List<MediaItem> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(MediaItem item);
    }

    public RecentlyAddedAdapter(Context context, List<MediaItem> items, OnItemClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recently_added, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediaItem item = items.get(position);

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
            // SQLite CURRENT_TIMESTAMP is YYYY-MM-DD HH:MM:SS
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            Date date = sdf.parse(rawTimestamp);
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
        TextView tvGenre, tvSavedTime, tvType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            tvGenre = itemView.findViewById(R.id.tvGenre);
            tvSavedTime = itemView.findViewById(R.id.tvSavedTime);
            tvType = itemView.findViewById(R.id.tvType);
        }
    }
}
