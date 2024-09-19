package emplay.entertainment.emplay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.models.TVShowModel;

public class UpComingTVAdapter extends RecyclerView.Adapter<UpComingTVAdapter.MyViewHolder> {
    private final Context mContext;
    private final List<TVShowModel> mData;
    private final OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(TVShowModel tv);
    }

    public UpComingTVAdapter(Context mContext, List<TVShowModel> mData, OnItemClickListener onItemClickListener) {
        this.mContext = mContext;
        this.mData = mData;
        this.onItemClickListener = onItemClickListener;
    }

    public void updateData(List<TVShowModel> newData) {
        if (newData != null) {
            mData.clear();
            mData.addAll(newData);
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.up_coming_tv_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        TVShowModel tv = mData.get(position);

        Glide.with(mContext)
                .load("https://image.tmdb.org/t/p/w500/" + tv.getPosterPath())
                .into(holder.img);

        // Set the click listener directly in onBindViewHolder
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(tv));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView img;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.header);
        }
    }
}
