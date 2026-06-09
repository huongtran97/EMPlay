package emplay.entertainment.emplay.adapter.movie;

import java.util.List;

import emplay.entertainment.emplay.adapter.common.BaseSearchAdapter;
import emplay.entertainment.emplay.models.movie.MovieModel;

public class SearchMovieAdapter extends BaseSearchAdapter<MovieModel> {

    public SearchMovieAdapter(List<MovieModel> list, OnItemClickListener<MovieModel> listener) {
        super(list, listener);
    }

    @Override
    protected String getDisplayDate(MovieModel item) { return item.getReleaseDate(); }

    @Override
    protected String getLanguage(MovieModel item) { return item.getOriginalLanguage(); }
}