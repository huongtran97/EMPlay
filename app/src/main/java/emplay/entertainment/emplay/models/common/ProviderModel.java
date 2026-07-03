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

    /** Full image URL from Movie of the Night — not a TMDB path fragment. */
    private String logoUrl;

    /** True when MOTN has confirmed a deep link for this provider; defaults to true (optimistic). */
    private boolean hasDeepLink = true;

    public int getProviderId()          { return providerId; }
    public String getProviderName()     { return providerName; }
    public String getLogoPath()         { return logoPath; }
    public int getDisplayPriority()     { return displayPriority; }
    public String getLogoUrl()          { return logoUrl; }
    public boolean hasDeepLink()        { return hasDeepLink; }
    public void setHasDeepLink(boolean v) { hasDeepLink = v; }

    /** Factory for Movie of the Night providers where the logo is a full URL. */
    public static ProviderModel fromMotn(String name, String logoUrl) {
        ProviderModel pm = new ProviderModel();
        pm.providerName = name;
        pm.logoUrl = logoUrl;
        return pm;
    }
}
