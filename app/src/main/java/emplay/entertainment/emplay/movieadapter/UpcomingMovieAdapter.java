package emplay.entertainment.emplay.movieadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.bumptech.glide.Glide;

import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.models.MovieModel;


public class UpcomingMovieAdapter extends RecyclerView.Adapter<UpcomingMovieAdapter.MyViewHolder> {

    private final Context mContext;
    private final List<MovieModel> mData;
    private final OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        /**
         * Called when a movie item is clicked.
         *
         * @param movie The MovieModel object associated with the clicked item.
         */
        void onItemClick(MovieModel movie);
    }

    public UpcomingMovieAdapter(Context mContext, List<MovieModel> mData, OnItemClickListener onItemClickListener) {
        this.mContext = mContext;
        this.mData = mData;
        this.onItemClickListener = onItemClickListener;
    }

    public void updateData(List<MovieModel> newMovies) {
        mData.clear();
        mData.addAll(newMovies);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for a movie item and create a ViewHolder
        View v = LayoutInflater.from(mContext).inflate(R.layout.movie_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // Bind data to the ViewHolder
        MovieModel movie = mData.get(position);

        Glide.with(mContext)
                .load("https://image.tmdb.org/t/p/w500/" + movie.getPosterPath())
                .into(holder.img);

        // Bind the click listener to the item
        holder.bind(movie, onItemClickListener);
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class MyViewHolder extends ViewHolder {
        ImageView img;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            img = itemView.findViewById(R.id.header);
        }
        public void bind(MovieModel item, UpcomingMovieAdapter.OnItemClickListener onItemClickListener) {
            itemView.setOnClickListener(v -> onItemClickListener.onItemClick(item));
        }
    }
}
