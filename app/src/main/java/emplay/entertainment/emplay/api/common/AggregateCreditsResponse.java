package emplay.entertainment.emplay.api.common;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AggregateCreditsResponse {

    @SerializedName("cast")
    private List<Cast> cast;

    @SerializedName("crew")
    private List<Crew> crew;

    public List<Cast> getCast() { return cast; }
    public List<Crew> getCrew() { return crew; }

    public static class Cast {
        @SerializedName("id")
        private int id;
        @SerializedName("name")
        private String name;
        @SerializedName("profile_path")
        private String profilePath;
        @SerializedName("roles")
        private List<Role> roles;

        public int getId() { return id; }
        public String getName() { return name; }
        public String getProfilePath() { return profilePath; }
        public String getCharacter() {
            return (roles != null && !roles.isEmpty()) ? roles.get(0).getCharacter() : "";
        }
    }

    public static class Crew {
        @SerializedName("id")
        private int id;
        @SerializedName("name")
        private String name;
        @SerializedName("profile_path")
        private String profilePath;
        @SerializedName("jobs")
        private List<Job> jobs;

        public int getId() { return id; }
        public String getName() { return name; }
        public String getProfilePath() { return profilePath; }
        public String getJob() {
            return (jobs != null && !jobs.isEmpty()) ? jobs.get(0).getJob() : "";
        }
    }

    public static class Role {
        @SerializedName("character")
        private String character;
        @SerializedName("episode_count")
        private int episodeCount;

        public String getCharacter() { return character; }
        public int getEpisodeCount() { return episodeCount; }
    }

    public static class Job {
        @SerializedName("job")
        private String job;
        @SerializedName("episode_count")
        private int episodeCount;

        public String getJob() { return job; }
        public int getEpisodeCount() { return episodeCount; }
    }
}