package emplay.entertainment.emplay.api.common;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import emplay.entertainment.emplay.models.common.GenresModel;

public class GenresResponse {

    @SerializedName("genres")
    private List<GenresModel> genres;

    public List<GenresModel> getGenres() {
        return genres;
    }

    public void setGenres(List<GenresModel> genres) {
        this.genres = genres;
    }
}
