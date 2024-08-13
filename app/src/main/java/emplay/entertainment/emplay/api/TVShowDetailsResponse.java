package emplay.entertainment.emplay.api;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import emplay.entertainment.emplay.api.MovieDetailsResponse.Cast;
import emplay.entertainment.emplay.api.MovieDetailsResponse.Genre;
import emplay.entertainment.emplay.models.ProductContries;
import emplay.entertainment.emplay.models.Season;
import emplay.entertainment.emplay.models.TVShowModel;

public class TVShowDetailsResponse {
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



    public TVShowDetailsResponse(int id, String name, String original_name, String first_air_date, String overview, String poster_path, String backdrop_path, String original_language, double vote_average, int vote_count, boolean in_production, int number_of_episodes, int number_of_seasons, String status, String tagline, List<Genre> genres, List<Season> seasons, List<CreatedBy> created_by, LastEpisodeToAir last_episode_to_air, List<Network> networks, List<SpokenLanguage> spoken_languages, List<ProductionCountry> production_countries) {
        this.id = id;
        this.name = name;
        this.original_name = original_name;
        this.first_air_date = first_air_date;
        this.overview = overview;
        this.poster_path = poster_path;
        this.backdrop_path = backdrop_path;
        this.original_language = original_language;
        this.vote_average = vote_average;
        this.vote_count = vote_count;
        this.in_production = in_production;
        this.number_of_episodes = number_of_episodes;
        this.number_of_seasons = number_of_seasons;
        this.status = status;
        this.tagline = tagline;
        this.genres = genres;
        this.seasons = seasons;
        this.created_by = created_by;
        this.last_episode_to_air = last_episode_to_air;
        this.networks = networks;
        this.spoken_languages = spoken_languages;
        this.production_countries = production_countries;
    }

    // Getters and Setters for all fields

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

        public Genre(int id, String name) {
            this.id = id;
            this.name = name;
        }

        //Getters and setters
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


    public static class Season {
        @SerializedName("id")
        private int id;
        @SerializedName("name")
        private String name;
        @SerializedName("air_date")
        private String air_date;
        @SerializedName("overview")
        private String overview;
        @SerializedName("poster_path")
        private String poster_path;
        @SerializedName("season_number")
        private int season_number;
        @SerializedName("episode_count")
        private int episode_count;
        @SerializedName("vote_average")
        private double vote_average;

        public Season(int id, String name, String air_date, String overview, String poster_path, int season_number, int episode_count, double vote_average) {
            this.id = id;
            this.name = name;
            this.air_date = air_date;
            this.overview = overview;
            this.poster_path = poster_path;
            this.season_number = season_number;
            this.episode_count = episode_count;
            this.vote_average = vote_average;
        }

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

        public String getAir_date() {
            return air_date;
        }

        public void setAir_date(String air_date) {
            this.air_date = air_date;
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

        public int getSeason_number() {
            return season_number;
        }

        public void setSeason_number(int season_number) {
            this.season_number = season_number;
        }

        public int getEpisode_count() {
            return episode_count;
        }

        public void setEpisode_count(int episode_count) {
            this.episode_count = episode_count;
        }

        public double getVote_average() {
            return vote_average;
        }

        public void setVote_average(double vote_average) {
            this.vote_average = vote_average;
        }


    }

    public static class CreatedBy {
        @SerializedName("id")
        private int id;
        @SerializedName("credit_id")
        private String credit_id;
        @SerializedName("name")
        private String name;
        @SerializedName("original_name")
        private String original_name;
        @SerializedName("gender")
        private int gender;
        @SerializedName("profile_path")
        private String profile_path;

        public CreatedBy(int id, String credit_id, String name, String original_name, int gender, String profile_path) {
            this.id = id;
            this.credit_id = credit_id;
            this.name = name;
            this.original_name = original_name;
            this.gender = gender;
            this.profile_path = profile_path;
        }

        // Getters and Setters

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getCredit_id() {
            return credit_id;
        }

        public void setCredit_id(String credit_id) {
            this.credit_id = credit_id;
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

        public int getGender() {
            return gender;
        }

        public void setGender(int gender) {
            this.gender = gender;
        }

        public String getProfile_path() {
            return profile_path;
        }

        public void setProfile_path(String profile_path) {
            this.profile_path = profile_path;
        }


    }

