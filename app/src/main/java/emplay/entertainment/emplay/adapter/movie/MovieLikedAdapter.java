package emplay.entertainment.emplay.adapter.movie;

import android.content.Context;
import android.view.View;
import java.util.List;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.BasePosterAdapter;
import emplay.entertainment.emplay.models.movie.MovieModel;

public class MovieLikedAdapter extends BasePosterAdapter<MovieModel> {

    public interface OnItemClickListener {
        void onItemClick(MovieModel movie, View sharedElement);
    }

    public MovieLikedAdapter(Context context, List<MovieModel> movies, OnItemClickListener listener) {
        super(context, movies, listener::onItemClick);
    }

    @Override
    protected int getLayoutRes() { return R.layout.item_liked_movie; }

    @Override
    protected int getImageViewId() { return R.id.header; }
}
