package emplay.entertainment.emplay.fragment.common;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.adapter.common.GenresAdapter;
import emplay.entertainment.emplay.adapter.common.OriginAdapter;
import emplay.entertainment.emplay.adapter.common.TrendingSearchAdapter;
import emplay.entertainment.emplay.api.common.ApiClient;
import emplay.entertainment.emplay.api.common.GenresResponse;
import emplay.entertainment.emplay.api.common.MovieApiService;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.fragment.details.MovieResultDetailsFragment;
import emplay.entertainment.emplay.fragment.details.TVShowResultDetailsFragment;
import emplay.entertainment.emplay.fragment.genre.OriginResultsFragment;
import emplay.entertainment.emplay.models.common.GenresModel;
import emplay.entertainment.emplay.models.common.MediaItem;
import emplay.entertainment.emplay.models.common.OriginModel;
import emplay.entertainment.emplay.models.common.SharedViewModel;
import emplay.entertainment.emplay.models.movie.MovieModel;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseSearchFragment<T extends MediaItem> extends BaseFragment {

    protected GenresAdapter genresAdapter;
    protected TrendingSearchAdapter trendingAdapter;
    protected List<GenresModel> genresList = new ArrayList<>();
    protected RecyclerView rvGenres, rvSearchResults, rvTrendingSearches, rvOrigin;
    protected MovieApiService apiService;
    protected TextInputEditText etSearch;
    protected SharedViewModel viewModel;
    protected MaterialButton btnMovie, btnTvShow;
    private MaterialButton btnGenreMovies, btnGenreTV;
    private boolean genreIsTV = false;
    protected View svSearchDefault;
    protected FlexboxLayout pillsContainer;
    protected View recentSearch;
    protected DatabaseHelper dbHelper;
    protected String lastQuery = "";
    protected TextView clearAll, btnSeeAll;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_view, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        apiService = ApiClient.getClient().create(MovieApiService.class);
        dbHelper = DatabaseHelper.getInstance(requireContext());

        rvGenres = view.findViewById(R.id.rvGenres);
        rvSearchResults = view.findViewById(R.id.rvSearchResults);
        rvTrendingSearches = view.findViewById(R.id.rvTrendingSearches);
        rvOrigin = view.findViewById(R.id.rvOrigin);
        etSearch = view.findViewById(R.id.etSearch);
        btnMovie = view.findViewById(R.id.btnMovie);
        btnTvShow = view.findViewById(R.id.btnTvShow);
        svSearchDefault = view.findViewById(R.id.svSearchDefault);
        pillsContainer = view.findViewById(R.id.pillsContainer);
        recentSearch = view.findViewById(R.id.recentSearch);
        clearAll = view.findViewById(R.id.btnClearAll);
        btnSeeAll = view.findViewById(R.id.btnSeeAll);

        genresAdapter = new GenresAdapter(genresList, this::onGenreSelected);
        rvGenres.setAdapter(genresAdapter);
        rvGenres.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        trendingAdapter = new TrendingSearchAdapter<>(requireContext(), new ArrayList<>(), (item, v) -> onTrendingSelected(item));
        rvTrendingSearches.setAdapter(trendingAdapter);

        setupOriginSection();
        setupSearchResultAdapter();
        setupResultObserver();
        setupSearchInput();
        loadRecentSearches();
        updateToggleUI();

        fetchGenres();
        fetchTrending();

        btnGenreMovies = view.findViewById(R.id.btnGenreMovies);
        btnGenreTV = view.findViewById(R.id.btnGenreTV);

        btnMovie.setOnClickListener(v -> onMovieTabClick());
        btnTvShow.setOnClickListener(v -> onTVTabClick());
        clearAll.setOnClickListener(v -> clearHistory());

        if (btnGenreMovies != null) {
            btnGenreMovies.setOnClickListener(v -> {
                genreIsTV = false;
                updateGenreToggleUI();
                loadGenres(apiService.getGenresMovie(TMDBpath.genresMovie()));
            });
        }
        if (btnGenreTV != null) {
            btnGenreTV.setOnClickListener(v -> {
                genreIsTV = true;
                updateGenreToggleUI();
                loadGenres(apiService.getGenresTVShows(TMDBpath.genresTVShows()));
            });
        }

        if (savedInstanceState != null) {
            lastQuery = savedInstanceState.getString("last_query", "");
            if (!lastQuery.isEmpty()) {
                etSearch.setText(lastQuery);
                performSearchQuery(lastQuery);
            }
        }

        return view;
    }

    private void setupOriginSection() {
        List<OriginModel> origins = new ArrayList<>();
        origins.add(new OriginModel("KR", "Korean", "한"));
        origins.add(new OriginModel("JP", "Japanese", "日"));
        origins.add(new OriginModel("JP", "Anime", "ア"));
        origins.add(new OriginModel("CN", "Chinese", "中"));
        origins.add(new OriginModel("FR", "French", "É"));
        origins.add(new OriginModel("GB", "British", "B"));

        OriginAdapter originAdapter = new OriginAdapter(origins, origin -> {
            boolean defaultTV = "KR".equals(origin.getCountryCode())
                    || "CN".equals(origin.getCountryCode())
                    || "Anime".equals(origin.getName());
            boolean isAnime = "Anime".equals(origin.getName());
            String code = isAnime ? "JP" : origin.getCountryCode();
            navigateTo(OriginResultsFragment.newInstance(
                    code, origin.getName(), origin.getGlyph(), isAnime, defaultTV));
        });
        rvOrigin.setAdapter(originAdapter);
        rvOrigin.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    protected abstract void setupSearchResultAdapter();
    protected abstract void setupResultObserver();
    protected abstract void fetchGenres();
    protected abstract void fetchTrending();
    protected abstract void performSearchQuery(String query);
    protected abstract void clearSearchResults();
    protected abstract void onGenreSelected(GenresModel genresModel);
    protected abstract void onTrendingSelected(MediaItem item);

    protected void onItemClicked(MediaItem item, @Nullable View sharedElement) {
        Fragment fragment;
        String transitionName = null;
        if (item instanceof MovieModel) {
            fragment = MovieResultDetailsFragment.newInstance(item.getMediaId());
            if (sharedElement != null) transitionName = "poster_transition";
        } else if (item instanceof TVShowModel) {
            fragment = TVShowResultDetailsFragment.newInstance(item.getMediaId());
            if (sharedElement != null) transitionName = "poster_transition";
        } else {
            return;
        }
        navigateTo(fragment, sharedElement, transitionName);
    }

    protected boolean isTVTab() { return false; }
    protected abstract void onMovieTabClick();
    protected abstract void onTVTabClick();

    protected void setupSearchInput() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                lastQuery = s.toString().trim();
                if (pendingSearch != null) searchHandler.removeCallbacks(pendingSearch);
                if (lastQuery.isEmpty()) {
                    clearSearchResults();
                    rvSearchResults.setVisibility(View.GONE);
                    svSearchDefault.setVisibility(View.VISIBLE);
                } else {
                    pendingSearch = () -> performSearchQuery(lastQuery);
                    searchHandler.postDelayed(pendingSearch, 600);
                }
            }
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });
    }

    protected void loadRecentSearches() {
        List<String> history = dbHelper.getRecentSearches();
        recentSearch.setVisibility(history.isEmpty() ? View.GONE : View.VISIBLE);
        pillsContainer.removeAllViews();
        for (String q : history) {
            View pill = LayoutInflater.from(requireContext()).inflate(R.layout.search_pill_item, pillsContainer, false);
            TextView tv = pill.findViewById(R.id.tvPillText);
            tv.setText(q);
            pill.setOnClickListener(v -> {
                etSearch.setText(q);
                performSearch();
            });
            pillsContainer.addView(pill);
        }
    }

    protected void clearHistory() {
        dbHelper.clearRecentSearches();
        loadRecentSearches();
    }

    protected void performSearch() {
        if (etSearch.getText() == null) return;
        String q = etSearch.getText().toString().trim();
        if (!q.isEmpty()) {
            dbHelper.addRecentSearch(q);
            hideKeyboard();
            performSearchQuery(q);
        }
    }

    protected void loadGenres(Call<GenresResponse> call) {
        safeEnqueue(call, new Callback<GenresResponse>() {
            @Override
            public void onResponse(@NonNull Call<GenresResponse> call, @NonNull Response<GenresResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    genresList.clear();
                    List<GenresModel> full = response.body().getGenres();
                    if (full != null) {
                        genresList.addAll(full.subList(0, Math.min(5, full.size())));
                        genresList.add(new GenresModel(-1, "All " + full.size() + " genres ›", 0));
                    }
                    genresAdapter.notifyDataSetChanged();
                }
            }
            @Override public void onFailure(@NonNull Call<GenresResponse> call, @NonNull Throwable t) {}
        });
    }

    private void updateGenreToggleUI() {
        if (btnGenreMovies == null || btnGenreTV == null) return;
        int accent = ContextCompat.getColor(requireContext(), R.color.accent);
        int onAccent = ContextCompat.getColor(requireContext(), R.color.on_accent);
        int text2 = ContextCompat.getColor(requireContext(), R.color.text_2);
        btnGenreMovies.setBackgroundTintList(ColorStateList.valueOf(genreIsTV ? Color.TRANSPARENT : accent));
        btnGenreMovies.setTextColor(genreIsTV ? text2 : onAccent);
        btnGenreMovies.setStrokeWidth(genreIsTV ? dpToPx(1) : 0);
        btnGenreTV.setBackgroundTintList(ColorStateList.valueOf(genreIsTV ? accent : Color.TRANSPARENT));
        btnGenreTV.setTextColor(genreIsTV ? onAccent : text2);
        btnGenreTV.setStrokeWidth(genreIsTV ? 0 : dpToPx(1));
    }

    protected void updateToggleUI() {
        boolean tv = isTVTab();
        btnMovie.setBackgroundTintList(ColorStateList.valueOf(tv ? Color.TRANSPARENT : ContextCompat.getColor(requireContext(), R.color.accent)));
        btnMovie.setTextColor(tv ? ContextCompat.getColor(requireContext(), R.color.text_2) : ContextCompat.getColor(requireContext(), R.color.on_accent));
        btnMovie.setStrokeWidth(tv ? dpToPx(1) : 0);
        
        btnTvShow.setBackgroundTintList(ColorStateList.valueOf(tv ? ContextCompat.getColor(requireContext(), R.color.accent) : Color.TRANSPARENT));
        btnTvShow.setTextColor(tv ? ContextCompat.getColor(requireContext(), R.color.on_accent) : ContextCompat.getColor(requireContext(), R.color.text_2));
        btnTvShow.setStrokeWidth(tv ? 0 : dpToPx(1));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (pendingSearch != null) searchHandler.removeCallbacks(pendingSearch);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("last_query", lastQuery);
    }
}
