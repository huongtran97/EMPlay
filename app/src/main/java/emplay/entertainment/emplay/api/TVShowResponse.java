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
    @SerializedName("page")
    private int page;
    @SerializedName("total_pages")
    private int total_pages;
    @SerializedName("total_results")
    private int total_results;


    public List<TVShowModel> getResults() {
        return results;
    }

    public void setResults(List<TVShowModel> results) {
        this.results = results;
    }

    public TVShowModel getTvShowDetails() {
        return tvShowDetails;
    }

    public void setTvShowDetails(TVShowModel tvShowDetails) {
        this.tvShowDetails = tvShowDetails;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotal_pages() {
        return total_pages;
    }

    public void setTotal_pages(int total_pages) {
        this.total_pages = total_pages;
    }

    public int getTotal_results() {
        return total_results;
    }

    public void setTotal_results(int total_results) {
        this.total_results = total_results;
    }
}
