package emplay.entertainment.emplay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author Tran Ngoc Que Huong
 * @version 1.0
 *
 * Fragment for searching movies.
 */
public class SearchFragment extends Fragment {

    private SearchMovieAdapter searchAdapter;
    private List<MovieModel> searchMovieList;
    private MovieApiService apiService;

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to. The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return The View for the fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.movie_search_view, container, false);

        EditText inputSearch = view.findViewById(R.id.input_title);
        RecyclerView recyclerView = view.findViewById(R.id.search_recycler_view);
        ImageButton searchButton = view.findViewById(R.id.search_movie_btn);

        searchMovieList = new ArrayList<>();
        searchAdapter = new SearchMovieAdapter(searchMovieList, this::showMovieDetails);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(searchAdapter);

        apiService = ApiClient.getClient().create(MovieApiService.class);

        searchButton.setOnClickListener(v -> {
            String query = inputSearch.getText().toString().trim();
            if (!query.isEmpty()) {
                searchMovies(query);
            } else {
                Toast.makeText(getContext(), "Please enter a movie title", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    /**
     * Performs a search for movies using the specified query.
     *
     * @param query The movie title query.
     */
    private void searchMovies(String query) {
        String apiKey = "ff3dce8592d15d036bf53cbedeca224b";
        Call<MovieResponse> call = apiService.searchMovies(apiKey, query);
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    searchMovieList.clear();
                    List<MovieModel> movies = response.body().getResults();
                    if (movies != null && !movies.isEmpty()) {
                        searchMovieList.addAll(movies);
                        searchAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "No results found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to get results", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load movies", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Displays movie details.
     *
     * @param movie The movie to display details for.
     */
    private void showMovieDetails(MovieModel movie) {
        String movieName = movie.getName();
        if (movieName != null) {
            Toast.makeText(getContext(), movieName + " Movie", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Movie name is not found", Toast.LENGTH_SHORT).show();
        }
    }
}


















