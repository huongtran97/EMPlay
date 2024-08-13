package emplay.entertainment.emplay.api;

import java.util.List;

import emplay.entertainment.emplay.models.TVShowModel;

public class TVShowSimilarResponse {
    private List<TVShowModel> results;

    public List<TVShowModel> getResults() {
        return results;
    }

    public void setResults(List<TVShowModel> results) {
        this.results = results;
    }
}
