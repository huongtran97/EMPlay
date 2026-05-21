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
import emplay.entertainment.emplay.api.ApiClient;
import emplay.entertainment.emplay.api.GenresResponse;
import emplay.entertainment.emplay.api.MovieApiService;
import emplay.entertainment.emplay.api.TMDBpath;
import emplay.entertainment.emplay.api.TVShowResponse;
import emplay.entertainment.emplay.models.GenresModel;
import emplay.entertainment.emplay.models.SharedViewModel;
import emplay.entertainment.emplay.models.TVShowModel;
import emplay.entertainment.emplay.adapter.SearchTVAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchTVShowsFragment extends Fragment {

    private SearchTVAdapter searchAdapter;
    private GenresAdapter genresAdapter;
    private List<GenresModel> genresList;
    private List<TVShowModel> searchTVList;
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

        searchTVList = new ArrayList<>();
        genresList = new ArrayList<>();

        searchAdapter = new SearchTVAdapter(searchTVList, this::showTVDetails);
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
        updateToggleUI(false);
        setupObservers();
        fetchGenresForTVShows();

        return view;
    }

    private void onItemClick(GenresModel genre) {
        if (genre == null) return;
        TVShowsByGenresFragment fragment = TVShowsByGenresFragment.newInstance(genre.getId(), genre.getName());
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void fetchGenresForTVShows() {
        Call<GenresResponse> call = apiService.getGenresTVShows(TMDBpath.genresTVShows());
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
        viewModel.setIsTVShowSearch(true);
        updateMenuSelection();
        updateToggleUI(false);
        replaceFragment(new SearchTVShowsFragment(), "SearchTVShowsFragment");
    }

    private void onMovieClick() {
        viewModel.setIsTVShowSearch(false);
        updateMenuSelection();
        updateToggleUI(true);
        replaceFragment(new SearchMoviesFragment(), "SearchMoviesFragment");
    }

    private void performSearch() {
        String query = inputSearch.getText().toString().trim();
        if (!query.isEmpty()) {
            if (query.equals(lastQuery)) return;
            lastQuery = query;
            searchTVShows(query);
            viewModel.setLastSearchWasTVShow(true);
        } else {
            lastQuery = "";
            searchTVList.clear();
            searchAdapter.notifyDataSetChanged();
            viewModel.setSearchTVResults(new ArrayList<>());
        }
    }

    void searchTVShows(String query) {
        Call<TVShowResponse> call = apiService.searchTVShows(TMDBpath.searchTVShows(), query);
        call.enqueue(new Callback<TVShowResponse>() {
            @Override
            public void onResponse(Call<TVShowResponse> call, Response<TVShowResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TVShowModel> tvShows = response.body().getResults();
                    if (tvShows != null && !tvShows.isEmpty()) {
                        List<TVShowModel> filtered = new ArrayList<>();
                        for (TVShowModel tv : tvShows) {
                            if (tv.getPosterPath() != null) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    tv.setOriginalLanguage(LanguageMapper.getLanguageName(tv.getOriginalLanguage()));
                                }
                                filtered.add(tv);
                            }
                        }
                        viewModel.setSearchTVResults(filtered.isEmpty() ? new ArrayList<>() : filtered);
                        if (filtered.isEmpty()) Toast.makeText(getContext(), "No results found", Toast.LENGTH_SHORT).show();
                    } else {
                        viewModel.setSearchTVResults(new ArrayList<>());
                        Toast.makeText(getContext(), "No results found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to get results", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TVShowResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load TV shows", Toast.LENGTH_SHORT).show();
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

    private void showTVDetails(TVShowModel tv) {
        if (tv == null) {
            Toast.makeText(getContext(), "TV show details are not available", Toast.LENGTH_SHORT).show();
            return;
        }
        ShowResultTVShowDetailsFragment fragment = ShowResultTVShowDetailsFragment.newInstance(tv.getTVShowId());
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setupObservers() {
        viewModel.getSearchTVResults().observe(getViewLifecycleOwner(), tvShows -> {
            searchTVList.clear();
            searchTVList.addAll(tvShows);
            searchAdapter.notifyDataSetChanged();
        });

        viewModel.getIsTVShowSearch().observe(getViewLifecycleOwner(), isTVShowSearch -> updateMenuSelection());
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
