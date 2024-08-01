package emplay.entertainment.emplay.moviefragment;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.LanguageMapper;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.movieadapter.SearchMovieAdapter;
import emplay.entertainment.emplay.api.ApiClient;
import emplay.entertainment.emplay.api.MovieApiService;
import emplay.entertainment.emplay.api.MovieResponse;
import emplay.entertainment.emplay.models.MovieModel;
import emplay.entertainment.emplay.models.SharedViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private SearchMovieAdapter searchAdapter;
    private List<MovieModel> searchMovieList;
    private MovieApiService apiService;
    private EditText inputSearch;
    private SharedViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.movie_search_view, container, false);

        // Initialize the ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        inputSearch = view.findViewById(R.id.input_title);
        RecyclerView recyclerView = view.findViewById(R.id.search_recycler_view);
        ImageButton searchButton = view.findViewById(R.id.search_movie_btn);

        searchMovieList = new ArrayList<>();
        searchAdapter = new SearchMovieAdapter(searchMovieList, this::showMovieDetails);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(searchAdapter);

        apiService = ApiClient.getClient().create(MovieApiService.class);

        // Set up EditorActionListener for EditText
        inputSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch();
                return true;
            }
            return false;
        });

        // Set up OnClickListener for Search Button
        searchButton.setOnClickListener(v -> performSearch());

        // Observe LiveData from ViewModel
        setupObservers();

        return view;
    }

    private void performSearch() {
        String query = inputSearch.getText().toString().trim();
        if (!query.isEmpty()) {
            searchMovies(query);
            hideKeyboard();
        } else {
            Toast.makeText(getContext(), "Please enter a movie title", Toast.LENGTH_SHORT).show();
        }
    }

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
                        for (MovieModel movie : movies) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                movie.setOriginalLanguage(LanguageMapper.getLanguageName(movie.getOriginalLanguage()));
                            }
                        }
                        searchMovieList.addAll(movies);
                        searchAdapter.notifyDataSetChanged();
                        viewModel.setSearchResults(movies);
                    } else {
                        Toast.makeText(getContext(), "No results found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("SearchMovies", "Failed to get results. Status code: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e("SearchMovies", "Error response: " + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getContext(), "Failed to get results", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Log.e("SearchMovies", "Failed to load movies", t);
                Toast.makeText(getContext(), "Failed to load movies", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showMovieDetails(MovieModel movie) {
        if (movie != null) {
            ShowResultDetailsFragment showResultDetailsFragment = ShowResultDetailsFragment.newInstance(movie.getId());

            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, showResultDetailsFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            Toast.makeText(getContext(), "Movie details are not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupObservers() {
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), movies -> {
            searchMovieList.clear();
            searchMovieList.addAll(movies);
            searchAdapter.notifyDataSetChanged();
        });
    }
}
