package emplay.entertainment.emplay.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import emplay.entertainment.emplay.api.MovieCreditsResponse.Cast;

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

    public void setCrew(List<Crew> crew) {
        this.crew = crew;
    }

    public static class Crew{
        @SerializedName("id")
        private int crewId;
        @SerializedName("name")
        private String name;
        @SerializedName("profile_path")
        private String profilePath;

        public int getCrewId() {
            return crewId;
        }

        public void setCrewId(int crewId) {
            this.crewId = crewId;
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
    }

    public static class Cast{
        @SerializedName("id")
        private int crewId;
        @SerializedName("name")
        private String name;
        @SerializedName("profile_path")
        private String profilePath;

        public Cast(int crewId, String name, String profilePath) {
            this.crewId = crewId;
            this.name = name;
            this.profilePath = profilePath;
        }

        public int getCrewId() {
            return crewId;
        }

        public void setCrewId(int crewId) {
            this.crewId = crewId;
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


    }
}
