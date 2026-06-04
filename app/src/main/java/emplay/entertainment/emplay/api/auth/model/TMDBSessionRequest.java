package emplay.entertainment.emplay.api.auth.model;

import com.google.gson.annotations.SerializedName;

public class TMDBSessionRequest {
    @SerializedName("request_token")
    public final String requestToken;

    public TMDBSessionRequest(String requestToken) {
        this.requestToken = requestToken;
    }
}