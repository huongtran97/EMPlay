package emplay.entertainment.emplay.api.motn;

import com.google.gson.annotations.SerializedName;

public class MotnServiceInfo {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("homePage")
    private String homePage;

    @SerializedName("imageSet")
    private MotnImageSet imageSet;

    public String getId()          { return id; }
    public String getName()        { return name; }
    public String getHomePage()    { return homePage; }
    public MotnImageSet getImageSet() { return imageSet; }
}