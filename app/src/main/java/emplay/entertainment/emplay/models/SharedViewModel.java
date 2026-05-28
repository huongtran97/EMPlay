package emplay.entertainment.emplay.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared state scoped to MainActivity — survives fragment transactions and config changes.
 *
 */
public class SharedViewModel extends ViewModel {
    private final MutableLiveData<List<MovieModel>> searchResults = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<TVShowModel>> searchTVResults = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isTVShowSearch = new MutableLiveData<>(false);

    /**
     * Remembers whether the user's last search was for TV shows, so the Search tab
     * reopens to the right screen when you tap it again.
     */
    private final MutableLiveData<Boolean> lastSearchWasTVShow = new MutableLiveData<>();

    // Passed to MovieByGenresFragment / TVShowsByGenresFragment when a genre chip is tapped.
    private final MutableLiveData<Integer> selectedGenreId = new MutableLiveData<>();

    public LiveData<List<MovieModel>> getSearchResults() {
        return searchResults;
    }

    public LiveData<List<TVShowModel>> getSearchTVResults() {
        return searchTVResults;
    }

    public LiveData<Boolean> getIsTVShowSearch() {
        return isTVShowSearch;
    }

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
