package emplay.entertainment.emplay.api.auth.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import emplay.entertainment.emplay.models.movie.MovieModel;

public class TMDBMovieWatchlistResponse {
    @SerializedName("results")
    public List<MovieModel> results;

    @SerializedName("page")
    public int page;

    @SerializedName("total_pages")
    public int totalPages;

    @SerializedName("total_results")
    public int totalResults;
}