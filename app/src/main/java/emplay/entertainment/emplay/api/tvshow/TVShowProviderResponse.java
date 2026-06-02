package emplay.entertainment.emplay.api.tvshow;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

import emplay.entertainment.emplay.models.common.RegionProvidersModel;

public class TVShowProviderResponse {
    @SerializedName("results")
    private Map<String, RegionProvidersModel> results;

    public Map<String, RegionProvidersModel> getResults() { return results; }
}
