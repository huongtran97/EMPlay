package emplay.entertainment.emplay.fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import emplay.entertainment.emplay.tool.LanguageMapper;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.GenresAdapter;
import emplay.entertainment.emplay.adapter.SearchMovieAdapter;
import emplay.entertainment.emplay.api.ApiClient;
import emplay.entertainment.emplay.api.GenresResponse;
import emplay.entertainment.emplay.api.MovieApiService;
import emplay.entertainment.emplay.api.MovieResponse;
import emplay.entertainment.emplay.models.GenresModel;
import emplay.entertainment.emplay.models.MovieModel;
import emplay.entertainment.emplay.models.SharedViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchMoviesFragment extends Fragment {
    private static final String API_KEY = "ff3dce8592d15d036bf53cbedeca224b";
    private SearchMovieAdapter searchAdapter;
    private GenresAdapter genresAdapter;
    private List<MovieModel> searchMovieList;
    private List<GenresModel> genresList;
    private RecyclerView genresRecyclerView, recyclerView;
    private ImageButton searchButton;
    private MovieApiService apiService;
    private EditText inputSearch;
    private SharedViewModel viewModel;
    private LinearLayout searchMovieLayout;
    private LinearLayout searchTVShowLayout;
    private boolean isTVShowSearch = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.movie_search_view, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        inputSearch = view.findViewById(R.id.input_title);
        recyclerView = view.findViewById(R.id.search_recycler_view);
        genresRecyclerView = view.findViewById(R.id.genres_recycler_view);
        searchButton = view.findViewById(R.id.search_movie_btn);
        searchMovieLayout = view.findViewById(R.id.menu_movie_layout);
        searchTVShowLayout = view.findViewById(R.id.menu_tvshow_layout);


        searchMovieLayout.setOnClickListener(v -> onMovieClick());
        searchTVShowLayout.setOnClickListener(v -> onTVShowClick());

        searchMovieList = new ArrayList<>();
        genresList = new ArrayList<>();


        searchAdapter = new SearchMovieAdapter(searchMovieList, this::showMovieDetails);
        genresAdapter = new GenresAdapter(genresList, this::onItemClick);


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(searchAdapter);

        genresRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        genresRecyclerView.setAdapter(genresAdapter);


        apiService = ApiClient.getClient().create(MovieApiService.class);

        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        searchButton.setOnClickListener(v -> {
            performSearch();
            hideKeyboard();
        });

        inputSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                hideKeyboard();
                return true;
            }
            return false;
        });

        if (savedInstanceState != null) {
            isTVShowSearch = savedInstanceState.getBoolean("isTVShowSearch", false);
            inputSearch.setText(savedInstanceState.getString("searchQuery", ""));
        }

        updateMenuSelection();
        setupObservers();
        fetchGenresForMovie();

        return view;
    }

    private void onItemClick(GenresModel genre) {
        if (genre != null) {
            MovieByGenresFragment movieByGenresFragment = MovieByGenresFragment.newInstance(genre.getId(), genre.getName());
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, movieByGenresFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    private void fetchGenresForMovie() {
        Call<GenresResponse> call = apiService.getGenresMovie(API_KEY);
        call.enqueue(new Callback<GenresResponse>() {
            @Override
            public void onResponse(Call<GenresResponse> call, Response<GenresResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GenresModel> genres = response.body().getGenres();
                    if (genres != null) {
                        genresList.clear();
                        genresList.addAll(genres);
                        genresAdapter.notifyDataSetChanged();
                        Log.d("Adapter Update", "Genres list updated, size: " + genresList.size());
                        Log.d("API Response", "Genres fetched: " + genres.size());
                    } else {
                        Log.e("API Response", "Genres list is null");
                    }
                } else {
                    Log.e("API Error", "Response code: " + response.code() + ", message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<GenresResponse> call, Throwable t) {
                Log.e("API Failure", "Error: " + t.getMessage());
                Toast.makeText(getContext(), "Failed to fetch genres", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateMenuSelection() {
        boolean isTVShow = viewModel.getIsTVShowSearch().getValue() != null && viewModel.getIsTVShowSearch().getValue();
        searchMovieLayout.setSelected(!isTVShow);
        searchTVShowLayout.setSelected(isTVShow);
    }

    private void onTVShowClick() {
        inputSearch.setText("");
        viewModel.setIsTVShowSearch(true);
        updateMenuSelection();
        replaceFragment(new SearchTVShowsFragment(), "SearchTVShowsFragment");
    }

    private void onMovieClick() {
        viewModel.setIsTVShowSearch(false);
        updateMenuSelection();
        replaceFragment(new SearchMoviesFragment(), "SearchMoviesFragment");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isTVShowSearch", isTVShowSearch);
        outState.putString("searchQuery", inputSearch.getText().toString());
    }

    private void performSearch() {
        String query = inputSearch.getText().toString().trim();
        if (!query.isEmpty()) {
            if (isTVShowSearch) {
                Fragment tvShowsFragment = getParentFragmentManager().findFragmentByTag("SearchTVShowsFragment");
                if (tvShowsFragment instanceof SearchTVShowsFragment) {
                    ((SearchTVShowsFragment) tvShowsFragment).searchTVShows(query);
                } else {
                    Log.e("PerformSearch", "SearchTVShowsFragment is not found, creating a new instance");
                    SearchTVShowsFragment newFragment = new SearchTVShowsFragment();
                    replaceFragment(newFragment, "SearchTVShowsFragment");
                    newFragment.searchTVShows(query);  // perform the search
                }
            } else {
                searchMovies(query);
            }
            viewModel.setLastSearchWasTVShow(isTVShowSearch);
        } else {
            searchMovieList.clear();
            searchAdapter.notifyDataSetChanged();
        }
    }

    void searchMovies(String query) {
        Call<MovieResponse> call = apiService.searchMovies(API_KEY, query);
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    searchMovieList.clear();
                    List<MovieModel> movies = response.body().getResults();

                    if (movies != null && !movies.isEmpty()) {
                        // Filter out movies with null posterPath
                        List<MovieModel> filteredMovies = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            filteredMovies = movies.stream()
                                    .filter(movie -> movie.getPosterPath() != null)
                                    .collect(Collectors.toList());
                        }

                        if (!filteredMovies.isEmpty()) {
                            for (MovieModel movie : filteredMovies) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    movie.setOriginalLanguage(LanguageMapper.getLanguageName(movie.getOriginalLanguage()));
                                }
                            }
                            searchMovieList.addAll(filteredMovies);
                            searchAdapter.notifyDataSetChanged();
                            viewModel.setSearchResults(filteredMovies);
                        } else {
                            Toast.makeText(getContext(), "No results found", Toast.LENGTH_SHORT).show();
                        }
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

        viewModel.getIsTVShowSearch().observe(getViewLifecycleOwner(), isTVShowSearch -> {
            this.isTVShowSearch = isTVShowSearch;
            updateMenuSelection();
        });
    }

    private void replaceFragment(Fragment fragment, String tag) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, tag);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
