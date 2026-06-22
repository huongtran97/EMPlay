package emplay.entertainment.emplay.adapter.tvshow;

import android.content.Context;
import android.view.View;
import java.util.List;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.BasePosterAdapter;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;

public class TVShowAdapter extends BasePosterAdapter<TVShowModel> {
    private boolean useBackdrop = false;

    public interface OnItemClickListener {
        void onItemClick(TVShowModel tvShow, View sharedElement);
    }

    public TVShowAdapter(Context context, List<TVShowModel> data, OnItemClickListener listener) {
        super(context, data, listener::onItemClick);
    }
    public TVShowAdapter setUseBackdrop(boolean useBackdrop) {
        this.useBackdrop = useBackdrop;
        return this;
    }
    @Override
    protected String getImageUrl(TVShowModel item) {
        if (useBackdrop) {
            String path = item.getBackdropPath();
            if (path != null) return ImageUrl.BACKDROP + path;
        }
        return super.getImageUrl(item);
    }

    @Override
    protected int getLayoutRes() {
        return useBackdrop ? R.layout.tvshow_backdrop_item : R.layout.tvshow_popular_item;
    }

    @Override
    protected int getImageViewId() { return R.id.header; }
}
