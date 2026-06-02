package emplay.entertainment.emplay.fragment.layout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import androidx.core.widget.NestedScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.fragment.common.TrendingSeeAllFragment;
import emplay.entertainment.emplay.fragment.details.MovieResultDetailsFragment;
import emplay.entertainment.emplay.fragment.genre.MovieByGenresFragment;
import emplay.entertainment.emplay.tool.BadgeHelper;
import emplay.entertainment.emplay.tool.LanguageMapper;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.GenresAdapter;
import emplay.entertainment.emplay.adapter.movie.SearchMovieAdapter;
import emplay.entertainment.emplay.adapter.common.TrendingSearchAdapter;
import emplay.entertainment.emplay.api.common.ApiClient;
import emplay.entertainment.emplay.api.common.GenresResponse;
import emplay.entertainment.emplay.api.common.MovieApiService;
import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.api.movie.MovieResponse;
import emplay.entertainment.emplay.models.common.GenresModel;
import emplay.entertainment.emplay.models.common.MediaItem;
import emplay.entertainment.emplay.models.movie.MovieModel;
import emplay.entertainment.emplay.models.common.SharedViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *  Movie search tab. Shows a genre grid by default, switches to search results as soon as
 *  the user starts typing. Tapping "TV Shows" in the toggle swaps this fragment out for
 *  SearchTVShowsFragment so both tabs share the same layout.
 */
public class SearchMoviesFragment extends Fragment {

    private SearchMovieAdapter searchAdapter;
    private GenresAdapter genresAdapter;
    private TrendingSearchAdapter trendingAdapter;
    private final List<MovieModel> searchMovieList = new ArrayList<>();
    private final List<GenresModel> genresList = new ArrayList<>();
    private final List<MovieModel> trendingList = new ArrayList<>();
    private RecyclerView rvGenres, rvSearchResults, rvTrendingSearches;
    private MovieApiService apiService;
    private TextInputEditText etSearch;
    private SharedViewModel viewModel;
    private MaterialButton btnMovie, btnTvShow;
    private NestedScrollView svSearchDefault;
    private FlexboxLayout pillsContainer;
    private DatabaseHelper dbHelper;
    private String lastQuery = "";
    TextView movieClearAll, btnSeeAll;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_view, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        dbHelper = DatabaseHelper.getInstance(requireContext());
        apiService = ApiClient.getClient().create(MovieApiService.class);

        etSearch = view.findViewById(R.id.etSearch);
        rvSearchResults = view.findViewById(R.id.rvSearchResults);
        rvGenres = view.findViewById(R.id.rvGenres);
        rvTrendingSearches = view.findViewById(R.id.rvTrendingSearches);
        svSearchDefault = view.findViewById(R.id.svSearchDefault);
        pillsContainer = view.findViewById(R.id.pillsContainer);
        movieClearAll = view.findViewById(R.id.btnClearAll);
        if (movieClearAll != null) {
            movieClearAll.setPaintFlags(movieClearAll.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }


        btnMovie = view.findViewById(R.id.btnMovie);
        btnTvShow = view.findViewById(R.id.btnTvShow);
        btnSeeAll = view.findViewById(R.id.btnSeeAll);

        btnMovie.setOnClickListener(v -> onMovieClick());
        btnTvShow.setOnClickListener(v -> onTVShowClick());
        btnSeeAll.setOnClickListener(v -> navigateTo(TrendingSeeAllFragment.newInstance(false)));
        assert movieClearAll != null;
        movieClearAll.setOnClickListener(v -> clearHistory());

        setupRecyclerViews();
        setupSearchInput();
        setupObservers();

        loadDiscoveryData();

        if (savedInstanceState != null) {
            etSearch.setText(savedInstanceState.getString("searchQuery", ""));
        }

        updateToggleUI();

        return view;
    }

    private void navigateTo(Fragment fragment) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setupRecyclerViews() {
        searchAdapter = new SearchMovieAdapter(searchMovieList, this::showMovieDetails);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSearchResults.setAdapter(searchAdapter);

        genresAdapter = new GenresAdapter(genresList, this::onGenreClick);
        rvGenres.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvGenres.setNestedScrollingEnabled(false);
        rvGenres.setAdapter(genresAdapter);

        trendingAdapter = new TrendingSearchAdapter(getContext(), trendingList, this::onTrendingClick);
        rvTrendingSearches.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTrendingSearches.setAdapter(trendingAdapter);
    }

