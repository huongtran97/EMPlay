package emplay.entertainment.emplay.adapter.tvshow;

import android.annotation.SuppressLint;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
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
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;

/**
 *  Search results list for TV shows — same compact row layout as SearchMovieAdapter.
 *  Tapping a row opens the TV show detail screen via OnItemClickListener.
 */
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item, parent, false);
        return new TVViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TVViewHolder holder, int position) {
        TVShowModel tv = tvList.get(position);

        if (tv != null) {
            holder.title.setText(tv.getName());
            String fad = tv.getFirstAirDate();
            holder.firstAirDate.setText((fad != null && fad.length() >= 4) ? fad.substring(0, 4) : "—");
            if (VERSION.SDK_INT >= VERSION_CODES.N) {
                holder.language.setText("Language: " + tv.getOriginalLanguage());
            }
            holder.rating.setText(String.format("%.1f", tv.getVoteAverage()));

            Glide.with(holder.itemView.getContext())
                    .load(ImageUrl.THUMBNAIL + tv.getPosterPath())
                    .into(holder.poster);

            holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(tv));
        }
    }

    @Override
    public int getItemCount() {
        return tvList.size();
    }

    public static class TVViewHolder extends RecyclerView.ViewHolder {
        TextView title, firstAirDate, language, rating;
        ImageView poster;

        public TVViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.search_title);
            firstAirDate = itemView.findViewById(R.id.search_release_date);
            language = itemView.findViewById(R.id.search_language);
            poster = itemView.findViewById(R.id.search_poster);
            rating = itemView.findViewById(R.id.tvPosterRating);
        }
    }
}




