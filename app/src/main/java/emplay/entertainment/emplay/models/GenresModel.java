package emplay.entertainment.emplay.models;

import com.google.gson.annotations.SerializedName;

public class GenresModel {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    private final int colorRes;

    public GenresModel(int id, String name, int colorRes) {
        this.id = id;
        this.name = name;
        this.colorRes = colorRes;
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
    public int getColorRes() {return colorRes;}


}
