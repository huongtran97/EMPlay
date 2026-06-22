package emplay.entertainment.emplay.adapter.movie;

import android.content.Context;
import android.view.View;
import java.util.List;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.BasePosterAdapter;
import emplay.entertainment.emplay.models.movie.MovieModel;

public class MovieByGenreAdapter extends BasePosterAdapter<MovieModel> {

    public interface OnItemClickListener {
        void onItemClick(MovieModel movie, View sharedElement);
    }

    public MovieByGenreAdapter(Context context, List<MovieModel> data, OnItemClickListener listener) {
        super(context, data, listener::onItemClick);
    }

    @Override
    protected int getLayoutRes() { return R.layout.movie_by_genre_item; }

    @Override
    protected int getImageViewId() { return R.id.header; }
}
