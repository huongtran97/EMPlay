package emplay.entertainment.emplay.api.motn;

import com.google.gson.annotations.SerializedName;

public class MotnStreamingOption {
    @SerializedName("service")
    private MotnServiceInfo service;

    /** Present when type = "addon" — the actual channel/add-on (e.g. MGM+, HBO Max). */
    @SerializedName("addon")
    private MotnServiceInfo addon;

    @SerializedName("type")
    private String type;

    /** Direct URL to this specific movie/show on the platform. */
    @SerializedName("link")
    private String link;

    @SerializedName("quality")
    private String quality;

    public MotnServiceInfo getService() { return service; }
    public MotnServiceInfo getAddon()   { return addon; }
    public String getType()             { return type; }
    public String getLink()             { return link; }
    public String getQuality()          { return quality; }
}