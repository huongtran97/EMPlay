package emplay.entertainment.emplay.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import emplay.entertainment.emplay.models.MovieModel;

public class SharedViewModel extends ViewModel {
    private MutableLiveData<List<MovieModel>> searchResults = new MutableLiveData<>();

    public LiveData<List<MovieModel>> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<MovieModel> results) {
        searchResults.setValue(results);
    }


}

