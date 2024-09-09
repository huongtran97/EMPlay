package emplay.entertainment.emplay.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import emplay.entertainment.emplay.api.MovieCreditsResponse.Cast;

public class TVShowCreditsResponses {
    @SerializedName("cast")
    private List<Cast> cast;

    public List<Cast> getCast() {
        return cast;
    }

    public void setCast(List<Cast> cast) {
        this.cast = cast;
    }




    public static class Cast {
        @SerializedName("id")
        private int castId;
        @SerializedName("name")
        private String name;
        @SerializedName("profile_path")
        private String profilePath;
        @SerializedName("character")
        private String character;



        public Cast(int castId, String name, String profilePath, String character) {
            this.castId = castId;
            this.name = name;
            this.profilePath = profilePath;
            this.character = character;
        }

        public int getCastId() {
            return castId;
        }

        public void setCastId(int castId) {
            this.castId = castId;
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
