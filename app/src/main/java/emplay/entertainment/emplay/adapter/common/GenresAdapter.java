package emplay.entertainment.emplay.adapter.common;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.models.common.GenresModel;

public class GenresAdapter extends RecyclerView.Adapter<GenresAdapter.GenresViewHolder> {

    private final List<GenresModel> genresList;
    private final OnItemClickListener onItemClickListener;

    public GenresAdapter(List<GenresModel> genresList, OnItemClickListener onItemClickListener) {
        this.genresList = genresList;
        this.onItemClickListener = onItemClickListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setGenres(List<GenresModel> genres) {
        genresList.clear();
        if (genres != null) {
            genresList.addAll(genres);
        }
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(GenresModel genresModel);
    }

    @NonNull
    @Override
    public GenresViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.genres_item, parent, false);
        return new GenresViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenresViewHolder holder, int position) {
        GenresModel genre = genresList.get(position);
        holder.genreName.setText(genre.getName());
        
        // Use first letter as Ghost Glyph
        if (genre.getName() != null && !genre.getName().isEmpty()) {
            holder.tvGhostGlyph.setText(genre.getName().substring(0, 1).toUpperCase());
        }

        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(genre));
    }

    @Override
    public int getItemCount() {
        return genresList.size();
    }

    public static class GenresViewHolder extends RecyclerView.ViewHolder {
        TextView genreName, tvGhostGlyph;

        public GenresViewHolder(@NonNull View view) {
            super(view);
            genreName = view.findViewById(R.id.genre_name);
            tvGhostGlyph = view.findViewById(R.id.tvGhostGlyph);
        }
    }
}
