package emplay.entertainment.emplay.fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.tool.LanguageMapper;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.GenresAdapter;
import emplay.entertainment.emplay.adapter.SearchMovieAdapter;
import emplay.entertainment.emplay.api.ApiClient;
import emplay.entertainment.emplay.api.GenresResponse;
import emplay.entertainment.emplay.api.MovieApiService;
import emplay.entertainment.emplay.api.TMDBpath;
import emplay.entertainment.emplay.api.MovieResponse;
import emplay.entertainment.emplay.models.GenresModel;
import emplay.entertainment.emplay.models.MovieModel;
import emplay.entertainment.emplay.models.SharedViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchMoviesFragment extends Fragment {

    private SearchMovieAdapter searchAdapter;
    private GenresAdapter genresAdapter;
    private List<MovieModel> searchMovieList;
    private List<GenresModel> genresList;
    private RecyclerView genresRecyclerView, recyclerView;
    private MovieApiService apiService;
    private EditText inputSearch;
    private SharedViewModel viewModel;
    private LinearLayout searchMovieLayout;
    private LinearLayout searchTVShowLayout;
    private android.widget.TextView genresLabel;
    private String lastQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.movie_search_view, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        inputSearch = view.findViewById(R.id.input_title);
        recyclerView = view.findViewById(R.id.search_recycler_view);
        genresRecyclerView = view.findViewById(R.id.genres_recycler_view);
        genresLabel = view.findViewById(R.id.genres_label);
        ImageButton searchButton = view.findViewById(R.id.search_movie_btn);
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

        genresRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        genresRecyclerView.setAdapter(genresAdapter);

        apiService = ApiClient.getClient().create(MovieApiService.class);

        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = s.length() > 0;
                genresRecyclerView.setVisibility(hasText ? View.GONE : View.VISIBLE);
                genresLabel.setVisibility(hasText ? View.GONE : View.VISIBLE);
                performSearch();
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
            inputSearch.setText(savedInstanceState.getString("searchQuery", ""));
        }

        updateMenuSelection();
        Boolean isTVShow = viewModel.getIsTVShowSearch().getValue();
        updateToggleUI(isTVShow == null || !isTVShow);
        setupObservers();
        fetchGenresForMovie();

        return view;
    }

    private void onItemClick(GenresModel genre) {
        if (genre == null) return;
        MovieByGenresFragment fragment = MovieByGenresFragment.newInstance(genre.getId(), genre.getName());
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void fetchGenresForMovie() {
        Call<GenresResponse> call = apiService.getGenresMovie(TMDBpath.genresMovie());
        call.enqueue(new Callback<GenresResponse>() {
            @Override
            public void onResponse(Call<GenresResponse> call, Response<GenresResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GenresModel> genres = response.body().getGenres();
                    if (genres != null) {
                        genresList.clear();
                        genresList.addAll(genres);
                        genresAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<GenresResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to fetch genres", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMenuSelection() {
        boolean isTVShow = Boolean.TRUE.equals(viewModel.getIsTVShowSearch().getValue());
        searchMovieLayout.setSelected(!isTVShow);
        searchTVShowLayout.setSelected(isTVShow);
    }

    private void onTVShowClick() {
        inputSearch.setText("");
        viewModel.setIsTVShowSearch(true);
        updateMenuSelection();
        updateToggleUI(false);
        replaceFragment(new SearchTVShowsFragment(), "SearchTVShowsFragment");
    }

    private void onMovieClick() {
        if (Boolean.FALSE.equals(viewModel.getIsTVShowSearch().getValue())) return;
        viewModel.setIsTVShowSearch(false);
        updateMenuSelection();
        updateToggleUI(true);
        replaceFragment(new SearchMoviesFragment(), "SearchMoviesFragment");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("searchQuery", inputSearch.getText().toString());
    }

    private void performSearch() {
        String query = inputSearch.getText().toString().trim();
        if (!query.isEmpty()) {
            if (query.equals(lastQuery)) return;
            lastQuery = query;
            searchMovies(query);
            viewModel.setLastSearchWasTVShow(false);
        } else {
            lastQuery = "";
            searchMovieList.clear();
            searchAdapter.notifyDataSetChanged();
            viewModel.setSearchResults(new ArrayList<>());
        }
    }

    void searchMovies(String query) {
        Call<MovieResponse> call = apiService.searchMovies(TMDBpath.searchMovies(), query);
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MovieModel> movies = response.body().getResults();
                    if (movies != null && !movies.isEmpty()) {
                        List<MovieModel> filtered = new ArrayList<>();
                        for (MovieModel movie : movies) {
                            if (movie.getPosterPath() != null) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    movie.setOriginalLanguage(LanguageMapper.getLanguageName(movie.getOriginalLanguage()));
                                }
                                filtered.add(movie);
                            }
                        }
                        viewModel.setSearchResults(filtered.isEmpty() ? new ArrayList<>() : filtered);
                        if (filtered.isEmpty()) Toast.makeText(getContext(), "No results found", Toast.LENGTH_SHORT).show();
                    } else {
                        viewModel.setSearchResults(new ArrayList<>());
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

    private void hideKeyboard() {
        View focused = requireActivity().getCurrentFocus();
        if (focused != null) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
        }
    }

    private void showMovieDetails(MovieModel movie) {
        if (movie == null) {
            Toast.makeText(getContext(), "Movie details are not available", Toast.LENGTH_SHORT).show();
            return;
        }
        ShowResultDetailsFragment fragment = ShowResultDetailsFragment.newInstance(movie.getId());
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setupObservers() {
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), movies -> {
            searchMovieList.clear();
            searchMovieList.addAll(movies);
            searchAdapter.notifyDataSetChanged();
        });

        viewModel.getIsTVShowSearch().observe(getViewLifecycleOwner(), isTVShowSearch -> {
            updateMenuSelection();
            updateToggleUI(!isTVShowSearch);
        });
    }

    private void replaceFragment(Fragment fragment, String tag) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, tag);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void updateToggleUI(boolean isMovie) {
        searchMovieLayout.setBackgroundResource(isMovie ? R.drawable.toggle_selected_bg : android.R.color.transparent);
        searchTVShowLayout.setBackgroundResource(isMovie ? android.R.color.transparent : R.drawable.toggle_selected_bg);
    }
}
