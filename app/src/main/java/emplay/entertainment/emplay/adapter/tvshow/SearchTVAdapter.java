package emplay.entertainment.emplay.adapter.tvshow;

import java.util.List;

import emplay.entertainment.emplay.adapter.common.BaseSearchAdapter;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;

public class SearchTVAdapter extends BaseSearchAdapter<TVShowModel> {

    public SearchTVAdapter(List<TVShowModel> list, OnItemClickListener<TVShowModel> listener) {
        super(list, listener);
    }

    @Override
    protected String getDisplayDate(TVShowModel item) { return item.getFirstAirDate(); }

    @Override
    protected String getLanguage(TVShowModel item) { return item.getOriginalLanguage(); }
}