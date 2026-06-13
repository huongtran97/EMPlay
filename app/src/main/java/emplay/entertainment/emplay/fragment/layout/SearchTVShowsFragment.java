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
import emplay.entertainment.emplay.adapter.tvshow.SearchTVAdapter;
import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.api.tvshow.TVShowResponse;
import emplay.entertainment.emplay.fragment.common.BaseSearchFragment;
import emplay.entertainment.emplay.models.common.GenresModel;
import emplay.entertainment.emplay.models.common.MediaItem;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchTVShowsFragment extends BaseSearchFragment<TVShowModel> {

    private final List<TVShowModel> searchTVList = new ArrayList<>();
    private SearchTVAdapter searchAdapter;

    @Override
    protected void setupSearchResultAdapter() {
        searchAdapter = new SearchTVAdapter(searchTVList, this::onItemClicked);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSearchResults.setAdapter(searchAdapter);
    }

    @Override
    protected void setupResultObserver() {
        viewModel.getSearchTVResults().observe(getViewLifecycleOwner(), new Observer<List<TVShowModel>>() {
            @Override
            public void onChanged(List<TVShowModel> tvShowModels) {
                if (tvShowModels != null) {
                    searchTVList.clear();
                    searchTVList.addAll(tvShowModels);
                    searchAdapter.updateData(searchTVList);
                }
            }
        });
    }

    @Override
    protected void fetchGenres() {
        loadGenres(apiService.getGenresTVShows(TMDBpath.genresTVShows()));
    }

    @Override
    protected void fetchTrending() {
        safeEnqueue(apiService.getTrendingTVShows(TMDBpath.trendingTVShows()), new Callback<TVShowResponse>() {
            @Override
            public void onResponse(@NonNull Call<TVShowResponse> call, @NonNull Response<TVShowResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TVShowModel> trending = response.body().getResults();
                    if (trending != null) {
                        trendingAdapter.updateData(trending);
                    }
                }
            }
            @Override public void onFailure(@NonNull Call<TVShowResponse> call, @NonNull Throwable t) {
                Log.e("SearchTVShowsFragment", "Failed to fetch trending TV", t);
            }
        });
    }

    @Override
    protected void performSearchQuery(String query) {
        viewModel.searchTVShows(query);
        svSearchDefault.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.VISIBLE);
    }

    @Override
    protected void clearSearchResults() {
        searchTVList.clear();
        searchAdapter.updateData(searchTVList);
    }

    @Override
    protected void onGenreSelected(GenresModel genresModel) {
        // Handle genre selected
    }

    @Override
    protected void onTrendingSelected(MediaItem item) {
        onItemClicked(item, null);
    }

    @Override
    protected boolean isTVTab() { return true; }

    @Override
    protected void onTVTabClick() {}

    @Override
    protected void onMovieTabClick() {
        viewModel.setLastSearchWasTVShow(false);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SearchMoviesFragment())
                .commit();
    }
}
