package emplay.entertainment.emplay.movieadapter;

import android.content.Context;
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
import emplay.entertainment.emplay.models.MovieModel;
import emplay.entertainment.emplay.models.TVShowModel;
import emplay.entertainment.emplay.movieadapter.SuggestionTVAdapter.OnItemClickListener;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder> {

    private List<MovieModel> suggestionList;
    private final Context context;
    private final OnItemClickListener onItemClickListener;

    public SuggestionAdapter(List<MovieModel> suggestionList, Context context, OnItemClickListener onItemClickListener) {
        this.suggestionList = suggestionList;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(MovieModel movie);
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_result_suggestion_item, parent,false);
        return new SuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        MovieModel movieModel = suggestionList.get(position);
        holder.title.setText(movieModel.getTitle());
        holder.ratingBar.setRating((float) (movieModel.getVoteAverage() / 2));
        holder.releaseDate.setText(movieModel.getReleaseDate());
        Glide.with(context)
                .load("https://image.tmdb.org/t/p/w500" + movieModel.getPosterPath())
                .into(holder.poster);

        holder.bind(movieModel, onItemClickListener);

    }

    @Override
    public int getItemCount() {
        return suggestionList.size();
    }

    public class SuggestionViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        RatingBar ratingBar;
        TextView releaseDate;
        ImageView poster;
        public SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.suggestion_title);
            ratingBar = itemView.findViewById(R.id.suggestion_rating_bar);
            releaseDate = itemView.findViewById(R.id.suggestion_release_date);
            poster = itemView.findViewById(R.id.poster);
        }

        public void bind(MovieModel movie, OnItemClickListener onItemClickListener) {
            itemView.setOnClickListener(v -> onItemClickListener.onItemClick(movie));
        }
    }
}
