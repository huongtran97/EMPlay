package emplay.entertainment.emplay.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import emplay.entertainment.emplay.models.MovieModel;
import emplay.entertainment.emplay.models.TVShowModel;

public class TVShowResponse {
    @SerializedName("results")
    private List<TVShowModel> results;

    @SerializedName("tv")
    private TVShowModel tvShowDetails;

    public List<TVShowModel> getResults() {
        return results;
    }

    public void setResults(List<TVShowModel> results) {
        this.results = results;
    }

    public TVShowModel getTvShowDetails() {
        return tvShowDetails;
    }

    public void setTVShowDetails(TVShowModel tvShowDetails) {
        this.tvShowDetails = tvShowDetails;
    }
}
