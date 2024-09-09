package emplay.entertainment.emplay.movieadapter;

import android.content.Context;
import android.util.Log;
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
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.models.MovieModel;
import emplay.entertainment.emplay.models.TVShowModel;



public class TVLikedAdapter extends RecyclerView.Adapter<TVLikedAdapter.MyViewHolder> {

    private final Context mContext;
    private final List<TVShowModel> mData;
    private final OnItemClickListener onItemClickListener;
    private final DatabaseHelper databaseHelper;

    public TVLikedAdapter(Context mContext, List<TVShowModel> mData, OnItemClickListener onItemClickListener, DatabaseHelper databaseHelper) {
        this.mContext = mContext;
        this.mData = mData;
        this.onItemClickListener = onItemClickListener;
        this.databaseHelper = databaseHelper;
    }

    public void removeItem(int position) {
        // Check if position is within the valid range
        if (position >= 0 && position < mData.size()) {
            // Retrieve the movie to be deleted
            TVShowModel tvDelete = mData.get(position);

            // Remove the movie from the list
            mData.remove(position);
            notifyItemRemoved(position);

            // Delete the movie from the database
            if (tvDelete != null) {
                databaseHelper.deleteTV(tvDelete.getId());
            }
        } else {
            // Handle the case where position is out of bounds
            Log.e("MovieLikedAdapter", "Invalid position: " + position);
        }
    }


    public interface OnItemClickListener {
        void onItemClick(TVShowModel tv);
    }


    public void updateData(List<TVShowModel> newTVShows) {
        mData.clear();
        if (newTVShows != null) {
            mData.addAll(newTVShows);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.liked_tv_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        TVShowModel tv = mData.get(position);
        holder.bind(tv);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(tv);
            }
        });

    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView name;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.liked_poster);
            name = itemView.findViewById(R.id.liked_name);
        }

        public void bind(TVShowModel tv) {
            String fullUrl = tv.getPosterPath() != null ? "https://image.tmdb.org/t/p/w500" + tv.getPosterPath() : null;
            Glide.with(itemView.getContext())
                    .load(fullUrl)
                    .into(img);
            name.setText(tv.getName());
        }

    }
}

