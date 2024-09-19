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

        // Load the appropriate image
        String posterUrl = tvModel.getPosterPath() != null && !tvModel.getPosterPath().isEmpty()
                ? "https://image.tmdb.org/t/p/w500/" + tvModel.getPosterPath()
                : tvModel.getBackdropPath() != null && !tvModel.getBackdropPath().isEmpty()
                ? "https://image.tmdb.org/t/p/w500/" + tvModel.getBackdropPath()
                : null;

        Glide.with(context)
                .load(posterUrl != null ? posterUrl : R.drawable.placeholder_image)
                .placeholder(R.drawable.placeholder_image)
                .into(holder.poster);

        // Set the click listener directly here
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(tvModel));
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
    }
}
