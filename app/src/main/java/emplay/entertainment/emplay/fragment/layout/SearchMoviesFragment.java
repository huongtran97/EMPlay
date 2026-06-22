package emplay.entertainment.emplay.fragment.layout;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.List;

import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.fragment.common.BaseSearchFragment;
import emplay.entertainment.emplay.models.common.MediaItem;
import emplay.entertainment.emplay.models.movie.MovieModel;
import emplay.entertainment.emplay.api.movie.MovieResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchMoviesFragment extends BaseSearchFragment<MovieModel> {

    @Override
    protected void fetchGenres() {
        loadGenres(apiService.getGenresMovie(TMDBpath.genresMovie()));
    }

    @Override
    protected void fetchTrending() {
        safeEnqueue(apiService.getPopularMovies(TMDBpath.poppularMovies(), 1), new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MovieModel> popular = response.body().getResults();
                    if (popular != null) trendingAdapter.updateData(popular);
                }
            }
            @Override public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                Log.e("SearchMoviesFragment", "Failed to fetch popular movies", t);
            }
        });
    }

    @Override
    protected void onTrendingSelected(MediaItem item) {
        navigateToMedia(item);
    }

    @Override
    protected boolean isTVTab() { return false; }
}