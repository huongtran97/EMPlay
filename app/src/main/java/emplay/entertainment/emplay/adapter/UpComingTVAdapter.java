package emplay.entertainment.emplay.adapter;

import android.content.Context;

import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.models.TVShowModel;

public class UpComingTVAdapter extends BasePosterAdapter<TVShowModel> {

    public interface OnItemClickListener {
        void onItemClick(TVShowModel tv);
    }

    public UpComingTVAdapter(Context context, List<TVShowModel> data, OnItemClickListener listener) {
        super(context, data, listener::onItemClick);
    }

    @Override
    protected int getLayoutRes() { return R.layout.up_coming_tv_item; }

    @Override
    protected int getImageViewId() { return R.id.header; }
}
