package emplay.entertainment.emplay.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import emplay.entertainment.emplay.models.TrailerModel;

public class MoviesTrailerResponses {

    @SerializedName("id")
    private int id;

    @SerializedName("results")
    private List<TrailerModel> results;


    // Parameterized constructor
    public MoviesTrailerResponses(int id, List<TrailerModel> results) {
        this.id = id;
        this.results = results;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<TrailerModel> getResults() {
        return results;
    }

    public void setResults(List<TrailerModel> results) {
        this.results = results;
    }

    // Nested TrailerModel class
    public static class TrailerModel {
        @SerializedName("id")
        private String id;
        @SerializedName("iso_639_1")
        private String iso_639_1;
        @SerializedName("iso_3166_1")
        private String iso_3166_1;
        @SerializedName("name")
        private String name;
        @SerializedName("key")
        private String key;
        @SerializedName("size")
        private int size;
        @SerializedName("type")
        private String type;
        @SerializedName("official")
        private boolean official;
        @SerializedName("published_at")
        private String published_at;


        public TrailerModel(String id, String iso_639_1, String iso_3166_1, String name, String key, int size, String type, boolean official, String published_at) {
            this.id = id;
            this.iso_639_1 = iso_639_1;
            this.iso_3166_1 = iso_3166_1;
            this.name = name;
            this.key = key;
            this.size = size;
            this.type = type;
            this.official = official;
            this.published_at = published_at;
        }

        public TrailerModel(String key, String name) {
            this.name = name;
            this.key = key;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getIso_639_1() {
            return iso_639_1;
        }

        public void setIso_639_1(String iso_639_1) {
            this.iso_639_1 = iso_639_1;
        }

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

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isOfficial() {
            return official;
        }

        public void setOfficial(boolean official) {
            this.official = official;
        }

        public String getPublished_at() {
            return published_at;
        }

        public void setPublished_at(String published_at) {
            this.published_at = published_at;
        }
    }
}
