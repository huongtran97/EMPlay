package emplay.entertainment.emplay.fragment.layout;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.List;

import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.fragment.common.BaseSearchFragment;
import emplay.entertainment.emplay.models.common.MediaItem;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;
import emplay.entertainment.emplay.api.tvshow.TVShowResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchTVShowsFragment extends BaseSearchFragment<TVShowModel> {

    @Override
    protected void fetchGenres() {
        loadGenres(apiService.getGenresTVShows(TMDBpath.genresTVShows()));
    }

    @Override
    protected void fetchTrending() {
        safeEnqueue(apiService.getPopularTVShows(TMDBpath.poppularTVShows(), 1), new Callback<TVShowResponse>() {
            @Override
            public void onResponse(@NonNull Call<TVShowResponse> call, @NonNull Response<TVShowResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TVShowModel> popular = response.body().getResults();
                    if (popular != null) trendingAdapter.updateData(popular);
                }
            }
            @Override public void onFailure(@NonNull Call<TVShowResponse> call, @NonNull Throwable t) {
                Log.e("SearchTVShowsFragment", "Failed to fetch popular TV", t);
            }
        });
    }

    @Override
    protected void onTrendingSelected(MediaItem item) {
        navigateToMedia(item);
    }

    @Override
    protected boolean isTVTab() { return true; }
}