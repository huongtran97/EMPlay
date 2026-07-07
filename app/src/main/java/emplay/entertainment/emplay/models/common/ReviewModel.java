package emplay.entertainment.emplay.models.common;

import com.google.gson.annotations.SerializedName;

public class ReviewModel {

    @SerializedName("id")
    private String id;

    @SerializedName("author")
    private String author;

    @SerializedName("author_details")
    private AuthorDetails authorDetails;

    @SerializedName("content")
    private String content;

    public String getId() { return id; }
    public String getAuthor() { return author; }
    public AuthorDetails getAuthorDetails() { return authorDetails; }
    public String getContent() { return content; }

    public static class AuthorDetails {
        @SerializedName("name")
        private String name;

        @SerializedName("username")
        private String username;

        @SerializedName("avatar_path")
        private String avatarPath;

        @SerializedName("rating")
        private Double rating;

        public String getName() { return name; }
        public String getUsername() { return username; }
        public String getAvatarPath() { return avatarPath; }
        public Double getRating() { return rating; }
    }
}
