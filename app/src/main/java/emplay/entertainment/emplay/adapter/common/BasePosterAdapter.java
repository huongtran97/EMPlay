package emplay.entertainment.emplay.adapter.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.models.common.MediaItem;

public abstract class BasePosterAdapter<T extends MediaItem>
        extends RecyclerView.Adapter<BasePosterAdapter.PosterViewHolder> {

    protected final Context mContext;
    protected final List<T> mData;
    protected final OnItemClickListener<T> listener;

    // Optional — only used by adapters that want Palette color extraction (TrendingBannerAdapter).
    // Null by default so existing adapters are completely unaffected.
    @Nullable
    private OnPaletteColorListener paletteListener;

    public interface OnItemClickListener<T> {
        void onItemClick(T item);
    }

    // Callback fired once per item when Palette finishes extracting the dominant dark color.
    public interface OnPaletteColorListener {
        void onColorReady(int position, int color);
    }

    protected BasePosterAdapter(Context context, List<T> data, OnItemClickListener<T> listener) {
        this.mContext = context;
        this.mData    = data != null ? data : new ArrayList<>();
        this.listener = listener;
    }

    // Subclasses that need Palette call this once after construction.
    public void setPaletteColorListener(@Nullable OnPaletteColorListener listener) {
        this.paletteListener = listener;
    }

    protected abstract int getLayoutRes();
    protected abstract int getImageViewId();
    protected String getImagePrefix() { return ImageUrl.THUMBNAIL; }

    // Prefer the poster, fall back to the backdrop if there is no poster.
    protected String getImageUrl(T item) {
        String prefix = getImagePrefix();
        if (item.getPosterPath() != null && !item.getPosterPath().isEmpty())
            return prefix + item.getPosterPath();
        if (item.getBackdropPath() != null && !item.getBackdropPath().isEmpty())
            return prefix + item.getBackdropPath();
        return null;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<T> newData) {
        mData.clear();
        if (newData != null) mData.addAll(newData);
        notifyDataSetChanged();
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
    public void onBindViewHolder(@NonNull PosterViewHolder holder, @SuppressLint("RecyclerView") int position) {
        T item = mData.get(position);
        String url = getImageUrl(item);

        if (paletteListener != null) {
            // Load as Bitmap so Palette can sample it; also set the image on the ImageView.
            Glide.with(mContext)
                    .asBitmap()
                    .load(url != null ? url : R.drawable.placeholder_image)
                    .placeholder(R.drawable.placeholder_image)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap bitmap,
                                                    @Nullable Transition<? super Bitmap> transition) {
                            holder.image.setImageBitmap(bitmap);
                            extractPaletteColor(bitmap, position);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            holder.image.setImageDrawable(placeholder);
                        }
                    });
        } else {
            // Default path — unchanged from original, no Palette overhead.
            Glide.with(mContext)
                    .load(url != null ? url : R.drawable.placeholder_image)
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.image);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    // Runs Palette on a background thread (Palette.from().generate() is already async).
    private void extractPaletteColor(Bitmap bitmap, int position) {
        Palette.from(bitmap).generate(palette -> {
            if (palette == null || paletteListener == null) return;
            int fallback  = 0xFF0D0D0D;
            int dominant  = palette.getDarkMutedColor(
                    palette.getDarkVibrantColor(fallback));
            int darkened  = darkenColor(dominant, 0.55f);
            paletteListener.onColorReady(position, darkened);
        });
    }

    // Reduces brightness of a color by the given factor (0.0 = black, 1.0 = unchanged).
    private static int darkenColor(int color, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= factor;
        return Color.HSVToColor(hsv);
    }

    @Override
    public int getItemCount() { return mData.size(); }

    public static class PosterViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        public PosterViewHolder(@NonNull View itemView, int imageViewId) {
            super(itemView);
            image = itemView.findViewById(imageViewId);
        }
    }
}