package emplay.entertainment.emplay.adapter.movie;

import android.content.Context;
import android.view.View;

import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.PaginatableAdapter;
import emplay.entertainment.emplay.models.movie.MovieModel;
import emplay.entertainment.emplay.tool.BadgeHelper;

public class UpcomingMovieAdapter extends PaginatableAdapter<MovieModel> {

    public interface OnItemClickListener {
        void onItemClick(MovieModel movie, View sharedElement);
    }

    public UpcomingMovieAdapter(Context context, List<MovieModel> data, OnItemClickListener listener) {
        super(context, data, listener::onItemClick);
    }

    @Override protected int getLayoutRes() { return R.layout.item_movie; }
    @Override protected int getImageViewId() { return R.id.header; }

    @Override
    protected void bindBadge(PosterViewHolder holder, MovieModel item) {
        if (holder.badge == null) return;
        BadgeHelper.applyMovieBadge(holder.badge, item.getReleaseDate());
    }
}