package emplay.entertainment.emplay.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import emplay.entertainment.emplay.api.TVShowDetailsResponse.CreatedBy;
import emplay.entertainment.emplay.api.TVShowDetailsResponse.LastEpisodeToAir;
import emplay.entertainment.emplay.api.TVShowDetailsResponse.Network;
import emplay.entertainment.emplay.api.TVShowDetailsResponse.ProductionCountry;
import emplay.entertainment.emplay.api.TVShowDetailsResponse.Season;
import emplay.entertainment.emplay.api.TVShowDetailsResponse.SpokenLanguage;
import emplay.entertainment.emplay.models.TVShowModel;

public class UpComingTVShowsResponse {
    @SerializedName("dates")
    private Dates dates;
    @SerializedName("page")
    private int page;
    @SerializedName("results")
    private List<TVShowModel> results;

    public List<TVShowModel> getResults() {return results;}
    public void setResults(List<TVShowModel> results) {
        this.results = results;
    }

    public static class Dates {
        @SerializedName("maximum")
        private String maximum;
        @SerializedName("minimum")
        private String minimum;

        // Getters and setters
        public String getMaximum() {
            return maximum;
        }

        public void setMaximum(String maximum) {
            this.maximum = maximum;
        }

        public String getMinimum() {
            return minimum;
        }

        public void setMinimum(String minimum) {
            this.minimum = minimum;
        }

    }

    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("original_name")
    private String original_name;
    @SerializedName("first_air_date")
    private String first_air_date;
    @SerializedName("overview")
    private String overview;
    @SerializedName("poster_path")
    private String poster_path;
    @SerializedName("backdrop_path")
    private String backdrop_path;
    @SerializedName("original_language")
    private String original_language;
    @SerializedName("vote_average")
    private double vote_average;
    @SerializedName("vote_count")
    private int vote_count;
    @SerializedName("in_production")
    private boolean in_production;
    @SerializedName("number_of_episodes")
    private int number_of_episodes;
    @SerializedName("number_of_seasons")
    private int number_of_seasons;
    @SerializedName("status")
    private String status;
    @SerializedName("tagline")
    private String tagline;
    @SerializedName("genres")
    private List<Genre> genres;
    @SerializedName("seasons")
    private List<Season> seasons;
    @SerializedName("created_by")
    private List<CreatedBy> created_by;
    @SerializedName("last_episode_to_air")
    private LastEpisodeToAir last_episode_to_air;
    @SerializedName("networks")
    private List<Network> networks;
    @SerializedName("spoken_languages")
    private List<SpokenLanguage> spoken_languages;
    @SerializedName("production_countries")
    private List<ProductionCountry> production_countries;

    public Dates getDates() {
        return dates;
    }

    public void setDates(Dates dates) {
        this.dates = dates;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
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

    public String getOriginal_name() {
        return original_name;
    }

    public void setOriginal_name(String original_name) {
        this.original_name = original_name;
    }

    public String getFirst_air_date() {
        return first_air_date;
    }

    public void setFirst_air_date(String first_air_date) {
        this.first_air_date = first_air_date;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }

    public String getBackdrop_path() {
        return backdrop_path;
    }

    public void setBackdrop_path(String backdrop_path) {
        this.backdrop_path = backdrop_path;
    }

    public String getOriginal_language() {
        return original_language;
    }

    public void setOriginal_language(String original_language) {
        this.original_language = original_language;
    }

    public double getVote_average() {
        return vote_average;
    }

    public void setVote_average(double vote_average) {
        this.vote_average = vote_average;
    }

    public int getVote_count() {
        return vote_count;
    }

    public void setVote_count(int vote_count) {
        this.vote_count = vote_count;
    }

    public boolean isIn_production() {
        return in_production;
    }

    public void setIn_production(boolean in_production) {
        this.in_production = in_production;
    }

    public int getNumber_of_episodes() {
        return number_of_episodes;
    }

    public void setNumber_of_episodes(int number_of_episodes) {
        this.number_of_episodes = number_of_episodes;
    }

    public int getNumber_of_seasons() {
        return number_of_seasons;
    }

    public void setNumber_of_seasons(int number_of_seasons) {
        this.number_of_seasons = number_of_seasons;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    public List<Season> getSeasons() {
        return seasons;
    }

    public void setSeasons(List<Season> seasons) {
        this.seasons = seasons;
    }

    public List<CreatedBy> getCreated_by() {
        return created_by;
    }

    public void setCreated_by(List<CreatedBy> created_by) {
        this.created_by = created_by;
    }

    public LastEpisodeToAir getLast_episode_to_air() {
        return last_episode_to_air;
    }

    public void setLast_episode_to_air(LastEpisodeToAir last_episode_to_air) {
        this.last_episode_to_air = last_episode_to_air;
    }

    public List<Network> getNetworks() {
        return networks;
    }

    public void setNetworks(List<Network> networks) {
        this.networks = networks;
    }

    public List<SpokenLanguage> getSpoken_languages() {
        return spoken_languages;
    }

    public void setSpoken_languages(List<SpokenLanguage> spoken_languages) {
        this.spoken_languages = spoken_languages;
    }

    public List<ProductionCountry> getProduction_countries() {
        return production_countries;
    }

    public void setProduction_countries(List<ProductionCountry> production_countries) {
        this.production_countries = production_countries;
    }

    public static class Genre {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        // Getters and Setters
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
    }

    public static class Cast {
        @SerializedName("cast_id")
        private int castId;

        @SerializedName("name")
        private String castName;

        @SerializedName("profile_path")
        private String profilePath;

        // Getters and Setters
        public int getCastId() {
            return castId;
        }

        public void setCastId(int castId) {
            this.castId = castId;
        }

        public String getCastName() {
            return castName;
        }

        public void setCastName(String castName) {
            this.castName = castName;
        }

        public String getProfilePath() {
            return profilePath;
        }

        public void setProfilePath(String profilePath) {
            this.profilePath = profilePath;
        }

    }
}
