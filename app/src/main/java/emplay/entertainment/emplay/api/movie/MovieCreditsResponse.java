package emplay.entertainment.emplay.api.movie;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MovieCreditsResponse {

    @SerializedName("cast")
    private List<Cast> cast;

    @SerializedName("crew")
    private List<Crew> crew;

    public List<Cast> getCast() {
        return cast;
    }

    public void setCast(List<Cast> cast) {
        this.cast = cast;
    }

    public List<Crew> getCrew() {
        return crew;
    }

    public static class Crew {
        @SerializedName("id")
        private int id;
        @SerializedName("name")
        private String name;
        @SerializedName("profile_path")
        private String profilePath;
        @SerializedName("job")
        private String job;

        public int getId() { return id; }
        public String getName() { return name; }
        public String getProfilePath() { return profilePath; }
        public String getJob() { return job; }
    }

    public static class Cast {

        @SerializedName("id")
        private int id;

        @SerializedName("cast_id")
        private int castId;

        @SerializedName("name")
        private String name;

        @SerializedName("profile_path")
        private String profilePath;

        @SerializedName("character")
        private String character;

        public Cast(int id, int castId, String name, String profilePath, String character) {
            this.id = id;
            this.castId = castId;
            this.name = name;
            this.profilePath = profilePath;
            this.character = character;
        }

        public int getId() {
            return id;
        }

        public int getCastId() {
            return castId;
        }

        public String getCastName() {
            return name;
        }

        public String getProfilePath() {
            return profilePath;
        }

        public String getCharacter() { return character;
        }
    }
}
