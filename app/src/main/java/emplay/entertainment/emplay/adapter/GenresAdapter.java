package emplay.entertainment.emplay.adapter;

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
import emplay.entertainment.emplay.models.GenresModel;

/**
 *  Genre chip grid on the Search tabs. Tapping a chip opens the genre browse screen.
 */
public class GenresAdapter extends RecyclerView.Adapter<GenresAdapter.GenresViewHolder> {
    private final List<GenresModel> genresList;
    private final OnItemClickListener onItemClickListener;

    public GenresAdapter(List<GenresModel> genresList, OnItemClickListener onItemClickListener) {
        this.genresList = genresList != null ? genresList : new ArrayList<>();
        this.onItemClickListener = onItemClickListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setGenres(List<GenresModel> genres) {
        if (genres != null) {
            genresList.clear();
            genresList.addAll(genres);
            notifyDataSetChanged();
        }
    }

    public interface OnItemClickListener {
        void onItemClick(GenresModel genre);
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


        if (genre.getColorRes() != 0) {
            holder.genreName.setTextColor(genre.getColorRes());
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null && position != RecyclerView.NO_POSITION) {
                onItemClickListener.onItemClick(genre);
            }
        });
    }

    @Override
    public int getItemCount() {
        return genresList.size();
    }

    public static class GenresViewHolder extends RecyclerView.ViewHolder {
        TextView genreName;

        public GenresViewHolder(@NonNull View itemView) {
            super(itemView);
            genreName = itemView.findViewById(R.id.genre_name);
        }
    }
}
