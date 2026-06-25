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
    public static String trendingAll(String timeWindow) { return "3/trending/all/" + timeWindow; }
    public static String nowPlayingMovies() { return "3/movie/now_playing"; }
    public static String topRatedMovies() { return "3/movie/top_rated"; }
    public static String trendingTVShows() { return "3/trending/tv/week"; }
    public static String poppularMovies() {return "3/movie/popular"; }
    public static String poppularTVShows() {return "3/tv/popular"; }
    public static String onAirTVShows() { return "3/tv/on_the_air"; }
    public static String movieDetails(int movieId) { return "3/movie/" + movieId; }
    public static String movieCredits(int movieId) { return "3/movie/" + movieId + "/credits"; }
    public static String movieSimilar(int movieId) { return "3/movie/" + movieId + "/similar"; }
    public static String movieTrailer(int movieId) { return "3/movie/" + movieId + "/videos"; }
    public static String tvShowDetails(int tvId) { return "3/tv/" + tvId; }
    public static String tvShowCredits(int tvId) { return "3/tv/" + tvId + "/credits"; }
    public static String tvShowAggregateCredits(int tvId) { return "3/tv/" + tvId + "/aggregate_credits"; }
    public static String tvShowSimilar(int tvId) { return "3/tv/" + tvId + "/similar"; }
    public static String tvShowTrailer(int tvId) { return "3/tv/" + tvId + "/videos"; }
    public static String searchMovies() { return "3/search/movie"; }
    public static String searchTVShows() { return "3/search/tv"; }
    public static String searchMulti() { return "3/search/multi"; }
    public static String discoverMovies() { return "3/discover/movie"; }
    public static String discoverTVShows() { return "3/discover/tv"; }
    public static String genresMovie() { return "3/genre/movie/list"; }
    public static String genresTVShows() { return "3/genre/tv/list"; }
    public static String personDetails(int personId) { return "3/person/" + personId; }
    public static String personCredits(int personId) { return "3/person/" + personId + "/combined_credits"; }
    public static String tvSeasonDetails(int tvId, int seasonNumber) { return "3/tv/" + tvId + "/season/" + seasonNumber; }
    public static String tvProvider(int tvId) { return "3/tv/" + tvId + "/watch/providers"; }
    public static String movieProvider(int movieId) { return "3/movie/" + movieId + "/watch/providers"; }
    public static String movieReleaseDates(int movieId) { return "3/movie/" + movieId + "/release_dates"; }
    public static String collectionDetails(int collectionId) { return "3/collection/" + collectionId; }
    public static String tvShowContentRatings(int tvId) { return "3/tv/" + tvId + "/content_ratings"; }
    public static String countries() { return "3/configuration/countries"; }

    // TMDB account / watchlist paths
    public static String accountWatchlistMovies(String accountId) { return "3/account/" + accountId + "/watchlist/movies"; }
    public static String accountWatchlistTVShows(String accountId) { return "3/account/" + accountId + "/watchlist/tv"; }
    public static String accountAddToWatchlist(String accountId) { return "3/account/" + accountId + "/watchlist"; }
    public static String movieAccountStates(int movieId) { return "3/movie/" + movieId + "/account_states"; }
    public static String tvAccountStates(int tvId) { return "3/tv/" + tvId + "/account_states"; }


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