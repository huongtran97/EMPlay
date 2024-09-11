package emplay.entertainment.emplay.api;



import java.util.List;
import emplay.entertainment.emplay.models.MovieModel;

public class MovieSimilarResponse {
    private List<MovieModel> results;

    public List<MovieModel> getResults() {
        return results;
    }

    public void setResults(List<MovieModel> results) {
        this.results = results;
    }


}
