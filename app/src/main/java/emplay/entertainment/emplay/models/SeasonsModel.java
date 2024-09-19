package emplay.entertainment.emplay.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SeasonsModel {
    @SerializedName("season_id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("air_date")
    private String air_date;
    @SerializedName("episode_count")
    private int numberOfEpisodes;
    @SerializedName("season_number")
    private int numberOfSeasons;
    @SerializedName("poster_path")
    private String posterPath;

    public SeasonsModel(int id, String name, String posterPath, int numberOfEpisodes) {
        this.id = id;
        this.name = name;
        this.numberOfEpisodes = numberOfEpisodes;
        this.posterPath = posterPath;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAir_date() {
        return air_date;
    }

    public void setAir_date(String air_date) {
        this.air_date = air_date;
    }

    public int getNumberOfEpisodes() {
        return numberOfEpisodes;
    }

    public void setNumberOfEpisodes(int numberOfEpisodes) {
        this.numberOfEpisodes = numberOfEpisodes;
    }
    public int getNumberOfSeasons() {
        return numberOfSeasons;
    }

    public void setNumberOfSeasons(int numberOfSeasons) {
        this.numberOfSeasons = numberOfSeasons;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }
}
