package emplay.entertainment.emplay.adapter;

import android.content.Context;

import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.models.TVShowModel;

public class TVShowAdapter extends BasePosterAdapter<TVShowModel> {

    public interface OnItemClickListener {
        void onItemClick(TVShowModel tv);
    }

    public TVShowAdapter(Context context, List<TVShowModel> data, OnItemClickListener listener) {
        super(context, data, listener::onItemClick);
    }

    @Override
    protected int getLayoutRes() { return R.layout.tvshow_popular_item; }

    @Override
    protected int getImageViewId() { return R.id.header; }
}
