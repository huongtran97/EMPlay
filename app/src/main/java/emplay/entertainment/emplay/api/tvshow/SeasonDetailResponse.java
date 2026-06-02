package emplay.entertainment.emplay.api.tvshow;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SeasonDetailResponse {

    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("overview")
    private String overview;
    @SerializedName("poster_path")
    private String posterPath;
    @SerializedName("air_date")
    private String airDate;
    @SerializedName("season_number")
    private int seasonNumber;
    @SerializedName("episodes")
    private List<Episode> episodes;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getOverview() { return overview; }
    public String getPosterPath() { return posterPath; }
    public String getAirDate() { return airDate; }
    public int getSeasonNumber() { return seasonNumber; }
    public List<Episode> getEpisodes() { return episodes; }

    public static class Episode {
        @SerializedName("id")
        private int id;
        @SerializedName("name")
        private String name;
        @SerializedName("overview")
        private String overview;
        @SerializedName("episode_number")
        private int episodeNumber;
        @SerializedName("air_date")
        private String airDate;
        @SerializedName("still_path")
        private String stillPath;
        @SerializedName("vote_average")
        private double voteAverage;
        @SerializedName("runtime")
        private int runtime;

        public int getId() { return id; }
        public String getName() { return name; }
        public String getOverview() { return overview; }
        public int getEpisodeNumber() { return episodeNumber; }
        public String getAirDate() { return airDate; }
        public String getStillPath() { return stillPath; }
        public double getVoteAverage() { return voteAverage; }
        public int getRuntime() { return runtime; }
    }
}
