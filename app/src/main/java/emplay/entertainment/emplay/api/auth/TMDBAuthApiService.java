package emplay.entertainment.emplay.api.auth;

import emplay.entertainment.emplay.api.auth.model.TMDBCreateSessionResponse;
import emplay.entertainment.emplay.api.auth.model.TMDBRequestTokenResponse;
import emplay.entertainment.emplay.api.auth.model.TMDBSessionRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * TMDB auth endpoints served by the Railway proxy.
 * The proxy handles the TMDB API key.
 */
public interface TMDBAuthApiService {

    @GET("api/tmdb/auth/request-token")
    Call<TMDBRequestTokenResponse> getRequestToken();

    /** Exchanges an approved request_token for session_id + account_id + username in one call. */
    @POST("api/tmdb/auth/create-session")
    Call<TMDBCreateSessionResponse> createSession(@Body TMDBSessionRequest body);
}