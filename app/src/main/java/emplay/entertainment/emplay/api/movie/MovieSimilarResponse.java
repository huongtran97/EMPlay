package emplay.entertainment.emplay.api.movie;

import java.util.List;
import emplay.entertainment.emplay.models.movie.MovieModel;

public class MovieSimilarResponse {
    private List<MovieModel> results;

    public List<MovieModel> getResults() {
        return results;
    }

    public void setResults(List<MovieModel> results) {
        this.results = results;
    }


}
