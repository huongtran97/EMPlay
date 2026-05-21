package emplay.entertainment.emplay.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import emplay.entertainment.emplay.api.MoviesTrailerResponses.TrailerModel;

/**
 * Response wrapper for TV show trailer API calls.
 * Reuses {@link MoviesTrailerResponses.TrailerModel} — the trailer data shape is identical.
 */
public class TVShowsTrailerResponses {

    @SerializedName("id")
    private int id;

    @SerializedName("results")
    private List<TrailerModel> results;

    public TVShowsTrailerResponses(int id, List<TrailerModel> results) {
        this.id = id;
        this.results = results;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<TrailerModel> getResults() {
        return results;
    }

    public void setResults(List<TrailerModel> results) {
        this.results = results;
    }
}
