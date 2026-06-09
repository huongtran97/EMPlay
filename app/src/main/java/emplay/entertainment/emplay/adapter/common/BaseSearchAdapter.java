package emplay.entertainment.emplay.adapter.common;

import android.annotation.SuppressLint;
import android.os.Build;
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
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.models.common.MediaItem;

public abstract class BaseSearchAdapter<T extends MediaItem>
        extends RecyclerView.Adapter<BaseSearchAdapter.SearchViewHolder> {

    protected final List<T> list;
    protected final OnItemClickListener<T> listener;

    public interface OnItemClickListener<T> {
        void onItemClick(T item);
    }

    protected BaseSearchAdapter(List<T> list, OnItemClickListener<T> listener) {
        this.list = list;
        this.listener = listener;
    }

    protected abstract String getDisplayDate(T item);
    protected abstract String getLanguage(T item);

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item, parent, false);
        return new SearchViewHolder(view);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        T item = list.get(position);
        if (item == null) return;

        holder.title.setText(item.getTitle());
        String date = getDisplayDate(item);
        holder.date.setText((date != null && date.length() >= 4) ? date.substring(0, 4) : "—");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.language.setText("Language: " + getLanguage(item));
        }
        holder.rating.setText(String.format("%.1f", item.getVoteAverage()));

        Glide.with(holder.itemView.getContext())
                .load(ImageUrl.THUMBNAIL + item.getPosterPath())
                .into(holder.poster);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<T> newData) {
        list.clear();
        if (newData != null) list.addAll(newData);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class SearchViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, language, rating;
        ImageView poster;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            title    = itemView.findViewById(R.id.search_title);
            date     = itemView.findViewById(R.id.search_release_date);
            language = itemView.findViewById(R.id.search_language);
            poster   = itemView.findViewById(R.id.search_poster);
            rating   = itemView.findViewById(R.id.tvPosterRating);
        }
    }
}