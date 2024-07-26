package emplay.entertainment.emplay;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 *  * @author Tran Ngoc Que Huong
 *  * @version 1.0
 *
 * Interface for defining the API endpoints for movie-related operations.
 */
public interface MovieApiService {

    /**
     * Searches for movies based on the provided query.
     *
     * @param apiKey The API key for authenticating the request.
     *               This key is required to access the movie database API.
     * @param query  The search query to find movies. This is typically the movie title or keywords related to the movie.
     * @return A {@link Call} object which can be used to send the HTTP request asynchronously. The response will be a MovieResponse containing the search results.
     */
    @GET("search/movie")
    Call<MovieResponse> searchMovies(
            @Query("api_key") String apiKey,  // API key parameter
            @Query("query") String query      // Search query parameter
    );
}


