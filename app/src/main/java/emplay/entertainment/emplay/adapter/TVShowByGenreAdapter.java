package emplay.entertainment.emplay.adapter;

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

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.models.TVShowModel;

public class TVShowByGenreAdapter extends RecyclerView.Adapter<TVShowByGenreAdapter.ViewHolder> {
    private List<TVShowModel> tvByGenreList;
    private final Context mContext;
    private final OnItemClickListener onItemClickListener;

    public TVShowByGenreAdapter(List<TVShowModel> tvByGenreList, Context mContext, OnItemClickListener onItemClickListener) {
        this.tvByGenreList = tvByGenreList;
        this.mContext = mContext;
        this.onItemClickListener = onItemClickListener;
    }

    public void updateData(List<TVShowModel> newTV) {
        tvByGenreList.clear();
        tvByGenreList.addAll(newTV);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(TVShowModel tvShowModel);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tv_by_genre_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TVShowModel tvShowModel = tvByGenreList.get(position);

        // Load poster using Glide
        if (tvShowModel.getPosterPath() != null && !tvShowModel.getPosterPath().isEmpty()) {
            Glide.with(mContext)
                    .load("https://image.tmdb.org/t/p/w500/" + tvShowModel.getPosterPath())
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.poster);
        } else {
            Glide.with(mContext)
                    .load(R.drawable.placeholder_image)
                    .into(holder.poster);
        }

        // Set the click listener directly in onBindViewHolder
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(tvShowModel));
    }

    @Override
    public int getItemCount() {
        return tvByGenreList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView genreName;

        public ViewHolder(View view) {
            super(view);
            poster = view.findViewById(R.id.poster);
            genreName = view.findViewById(R.id.movie_genres);
        }
    }
}
