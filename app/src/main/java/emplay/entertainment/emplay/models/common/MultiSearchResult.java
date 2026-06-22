package emplay.entertainment.emplay.models.common;

import com.google.gson.annotations.SerializedName;


public class MultiSearchResult implements MediaItem {

    @SerializedName("id")
    private int id;

    @SerializedName("media_type")
    private String mediaType;

    @SerializedName("title")
    private String title;

    @SerializedName("name")
    private String name;

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("backdrop_path")
    private String backdropPath;

    @SerializedName("vote_average")
    private double voteAverage;

    @SerializedName("release_date")
    private String releaseDate;

    @SerializedName("first_air_date")
    private String firstAirDate;

    @SerializedName("original_language")
    private String originalLanguage;

    // Person-specific fields
    @SerializedName("profile_path")
    private String profilePath;

    @SerializedName("known_for_department")
    private String department;



    @Override public int getMediaId() { return id; }

    @Override
    public String getTitle() {
        return title != null ? title : (name != null ? name : "");
    }

    @Override public String getPosterPath() { return posterPath; }
    @Override public String getBackdropPath() { return backdropPath; }
    @Override public double getVoteAverage() { return voteAverage; }
    @Override public String getSavedTimestamp() { return null; }

    public String getMediaType() { return mediaType; }
    public String getProfilePath() { return profilePath; }
    public String getDepartment() { return department; }

    public String getDisplayYear() {
        String date = releaseDate != null ? releaseDate : firstAirDate;
        if (date != null && date.length() >= 4) return date.substring(0, 4);
        return "";
    }
}
