package emplay.entertainment.emplay.fragment.layout;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.fragment.common.BaseSearchFragment;
import emplay.entertainment.emplay.fragment.details.MovieResultDetailsFragment;
import emplay.entertainment.emplay.models.common.GenresModel;
import emplay.entertainment.emplay.models.common.MediaItem;
import emplay.entertainment.emplay.models.movie.MovieModel;
import emplay.entertainment.emplay.adapter.movie.SearchMovieAdapter;
import emplay.entertainment.emplay.api.movie.MovieResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchMoviesFragment extends BaseSearchFragment<MovieModel> {

    private final List<MovieModel> searchMovieList = new ArrayList<>();
    private SearchMovieAdapter searchAdapter;

    @Override
    protected void setupSearchResultAdapter() {
        searchAdapter = new SearchMovieAdapter(searchMovieList, this::onItemClicked);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSearchResults.setAdapter(searchAdapter);
    }

    @Override
    protected void setupResultObserver() {
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), new Observer<List<MovieModel>>() {
            @Override
            public void onChanged(List<MovieModel> movieModels) {
                if (movieModels != null) {
                    searchMovieList.clear();
                    searchMovieList.addAll(movieModels);
                    searchAdapter.updateData(searchMovieList);
                }
            }
        });
    }

    @Override
    protected void fetchGenres() {
        loadGenres(apiService.getGenresMovie(TMDBpath.genresMovie()));
    }

    @Override
    protected void fetchTrending() {
        safeEnqueue(apiService.getTrendingMovies(TMDBpath.trendingMovies()), new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MovieModel> trending = response.body().getResults();
                    if (trending != null) {
                        trendingAdapter.updateData(trending);
                    }
                }
            }
            @Override public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                Log.e("SearchMoviesFragment", "Failed to fetch trending movies", t);
            }
        });
    }

    @Override
    protected void performSearchQuery(String query) {
        viewModel.searchMovies(query);
        svSearchDefault.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.VISIBLE);
    }

    @Override
    protected void clearSearchResults() {
        searchMovieList.clear();
        searchAdapter.updateData(searchMovieList);
    }

    @Override
    protected void onGenreSelected(GenresModel genresModel) {
        // Handle genre selection
    }

    @Override
    protected void onTrendingSelected(MediaItem item) {
        onItemClicked(item, null);
    }

    @Override
    protected boolean isTVTab() { return false; }

    @Override
    protected void onMovieTabClick() {}

    @Override
    protected void onTVTabClick() {
        viewModel.setLastSearchWasTVShow(true);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SearchTVShowsFragment())
                .commit();
    }
}
