package emplay.entertainment.emplay.models.common;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {

    private final MutableLiveData<Boolean> lastSearchWasTVShow = new MutableLiveData<>();
    private final MutableLiveData<Integer> selectedGenreId = new MutableLiveData<>();

    public LiveData<Boolean> getLastSearchWasTVShow() { return lastSearchWasTVShow; }
    public void setLastSearchWasTVShow(boolean wasTVShow) { lastSearchWasTVShow.setValue(wasTVShow); }

    public LiveData<Integer> getSelectedGenreId() { return selectedGenreId; }
    public void setSelectedGenreId(int genreId) { selectedGenreId.setValue(genreId); }
}