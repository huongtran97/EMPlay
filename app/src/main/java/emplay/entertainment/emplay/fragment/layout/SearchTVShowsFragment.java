package emplay.entertainment.emplay.fragment.layout;

import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import emplay.entertainment.emplay.adapter.tvshow.SearchTVAdapter;
import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.api.tvshow.TVShowResponse;
import emplay.entertainment.emplay.fragment.common.BaseSearchFragment;
import emplay.entertainment.emplay.fragment.details.TVShowResultDetailsFragment;
import emplay.entertainment.emplay.fragment.genre.TVShowsByGenresFragment;
import emplay.entertainment.emplay.models.common.GenresModel;
import emplay.entertainment.emplay.models.common.MediaItem;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;
import emplay.entertainment.emplay.tool.LanguageMapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchTVShowsFragment extends BaseSearchFragment<TVShowModel> {

    private final List<TVShowModel> searchTVList = new ArrayList<>();
    private SearchTVAdapter searchAdapter;

    @Override
    protected void setupSearchResultAdapter() {
        searchAdapter = new SearchTVAdapter(searchTVList,
                tv -> navigateTo(TVShowResultDetailsFragment.newInstance(tv.getTVShowId())));
        rvSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSearchResults.setAdapter(searchAdapter);
    }

    @Override
    protected void setupResultObserver() {
        viewModel.getSearchTVResults().observe(getViewLifecycleOwner(),
                tvShows -> searchAdapter.updateData(tvShows));
    }

    @Override
    protected void fetchGenres() {
        loadGenres(apiService.getGenresTVShows(TMDBpath.genresTVShows()));
    }

    @Override
    protected void fetchTrending() {
        safeEnqueue(apiService.getTrendingTVShows(TMDBpath.trendingTVShows()),
                new Callback<TVShowResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TVShowResponse> call,
                                           @NonNull Response<TVShowResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<TVShowModel> results = response.body().getResults();
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
                    public void onFailure(@NonNull Call<TVShowResponse> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Failed to load trending TV shows", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void performSearchQuery(String query) {
        viewModel.setLastSearchWasTVShow(true);
        safeEnqueue(apiService.searchTVShows(TMDBpath.searchTVShows(), query),
                new Callback<TVShowResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TVShowResponse> call,
                                           @NonNull Response<TVShowResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<TVShowModel> tvShows = response.body().getResults();
                            List<TVShowModel> filtered = new ArrayList<>();
                            if (tvShows != null) {
                                for (TVShowModel tv : tvShows) {
                                    if (tv.getPosterPath() != null) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            tv.setOriginalLanguage(LanguageMapper.getLanguageName(tv.getOriginalLanguage()));
                                        }
                                        filtered.add(tv);
                                    }
                                }
                            }
                            viewModel.setSearchTVResults(filtered);
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<TVShowResponse> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Search failed. Check your connection.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void clearSearchResults() {
        searchAdapter.updateData(new ArrayList<>());
        viewModel.setSearchTVResults(new ArrayList<>());
    }

    @Override
    protected void onGenreSelected(GenresModel genre) {
        navigateTo(TVShowsByGenresFragment.newInstance(genre.getId(), genre.getName()));
    }

    @Override
    protected void onTrendingSelected(MediaItem item) {
        navigateTo(TVShowResultDetailsFragment.newInstance(item.getMediaId()));
    }

    @Override protected boolean isTVTab() { return true; }
    @Override protected void onTVTabClick() { /* already on TV tab */ }

    @Override
    protected void onMovieTabClick() {
        viewModel.setIsTVShowSearch(false);
        navigateTo(new SearchMoviesFragment());
    }
}