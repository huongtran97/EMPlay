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
import emplay.entertainment.emplay.models.TVShowModel;

public class SuggestionTVAdapter extends RecyclerView.Adapter<SuggestionTVAdapter.SuggestionTVViewHolder> {

    private final List<TVShowModel> suggestionTVList;
    private final Context context;
    private final OnItemClickListener onItemClickListener;

    public SuggestionTVAdapter(List<TVShowModel> suggestionList, Context context, OnItemClickListener onItemClickListener) {
        this.suggestionTVList = suggestionList;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(TVShowModel tv);
    }

    @NonNull
    @Override
    public SuggestionTVViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_result_suggestion_item, parent, false);
        return new SuggestionTVViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionTVViewHolder holder, int position) {
        TVShowModel tvModel = suggestionTVList.get(position);
        holder.name.setText(tvModel.getName());
        holder.ratingBar.setRating((float) (tvModel.getVoteAverage() / 2));
        holder.firstAirDate.setText(tvModel.getFirstAirDate());
        Glide.with(context)
                .load("https://image.tmdb.org/t/p/w500" + tvModel.getPosterPath())
                .into(holder.poster);

        holder.bind(tvModel, onItemClickListener);
    }

    @Override
    public int getItemCount() {
        return suggestionTVList.size();
    }

    public class SuggestionTVViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        RatingBar ratingBar;
        TextView firstAirDate;
        ImageView poster;

        public SuggestionTVViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.suggestion_title);
            ratingBar = itemView.findViewById(R.id.suggestion_rating_bar);
            firstAirDate = itemView.findViewById(R.id.suggestion_release_date);
            poster = itemView.findViewById(R.id.poster);
        }

        public void bind(TVShowModel tv, OnItemClickListener onItemClickListener) {
            itemView.setOnClickListener(v -> onItemClickListener.onItemClick(tv));
        }
    }
}
