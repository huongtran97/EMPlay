package emplay.entertainment.emplay.api.motn;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class MotnShowResponse {
    @SerializedName("tmdbId")
    private String tmdbId;

    @SerializedName("title")
    private String title;

    /** Keys are lowercase country codes, e.g. "us", "ca", "gb". */
    @SerializedName("streamingOptions")
    private Map<String, List<MotnStreamingOption>> streamingOptions;

    public String getTmdbId() { return tmdbId; }
    public String getTitle()  { return title; }
    public Map<String, List<MotnStreamingOption>> getStreamingOptions() { return streamingOptions; }
}