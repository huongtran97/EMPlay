package emplay.entertainment.emplay.fragment.layout;

import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.fragment.common.BaseSearchFragment;
import emplay.entertainment.emplay.models.common.MediaItem;
import emplay.entertainment.emplay.models.movie.MovieModel;

public class SearchMoviesFragment extends BaseSearchFragment<MovieModel> {

    @Override
    protected void fetchGenres() {
        loadGenres(apiService.getGenresMovie(TMDBpath.genresMovie()));
    }

    @Override
    protected void onTrendingSelected(MediaItem item) {
        navigateToMedia(item);
    }

    @Override
    protected boolean isTVTab() { return false; }
}