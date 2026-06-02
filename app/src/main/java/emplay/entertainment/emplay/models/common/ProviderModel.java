package emplay.entertainment.emplay.models.common;

import com.google.gson.annotations.SerializedName;

public class ProviderModel {
    @SerializedName("provider_id")
    private int providerId;

    @SerializedName("provider_name")
    private String providerName;

    @SerializedName("logo_path")
    private String logoPath;

    @SerializedName("display_priority")
    private int displayPriority;

    public int getProviderId() { return providerId; }
    public String getProviderName() { return providerName; }
    public String getLogoPath() { return logoPath; }
    public int getDisplayPriority() { return displayPriority; }
}
