package emplay.entertainment.emplay.api.motn;

import com.google.gson.annotations.SerializedName;

public class MotnStreamingOption {
    @SerializedName("service")
    private MotnServiceInfo service;

    @SerializedName("type")
    private String type;

    @SerializedName("link")
    private String link;

    @SerializedName("quality")
    private String quality;

    public MotnServiceInfo getService() { return service; }
    public String getType()             { return type; }
    public String getLink()             { return link; }
    public String getQuality()          { return quality; }
}