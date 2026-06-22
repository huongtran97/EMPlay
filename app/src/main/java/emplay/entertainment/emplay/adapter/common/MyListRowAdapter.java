package emplay.entertainment.emplay.adapter.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import emplay.entertainment.emplay.models.movie.MovieModel;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;

public class MyListRowAdapter extends RecyclerView.Adapter<MyListRowAdapter.RowViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(MediaItem item, View sharedElement);
    }

    private final Context context;
    private final List<MediaItem> items;
    private final OnItemClickListener listener;

    public MyListRowAdapter(Context context, List<MediaItem> items, OnItemClickListener listener) {
        this.context = context;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<MediaItem> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_mylist_row, parent, false);
        return new RowViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RowViewHolder holder, int position) {
        MediaItem item = items.get(position);

        Glide.with(context)
                .load(ImageUrl.of(ImageUrl.CARD, item.getPosterPath()))
                .placeholder(R.drawable.bg_poster_placeholder)
                .into(holder.thumb);

        holder.title.setText(item.getTitle());
        holder.meta.setText(buildMeta(item));

        double vote = item.getVoteAverage();
        if (vote > 0) {
            holder.rating.setText(String.format(Locale.getDefault(), "★ %.1f", vote));
            holder.rating.setVisibility(View.VISIBLE);
        } else {
            holder.rating.setVisibility(View.GONE);
        }

        int typeLabel = (item instanceof MovieModel) ? R.string.movie : R.string.tv_show;
        holder.status.setText(typeLabel);

        holder.overflow.setVisibility(View.GONE);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item, holder.thumb));
    }

    private String buildMeta(MediaItem item) {
        String year = null;
        String firstGenre = null;
        if (item instanceof MovieModel) {
            MovieModel m = (MovieModel) item;
            String date = m.getReleaseDate();
            if (date != null && date.length() >= 4) year = date.substring(0, 4);
            List<String> genres = m.getGenres();
            if (genres != null && !genres.isEmpty()) firstGenre = genres.get(0);
        } else if (item instanceof TVShowModel) {
            TVShowModel t = (TVShowModel) item;
            String date = t.getFirstAirDate();
            if (date != null && date.length() >= 4) year = date.substring(0, 4);
            List<String> genres = t.getGenres();
            if (genres != null && !genres.isEmpty()) firstGenre = genres.get(0);
        }
        StringBuilder sb = new StringBuilder();
        if (year != null) sb.append(year);
        if (firstGenre != null) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(firstGenre);
        }
        return sb.toString();
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class RowViewHolder extends RecyclerView.ViewHolder {
        final ImageView thumb;
        final TextView title, meta, rating, status;
        final ImageButton overflow;

        RowViewHolder(@NonNull View itemView) {
            super(itemView);
            thumb = itemView.findViewById(R.id.img_thumb);
            title = itemView.findViewById(R.id.tv_item_title);
            meta = itemView.findViewById(R.id.tv_item_meta);
            rating = itemView.findViewById(R.id.tag_rating);
            status = itemView.findViewById(R.id.tag_status);
            overflow = itemView.findViewById(R.id.btn_overflow);
        }
    }
}