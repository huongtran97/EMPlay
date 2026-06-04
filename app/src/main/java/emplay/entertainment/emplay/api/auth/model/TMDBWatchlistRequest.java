package emplay.entertainment.emplay.api.auth.model;

import com.google.gson.annotations.SerializedName;

public class TMDBWatchlistRequest {
    @SerializedName("media_type")
    public final String mediaType;

    @SerializedName("media_id")
    public final int mediaId;

    @SerializedName("watchlist")
    public final boolean watchlist;

    public TMDBWatchlistRequest(String mediaType, int mediaId, boolean watchlist) {
        this.mediaType = mediaType;
        this.mediaId = mediaId;
        this.watchlist = watchlist;
    }
}