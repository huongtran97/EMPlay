package emplay.entertainment.emplay.api.movie;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import emplay.entertainment.emplay.models.movie.MovieModel;

public class MovieDetailsResponse {

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
    private List<Genre> genres;

    @SerializedName("cast")
    private List<Cast> cast;

    @SerializedName("movie")
    private MovieModel movieDetails;

    @SerializedName("production_countries")
    private List<ProductionCountry> production_countries;

    @SerializedName("belongs_to_collection")
    private BelongsToCollection belongsToCollection;

    public MovieDetailsResponse(int id, String title, String posterPath, String overview, float rating) {
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getRuntime() {
        return runtime;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    public List<Cast> getCast() {
        return cast;
    }

    public void setCast(List<Cast> cast) {
        this.cast = cast;
    }

    public MovieModel getMovieDetails() {
        return movieDetails;
    }

    public void setMovieDetails(MovieModel movieDetails) {
        this.movieDetails = movieDetails;
    }


    public static class Genre {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;


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
    }

    public List<ProductionCountry> getProduction_countries() {
        return production_countries;
    }

    public void setProduction_countries(List<ProductionCountry> production_countries) {
        this.production_countries = production_countries;
    }



    public static class Cast {
        @SerializedName("cast_id")
        private int castId;

        @SerializedName("name")
        private String castName;

        @SerializedName("profile_path")
        private String profilePath;

            public int getCastId() {
            return castId;
        }

        public void setCastId(int castId) {
            this.castId = castId;
        }

        public String getCastName() {
            return castName;
        }

        public void setCastName(String castName) {
            this.castName = castName;
        }

        public String getProfilePath() {
            return profilePath;
        }

        public void setProfilePath(String profilePath) {
            this.profilePath = profilePath;
        }


    }
    public BelongsToCollection getBelongsToCollection() { return belongsToCollection; }

    public static class BelongsToCollection {
        @SerializedName("id")
        private int id;
        @SerializedName("name")
        private String name;

        public int getId() { return id; }
        public String getName() { return name; }
    }

    public static class ProductionCountry {
        @SerializedName("iso_3166_1")
        private String isoCodeMovie;
        @SerializedName("name")
        private String name;

        public ProductionCountry(String iso_3166_1, String name) {
            this.isoCodeMovie = iso_3166_1;
            this.name = name;
        }

        // Getters and Setters

        public String getIsoCodeMovie() {
            return isoCodeMovie;
        }

        public void setIsoCodeMovie(String isoCodeMovie) {
            this.isoCodeMovie = isoCodeMovie;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
