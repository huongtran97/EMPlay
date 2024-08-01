package emplay.entertainment.emplay.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MovieCreditsResponse {

    @SerializedName("cast")
    private List<Cast> cast;

    public List<Cast> getCast() {
        return cast;
    }

    public void setCast(List<Cast> cast) {
        this.cast = cast;
    }

    public static class Cast {

        @SerializedName("cast_id")
        private int castId;

        @SerializedName("name")
        private String name;

        @SerializedName("profile_path")
        private String profilePath;

        public int getCastId() {
            return castId;
        }

        public String getCastName() {
            return name;
        }

        public String getProfilePath() {
            return profilePath;
        }
    }
}
