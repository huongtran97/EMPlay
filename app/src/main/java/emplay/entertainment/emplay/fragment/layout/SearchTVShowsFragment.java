package emplay.entertainment.emplay.fragment.layout;

import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.fragment.common.BaseSearchFragment;
import emplay.entertainment.emplay.models.common.MediaItem;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;

public class SearchTVShowsFragment extends BaseSearchFragment<TVShowModel> {

    @Override
    protected void fetchGenres() {
        loadGenres(apiService.getGenresTVShows(TMDBpath.genresTVShows()));
    }

    @Override
    protected void onTrendingSelected(MediaItem item) {
        navigateToMedia(item);
    }

    @Override
    protected boolean isTVTab() { return true; }
}