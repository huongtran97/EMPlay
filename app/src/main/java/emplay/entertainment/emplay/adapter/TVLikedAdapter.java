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
import emplay.entertainment.emplay.database.DatabaseHelper;
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
        // Remove the item from the database
        TVShowModel tvShowToDelete = mData.get(position);
        databaseHelper.deleteTV(tvShowToDelete.getTvShowId());

        // Remove the item from the list and notify the adapter
        mData.remove(position);
        notifyItemRemoved(position);
    }

    public interface OnItemClickListener {
        void onItemClick(TVShowModel tv);
    }

    public void updateData(List<TVShowModel> newTVShows) {
        mData.clear();
        mData.addAll(newTVShows);
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

        // Load the poster and set the name directly
        String fullUrl = tv.getPosterPath() != null ? "https://image.tmdb.org/t/p/w500" + tv.getPosterPath() : null;
        Glide.with(mContext)
                .load(fullUrl)
                .into(holder.img);
        holder.name.setText(tv.getName());

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
    }
}