    public static class LastEpisodeToAir {
        @SerializedName("id")
        private int id;
        @SerializedName("name")
        private String name;
        @SerializedName("overview")
        private String overview;
        @SerializedName("vote_average")
        private double vote_average;
        @SerializedName("vote_count")
        private int vote_count;
        @SerializedName("air_date")
        private String air_date;
        @SerializedName("episode_number")
        private int episode_number;
        @SerializedName("episode_type")
        private String episode_type;
        @SerializedName("production_code")
        private String production_code;
        @SerializedName("runtime")
        private int runtime;
        @SerializedName("season_number")
        private int season_number;
        @SerializedName("still_path")
        private String still_path;

        public LastEpisodeToAir(int id, String name, String overview, double vote_average, int vote_count, String air_date, int episode_number, String episode_type, String production_code, int runtime, int season_number, String still_path) {
            this.id = id;
            this.name = name;
            this.overview = overview;
            this.vote_average = vote_average;
            this.vote_count = vote_count;
            this.air_date = air_date;
            this.episode_number = episode_number;
            this.episode_type = episode_type;
            this.production_code = production_code;
            this.runtime = runtime;
            this.season_number = season_number;
            this.still_path = still_path;
        }

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

        public String getOverview() {
            return overview;
        }

        public void setOverview(String overview) {
            this.overview = overview;
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

        public String getAir_date() {
            return air_date;
        }

        public void setAir_date(String air_date) {
            this.air_date = air_date;
        }

        public int getEpisode_number() {
            return episode_number;
        }

        public void setEpisode_number(int episode_number) {
            this.episode_number = episode_number;
        }

        public String getEpisode_type() {
            return episode_type;
        }

        public void setEpisode_type(String episode_type) {
            this.episode_type = episode_type;
        }

        public String getProduction_code() {
            return production_code;
        }

        public void setProduction_code(String production_code) {
            this.production_code = production_code;
        }

        public int getRuntime() {
            return runtime;
        }

        public void setRuntime(int runtime) {
            this.runtime = runtime;
        }

        public int getSeason_number() {
            return season_number;
        }

        public void setSeason_number(int season_number) {
            this.season_number = season_number;
        }

        public String getStill_path() {
            return still_path;
        }

        public void setStill_path(String still_path) {
            this.still_path = still_path;
        }


    }

    public static class Network {
        @SerializedName("id")
        private int id;
        @SerializedName("name")
        private String name;
        @SerializedName("origin_country")
        private String origin_country;
        @SerializedName("logo_path")
        private String logo_path;

        public Network(int id, String name, String origin_country, String logo_path) {
            this.id = id;
            this.name = name;
            this.origin_country = origin_country;
            this.logo_path = logo_path;
        }

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

        public String getOrigin_country() {
            return origin_country;
        }

        public void setOrigin_country(String origin_country) {
            this.origin_country = origin_country;
        }

        public String getLogo_path() {
            return logo_path;
        }

        public void setLogo_path(String logo_path) {
            this.logo_path = logo_path;
        }


    }

    public static class SpokenLanguage {
        @SerializedName("english_name")
        private String english_name;
        @SerializedName("iso_639_1")
        private String iso_639_1;
        @SerializedName("name")
        private String name;

        public SpokenLanguage(String english_name, String iso_639_1, String name) {
            this.english_name = english_name;
            this.iso_639_1 = iso_639_1;
            this.name = name;
        }

        // Getters and Setters

        public String getEnglish_name() {
            return english_name;
        }

        public void setEnglish_name(String english_name) {
            this.english_name = english_name;
        }

        public String getIso_639_1() {
            return iso_639_1;
        }

        public void setIso_639_1(String iso_639_1) {
            this.iso_639_1 = iso_639_1;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


    }

    public static class ProductionCountry {
        @SerializedName("iso_3166_1")
        private String iso_3166_1;
        @SerializedName("name")
        private String name;

        public ProductionCountry(String iso_3166_1, String name) {
            this.iso_3166_1 = iso_3166_1;
            this.name = name;
        }

        // Getters and Setters

        public String getIso_3166_1() {
            return iso_3166_1;
        }

        public void setIso_3166_1(String iso_3166_1) {
            this.iso_3166_1 = iso_3166_1;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


    }
}
