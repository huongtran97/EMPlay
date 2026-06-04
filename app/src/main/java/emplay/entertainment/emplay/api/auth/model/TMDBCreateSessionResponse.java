package emplay.entertainment.emplay.api.auth.model;

import com.google.gson.annotations.SerializedName;

public class TMDBCreateSessionResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("session_id")
    public String sessionId;

    @SerializedName("account_id")
    public int accountId;

    @SerializedName("username")
    public String username;
}
