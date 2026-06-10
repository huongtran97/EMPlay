package emplay.entertainment.emplay.adapter.common;

import android.annotation.SuppressLint;
import android.os.Build;
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

    public void updateData(List<T> newData) {
        List<T> oldData = new ArrayList<>(list);
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return oldData.size(); }
            @Override public int getNewListSize() { return newData != null ? newData.size() : 0; }
            @Override public boolean areItemsTheSame(int o, int n) {
                return oldData.get(o).getMediaId() == newData.get(n).getMediaId();
            }
            @Override public boolean areContentsTheSame(int o, int n) {
                return Objects.equals(oldData.get(o).getTitle(), newData.get(n).getTitle())
                        && Objects.equals(oldData.get(o).getPosterPath(), newData.get(n).getPosterPath());
            }
        });
        list.clear();
        if (newData != null) list.addAll(newData);
        diff.dispatchUpdatesTo(this);
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class SearchViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, language, rating;
        ImageView poster;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.search_title);
            date = itemView.findViewById(R.id.search_release_date);
            language = itemView.findViewById(R.id.search_language);
            poster = itemView.findViewById(R.id.search_poster);
            rating = itemView.findViewById(R.id.tvPosterRating);
        }
    }
}