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

    @GET("3/movie/{id}/recommendations")
    Call<MovieRecommendationsResponse> getMovieRecommendations(
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
}
