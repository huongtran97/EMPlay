package emplay.entertainment.emplay.api.movie;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CollectionResponse {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("parts")
    private List<Part> parts;

    public int getId() { return id; }
    public String getName() { return name; }
    public List<Part> getParts() { return parts; }
    public int getPartCount() { return parts != null ? parts.size() : 0; }

    public static class Part {
        @SerializedName("id")
        private int id;
        @SerializedName("title")
        private String title;
        @SerializedName("poster_path")
        private String posterPath;
        @SerializedName("release_date")
        private String releaseDate;
        @SerializedName("vote_average")
        private double voteAverage;

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getPosterPath() { return posterPath; }
        public String getReleaseDate() { return releaseDate; }
        public double getVoteAverage() { return voteAverage; }
    }
}