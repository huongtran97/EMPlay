package emplay.entertainment.emplay.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import emplay.entertainment.emplay.models.GenresModel;

public class GenresResponse {

    @SerializedName("genres")
    private List<GenresModel> genres;

    // Getter and Setter for genres
    public List<GenresModel> getGenres() {
        return genres;
    }

    public void setGenres(List<GenresModel> genres) {
        this.genres = genres;
    }
}
