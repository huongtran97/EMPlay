package emplay.entertainment.emplay.models;

import com.google.gson.annotations.SerializedName;

public class GenresModel {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    public GenresModel() {
    }
    public GenresModel(int id, String name) {
        this.id = id;
        this.name = name;
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
}
