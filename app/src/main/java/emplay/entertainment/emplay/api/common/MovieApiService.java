package emplay.entertainment.emplay.api.common;

import emplay.entertainment.emplay.api.movie.MovieCreditsResponse;
import emplay.entertainment.emplay.api.movie.MovieDetailsResponse;
import emplay.entertainment.emplay.api.movie.MovieResponse;
import emplay.entertainment.emplay.api.movie.MovieSimilarResponse;
import emplay.entertainment.emplay.api.movie.MoviesTrailerResponses;
import emplay.entertainment.emplay.api.tvshow.SeasonDetailResponse;
import emplay.entertainment.emplay.api.tvshow.TVShowCreditsResponses;
import emplay.entertainment.emplay.api.tvshow.TVShowDetailsResponse;
import emplay.entertainment.emplay.api.tvshow.TVShowResponse;
import emplay.entertainment.emplay.api.tvshow.TVShowSimilarResponse;
import emplay.entertainment.emplay.api.tvshow.TVShowProviderResponse;
import emplay.entertainment.emplay.api.tvshow.TVShowsTrailerResponses;
import emplay.entertainment.emplay.api.movie.MovieReleaseDatesResponse;
import emplay.entertainment.emplay.api.tvshow.TVShowContentRatingsResponse;
import emplay.entertainment.emplay.api.movie.UpComingMovieResponse;
import emplay.entertainment.emplay.api.tvshow.UpComingTVShowsResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Every network call in the app goes through here.
 *
 * All requests share the same endpoint: GET /api/tmdb?path=<tmdb_path>
 * The "path" param tells the proxy which TMDB endpoint to forward to.
 * Extra query params (query, page, genres, etc.) are passed alongside it.
 *
 * The API key is never included here — the proxy injects it server-side,
 * so it's never baked into the APK.
 */
public interface MovieApiService {

    @GET("api/tmdb")
    Call<MovieResponse> getTrendingMovies(
            @Query("path") String path);

    @GET("api/tmdb")
    Call<MovieResponse> getTopRatedMovies(
            @Query("path") String path,
            @Query("page") int page);

    @GET("api/tmdb")
    Call<MovieDetailsResponse> getMovieDetails(
            @Query("path") String path);

    @GET("api/tmdb")
    Call<MovieResponse> searchMovies(
            @Query("path") String path,
            @Query("query") String query);

    @GET("api/tmdb")
    Call<TVShowResponse> searchTVShows(
            @Query("path") String path,
            @Query("query") String query);

    @GET("api/tmdb")
    Call<MovieCreditsResponse> getMovieCredits(
            @Query("path") String path);

    @GET("api/tmdb")
    Call<MovieSimilarResponse> getMovieSimilar(
            @Query("path") String path);

    @GET("api/tmdb")
    Call<TVShowResponse> getTrendingTVShows(
            @Query("path") String path);

    @GET("api/tmdb")
    Call<TVShowDetailsResponse> getTVShowDetails(
            @Query("path") String path);

    @GET("api/tmdb")
    Call<TVShowCreditsResponses> getTVShowCredits(
            @Query("path") String path);

    @GET("api/tmdb")
    Call<TVShowSimilarResponse> getTVShowSimilar(
            @Query("path") String path);

    @GET("api/tmdb")
    Call<UpComingMovieResponse> getUpcomingMovies(
            @Query("path") String path,
            @Query("primary_release_date.gte") String dateGte,
            @Query("primary_release_date.lte") String dateLte,
            @Query("sort_by") String sortBy,
            @Query("include_video") boolean includeVideo,
            @Query("language") String language,
            @Query("page") int page);

    @GET("api/tmdb")
    Call<UpComingTVShowsResponse> getUpcomingTVShows(
            @Query("path") String path,
            @Query("first_air_date.gte") String dateGte,
            @Query("first_air_date.lte") String dateLte,
            @Query("sort_by") String sortBy,
            @Query("include_video") boolean includeVideo,
            @Query("language") String language,
            @Query("page") int page);

    @GET("api/tmdb")
    Call<MoviesTrailerResponses> getMoviesTrailer(
            @Query("path") String path);

    @GET("api/tmdb")
    Call<TVShowsTrailerResponses> getTVShowsTrailer(
            @Query("path") String path);

    @GET("api/tmdb")
    Call<GenresResponse> getGenresMovie(
            @Query("path") String path);

    @GET("api/tmdb")
    Call<GenresResponse> getGenresTVShows(
            @Query("path") String path);

    @GET("api/tmdb")
    Call<SeasonDetailResponse> getTVSeasonDetails(
            @Query("path") String path);

    @GET("api/tmdb")
    Call<TVShowProviderResponse> getTVShowProviders(
            @Query("path") String path);

    @GET("api/tmdb")
    Call<TVShowProviderResponse> getMovieProviders(
            @Query("path") String path);

    @GET("api/tmdb")
    Call<PersonDetailsResponse> getPersonDetails(
            @Query("path") String path);

    @GET("api/tmdb")
    Call<PersonCreditsResponse> getPersonCredits(
            @Query("path") String path);

    @GET("api/tmdb")
    Call<MovieResponse> getMoviesByGenre(
            @Query("path") String path,
            @Query("with_genres") int genreId,
            @Query("page") int page);

    @GET("api/tmdb")
    Call<TVShowResponse> getTVShowsByGenre(
            @Query("path") String path,
            @Query("with_genres") int genreId,
            @Query("page") int page);

    @GET("api/tmdb")
    Call<MovieResponse> getNowPlayingMovies(
            @Query("path") String path,
            @Query("page") int page,
            @Query("region") String region);

    @GET("api/tmdb")
    Call<MovieReleaseDatesResponse> getMovieReleaseDates(
            @Query("path") String path);

    @GET("api/tmdb")
    Call<TVShowContentRatingsResponse> getTVShowContentRatings(
            @Query("path") String path);

    @GET("api/tmdb")
    Call<TVShowResponse> getOnAirTVShows(
            @Query("path") String path,
            @Query("page") int page);

    @GET("api/tmdb")
    Call<MovieResponse> getMoviesByOrigin(
            @Query("path") String path,
            @Query("with_origin_country") String countryCode,
            @Query("sort_by") String sortBy,
            @Query("page") int page);

    @GET("api/tmdb")
    Call<TVShowResponse> getTVShowsByOrigin(
            @Query("path") String path,
            @Query("with_origin_country") String countryCode,
            @Query("with_genres") Integer genreId,
            @Query("sort_by") String sortBy,
            @Query("page") int page);
}