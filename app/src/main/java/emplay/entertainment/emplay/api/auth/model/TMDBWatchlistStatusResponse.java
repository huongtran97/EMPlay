package emplay.entertainment.emplay.api.auth.model;

import com.google.gson.annotations.SerializedName;

public class TMDBWatchlistStatusResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("status_code")
    public int statusCode;

    @SerializedName("status_message")
    public String statusMessage;
}