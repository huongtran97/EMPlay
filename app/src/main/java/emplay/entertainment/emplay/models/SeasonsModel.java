package emplay.entertainment.emplay.models;

import com.google.gson.annotations.SerializedName;

public class SeasonsModel {
    @SerializedName("season_id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("air_date")
    private String airDate;
    @SerializedName("episode_count")
    private int numberOfEpisodes;
    @SerializedName("season_number")
    private int seasonNumber;
    @SerializedName("poster_path")
    private String posterPath;

    public SeasonsModel(int id, String name, String posterPath, int numberOfEpisodes, int seasonNumber) {
        this.id = id;
        this.name = name;
        this.numberOfEpisodes = numberOfEpisodes;
        this.posterPath = posterPath;
        this.seasonNumber = seasonNumber;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAirDate() { return airDate; }
    public void setAirDate(String airDate) { this.airDate = airDate; }

    public int getNumberOfEpisodes() { return numberOfEpisodes; }
    public void setNumberOfEpisodes(int numberOfEpisodes) { this.numberOfEpisodes = numberOfEpisodes; }

    public int getSeasonNumber() { return seasonNumber; }
    public void setSeasonNumber(int seasonNumber) { this.seasonNumber = seasonNumber; }

    public String getPosterPath() { return posterPath; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }
}
