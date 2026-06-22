package emplay.entertainment.emplay.adapter.movie;

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

import java.util.List;
import java.util.Locale;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.api.movie.CollectionResponse;

public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(int movieId);
    }

    private List<CollectionResponse.Part> parts;
    private final Context context;
    private final OnItemClickListener listener;

    public CollectionAdapter(List<CollectionResponse.Part> parts, Context context,
                             OnItemClickListener listener) {
        this.parts = parts;
        this.context = context;
        this.listener = listener;
    }

    public void updateData(List<CollectionResponse.Part> newParts) {
        this.parts = newParts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_mylist_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CollectionResponse.Part part = parts.get(position);

        holder.tvTitle.setText(part.getTitle());

        String year = (part.getReleaseDate() != null && part.getReleaseDate().length() >= 4)
                ? part.getReleaseDate().substring(0, 4) : "";
        holder.tvMeta.setText(year);

        if (part.getVoteAverage() > 0) {
            holder.tagRating.setText(String.format(Locale.getDefault(), "★ %.1f", part.getVoteAverage()));
            holder.tagRating.setVisibility(View.VISIBLE);
        } else {
            holder.tagRating.setVisibility(View.GONE);
        }

        holder.tagStatus.setVisibility(View.GONE);
        holder.btnOverflow.setVisibility(View.GONE);

        Glide.with(context)
                .load(ImageUrl.of(ImageUrl.THUMBNAIL, part.getPosterPath()))
                .placeholder(R.drawable.bg_poster_placeholder)
                .into(holder.imgThumb);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(part.getId());
        });
    }

    @Override
    public int getItemCount() {
        return parts != null ? parts.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView tvTitle, tvMeta, tagRating, tagStatus;
        ImageButton btnOverflow;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumb = itemView.findViewById(R.id.img_thumb);
            tvTitle = itemView.findViewById(R.id.tv_item_title);
            tvMeta = itemView.findViewById(R.id.tv_item_meta);
            tagRating = itemView.findViewById(R.id.tag_rating);
            tagStatus = itemView.findViewById(R.id.tag_status);
            btnOverflow = itemView.findViewById(R.id.btn_overflow);
        }
    }
}