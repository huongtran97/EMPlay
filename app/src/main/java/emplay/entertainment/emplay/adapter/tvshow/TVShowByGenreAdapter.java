package emplay.entertainment.emplay.adapter.tvshow;

import android.content.Context;
import android.view.View;
import java.util.List;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.BasePosterAdapter;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;

public class TVShowByGenreAdapter extends BasePosterAdapter<TVShowModel> {

    public interface OnItemClickListener {
        void onItemClick(TVShowModel tvShow, View sharedElement);
    }

    public TVShowByGenreAdapter(Context context, List<TVShowModel> data, OnItemClickListener listener) {
        super(context, data, listener::onItemClick);
    }

    @Override
    protected int getLayoutRes() { return R.layout.item_tv_by_genre; }

    @Override
    protected int getImageViewId() { return R.id.header; }
}
