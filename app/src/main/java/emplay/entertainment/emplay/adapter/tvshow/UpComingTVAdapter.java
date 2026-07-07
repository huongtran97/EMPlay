package emplay.entertainment.emplay.adapter.tvshow;

import android.content.Context;
import android.view.View;

import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.PaginatableAdapter;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;
import emplay.entertainment.emplay.tool.BadgeHelper;

public class UpComingTVAdapter extends PaginatableAdapter<TVShowModel> {

    public interface OnItemClickListener {
        void onItemClick(TVShowModel tvShow, View sharedElement);
    }

    public UpComingTVAdapter(Context context, List<TVShowModel> data, OnItemClickListener listener) {
        super(context, data, listener::onItemClick);
    }

    @Override protected int getLayoutRes() { return R.layout.item_up_coming_tv; }
    @Override protected int getImageViewId() { return R.id.header; }

    @Override
    protected void bindBadge(PosterViewHolder holder, TVShowModel item) {
        if (holder.badge == null) return;
        BadgeHelper.applyTVStatusBadge(holder.badge, item.getFirstAirDate(), null);
    }
}