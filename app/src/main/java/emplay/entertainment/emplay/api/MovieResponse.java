package emplay.entertainment.emplay.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import emplay.entertainment.emplay.models.MovieModel;

public class MovieResponse {
    @SerializedName("results")
    private List<MovieModel> results;
    @SerializedName("movie")
    private MovieModel movieDetails;
    @SerializedName("page")
    private int page;
    @SerializedName("total_pages")
    private int total_pages;
    @SerializedName("total_results")
    private int total_results;


    public List<MovieModel> getResults() {
        return results;
    }

    public void setResults(List<MovieModel> results) {
        this.results = results;
    }

    public MovieModel getMovieDetails() {
        return movieDetails;
    }

    public void setMovieDetails(MovieModel movieDetails) {
        this.movieDetails = movieDetails;
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