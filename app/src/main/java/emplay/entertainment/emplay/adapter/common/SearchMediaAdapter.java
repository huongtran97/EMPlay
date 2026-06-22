package emplay.entertainment.emplay.adapter.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.Locale;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.models.common.MultiSearchResult;

public class SearchMediaAdapter extends ListAdapter<MultiSearchResult, SearchMediaAdapter.ViewHolder> {

    public interface OnClickListener {
        void onClick(MultiSearchResult item, View sharedElement);
    }

    private final OnClickListener listener;

    public SearchMediaAdapter(OnClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<MultiSearchResult> DIFF = new DiffUtil.ItemCallback<MultiSearchResult>() {
        @Override public boolean areItemsTheSame(@NonNull MultiSearchResult a, @NonNull MultiSearchResult b) {
            return a.getMediaId() == b.getMediaId()
                    && a.getMediaType() != null && a.getMediaType().equals(b.getMediaType());
        }
        @Override public boolean areContentsTheSame(@NonNull MultiSearchResult a, @NonNull MultiSearchResult b) {
            return a.getTitle().equals(b.getTitle()) && a.getDisplayYear().equals(b.getDisplayYear());
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_media, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MultiSearchResult item = getItem(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvMeta.setText(item.getDisplayYear());
        holder.tvRating.setText(item.getVoteAverage() > 0
                ? String.format(Locale.getDefault(), "★ %.1f", item.getVoteAverage())
                : "");

        boolean isTV = "tv".equals(item.getMediaType());
        holder.badgeTv.setVisibility(isTV ? View.VISIBLE : View.GONE);
        holder.badgeTheater.setVisibility(View.GONE);

        Glide.with(holder.itemView.getContext())
                .load(ImageUrl.of(ImageUrl.THUMBNAIL, item.getPosterPath()))
                .placeholder(R.drawable.bg_poster_placeholder)
                .into(holder.ivPoster);

        holder.ivPoster.setTransitionName("poster_" + item.getMediaId());
        holder.itemView.setOnClickListener(v -> listener.onClick(item, holder.ivPoster));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivPoster;
        TextView tvTitle, tvMeta, tvRating, badgeTv, badgeTheater;

        public ViewHolder(@NonNull View v) {
            super(v);
            ivPoster = v.findViewById(R.id.ivPoster);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvMeta = v.findViewById(R.id.tvMeta);
            tvRating = v.findViewById(R.id.tvRating);
            badgeTv = v.findViewById(R.id.badgeTv);
            badgeTheater = v.findViewById(R.id.badgeTheater);
        }
    }
}