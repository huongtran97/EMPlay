package emplay.entertainment.emplay.adapter.tvshow;

import android.content.Context;

import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.BasePosterAdapter;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;
import emplay.entertainment.emplay.tool.BadgeHelper;

/**
 * Upcoming TV shows row on the Home screen — thin wrapper over BasePosterAdapter.
 */
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

    @Override
    protected void bindBadge(PosterViewHolder holder, TVShowModel item) {
        if (holder.badge == null) return;
        BadgeHelper.applyTVStatusBadge(holder.badge, item.getFirstAirDate(), item.getNextEpisodeExists());
    }
}
