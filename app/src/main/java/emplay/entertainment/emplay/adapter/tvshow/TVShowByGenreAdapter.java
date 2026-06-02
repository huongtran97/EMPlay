package emplay.entertainment.emplay.adapter.tvshow;

import android.content.Context;

import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.BasePosterAdapter;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;

/**
 * Poster grid for the TV show genre browser — thin wrapper over BasePosterAdapter.
 */
public class TVShowByGenreAdapter extends BasePosterAdapter<TVShowModel> {

    public interface OnItemClickListener {
        void onItemClick(TVShowModel tvShowModel);
    }

    public TVShowByGenreAdapter(List<TVShowModel> data, Context context, OnItemClickListener listener) {
        super(context, data, listener::onItemClick);
    }

    @Override
    protected int getLayoutRes() { return R.layout.tv_by_genre_item; }

    @Override
    protected int getImageViewId() { return R.id.poster; }
}
