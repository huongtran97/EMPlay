package emplay.entertainment.emplay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.models.MediaItem;

public abstract class BasePosterAdapter<T extends MediaItem>
        extends RecyclerView.Adapter<BasePosterAdapter.PosterViewHolder> {

    protected final Context mContext;
    protected final List<T> mData;
    protected final OnItemClickListener<T> listener;

    public interface OnItemClickListener<T> {
        void onItemClick(T item);
    }

    protected BasePosterAdapter(Context context, List<T> data, OnItemClickListener<T> listener) {
        this.mContext = context;
        this.mData = data != null ? data : new ArrayList<>();
        this.listener = listener;
    }

    protected abstract int getLayoutRes();
    protected abstract int getImageViewId();

    protected String getImageUrl(T item) {
        if (item.getPosterPath() != null && !item.getPosterPath().isEmpty()) {
            return "https://image.tmdb.org/t/p/w500" + item.getPosterPath();
        }
        if (item.getBackdropPath() != null && !item.getBackdropPath().isEmpty()) {
            return "https://image.tmdb.org/t/p/w500" + item.getBackdropPath();
        }
        return null;
    }

    public void updateData(List<T> newData) {
        mData.clear();
        if (newData != null) mData.addAll(newData);
        notifyDataSetChanged();
    }

    public void addData(List<T> more) {
        if (more == null || more.isEmpty()) return;
        int start = mData.size();
        mData.addAll(more);
        notifyItemRangeInserted(start, more.size());
    }

    public void removeItem(int position) {
        if (position < 0 || position >= mData.size()) return;
        mData.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public PosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(getLayoutRes(), parent, false);
        return new PosterViewHolder(v, getImageViewId());
    }

    @Override
    public void onBindViewHolder(@NonNull PosterViewHolder holder, int position) {
        T item = mData.get(position);
        String url = getImageUrl(item);
        Glide.with(mContext)
                .load(url != null ? url : R.drawable.placeholder_image)
                .placeholder(R.drawable.placeholder_image)
                .into(holder.image);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class PosterViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;

        public PosterViewHolder(@NonNull View itemView, int imageViewId) {
            super(itemView);
            image = itemView.findViewById(imageViewId);
        }
    }
}
