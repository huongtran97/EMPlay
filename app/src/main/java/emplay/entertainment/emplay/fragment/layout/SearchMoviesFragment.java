package emplay.entertainment.emplay.fragment.layout;

import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import emplay.entertainment.emplay.adapter.movie.SearchMovieAdapter;
import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.api.movie.MovieResponse;
import emplay.entertainment.emplay.fragment.common.BaseSearchFragment;
import emplay.entertainment.emplay.fragment.details.MovieResultDetailsFragment;
import emplay.entertainment.emplay.fragment.genre.MovieByGenresFragment;
import emplay.entertainment.emplay.models.common.GenresModel;
import emplay.entertainment.emplay.models.common.MediaItem;
import emplay.entertainment.emplay.models.movie.MovieModel;
import emplay.entertainment.emplay.tool.LanguageMapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchMoviesFragment extends BaseSearchFragment<MovieModel> {

    private final List<MovieModel> searchMovieList = new ArrayList<>();
    private SearchMovieAdapter searchAdapter;

    @Override
    protected void setupSearchResultAdapter() {
        searchAdapter = new SearchMovieAdapter(searchMovieList,
                m -> navigateTo(MovieResultDetailsFragment.newInstance(m.getMovieId())));
        rvSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSearchResults.setAdapter(searchAdapter);
    }

    @Override
    protected void setupResultObserver() {
        viewModel.getSearchResults().observe(getViewLifecycleOwner(),
                movies -> searchAdapter.updateData(movies));
    }

    @Override
    protected void fetchGenres() {
        loadGenres(apiService.getGenresMovie(TMDBpath.genresMovie()));
    }

    @Override
    protected void fetchTrending() {
        safeEnqueue(apiService.getTrendingMovies(TMDBpath.trendingMovies()),
                new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MovieResponse> call,
                                           @NonNull Response<MovieResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<MovieModel> results = response.body().getResults();
                            if (results != null) {
                                Collections.sort(results, (a, b) ->
                                        Double.compare(b.getVoteAverage(), a.getVoteAverage()));
                                int size = Math.min(results.size(), 5);
                                trendingList.clear();
                                trendingList.addAll(results.subList(0, size));
                                trendingAdapter.notifyItemRangeChanged(0, size);
                            }
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Failed to load trending movies", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void performSearchQuery(String query) {
        viewModel.setLastSearchWasTVShow(false);
        safeEnqueue(apiService.searchMovies(TMDBpath.searchMovies(), query),
                new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MovieResponse> call,
                                           @NonNull Response<MovieResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<MovieModel> movies = response.body().getResults();
                            List<MovieModel> filtered = new ArrayList<>();
                            if (movies != null) {
                                for (MovieModel m : movies) {
                                    if (m.getPosterPath() != null) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            m.setOriginalLanguage(LanguageMapper.getLanguageName(m.getOriginalLanguage()));
                                        }
                                        filtered.add(m);
                                    }
                                }
                            }
                            viewModel.setSearchResults(filtered);
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Search failed. Check your connection.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void clearSearchResults() {
        searchAdapter.updateData(new ArrayList<>());
        viewModel.setSearchResults(new ArrayList<>());
    }

    @Override
    protected void onGenreSelected(GenresModel genre) {
        navigateTo(MovieByGenresFragment.newInstance(genre.getId(), genre.getName()));
    }

    @Override
    protected void onTrendingSelected(MediaItem item) {
        navigateTo(MovieResultDetailsFragment.newInstance(item.getMediaId()));
    }

    @Override protected boolean isTVTab() { return false; }
    @Override protected void onMovieTabClick() { /* already on movie tab */ }

    @Override
    protected void onTVTabClick() {
        viewModel.setIsTVShowSearch(true);
        navigateTo(new SearchTVShowsFragment());
    }
}