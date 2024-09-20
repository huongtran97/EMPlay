package emplay.entertainment.emplay.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Interface for defining the API endpoints for movie-related operations.
 */
public interface MovieApiService {

    @GET("3/trending/movie/week")
    Call<MovieResponse> getTrendingMovies(
            @Query("api_key") String apiKey);

    @GET("3/movie/{id}")
    Call<MovieDetailsResponse> getMovieDetails(
            @Path("id") int movieId,
            @Query("api_key") String apiKey
    );

    @GET("3/search/movie")
    Call<MovieResponse> searchMovies(
            @Query("api_key") String apiKey,
            @Query("query") String query
    );

    @GET("3/search/tv")
    Call<TVShowResponse> searchTVShows(
            @Query("api_key") String apiKey,
            @Query("query") String query
    );

    @GET("3/movie/{id}/credits")
    Call<MovieCreditsResponse> getMovieCredits(
            @Path("id") int movieId,
            @Query("api_key") String apiKey);

    @GET("3/movie/{id}/similar")
    Call<MovieSimilarResponse> getMovieSimilar(
            @Path("id") int movieId,
            @Query("api_key") String apiKey);

    @GET("3/trending/tv/week")
    Call<TVShowResponse> getTrendingTVShows(
            @Query("api_key") String apiKey);

    @GET("3/tv/{id}")
    Call<TVShowDetailsResponse> getTVShowDetails(
            @Path("id") int tvId,
            @Query("api_key") String apiKey
    );

    @GET("3/tv/{id}/credits")
    Call<TVShowCreditsResponses> getTVShowCredits(
            @Path("id") int tvId,
            @Query("api_key") String apiKey
    );

    @GET("3/tv/{id}/similar")
    Call<TVShowSimilarResponse> getTVShowSimilar(
            @Path("id") int tvId,
            @Query("api_key") String apiKey
    );

    @GET("3/discover/movie?include_video=false&language=en-US&page=1&primary_release_date.gte=2024-09-19&primary_release_date.lte=2024-10-19&sort_by=popularity.desc")
    Call<UpComingMovieResponse> getUpcomingMovies(
            @Query("api_key") String apiKey
    );

    @GET("3/discover/tv?include_video=false&language=en-US&page=1&first_air_date.gte=2024-09-19&first_air_date.lte=2024-10-19&sort_by=popularity.desc")
    Call<UpComingTVShowsResponse> getUpcomingTVShows(
            @Query("api_key") String apiKey
    );

    @GET("3/movie/{movie_id}/videos")
    Call<MoviesTrailerResponses> getMoviesTrailer(
            @Path("movie_id") int movieId,
            @Query("api_key") String apiKey);

    @GET("3/tv/{tv_id}/videos")
    Call<TVShowsTrailerResponses> getTVShowsTrailer(
            @Path("tv_id") int movieId,
            @Query("api_key") String apiKey);

    @GET("3/genre/movie/list")
    Call<GenresResponse> getGenresMovie(
            @Query("api_key") String apiKey);

    @GET("3/genre/tv/list")
    Call<GenresResponse> getGenresTVShows(
            @Query("api_key") String apiKey);

    @GET("3/discover/movie")
    Call<MovieResponse> getMoviesByGenre(
            @Query("api_key") String apiKey,
            @Query("with_genres") int genreId);

    @GET("3/discover/tv")
    Call<TVShowResponse> getTVShowsByGenre(
            @Query("api_key") String apiKey,
            @Query("with_genres") int genreId);




}
