package emplay.entertainment.emplay.adapter.movie;

import android.content.Context;

import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.BasePosterAdapter;
import emplay.entertainment.emplay.models.movie.MovieModel;

/**
 * Poster grid for the movie genre browser — thin wrapper over BasePosterAdapter.
 */
public class MovieByGenreAdapter extends BasePosterAdapter<MovieModel> {

    public interface OnItemClickListener {
        void onItemClick(MovieModel movie);
    }

    public MovieByGenreAdapter(List<MovieModel> data, Context context, OnItemClickListener listener) {
        super(context, data, listener::onItemClick);
    }

    @Override
    protected int getLayoutRes() { return R.layout.movie_by_genre_item; }

    @Override
    protected int getImageViewId() { return R.id.poster; }
}
