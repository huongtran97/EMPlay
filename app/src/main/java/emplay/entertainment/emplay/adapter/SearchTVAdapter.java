package emplay.entertainment.emplay.adapter;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.models.TVShowModel;


public class SearchTVAdapter extends RecyclerView.Adapter<SearchTVAdapter.TVViewHolder> {

    private final List<TVShowModel> tvList;
    private final OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(TVShowModel tv);
    }

    public SearchTVAdapter(List<TVShowModel> tvList, OnItemClickListener onItemClickListener) {
        this.tvList = tvList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public TVViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_movie_item, parent, false);
        return new TVViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TVViewHolder holder, int position) {
        TVShowModel tv = tvList.get(position);

        if (tv != null) {
            holder.title.setText(tv.getName());
            holder.firstAirDate.setText("First Air Date: " + tv.getFirstAirDate());
            if (VERSION.SDK_INT >= VERSION_CODES.N) {
                holder.language.setText("Language: " + tv.getOriginalLanguage());
            }
            holder.ratingBar.setRating((float) (tv.getVoteAverage() / 2));

            Glide.with(holder.itemView.getContext())
                    .load("https://image.tmdb.org/t/p/w500" + tv.getPosterPath())
                    .into(holder.poster);

            holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(tv));
        }
    }

    @Override
    public int getItemCount() {
        return tvList.size();
    }

    public static class TVViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView firstAirDate;
        TextView language;
        ImageView poster;
        RatingBar ratingBar;

        public TVViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.search_title);
            firstAirDate = itemView.findViewById(R.id.search_release_date);
            language = itemView.findViewById(R.id.search_language);
            poster = itemView.findViewById(R.id.search_poster);
            ratingBar = itemView.findViewById(R.id.search_ratingBar);
        }
    }
}




