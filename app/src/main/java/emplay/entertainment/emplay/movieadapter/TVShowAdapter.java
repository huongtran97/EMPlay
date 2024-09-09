package emplay.entertainment.emplay.movieadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.models.TVShowModel;

public class TVShowAdapter extends RecyclerView.Adapter<TVShowAdapter.MyViewHolder> {

    private final Context mContext;
    private final List<TVShowModel> mData;
    private final OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(TVShowModel tv);
    }

    public TVShowAdapter(Context context, List<TVShowModel> data, OnItemClickListener onItemClickListener) {
        this.mContext = context;
        this.mData = new ArrayList<>(data); // Initialize with a new ArrayList to avoid null issues
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
        View v = LayoutInflater.from(mContext).inflate(R.layout.tvshow_popular_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        TVShowModel tv = mData.get(position);

        Glide.with(mContext)
                .load("https://image.tmdb.org/t/p/w500/" + tv.getPosterPath())
                .into(holder.img);

        holder.bind(tv, onItemClickListener);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView img;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.header);
        }

        public void bind(final TVShowModel item, final OnItemClickListener onItemClickListener) {
            itemView.setOnClickListener(v -> onItemClickListener.onItemClick(item));
        }
    }
}
