package emplay.entertainment.emplay.api.movie;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MovieReleaseDatesResponse {
    @SerializedName("results")
    private List<CountryReleaseDates> results;

    public List<CountryReleaseDates> getResults() { return results; }

    public static class CountryReleaseDates {
        @SerializedName("iso_3166_1")
        private String country;

        @SerializedName("release_dates")
        private List<ReleaseDate> releaseDates;

        public String getCountry() { return country; }
        public List<ReleaseDate> getReleaseDates() { return releaseDates; }
    }

    public static class ReleaseDate {
        @SerializedName("certification")
        private String certification;

        @SerializedName("release_date")
        private String releaseDate;

        @SerializedName("type")
        private int type;

        public String getCertification() { return certification; }
        public String getReleaseDate() { return releaseDate; }
        public int getType() { return type; }
    }
}