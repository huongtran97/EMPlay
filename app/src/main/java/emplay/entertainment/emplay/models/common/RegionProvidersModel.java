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
    public List<ProviderModel> getRent()     { return rent; }
    public List<ProviderModel> getBuy()      { return buy; }

    public void setFlatrate(List<ProviderModel> flatrate) { this.flatrate = flatrate; }
    public void setRent(List<ProviderModel> rent)         { this.rent = rent; }
    public void setBuy(List<ProviderModel> buy)           { this.buy = buy; }
}