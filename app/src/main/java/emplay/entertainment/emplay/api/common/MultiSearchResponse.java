package emplay.entertainment.emplay.api.common;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import emplay.entertainment.emplay.models.common.MultiSearchResult;

public class MultiSearchResponse {
    @SerializedName("results")
    private List<MultiSearchResult> results;

    @SerializedName("total_pages")
    private int totalPages;

    public List<MultiSearchResult> getResults() { return results; }
    public int getTotalPages() { return totalPages; }
}