    private void setupSearchInput() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = s.length() > 0;
                svSearchDefault.setVisibility(hasText ? View.GONE : View.VISIBLE);
                rvSearchResults.setVisibility(hasText ? View.VISIBLE : View.GONE);
                performSearch();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = Objects.requireNonNull(etSearch.getText()).toString().trim();
                if (!query.isEmpty()) {
                    dbHelper.addRecentSearch(query);
                    loadRecentSearches();
                }
                hideKeyboard();
                return true;
            }
            return false;
        });
    }

    private void loadDiscoveryData() {
        loadRecentSearches();
        fetchGenresForMovie();
        fetchTrendingMovies();
    }

    private void loadRecentSearches() {
        pillsContainer.removeAllViews();
        List<String> searches = dbHelper.getRecentSearches();
        for (String query : searches) {
            View pillView = LayoutInflater.from(requireContext()).inflate(R.layout.search_pill_item, pillsContainer, false);
            TextView tvPill = pillView.findViewById(R.id.tvPillText);
            tvPill.setText(query);
            pillView.setOnClickListener(v -> {
                etSearch.setText(query);
                etSearch.setSelection(query.length());
            });
            pillsContainer.addView(pillView);
        }
    }

    private void clearHistory() {
        dbHelper.clearRecentSearches();
        pillsContainer.removeAllViews();
    }

    private void fetchTrendingMovies() {
        apiService.getTrendingMovies(TMDBpath.trendingMovies()).enqueue(new Callback<MovieResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    trendingList.clear();
                    List<MovieModel> results = response.body().getResults();
                    if (results != null) {
                        // Sort by rating highest to lowest
                        Collections.sort(results, (m1, m2) -> Double.compare(m2.getVoteAverage(), m1.getVoteAverage()));
                        trendingList.addAll(results.subList(0, Math.min(results.size(), 5)));
                    }
                    trendingAdapter.notifyDataSetChanged();
                }
            }
            @Override public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {}
        });
    }


    private void onGenreClick(GenresModel genre) {
        if (genre == null) return;
        MovieByGenresFragment fragment = MovieByGenresFragment.newInstance(genre.getId(), genre.getName());
        replaceFragment(fragment, "MovieByGenresFragment");
    }

    private void fetchGenresForMovie() {
        apiService.getGenresMovie(TMDBpath.genresMovie()).enqueue(new Callback<GenresResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<GenresResponse> call, @NonNull Response<GenresResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    genresList.clear();
                    for (GenresModel g : response.body().getGenres()) {
                        genresList.add(new GenresModel(
                                g.getId(), g.getName(),
                                BadgeHelper.getGenreColor(requireContext(), g.getId())));
                    }
                    genresAdapter.notifyDataSetChanged();
                }
            }
            @Override public void onFailure(@NonNull Call<GenresResponse> call, @NonNull Throwable t) {}
        });
    }



    private void onTVShowClick() {
        viewModel.setIsTVShowSearch(true);
        replaceFragment(new SearchTVShowsFragment(), "SearchTVShowsFragment");
    }

    private void onMovieClick() {

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("searchQuery", Objects.requireNonNull(etSearch.getText()).toString());
    }

    @SuppressLint("NotifyDataSetChanged")
    private void performSearch() {
        String query = Objects.requireNonNull(etSearch.getText()).toString().trim();
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
        apiService.searchMovies(TMDBpath.searchMovies(), query).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MovieModel> movies = response.body().getResults();
                    List<MovieModel> filtered = new ArrayList<>();
                    if (movies != null) {
                        for (MovieModel movie : movies) {
                            if (movie.getPosterPath() != null) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    movie.setOriginalLanguage(LanguageMapper.getLanguageName(movie.getOriginalLanguage()));
                                }
                                filtered.add(movie);
                            }
                        }
                    }
                    viewModel.setSearchResults(filtered);
                }
            }
            @Override public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {}
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
        if (movie == null) return;
        MovieResultDetailsFragment fragment = MovieResultDetailsFragment.newInstance(movie.getMovieId());
        replaceFragment(fragment, "MovieResultDetailsFragment");
    }

    private void onTrendingClick(MediaItem item) {
        if (item == null) return;
        MovieResultDetailsFragment fragment = MovieResultDetailsFragment.newInstance(item.getMediaId());
        replaceFragment(fragment, "MovieResultDetailsFragment");
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setupObservers() {
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), movies -> {
            searchMovieList.clear();
            searchMovieList.addAll(movies);
            searchAdapter.notifyDataSetChanged();
        });
    }

    private void replaceFragment(Fragment fragment, String tag) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, tag);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void updateToggleUI() {
        btnMovie.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.red_primary));
        btnMovie.setTextColor(Color.WHITE);
        btnMovie.setIconTint(ContextCompat.getColorStateList(requireContext(), android.R.color.white));

        btnTvShow.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.toggle_bg));
        btnTvShow.setTextColor(Color.parseColor("#888888"));
        btnTvShow.setIconTint(ContextCompat.getColorStateList(requireContext(), android.R.color.darker_gray));
    }
}
