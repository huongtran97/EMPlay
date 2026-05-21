package emplay.entertainment.emplay.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * API endpoints routed through the Vercel proxy.
 *
 * All requests go to: GET /api/tmdb?path=<tmdb_path>&<other_params>
 * The proxy injects the TMDB API key server-side — no key in APK.
 *
 * NOTE: @Query("api_key") is removed from all methods.
 *       The proxy handles authentication automatically.
 */
public interface MovieApiService {

    @GET("api/tmdb")
    Call<MovieResponse> getTrendingMovies(
            @Query("path") String path);

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
}