package emplay.entertainment.emplay.adapter.common;

import android.annotation.SuppressLint;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.models.common.GenresModel;

public class GenresAdapter extends RecyclerView.Adapter<GenresAdapter.GenresViewHolder> {

    private static final SparseIntArray GENRE_COLORS = new SparseIntArray();
    static {
        GENRE_COLORS.put(28,    R.color.genre_action);
        GENRE_COLORS.put(12,    R.color.genre_adventure);
        GENRE_COLORS.put(16,    R.color.genre_animation);
        GENRE_COLORS.put(35,    R.color.genre_comedy);
        GENRE_COLORS.put(80,    R.color.genre_crime);
        GENRE_COLORS.put(99,    R.color.genre_documentary);
        GENRE_COLORS.put(18,    R.color.genre_drama);
        GENRE_COLORS.put(10751, R.color.genre_family);
        GENRE_COLORS.put(14,    R.color.genre_fantasy);
        GENRE_COLORS.put(36,    R.color.genre_history);
        GENRE_COLORS.put(27,    R.color.genre_horror);
        GENRE_COLORS.put(10402, R.color.genre_music);
        GENRE_COLORS.put(9648,  R.color.genre_mystery);
        GENRE_COLORS.put(10749, R.color.genre_romance);
        GENRE_COLORS.put(878,   R.color.genre_scifi);
        GENRE_COLORS.put(10770, R.color.genre_tv_movie);
        GENRE_COLORS.put(53,    R.color.genre_thriller);
        GENRE_COLORS.put(10752, R.color.genre_war);
        GENRE_COLORS.put(37,    R.color.genre_western);
        // TV-only genre IDs
        GENRE_COLORS.put(10759, R.color.genre_action_adventure);
        GENRE_COLORS.put(10762, R.color.genre_kids);
        GENRE_COLORS.put(10763, R.color.genre_news);
        GENRE_COLORS.put(10764, R.color.genre_reality);
        GENRE_COLORS.put(10765, R.color.genre_scifi_fantasy);
        GENRE_COLORS.put(10766, R.color.genre_soap);
        GENRE_COLORS.put(10767, R.color.genre_talk);
        GENRE_COLORS.put(10768, R.color.genre_war_politics);
    }

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_genres, parent, false);
        return new GenresViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenresViewHolder holder, int position) {
        GenresModel genre = genresList.get(position);
        holder.genreName.setText(genre.getName());

        // Use first letter as Ghost Glyph
        if (genre.getName() != null && !genre.getName().isEmpty()) {
            holder.tvGhostGlyph.setText(genre.getName().substring(0, 1).toUpperCase(Locale.getDefault()));
        }

        @ColorRes int colorRes = GENRE_COLORS.get(genre.getId(), R.color.text_1);
        int color = ContextCompat.getColor(holder.itemView.getContext(), colorRes);
        holder.genreName.setTextColor(color);
        holder.tvGhostGlyph.setTextColor(color);

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
