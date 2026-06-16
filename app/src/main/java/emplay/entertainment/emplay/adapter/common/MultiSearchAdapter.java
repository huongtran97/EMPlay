package emplay.entertainment.emplay.adapter.common;

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
import java.util.Locale;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.models.common.MultiSearchResult;

public class MultiSearchAdapter extends RecyclerView.Adapter<MultiSearchAdapter.ViewHolder> {

    public interface OnClickListener {
        void onClick(MultiSearchResult item, View sharedElement);
    }

    private final List<MultiSearchResult> list = new ArrayList<>();
    private final OnClickListener listener;

    public MultiSearchAdapter(OnClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<MultiSearchResult> newData) {
        List<MultiSearchResult> old = new ArrayList<>(list);
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return old.size(); }
            @Override public int getNewListSize() { return newData != null ? newData.size() : 0; }
            @Override public boolean areItemsTheSame(int o, int n) {
                return old.get(o).getMediaId() == newData.get(n).getMediaId()
                        && old.get(o).getMediaType() != null
                        && old.get(o).getMediaType().equals(newData.get(n).getMediaType());
            }
            @Override public boolean areContentsTheSame(int o, int n) {
                return old.get(o).getTitle().equals(newData.get(n).getTitle());
            }
        });
        list.clear();
        if (newData != null) list.addAll(newData);
        diff.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MultiSearchResult item = list.get(position);

        holder.title.setText(item.getTitle());
        holder.year.setText(item.getDisplayYear());
        holder.rating.setText(String.format(Locale.getDefault(), "%.1f", item.getVoteAverage()));

        boolean isTV = "tv".equals(item.getMediaType());
        holder.typeChip.setText(isTV ? "TV" : "MOVIE");

        Glide.with(holder.itemView.getContext())
                .load(ImageUrl.of(ImageUrl.THUMBNAIL, item.getPosterPath()))
                .placeholder(R.drawable.bg_poster_placeholder)
                .into(holder.poster);

        holder.poster.setTransitionName("poster_" + item.getMediaId());
        holder.itemView.setOnClickListener(v -> listener.onClick(item, holder.poster));
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, year, rating, typeChip;
        ImageView poster;

        public ViewHolder(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.search_title);
            year = v.findViewById(R.id.search_release_date);
            rating = v.findViewById(R.id.tvPosterRating);
            typeChip = v.findViewById(R.id.tvTypeChip);
            poster = v.findViewById(R.id.search_poster);
        }
    }
}