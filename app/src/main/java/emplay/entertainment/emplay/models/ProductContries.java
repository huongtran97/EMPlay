package emplay.entertainment.emplay.models;

import com.google.gson.annotations.SerializedName;

public class ProductContries {
    @SerializedName("name")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
