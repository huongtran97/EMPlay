package emplay.entertainment.emplay.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import emplay.entertainment.emplay.models.MovieModel;

import com.google.gson.annotations.SerializedName;

public class MovieResponse {
    @SerializedName("results")
    private List<MovieModel> results;

    @SerializedName("movie")
    private MovieModel movieDetails;

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
}





