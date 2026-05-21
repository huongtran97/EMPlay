package emplay.entertainment.emplay.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class MovieModel implements Serializable, MediaItem {
    @SerializedName("id")
    private int id;
    
    @SerializedName("title")
    private String title;
    
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
    
    @SerializedName("release_date")
    private String releaseDate;
    
    @SerializedName("runtime")
    private int runtime;
    
    @SerializedName("genres")
    private List<String> genres;
    
    @SerializedName("cast")
    private List<String> cast;
    
    private String username;

    public MovieModel() {}

    public MovieModel(int id, String title, String posterPath) {
        this.id = id;
        this.title = title;
        this.posterPath = posterPath;
    }

    public MovieModel(int id, String title, double voteAverage, String posterPath,
                      String backdropPath, String overview, String originalLanguage,
                      String releaseDate, int runtime, List<String> genres) {
        this.id = id;
        this.title = title;
        this.voteAverage = voteAverage;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.overview = overview;
        this.originalLanguage = originalLanguage;
        this.releaseDate = releaseDate;
        this.runtime = runtime;
        this.genres = genres;
    }

    // MediaItem implementation
    @Override
    public int getMediaId() { return id; }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getVoteAverage() { return voteAverage; }
    public void setVoteAverage(double voteAverage) { this.voteAverage = voteAverage; }

    @Override
    public String getPosterPath() { return posterPath; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }

    @Override
    public String getBackdropPath() { return backdropPath; }
    public void setBackdropPath(String backdropPath) { this.backdropPath = backdropPath; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public String getOriginalLanguage() { return originalLanguage; }
    public void setOriginalLanguage(String originalLanguage) { this.originalLanguage = originalLanguage; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public int getRuntime() { return runtime; }
    public void setRuntime(int runtime) { this.runtime = runtime; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }

    public List<String> getCast() { return cast; }
    public void setCast(List<String> cast) { this.cast = cast; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
