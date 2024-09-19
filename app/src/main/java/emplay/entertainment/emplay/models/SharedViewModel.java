package emplay.entertainment.emplay.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class SharedViewModel extends ViewModel {
    // Initialize with empty lists to avoid null values
    private final MutableLiveData<List<MovieModel>> searchResults = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<TVShowModel>> searchTVResults = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isTVShowSearch = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> lastSearchWasTVShow = new MutableLiveData<>();
    private final MutableLiveData<Integer> selectedGenreId = new MutableLiveData<>();

    // Getters for LiveData
    public LiveData<List<MovieModel>> getSearchResults() {
        return searchResults;
    }

    public LiveData<List<TVShowModel>> getSearchTVResults() {
        return searchTVResults;
    }

    public LiveData<Boolean> getIsTVShowSearch() {
        return isTVShowSearch;
    }

    // Setters for MutableLiveData
    public void setSearchResults(List<MovieModel> results) {
        searchResults.setValue(results);
    }

    public void setSearchTVResults(List<TVShowModel> results) {
        searchTVResults.setValue(results);
    }

    public void setIsTVShowSearch(boolean isTVShowSearch) {
        this.isTVShowSearch.setValue(isTVShowSearch);
    }
    public LiveData<Boolean> getLastSearchWasTVShow() {
        return lastSearchWasTVShow;
    }

    public void setLastSearchWasTVShow(boolean wasTVShow) {
        this.lastSearchWasTVShow.setValue(wasTVShow);
    }

    public LiveData<Integer> getSelectedGenreId() {
        return selectedGenreId;
    }

    public void setSelectedGenreId(int genreId) {
        selectedGenreId.setValue(genreId);
    }
}
