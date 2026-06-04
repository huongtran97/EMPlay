package emplay.entertainment.emplay.api.auth.model;

import com.google.gson.annotations.SerializedName;

public class TMDBRequestTokenResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("expires_at")
    public String expiresAt;

    @SerializedName("request_token")
    public String requestToken;
}