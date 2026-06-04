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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
import emplay.entertainment.emplay.fragment.common.BaseFragment;
import emplay.entertainment.emplay.fragment.common.TrendingSeeAllFragment;
import emplay.entertainment.emplay.fragment.genre.TVShowsByGenresFragment;
import emplay.entertainment.emplay.tool.BadgeHelper;
import emplay.entertainment.emplay.tool.LanguageMapper;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.GenresAdapter;
import emplay.entertainment.emplay.adapter.common.TrendingSearchAdapter;
import emplay.entertainment.emplay.api.common.ApiClient;
import emplay.entertainment.emplay.api.common.GenresResponse;
import emplay.entertainment.emplay.api.common.MovieApiService;
import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.api.tvshow.TVShowResponse;
import emplay.entertainment.emplay.models.common.GenresModel;
import emplay.entertainment.emplay.models.common.MediaItem;
import emplay.entertainment.emplay.models.common.SharedViewModel;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;
import emplay.entertainment.emplay.adapter.tvshow.SearchTVAdapter;
import emplay.entertainment.emplay.fragment.details.TVShowResultDetailsFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *  TV show search tab — mirrors SearchMoviesFragment but for TV.
 */
public class SearchTVShowsFragment extends BaseFragment {

    private SearchTVAdapter searchAdapter;
    private GenresAdapter genresAdapter;
    private TrendingSearchAdapter trendingAdapter;
    private final List<TVShowModel> searchTVList = new ArrayList<>();
    private final List<GenresModel> genresList = new ArrayList<>();
    private final List<TVShowModel> trendingList = new ArrayList<>();
    private RecyclerView rvGenres, rvSearchResults, rvTrendingSearches;
    private MovieApiService apiService;
    private TextInputEditText etSearch;
    private SharedViewModel viewModel;
    private MaterialButton btnMovie, btnTvShow;
    private NestedScrollView svSearchDefault;
    private FlexboxLayout pillsContainer;
    private DatabaseHelper dbHelper;
    private String lastQuery = "";
    TextView tvClearAll, btnSeeAll;

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
        tvClearAll = view.findViewById(R.id.btnClearAll);
        if (tvClearAll != null) {
            tvClearAll.setPaintFlags(tvClearAll.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            tvClearAll.setOnClickListener(v -> clearHistory());
        }

        btnMovie = view.findViewById(R.id.btnMovie);
        btnTvShow = view.findViewById(R.id.btnTvShow);
        btnSeeAll = view.findViewById(R.id.btnSeeAll);

        btnMovie.setOnClickListener(v -> onMovieClick());
        btnTvShow.setOnClickListener(v -> onTVShowClick());
        btnSeeAll.setOnClickListener(v -> navigateTo(TrendingSeeAllFragment.newInstance(true)));

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

    private void setupRecyclerViews() {
        searchAdapter = new SearchTVAdapter(searchTVList, this::showTVDetails);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSearchResults.setAdapter(searchAdapter);

        genresAdapter = new GenresAdapter(genresList, this::onGenreClick);
        rvGenres.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvGenres.setNestedScrollingEnabled(false);
        rvGenres.setAdapter(genresAdapter);

        trendingAdapter = new TrendingSearchAdapter(requireContext(), trendingList, this::onTrendingClick);
        rvTrendingSearches.setLayoutManager(new LinearLayoutManager(requireContext()));
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
        fetchGenresForTVShows();
        fetchTrendingTVShows();
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

    private void fetchTrendingTVShows() {
        safeEnqueue(apiService.getTrendingTVShows(TMDBpath.trendingTVShows()), new Callback<TVShowResponse>() {
            @Override
            public void onResponse(@NonNull Call<TVShowResponse> call, @NonNull Response<TVShowResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TVShowModel> results = response.body().getResults();
                    if (results != null) {
                        Collections.sort(results, (t1, t2) ->
                                Double.compare(t2.getVoteAverage(), t1.getVoteAverage()));

                        int newSize = Math.min(results.size(), 5);
                        trendingList.clear();
                        trendingList.addAll(results.subList(0, newSize));
                        trendingAdapter.notifyItemRangeChanged(0, newSize);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<TVShowResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Failed to load trending TV shows", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onGenreClick(GenresModel genre) {
        if (genre == null) return;
        TVShowsByGenresFragment fragment = TVShowsByGenresFragment.newInstance(genre.getId(), genre.getName());
        navigateTo(fragment);
    }

    private void fetchGenresForTVShows() {
        safeEnqueue(apiService.getGenresTVShows(TMDBpath.genresTVShows()), new Callback<GenresResponse>() {
            @Override
            public void onResponse(@NonNull Call<GenresResponse> call, @NonNull Response<GenresResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Context ctx = getContext();
                    List<GenresModel> genres = response.body().getGenres();

                    if (genres != null && !genres.isEmpty() && genresList.isEmpty()) {
                        for (GenresModel g : genres) {
                            genresList.add(new GenresModel(
                                    g.getId(),
                                    g.getName(),
                                    BadgeHelper.getGenreColor(ctx, g.getId())));
                        }
                        genresAdapter.notifyItemRangeInserted(0, genresList.size());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<GenresResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Failed to load genres", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onTVShowClick() {
        // already on TV show tab — no-op
    }

    private void onMovieClick() {
        viewModel.setIsTVShowSearch(false);
        navigateTo(new SearchMoviesFragment());
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
        safeEnqueue(apiService.searchTVShows(TMDBpath.searchTVShows(), query), new Callback<TVShowResponse>() {
            @Override
            public void onResponse(@NonNull Call<TVShowResponse> call, @NonNull Response<TVShowResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TVShowModel> tvShows = response.body().getResults();
                    List<TVShowModel> filtered = new ArrayList<>();
                    if (tvShows != null) {
                        for (TVShowModel tv : tvShows) {
                            if (tv.getPosterPath() != null) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    tv.setOriginalLanguage(LanguageMapper.getLanguageName(tv.getOriginalLanguage()));
                                }
                                filtered.add(tv);
                            }
                        }
                    }
                    viewModel.setSearchTVResults(filtered);
                }
            }

            @Override
            public void onFailure(@NonNull Call<TVShowResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Search failed. Check your connection.", Toast.LENGTH_SHORT).show();
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
        if (tv == null) return;
        navigateTo(TVShowResultDetailsFragment.newInstance(tv.getTVShowId()));
    }

    private void onTrendingClick(MediaItem item) {
        if (item == null) return;
        navigateTo(TVShowResultDetailsFragment.newInstance(item.getMediaId()));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setupObservers() {
        viewModel.getSearchTVResults().observe(getViewLifecycleOwner(), tvShows -> {
            searchTVList.clear();
            searchTVList.addAll(tvShows);
            searchAdapter.notifyDataSetChanged();
        });
    }

    private void updateToggleUI() {
        btnMovie.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.toggle_bg));
        btnMovie.setTextColor(Color.parseColor("#888888"));
        btnMovie.setIconTint(ContextCompat.getColorStateList(requireContext(), android.R.color.darker_gray));

        btnTvShow.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.red_primary));
        btnTvShow.setTextColor(Color.WHITE);
        btnTvShow.setIconTint(ContextCompat.getColorStateList(requireContext(), android.R.color.white));
    }
}