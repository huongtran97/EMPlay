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


/**
 *  * @author Tran Ngoc Que Huong
 *  * @version 1.0
 *
 * Adapter for displaying a list of movies in a Search Movie RecyclerView.
 */
public class SearchTVAdapter extends RecyclerView.Adapter<SearchTVAdapter.TVViewHolder> {

    private final List<TVShowModel> tvList;
    private final OnItemClickListener onItemClickListener;

    /**
     * Interface for handling item click events.
     */
    public interface OnItemClickListener {
        /**
         * Called when a movie item is clicked.
         *
         * @param tv The movie that was clicked.
         */
        void onItemClick(TVShowModel tv);
    }

    /**
     * Constructor for the SearchMovieAdapter.
     *
     * @param tvList          The list of movies to be displayed.
     * @param onItemClickListener The listener for item click events.
     */
    public SearchTVAdapter(List<TVShowModel> tvList, OnItemClickListener onItemClickListener) {
        this.tvList = tvList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public TVViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item, parent, false);
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

    /**
     * ViewHolder for movie items in the RecyclerView.
     */
    public static class TVViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView firstAirDate;
        TextView language;
        ImageView poster;
        RatingBar ratingBar;

        /**
         * Constructor for the MovieViewHolder.
         *
         * @param itemView The view for each movie item.
         */
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




