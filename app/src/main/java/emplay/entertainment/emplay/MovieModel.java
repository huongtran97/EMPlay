package emplay.entertainment.emplay;

/**
 * @author Tran Ngoc Que Huong
 * @version 1.0
 *
 * Represents a movie with its details.
 * <p>
 * This class encapsulates the information about a movie, including its unique identifier, title, rating,
 * poster image path, overview, language, and release date.
 * </p>
 */
public class MovieModel {

    private String id;
    private String name;
    private String vote;
    private String posterPath; // Changed from 'img' to 'posterPath'
    private String overview;
    private String language;
    private String releaseDate;

    /**
     * Default constructor.
     * <p>
     * This constructor is intentionally left empty. It is used by some frameworks and libraries that require
     * a default constructor.
     * </p>
     */
    public MovieModel(String id, String title, String voteAverage, String posterPath, String overview, String originalLanguage) {
    }

    /**
     * Parameterized constructor to initialize a {@code MovieModel} object with specific values.
     *
     * @param id          the unique identifier for the movie
     * @param name        the title of the movie
     * @param vote        the movie's rating or vote average
     * @param posterPath  the path to the movie's poster image
     * @param overview    a brief description or overview of the movie
     * @param language    the language in which the movie is primarily made
     * @param releaseDate the release date of the movie
     */
    public MovieModel(String id, String name, String vote, String posterPath, String overview, String language, String releaseDate) {
        this.id = id;
        this.name = name;
        this.vote = vote;
        this.posterPath = posterPath;
        this.overview = overview;
        this.language = language;
        this.releaseDate = releaseDate;
    }

    /**
     * Gets the unique identifier of the movie.
     *
     * @return the unique identifier of the movie
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the movie.
     *
     * @param id the unique identifier to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the title of the movie.
     *
     * @return the title of the movie
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the title of the movie.
     *
     * @param name the title to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the rating or vote average of the movie.
     *
     * @return the rating of the movie
     */
    public String getVote() {
        return vote;
    }

    /**
     * Sets the rating or vote average of the movie.
     *
     * @param vote the rating to set
     */
    public void setVote(String vote) {
        this.vote = vote;
    }

    /**
     * Gets the path to the movie's poster image.
     *
     * @return the path to the poster image
     */
    public String getPosterPath() {
        return posterPath;
    }

    /**
     * Sets the path to the movie's poster image.
     *
     * @param posterPath the path to set
     */
    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    /**
     * Gets a brief description or overview of the movie.
     *
     * @return the overview of the movie
     */
    public String getOverview() {
        return overview;
    }

    /**
     * Sets a brief description or overview of the movie.
     *
     * @param overview the overview to set
     */
    public void setOverview(String overview) {
        this.overview = overview;
    }

    /**
     * Gets the language in which the movie is primarily made.
     *
     * @return the language of the movie
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Sets the language in which the movie is primarily made.
     *
     * @param language the language to set
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Gets the release date of the movie.
     *
     * @return the release date of the movie
     */
    public String getReleaseDate() {
        return releaseDate;
    }

    /**
     * Sets the release date of the movie.
     *
     * @param releaseDate the release date to set
     */
    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }
}
