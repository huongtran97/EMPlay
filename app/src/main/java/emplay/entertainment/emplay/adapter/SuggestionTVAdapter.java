package emplay.entertainment.emplay.adapter;

import android.content.Context;

import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.models.TVShowModel;

public class SuggestionTVAdapter extends BasePosterAdapter<TVShowModel> {

    public interface OnItemClickListener {
        void onItemClick(TVShowModel tv);
    }

    public SuggestionTVAdapter(List<TVShowModel> data, Context context, OnItemClickListener listener) {
        super(context, data, listener::onItemClick);
    }

    @Override
    protected int getLayoutRes() { return R.layout.search_result_suggestion_item; }

    @Override
    protected int getImageViewId() { return R.id.poster; }
}
