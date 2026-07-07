package emplay.entertainment.emplay;

import org.junit.Test;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import emplay.entertainment.emplay.api.common.TMDBpath;

import static org.junit.Assert.*;

public class TMDBpathTest {

    // Static path methods
    @Test
    public void trendingMovies_returnsCorrectPath() {
        assertEquals("3/trending/movie/week", TMDBpath.trendingMovies());
    }

    @Test
    public void trendingAll_embedsTimeWindow() {
        assertEquals("3/trending/all/week", TMDBpath.trendingAll("week"));
        assertEquals("3/trending/all/day", TMDBpath.trendingAll("day"));
    }

    @Test
    public void nowPlayingMovies_returnsCorrectPath() {
        assertEquals("3/movie/now_playing", TMDBpath.nowPlayingMovies());
    }

    @Test
    public void topRatedMovies_returnsCorrectPath() {
        assertEquals("3/movie/top_rated", TMDBpath.topRatedMovies());
    }

    @Test
    public void trendingTVShows_returnsCorrectPath() {
        assertEquals("3/trending/tv/week", TMDBpath.trendingTVShows());
    }

    @Test
    public void popularMovies_returnsCorrectPath() {
        assertEquals("3/movie/popular", TMDBpath.poppularMovies());
    }

    @Test
    public void popularTVShows_returnsCorrectPath() {
        assertEquals("3/tv/popular", TMDBpath.poppularTVShows());
    }

    @Test
    public void onAirTVShows_returnsCorrectPath() {
        assertEquals("3/tv/on_the_air", TMDBpath.onAirTVShows());
    }

    // ID-parameterized paths
    @Test
    public void movieDetails_embedsId() {
        assertEquals("3/movie/123", TMDBpath.movieDetails(123));
    }

    @Test
    public void movieCredits_embedsId() {
        assertEquals("3/movie/456/credits", TMDBpath.movieCredits(456));
    }

    @Test
    public void movieSimilar_embedsId() {
        assertEquals("3/movie/7/similar", TMDBpath.movieSimilar(7));
    }

    @Test
    public void movieTrailer_embedsId() {
        assertEquals("3/movie/1/videos", TMDBpath.movieTrailer(1));
    }

    @Test
    public void tvShowDetails_embedsId() {
        assertEquals("3/tv/99", TMDBpath.tvShowDetails(99));
    }

    @Test
    public void tvShowCredits_embedsId() {
        assertEquals("3/tv/10/credits", TMDBpath.tvShowCredits(10));
    }

    @Test
    public void tvShowAggregateCredits_embedsId() {
        assertEquals("3/tv/10/aggregate_credits", TMDBpath.tvShowAggregateCredits(10));
    }

    @Test
    public void tvShowSimilar_embedsId() {
        assertEquals("3/tv/5/similar", TMDBpath.tvShowSimilar(5));
    }

    @Test
    public void tvShowTrailer_embedsId() {
        assertEquals("3/tv/5/videos", TMDBpath.tvShowTrailer(5));
    }

    @Test
    public void tvSeasonDetails_embedsBothIds() {
        assertEquals("3/tv/1/season/2", TMDBpath.tvSeasonDetails(1, 2));
        assertEquals("3/tv/100/season/0", TMDBpath.tvSeasonDetails(100, 0));
    }

    @Test
    public void tvProvider_embedsId() {
        assertEquals("3/tv/42/watch/providers", TMDBpath.tvProvider(42));
    }

    @Test
    public void movieProvider_embedsId() {
        assertEquals("3/movie/42/watch/providers", TMDBpath.movieProvider(42));
    }

    @Test
    public void movieReleaseDates_embedsId() {
        assertEquals("3/movie/8/release_dates", TMDBpath.movieReleaseDates(8));
    }

    @Test
    public void collectionDetails_embedsId() {
        assertEquals("3/collection/55", TMDBpath.collectionDetails(55));
    }

    @Test
    public void tvShowContentRatings_embedsId() {
        assertEquals("3/tv/3/content_ratings", TMDBpath.tvShowContentRatings(3));
    }

    @Test
    public void movieReviews_embedsId() {
        assertEquals("3/movie/9/reviews", TMDBpath.movieReviews(9));
    }

    @Test
    public void tvReviews_embedsId() {
        assertEquals("3/tv/9/reviews", TMDBpath.tvReviews(9));
    }

    @Test
    public void personDetails_embedsId() {
        assertEquals("3/person/200", TMDBpath.personDetails(200));
    }

    @Test
    public void personCredits_embedsId() {
        assertEquals("3/person/200/combined_credits", TMDBpath.personCredits(200));
    }

    // Search / Discover / Genre paths
    @Test
    public void searchMovies_returnsCorrectPath() {
        assertEquals("3/search/movie", TMDBpath.searchMovies());
    }

    @Test
    public void searchTVShows_returnsCorrectPath() {
        assertEquals("3/search/tv", TMDBpath.searchTVShows());
    }

    @Test
    public void searchMulti_returnsCorrectPath() {
        assertEquals("3/search/multi", TMDBpath.searchMulti());
    }

    @Test
    public void discoverMovies_returnsCorrectPath() {
        assertEquals("3/discover/movie", TMDBpath.discoverMovies());
    }

    @Test
    public void discoverTVShows_returnsCorrectPath() {
        assertEquals("3/discover/tv", TMDBpath.discoverTVShows());
    }

    @Test
    public void genresMovie_returnsCorrectPath() {
        assertEquals("3/genre/movie/list", TMDBpath.genresMovie());
    }

    @Test
    public void genresTVShows_returnsCorrectPath() {
        assertEquals("3/genre/tv/list", TMDBpath.genresTVShows());
    }

    @Test
    public void countries_returnsCorrectPath() {
        assertEquals("3/configuration/countries", TMDBpath.countries());
    }

    // Account / Watchlist paths
    @Test
    public void accountWatchlistMovies_embedsAccountId() {
        assertEquals("3/account/abc123/watchlist/movies", TMDBpath.accountWatchlistMovies("abc123"));
    }

    @Test
    public void accountWatchlistTVShows_embedsAccountId() {
        assertEquals("3/account/abc123/watchlist/tv", TMDBpath.accountWatchlistTVShows("abc123"));
    }

    @Test
    public void accountAddToWatchlist_embedsAccountId() {
        assertEquals("3/account/abc123/watchlist", TMDBpath.accountAddToWatchlist("abc123"));
    }

    @Test
    public void movieAccountStates_embedsId() {
        assertEquals("3/movie/77/account_states", TMDBpath.movieAccountStates(77));
    }

    @Test
    public void tvAccountStates_embedsId() {
        assertEquals("3/tv/77/account_states", TMDBpath.tvAccountStates(77));
    }

    // Dynamic date methods
    @Test
    public void todayDate_matchesCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String expected = sdf.format(Calendar.getInstance().getTime());
        assertEquals(expected, TMDBpath.todayDate());
    }

    @Test
    public void thirtyDaysFromNow_isCorrect() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 30);
        String expected = sdf.format(cal.getTime());
        assertEquals(expected, TMDBpath.thirtyDaysFromNow());
    }
}