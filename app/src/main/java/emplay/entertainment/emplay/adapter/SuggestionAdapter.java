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
import emplay.entertainment.emplay.models.MovieModel;

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

        // Handle null or empty poster path
        if (movieModel.getPosterPath() != null && !movieModel.getPosterPath().isEmpty()) {
            // Load the poster path
            Glide.with(context)
                    .load("https://image.tmdb.org/t/p/w500/" + movieModel.getPosterPath())
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.poster);
        } else if (movieModel.getBackdropPath() != null && !movieModel.getBackdropPath().isEmpty()) {
            // Load the backdrop path
            Glide.with(context)
                    .load("https://image.tmdb.org/t/p/w500/" + movieModel.getBackdropPath())
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.poster);
        } else {
            // Load a drawable resource as a fallback when both paths are null or empty
            Glide.with(context)
                    .load(R.drawable.placeholder_image)
                    .into(holder.poster);
        }

        holder.bind(movieModel, onItemClickListener);

    }

    @Override
    public int getItemCount() {
        return suggestionList.size();
    }

    public class SuggestionViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        public SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.poster);
        }

        public void bind(MovieModel movie, OnItemClickListener onItemClickListener) {
            itemView.setOnClickListener(v -> onItemClickListener.onItemClick(movie));
        }
    }
}
