package emplay.entertainment.emplay.models.common;

import com.google.gson.annotations.SerializedName;

public class CountryModel {
    @SerializedName("iso_3166_1")
    private String iso31661;

    @SerializedName("english_name")
    private String englishName;

    @SerializedName("native_name")
    private String nativeName;

    public String getIso31661() { return iso31661; }
    public String getEnglishName() { return englishName; }
    public String getNativeName() { return nativeName; }
}