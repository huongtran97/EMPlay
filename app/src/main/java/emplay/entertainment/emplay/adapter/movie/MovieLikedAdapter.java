package emplay.entertainment.emplay.adapter.movie;

import android.content.Context;

import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.BasePosterAdapter;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.models.movie.MovieModel;

/**
 *  Liked movies row on the Profile screen. Overrides removeItem() so a swipe-to-delete
 *  also removes the entry from the local SQLite database.
 */
public class MovieLikedAdapter extends BasePosterAdapter<MovieModel> {

    private final DatabaseHelper databaseHelper;
    private final String userId;

    public interface OnItemClickListener {
        void onItemClick(MovieModel movie);
    }

    public MovieLikedAdapter(Context context, List<MovieModel> movies,
                             OnItemClickListener listener, DatabaseHelper databaseHelper, String userId) {
        super(context, movies, listener::onItemClick);
        this.databaseHelper = databaseHelper;
        this.userId = userId;
    }

    @Override
    protected int getLayoutRes() { return R.layout.liked_movie_item; }

    @Override
    protected int getImageViewId() { return R.id.liked_poster; }

    @Override
    protected String getImagePrefix() { return ImageUrl.CARD; }

    @Override
    public void removeItem(int position) {
        if (position < 0 || position >= mData.size()) return;
        MovieModel item = mData.get(position);
        super.removeItem(position);
        databaseHelper.deleteMovie(item.getMovieId(), userId);
    }
}
