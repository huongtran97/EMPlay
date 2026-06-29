package emplay.entertainment.emplay.models.movie;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

import emplay.entertainment.emplay.models.common.MediaItem;

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

    @SerializedName("genre_ids")
    private List<Integer> genreIds;
    
    @SerializedName("cast")
    private List<String> cast;
    
    private String username;
    private String savedTimestamp;
    private transient String theatricalDate;
    private transient String certification;

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

    @Override
    public String getSavedTimestamp() { return savedTimestamp; }
    public void setSavedTimestamp(String savedTimestamp) { this.savedTimestamp = savedTimestamp; }

    // Getters and Setters
    public int getMovieId() { return id; }
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

    public List<Integer> getGenreIds() { return genreIds; }
    public void setGenreIds(List<Integer> genreIds) { this.genreIds = genreIds; }

    public List<String> getCast() { return cast; }
    public void setCast(List<String> cast) { this.cast = cast; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getTheatricalDate() { return theatricalDate; }
    public void setTheatricalDate(String theatricalDate) { this.theatricalDate = theatricalDate; }

    public String getCertification() { return certification; }
    public void setCertification(String certification) { this.certification = certification; }
}
