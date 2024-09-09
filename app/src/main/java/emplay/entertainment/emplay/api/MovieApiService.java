package emplay.entertainment.emplay.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Interface for defining the API endpoints for movie-related operations.
 */
public interface MovieApiService {

    @GET("3/movie/popular")
    Call<MovieResponse> getPopularMovies(
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

    @GET("3/tv/popular")
    Call<TVShowResponse> getPopularTVShows(
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

    @GET("3/discover/movie?include_video=false&language=en-US&page=1&primary_release_date.gte=2024-09-16&primary_release_date.lte=2024-10-16&sort_by=popularity.desc")
    Call<UpComingMovieResponse> getUpcomingMovies(
            @Query("api_key") String apiKey
    );

    @GET("3/discover/tv?include_video=false&language=en-US&page=1&first_air_date.gte=2024-09-16&first_air_date.lte=2024-10-16&sort_by=popularity.desc")
    Call<UpComingTVShowsResponse> getUpcomingTVShows(
            @Query("api_key") String apiKey
    );
}
