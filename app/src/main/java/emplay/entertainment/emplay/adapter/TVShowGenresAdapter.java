package emplay.entertainment.emplay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.fragment.TVShowsByGenresFragment;
import emplay.entertainment.emplay.models.GenresModel;

public class TVShowGenresAdapter extends RecyclerView.Adapter<TVShowGenresAdapter.GenresViewHolder> {
    private List<GenresModel> genresList;
    private Context context;

    public TVShowGenresAdapter(List<GenresModel> genresList, Context context) {
        this.genresList = genresList;
        this.context = context;
    }

    @NonNull
    @Override
    public GenresViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tv_by_genre_view, parent, false);
        return new GenresViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenresViewHolder holder, int position) {
        GenresModel genre = genresList.get(position);
        holder.genres.setText(genre.getName());
        holder.itemView.setOnClickListener(v -> {
            // Pass both genre ID and genre name
            TVShowsByGenresFragment fragment = TVShowsByGenresFragment.newInstance(genre.getId(), genre.getName());
            FragmentTransaction transaction = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }

    @Override
    public int getItemCount() {
        return genresList.size();
    }

    public class GenresViewHolder extends RecyclerView.ViewHolder {
        TextView genres;

        public GenresViewHolder(@NonNull View itemView) {
            super(itemView);
            genres = itemView.findViewById(R.id.tv_genres);
        }
    }
}

