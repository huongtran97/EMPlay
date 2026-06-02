package emplay.entertainment.emplay.adapter.tvshow;

import android.content.Context;

import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.BasePosterAdapter;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;

/**
 *  Liked TV shows row on the Profile screen.
 *  Swipe-to-delete removes the entry from the local SQLite database.
 */
public class TVLikedAdapter extends BasePosterAdapter<TVShowModel> {

    private final DatabaseHelper databaseHelper;

    public interface OnItemClickListener {
        void onItemClick(TVShowModel tv);
    }

    public TVLikedAdapter(Context context, List<TVShowModel> data,
                          OnItemClickListener listener, DatabaseHelper databaseHelper) {
        super(context, data, listener::onItemClick);
        this.databaseHelper = databaseHelper;
    }

    @Override
    protected int getLayoutRes() { return R.layout.liked_tv_item; }

    @Override
    protected int getImageViewId() { return R.id.liked_poster; }
    @Override
    protected String getImagePrefix() { return ImageUrl.CARD; }

    @Override
    public void removeItem(int position) {
        if (position < 0 || position >= mData.size()) return;
        TVShowModel item = mData.get(position);
        super.removeItem(position);
        databaseHelper.deleteTV(item.getTVShowId());
    }
}
