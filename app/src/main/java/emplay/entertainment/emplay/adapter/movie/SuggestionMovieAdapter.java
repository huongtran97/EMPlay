package emplay.entertainment.emplay.adapter.movie;

import android.content.Context;

import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.BasePosterAdapter;
import emplay.entertainment.emplay.models.movie.MovieModel;

/**
 * "More like this" grid on the movie detail screen — poster-only tiles backed by BasePosterAdapter.
 */
public class SuggestionMovieAdapter extends BasePosterAdapter<MovieModel> {

    public interface OnItemClickListener {
        void onItemClick(MovieModel movie);
    }

    public SuggestionMovieAdapter(List<MovieModel> data, Context context, OnItemClickListener listener) {
        super(context, data, listener::onItemClick);
    }

    @Override
    protected int getLayoutRes() { return R.layout.search_result_suggestion_item; }

    @Override
    protected int getImageViewId() { return R.id.poster; }
}
