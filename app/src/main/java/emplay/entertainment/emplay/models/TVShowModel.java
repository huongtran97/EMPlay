package emplay.entertainment.emplay.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class TVShowModel implements Serializable {
    @SerializedName("id")
    private int tvShowId;
    @SerializedName("name")
    private String name;
    @SerializedName("vote_average")
    private double voteAverage;
    @SerializedName("poster_path")
    private String posterPath;
    @SerializedName("backdrop_path")
    private String backdropPath;
    @SerializedName("overview")
    private String overview;
    @SerializedName("original_language")
    private String originalLanguage;
    @SerializedName("first_air_date")
    private String firstAirDate;
    @SerializedName("genres")
    private List<String> genres;
    @SerializedName("crew")
    private List<String> crew;
    @SerializedName("seasons")
    private List<String> seasons;
    @SerializedName("production_countries")
    private List<String> productionCountries;
    @SerializedName("number_of_episodes")
    private int numberOfEpisodes;
    @SerializedName("number_of_seasons")
    private int numberOfSeasons;

    private String username;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public TVShowModel(int id, String name, double voteAverage, String posterPath, String backdropPath, String overview, String originalLanguage, String firstAirDate, List<String> seasons, int numberOfEpisodes, List<String> productionCountries, List<String> genres) {
        this.tvShowId = id;
        this.name = name;
        this.voteAverage = voteAverage;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.overview = overview;
        this.originalLanguage = originalLanguage;
        this.firstAirDate = firstAirDate;
        this.seasons = seasons;
        this.numberOfEpisodes = numberOfEpisodes;
        this.productionCountries = productionCountries;
        this.genres = genres;

    }

    public TVShowModel(int id, String name, double voteAverage, String posterPath, String overview) {
        this.tvShowId = id;
        this.name = name;
        this.voteAverage = voteAverage;
        this.posterPath = posterPath;
        this.overview = overview;
    }

    public TVShowModel(int id, String name, double voteAverage, String posterPath, String overview, String originalLanguage, String firstAirDate) {
        this.tvShowId = id;
        this.name = name;
        this.voteAverage = voteAverage;
        this.posterPath = posterPath;
        this.overview = overview;
        this.originalLanguage = originalLanguage;
        this.firstAirDate = firstAirDate;
    }

    public TVShowModel(int id, String name, String posterPath) {
        this.tvShowId = id;
        this.name = name;
        this.posterPath = posterPath;
    }


    public TVShowModel(int id, String name, String posterPath, List<String> seasons, int numberOfEpisodes) {
        this.tvShowId = id;
        this.name = name;
        this.posterPath = posterPath;
        this.seasons = seasons;
        this.numberOfEpisodes = numberOfEpisodes;
    }

    public TVShowModel(int id, String name, double voteAverage, String posterPath, String backdropPath, String overview, String originalLanguage, String firstAirDate, int numberOfSeasons, int numberOfEpisodes, List<String> productionCountries, List<String> genres) {
        this.tvShowId = id;
        this.name = name;
        this.voteAverage = voteAverage;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.overview = overview;
        this.originalLanguage = originalLanguage;
        this.firstAirDate = firstAirDate;
        this.genres = genres;
        this.productionCountries = productionCountries;
        this.numberOfEpisodes = numberOfEpisodes;
        this.numberOfSeasons = numberOfSeasons;
    }


    // Getters and Setters
    public int getTvShowId() {
        return tvShowId;
    }

    public void setTvShowId(int tvShowId) {
        this.tvShowId = tvShowId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }
    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    public void setOriginalLanguage(String originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    public String getFirstAirDate() {
        return firstAirDate;
    }

    public void setFirstAirDate(String firstAirDate) {
        this.firstAirDate = firstAirDate;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public List<String> getCrew() {
        return crew;
    }

    public void setCrew(List<String> crew) {
        this.crew = crew;
    }

    public List<String> getSeasons() {
        return seasons;
    }

    public void setSeasons(List<String> seasons) {
        this.seasons = seasons;
    }

    public List<String> getProductionCountries() {
        return productionCountries;
    }

    public void setProductionCountries(List<String> productionCountries) {
        this.productionCountries = productionCountries;
    }

    public int getNumberOfEpisodes() {
        return numberOfEpisodes;
    }

    public void setNumberOfEpisodes(int numberOfEpisodes) {
        this.numberOfEpisodes = numberOfEpisodes;
    }

    public int getNumberOfSeasons() {
        return numberOfSeasons;
    }

    public void setNumberOfSeasons(int numberOfSeasons) {
        this.numberOfSeasons = numberOfSeasons;
    }
}
