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

    public TVShowByGenreAdapter(List<TVShowModel> tvByGenreList,  Context mContext, OnItemClickListener onItemClickListener) {
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
        View view1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.tv_by_genre_view, parent, false);
        return new ViewHolder(view, view1);

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

        // Bind click listener
        holder.bind(tvShowModel, onItemClickListener);
    }

    @Override
    public int getItemCount() {
        return (tvByGenreList.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView genreName;
        View view1; // Store reference to second view

        // Constructor for handling two views
        public ViewHolder(View view, View view1) {
            super(view); // Pass the main item view to the parent constructor
            this.view1 = view1;

            // Initialize views from the first layout (view)
            poster = view.findViewById(R.id.poster);
            genreName = view1.findViewById(R.id.movie_genres);
        }

        public void bind(TVShowModel tvShowModel, OnItemClickListener onItemClickListener) {
            itemView.setOnClickListener(v -> onItemClickListener.onItemClick(tvShowModel));

        }
    }

}
