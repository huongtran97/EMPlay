package emplay.entertainment.emplay.api.tvshow;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TVShowCreditsResponses {
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
        @SerializedName("name")
        private String name;
        @SerializedName("profile_path")
        private String profilePath;
        @SerializedName("character")
        private String character;

        public Cast(int id, String name, String profilePath, String character) {
            this.id = id;
            this.name = name;
            this.profilePath = profilePath;
            this.character = character;
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

        public String getProfilePath() {
            return profilePath;
        }

        public void setProfilePath(String profilePath) {
            this.profilePath = profilePath;
        }

        public String getCharacter() {
            return character;
        }

        public void setCharacter(String character) {
            this.character = character;
        }

    }
}
