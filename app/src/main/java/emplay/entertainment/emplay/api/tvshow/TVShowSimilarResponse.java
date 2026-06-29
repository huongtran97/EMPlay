package emplay.entertainment.emplay.api.tvshow;

import java.util.List;

import emplay.entertainment.emplay.models.tvshow.TVShowModel;

public class TVShowSimilarResponse {
    private List<TVShowModel> results;
    private int total_pages;

    public List<TVShowModel> getResults() {
        return results;
    }

    public void setResults(List<TVShowModel> results) {
        this.results = results;
    }

    public int getTotal_pages() {
        return total_pages;
    }
}
