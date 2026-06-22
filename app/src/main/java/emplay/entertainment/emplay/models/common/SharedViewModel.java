package emplay.entertainment.emplay.models.common;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import emplay.entertainment.emplay.api.common.ApiClient;
import emplay.entertainment.emplay.api.common.MovieApiService;
import emplay.entertainment.emplay.api.common.MultiSearchResponse;
import emplay.entertainment.emplay.api.common.TMDBpath;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SharedViewModel extends ViewModel {

    private final MovieApiService apiService = ApiClient.getClient().create(MovieApiService.class);

    private final MutableLiveData<List<MultiSearchResult>> multiSearchResults = new MutableLiveData<>();

    private final MutableLiveData<Boolean> lastSearchWasTVShow = new MutableLiveData<>();

    private final MutableLiveData<Integer> selectedGenreId = new MutableLiveData<>();

    public LiveData<List<MultiSearchResult>> getMultiSearchResults() {
        return multiSearchResults;
    }

    public LiveData<Boolean> getLastSearchWasTVShow() {
        return lastSearchWasTVShow;
    }

    public void setLastSearchWasTVShow(boolean wasTVShow) {
        lastSearchWasTVShow.setValue(wasTVShow);
    }

    public LiveData<Integer> getSelectedGenreId() {
        return selectedGenreId;
    }

    public void setSelectedGenreId(int genreId) {
        selectedGenreId.setValue(genreId);
    }

    public void searchMulti(String query) {
        apiService.searchMulti(TMDBpath.searchMulti(), query, "en-US").enqueue(new Callback<MultiSearchResponse>() {
            @Override public void onResponse(Call<MultiSearchResponse> call, Response<MultiSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    multiSearchResults.setValue(response.body().getResults());
                }
            }
            @Override public void onFailure(Call<MultiSearchResponse> call, Throwable t) {}
        });
    }
}