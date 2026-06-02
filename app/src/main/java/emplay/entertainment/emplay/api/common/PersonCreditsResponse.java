package emplay.entertainment.emplay.api.common;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PersonCreditsResponse {

    @SerializedName("id")
    private int id;

    @SerializedName("cast")
    private List<CreditItem> cast;

    @SerializedName("crew")
    private List<CreditItem> crew;

    public int getId() { return id; }
    public List<CreditItem> getCast() { return cast; }
    public List<CreditItem> getCrew() { return crew; }

    public static class CreditItem {
        @SerializedName("id")
        private int id;
        @SerializedName("title")
        private String title;
        @SerializedName("name")
        private String name; // TV show name
        @SerializedName("poster_path")
        private String posterPath;
        @SerializedName("backdrop_path")
        private String backdropPath;
        @SerializedName("media_type")
        private String mediaType; // "movie" or "tv"
        @SerializedName("vote_average")
        private double voteAverage;
        @SerializedName("popularity")
        private double popularity;
        @SerializedName("release_date")
        private String releaseDate;
        @SerializedName("first_air_date")
        private String firstAirDate;
        @SerializedName("character")
        private String character;

        public int getId() { return id; }
        public String getMediaType() { return mediaType; }
        public String getPosterPath() { return posterPath; }
        public String getBackdropPath() { return backdropPath; }
        public double getVoteAverage() { return voteAverage; }
        public double getPopularity() { return popularity; }
        public String getCharacter() { return character; }

        // Returns title for movie, name for TV show
        public String getDisplayTitle() {
            return title != null ? title : name;
        }

        // Returns release date for movie, first_air_date for TV
        public String getDisplayDate() {
            return releaseDate != null ? releaseDate : firstAirDate;
        }
    }
}
