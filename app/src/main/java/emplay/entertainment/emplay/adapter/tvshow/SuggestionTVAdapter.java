package emplay.entertainment.emplay.adapter.tvshow;

import android.content.Context;
import android.view.View;

import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.PaginatableAdapter;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;

public class SuggestionTVAdapter extends PaginatableAdapter<TVShowModel> {

    public interface OnItemClickListener {
        void onItemClick(TVShowModel tvShow, View sharedElement);
    }

    public SuggestionTVAdapter(List<TVShowModel> data, Context context, OnItemClickListener listener) {
        super(context, data, listener::onItemClick);
    }

    @Override protected int getLayoutRes() { return R.layout.item_tvshow_popular; }
    @Override protected int getImageViewId() { return R.id.header; }
}