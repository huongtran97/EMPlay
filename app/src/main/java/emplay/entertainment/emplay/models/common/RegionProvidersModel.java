package emplay.entertainment.emplay.models.common;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RegionProvidersModel {
    @SerializedName("flatrate")
    private List<ProviderModel> flatrate;
    @SerializedName("rent")
    private List<ProviderModel> rent;
    @SerializedName("buy")
    private List<ProviderModel> buy;

    public List<ProviderModel> getFlatrate() { return flatrate; }
    public List<ProviderModel> getRent() { return rent; }
    public List<ProviderModel> getBuy() { return buy; }
}