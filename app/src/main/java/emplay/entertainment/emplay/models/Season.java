package emplay.entertainment.emplay.models;

import com.google.gson.annotations.SerializedName;

public class Season {
    @SerializedName("season_id")
    private int season_id;

    @SerializedName("name")
    private String name;

    @SerializedName("air_date")
    private String air_date;

    @SerializedName("episode_count")
    private String episode_count;

    @SerializedName("poster_path")
    private String poster_path;

    public int getSeason_id() {
        return season_id;
    }

    public void setSeason_id(int season_id) {
        this.season_id = season_id;
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

    public String getEpisode_count() {
        return episode_count;
    }

    public void setEpisode_count(String episode_count) {
        this.episode_count = episode_count;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }
}
