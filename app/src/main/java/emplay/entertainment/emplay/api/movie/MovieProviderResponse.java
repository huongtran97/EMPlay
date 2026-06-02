package emplay.entertainment.emplay.api.movie;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

import emplay.entertainment.emplay.models.common.RegionProvidersModel;

public class MovieProviderResponse {
    @SerializedName("results")
    private Map<String, RegionProvidersModel> results;

    public Map<String, RegionProvidersModel> getResults() { return results; }
}