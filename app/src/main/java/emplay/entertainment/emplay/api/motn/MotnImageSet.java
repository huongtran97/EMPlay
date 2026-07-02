package emplay.entertainment.emplay.api.motn;

import com.google.gson.annotations.SerializedName;

public class MotnImageSet {
    @SerializedName("lightThemeImage")
    private String lightThemeImage;

    @SerializedName("darkThemeImage")
    private String darkThemeImage;

    @SerializedName("whiteImage")
    private String whiteImage;

    public String getLightThemeImage() { return lightThemeImage; }
    public String getDarkThemeImage()  { return darkThemeImage; }
    public String getWhiteImage()      { return whiteImage; }
}