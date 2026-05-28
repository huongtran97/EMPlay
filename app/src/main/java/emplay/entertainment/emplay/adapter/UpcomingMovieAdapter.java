package emplay.entertainment.emplay.adapter;

import android.content.Context;

import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.models.MovieModel;

/**
 * Upcoming movies row on the Home screen — thin wrapper over BasePosterAdapter.
 */
public class UpcomingMovieAdapter extends BasePosterAdapter<MovieModel> {

    public interface OnItemClickListener {
        void onItemClick(MovieModel movie);
    }

    public UpcomingMovieAdapter(Context context, List<MovieModel> data, OnItemClickListener listener) {
        super(context, data, listener::onItemClick);
    }

    @Override
    protected int getLayoutRes() { return R.layout.movie_item; }

    @Override
    protected int getImageViewId() { return R.id.header; }
}
