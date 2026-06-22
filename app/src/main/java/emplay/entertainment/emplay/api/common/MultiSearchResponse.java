package emplay.entertainment.emplay.api.common;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import emplay.entertainment.emplay.models.common.MultiSearchResult;

public class MultiSearchResponse {
    @SerializedName("results")
    private List<MultiSearchResult> results;

    public List<MultiSearchResult> getResults() { return results; }
}