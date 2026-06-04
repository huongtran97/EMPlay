package emplay.entertainment.emplay.api.auth;

import emplay.entertainment.emplay.api.auth.model.TMDBAccountStatesResponse;
import emplay.entertainment.emplay.api.auth.model.TMDBMovieWatchlistResponse;
import emplay.entertainment.emplay.api.auth.model.TMDBTVWatchlistResponse;
import emplay.entertainment.emplay.api.auth.model.TMDBWatchlistRequest;
import emplay.entertainment.emplay.api.auth.model.TMDBWatchlistStatusResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Account-scoped TMDB endpoints routed through the Railway proxy.
 * Uses the same ?path= pattern as MovieApiService — the proxy injects the API key.
 */
public interface TMDBWatchlistApiService {

    @GET("api/tmdb")
    Call<TMDBAccountStatesResponse> getAccountStates(
            @Query("path") String path,
            @Query("session_id") String sessionId);

    @POST("api/tmdb")
    Call<TMDBWatchlistStatusResponse> updateWatchlist(
            @Query("path") String path,
            @Query("session_id") String sessionId,
            @Body TMDBWatchlistRequest body);

    @GET("api/tmdb")
    Call<TMDBMovieWatchlistResponse> getWatchlistMovies(
            @Query("path") String path,
            @Query("session_id") String sessionId,
            @Query("page") int page);

    @GET("api/tmdb")
    Call<TMDBTVWatchlistResponse> getWatchlistTV(
            @Query("path") String path,
            @Query("session_id") String sessionId,
            @Query("page") int page);
}