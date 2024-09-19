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
        // Handle null or empty poster path
        if (tvModel.getPosterPath() != null && !tvModel.getPosterPath().isEmpty()) {
            // Load the poster path
            Glide.with(context)
                    .load("https://image.tmdb.org/t/p/w500/" + tvModel.getPosterPath())
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.poster);
        } else if (tvModel.getBackdropPath() != null && !tvModel.getBackdropPath().isEmpty()) {
            // Load the backdrop path
            Glide.with(context)
                    .load("https://image.tmdb.org/t/p/w500/" + tvModel.getBackdropPath())
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.poster);
        } else {
            // Load a drawable resource as a fallback when both paths are null or empty
            Glide.with(context)
                    .load(R.drawable.placeholder_image)
                    .into(holder.poster);
        }

        holder.bind(tvModel, onItemClickListener);
    }



    @Override
    public int getItemCount() {
        return suggestionTVList.size();
    }

    public class SuggestionTVViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;

        public SuggestionTVViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.poster);
        }

        public void bind(TVShowModel tv, OnItemClickListener onItemClickListener) {
            itemView.setOnClickListener(v -> onItemClickListener.onItemClick(tv));
        }
    }

}
