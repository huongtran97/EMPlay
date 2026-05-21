package emplay.entertainment.emplay.adapter;

import android.content.Context;

import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.models.MovieModel;

public class MovieLikedAdapter extends BasePosterAdapter<MovieModel> {

    private final DatabaseHelper databaseHelper;

    public interface OnItemClickListener {
        void onItemClick(MovieModel movie);
    }

    public MovieLikedAdapter(Context context, List<MovieModel> movies,
                             OnItemClickListener listener, DatabaseHelper databaseHelper) {
        super(context, movies, listener::onItemClick);
        this.databaseHelper = databaseHelper;
    }

    @Override
    protected int getLayoutRes() { return R.layout.liked_movie_item; }

    @Override
    protected int getImageViewId() { return R.id.liked_poster; }

    @Override
    public void removeItem(int position) {
        if (position < 0 || position >= mData.size()) return;
        MovieModel item = mData.get(position);
        super.removeItem(position);
        databaseHelper.deleteMovie(item.getId());
    }
}
