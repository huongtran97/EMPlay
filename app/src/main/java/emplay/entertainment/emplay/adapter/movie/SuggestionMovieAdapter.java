package emplay.entertainment.emplay.adapter.movie;

import android.content.Context;
import android.view.View;

import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.PaginatableAdapter;
import emplay.entertainment.emplay.models.movie.MovieModel;

public class SuggestionMovieAdapter extends PaginatableAdapter<MovieModel> {

    public interface OnItemClickListener {
        void onItemClick(MovieModel movie, View sharedElement);
    }

    public SuggestionMovieAdapter(List<MovieModel> data, Context context, OnItemClickListener listener) {
        super(context, data, listener::onItemClick);
    }

    @Override protected int getLayoutRes() { return R.layout.item_movie; }
    @Override protected int getImageViewId() { return R.id.header; }
}