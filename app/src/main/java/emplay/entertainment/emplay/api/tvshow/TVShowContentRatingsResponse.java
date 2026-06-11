package emplay.entertainment.emplay.api.tvshow;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TVShowContentRatingsResponse {
    @SerializedName("results")
    private List<CountryRating> results;

    public List<CountryRating> getResults() { return results; }

    public static class CountryRating {
        @SerializedName("iso_3166_1")
        private String country;

        @SerializedName("rating")
        private String rating;

        public String getCountry() { return country; }
        public String getRating() { return rating; }
    }
}
