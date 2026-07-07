package emplay.entertainment.emplay.api.common;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import emplay.entertainment.emplay.models.common.ReviewModel;

public class ReviewResponse {

    @SerializedName("results")
    private List<ReviewModel> results;

    public List<ReviewModel> getResults() { return results; }
}
