package emplay.entertainment.emplay.api.common;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Helper class for building TMDB API path strings.
 *
 * Instead of hardcoding paths everywhere, use these methods:
 *   apiService.getMovieDetails(TmdbPaths.movieDetails(123))
 *   apiService.getMoviesTrailer(TmdbPaths.movieTrailer(123))
 */
public class TMDBpath {

    public static String trendingMovies() { return "3/trending/movie/week"; }
    public static String trendingTVShows() { return "3/trending/tv/week"; }
    public static String movieDetails(int movieId) { return "3/movie/" + movieId; }
    public static String movieCredits(int movieId) { return "3/movie/" + movieId + "/credits"; }
    public static String movieSimilar(int movieId) { return "3/movie/" + movieId + "/similar"; }
    public static String movieTrailer(int movieId) { return "3/movie/" + movieId + "/videos"; }
    public static String tvShowDetails(int tvId) { return "3/tv/" + tvId; }
    public static String tvCountry(int tvId) { return "3/tv/" + tvId; }
    public static String tvShowCredits(int tvId) { return "3/tv/" + tvId + "/credits"; }
    public static String tvShowSimilar(int tvId) { return "3/tv/" + tvId + "/similar"; }
    public static String tvShowTrailer(int tvId) { return "3/tv/" + tvId + "/videos"; }
    public static String searchMovies() { return "3/search/movie"; }
    public static String searchTVShows() { return "3/search/tv"; }
    public static String discoverMovies() { return "3/discover/movie"; }
    public static String discoverTVShows() { return "3/discover/tv"; }
    public static String genresMovie() { return "3/genre/movie/list"; }
    public static String genresTVShows() { return "3/genre/tv/list"; }
    public static String personDetails(int personId) { return "3/person/" + personId; }
    public static String personCredits(int personId) { return "3/person/" + personId + "/combined_credits"; }
    public static String tvSeasonDetails(int tvId, int seasonNumber) { return "3/tv/" + tvId + "/season/" + seasonNumber; }
    public static String tvProvider(int tvId) { return "3/tv/" + tvId + "/watch/providers"; }
    public static String movieProvider(int movieId) { return "3/movie/" + movieId + "/watch/providers"; }


    //Dynamic date for upcoming movies/TV

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    /**
     * Returns today's date in TMDB format: yyyy-MM-dd
     */
    public static String todayDate() {
        return DATE_FORMAT.format(Calendar.getInstance().getTime());
    }

    /**
     * Returns date 30 days from today in TMDB format: yyyy-MM-dd
     */
    public static String thirtyDaysFromNow() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 30);
        return DATE_FORMAT.format(cal.getTime());
    }

}