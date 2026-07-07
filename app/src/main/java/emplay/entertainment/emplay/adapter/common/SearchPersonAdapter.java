package emplay.entertainment.emplay.adapter.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.Locale;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.models.common.MultiSearchResult;
import emplay.entertainment.emplay.tool.GlideImageLoader;

public class SearchPersonAdapter extends ListAdapter<MultiSearchResult, SearchPersonAdapter.ViewHolder> {

    public interface OnClickListener {
        void onClick(MultiSearchResult item);
    }

    private final OnClickListener listener;

    public SearchPersonAdapter(OnClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<MultiSearchResult> DIFF = new DiffUtil.ItemCallback<MultiSearchResult>() {
        @Override public boolean areItemsTheSame(@NonNull MultiSearchResult a, @NonNull MultiSearchResult b) {
            return a.getMediaId() == b.getMediaId();
        }
        @Override public boolean areContentsTheSame(@NonNull MultiSearchResult a, @NonNull MultiSearchResult b) {
            return a.getTitle().equals(b.getTitle());
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_person, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MultiSearchResult item = getItem(position);

        holder.tvName.setText(item.getTitle());
        holder.tvRole.setText(item.getDepartment() != null ? item.getDepartment() : "");

        String profilePath = item.getProfilePath();
        if (profilePath != null && !profilePath.isEmpty()) {
            holder.tvInitials.setVisibility(View.GONE);
            holder.ivAvatar.setVisibility(View.VISIBLE);
            GlideImageLoader.loadCircle(holder.itemView.getContext(),
                    ImageUrl.of(ImageUrl.THUMBNAIL, profilePath),
                    holder.ivAvatar, R.drawable.bg_circle_surface_high);
        } else {
            holder.ivAvatar.setVisibility(View.INVISIBLE);
            holder.tvInitials.setVisibility(View.VISIBLE);
            String name = item.getTitle();
            holder.tvInitials.setText(name.isEmpty() ? "?" : String.valueOf(name.charAt(0)).toUpperCase(Locale.getDefault()));
        }

holder.itemView.setOnClickListener(v -> listener.onClick(item));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivAvatar;
        TextView tvInitials, tvName, tvRole;

        public ViewHolder(@NonNull View v) {
            super(v);
            ivAvatar = v.findViewById(R.id.ivAvatar);
            tvInitials = v.findViewById(R.id.tvInitials);
            tvName = v.findViewById(R.id.tvName);
            tvRole = v.findViewById(R.id.tvRole);
        }
    }
}