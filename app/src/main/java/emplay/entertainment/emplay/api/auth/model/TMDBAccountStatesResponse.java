package emplay.entertainment.emplay.api.auth.model;

import com.google.gson.annotations.SerializedName;

public class TMDBAccountStatesResponse {
    @SerializedName("id")
    public int id;

    @SerializedName("watchlist")
    public boolean watchlist;

    @SerializedName("favorite")
    public boolean favorite;
}