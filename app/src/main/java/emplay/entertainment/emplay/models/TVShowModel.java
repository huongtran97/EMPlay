package emplay.entertainment.emplay.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class TVShowModel implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("vote_average")
    private double voteAverage;

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("overview")
    private String overview;

    @SerializedName("original_language")
    private String originalLanguage;

    @SerializedName("first_air_date")
    private String firstAirDate;

    @SerializedName("genres")
    private List<String> genres; // Consider changing to List<Genre> if using complex objects

    @SerializedName("crew")
    private List<String> crew; // Consider changing to List<Crew> if using complex objects

    @SerializedName("seasons")
    private List<String> seasons; // Consider changing to List<Season> if using complex objects

    @SerializedName("production_countries")
    private List<String> productionCountries; // Consider changing to List<ProductionCountry> if using complex objects

    @SerializedName("number_of_episodes")
    private int numberOfEpisodes;

    @SerializedName("number_of_seasons")
    private int numberOfSeasons;


    // Constructor with fewer fields
    public TVShowModel(int id, String name, double voteAverage, String posterPath, String overview, String originalLanguage, String firstAirDate) {
        this.id = id;
        this.name = name;
        this.voteAverage = voteAverage;
        this.posterPath = posterPath;
        this.overview = overview;
        this.originalLanguage = originalLanguage;
        this.firstAirDate = firstAirDate;
    }

    public TVShowModel(int id, String name, double voteAverage, String posterPath, String overview, String originalLanguage, String firstAirDate, List<String> seasons, int numberOfEpisodes, List<String> productionCountries, List<String> genres) {
        this.id = id;
        this.name = name;
        this.voteAverage = voteAverage;
        this.posterPath = posterPath;
        this.overview = overview;
        this.originalLanguage = originalLanguage;
        this.firstAirDate = firstAirDate;
        this.seasons = seasons;
        this.numberOfEpisodes = numberOfEpisodes;
        this.productionCountries = productionCountries;
        this.genres = genres;

    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
