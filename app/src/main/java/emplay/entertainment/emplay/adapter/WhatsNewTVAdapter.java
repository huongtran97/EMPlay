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

import java.util.List;
import java.util.Locale;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.ImageUrl;
import emplay.entertainment.emplay.models.TVShowModel;
import emplay.entertainment.emplay.tool.BadgeHelper;

public class WhatsNewTVAdapter extends RecyclerView.Adapter<WhatsNewTVAdapter.ViewHolder> {

    private final Context context;
    private final List<TVShowModel> tvShowList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(TVShowModel tvShow);
    }

    public WhatsNewTVAdapter(Context context, List<TVShowModel> tvShowList, OnItemClickListener listener) {
        this.context = context;
        this.tvShowList = tvShowList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_whats_new_tvshow, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TVShowModel tvShow = tvShowList.get(position);
        holder.tvTitle.setText(tvShow.getName());
        
        String firstAirDate = tvShow.getFirstAirDate();
        String year = (firstAirDate != null && firstAirDate.length() >= 4) 
                ? firstAirDate.substring(0, 4) : "";
        holder.tvMeta.setText(year + " • " + tvShow.getOriginalLanguage().toUpperCase(Locale.ROOT));

        BadgeHelper.applyWhatsNewBadge(holder.tvBadge, firstAirDate);

        Glide.with(context)
                .load(ImageUrl.THUMBNAIL + tvShow.getBackdropPath())
                .placeholder(R.drawable.bg_poster_placeholder)
                .into(holder.ivThumb);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(tvShow));
    }

    @Override
    public int getItemCount() {
        return tvShowList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<TVShowModel> newList) {
        tvShowList.clear();
        tvShowList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvTitle, tvMeta, tvBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.ivEpisodeThumb);
            tvTitle = itemView.findViewById(R.id.tvShowTitle);
            tvMeta = itemView.findViewById(R.id.tvEpisodeMeta);
            tvBadge = itemView.findViewById(R.id.tvEpisodeBadge);
        }
    }
}
