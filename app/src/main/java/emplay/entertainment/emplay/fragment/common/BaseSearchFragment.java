package emplay.entertainment.emplay.fragment.common;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.GenresAdapter;
import emplay.entertainment.emplay.tool.PaginationHelper;
import emplay.entertainment.emplay.adapter.common.SearchMediaAdapter;
import emplay.entertainment.emplay.adapter.common.SearchPersonAdapter;
import emplay.entertainment.emplay.adapter.common.TrendingSearchAdapter;
import emplay.entertainment.emplay.api.common.ApiClient;
import emplay.entertainment.emplay.api.common.GenresResponse;
import emplay.entertainment.emplay.api.common.MovieApiService;
import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.fragment.details.CastDetailFragment;
import emplay.entertainment.emplay.fragment.details.MovieResultDetailsFragment;
import emplay.entertainment.emplay.fragment.details.TVShowResultDetailsFragment;
import emplay.entertainment.emplay.fragment.genre.AllGenresFragment;
import emplay.entertainment.emplay.fragment.genre.MovieByGenresFragment;
import emplay.entertainment.emplay.fragment.genre.SearchByOriginFragment;
import emplay.entertainment.emplay.fragment.genre.TVShowsByGenresFragment;
import emplay.entertainment.emplay.models.common.GenresModel;
import emplay.entertainment.emplay.models.common.MediaItem;
import emplay.entertainment.emplay.models.common.MultiSearchResult;
import emplay.entertainment.emplay.models.common.SharedViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseSearchFragment<T extends MediaItem> extends BaseFragment {

    protected GenresAdapter genresAdapter;
    protected TrendingSearchAdapter trendingAdapter;
    protected List<GenresModel> genresList = new ArrayList<>();
    private final List<GenresModel> fullGenresList = new ArrayList<>();
    protected RecyclerView rvGenres, rvPopularSearches;
    protected MovieApiService apiService;
    protected TextInputEditText etSearch;
    protected SharedViewModel viewModel;
    protected View svSearchDefault;
    protected FlexboxLayout pillsContainer;
    protected View recentSearch;
    protected DatabaseHelper dbHelper;
    protected String lastQuery = "";
    protected TextView clearAll;
    private ImageView btnClose;

    // Dropdown views
    private View cardDropdown;
    private TabLayout tabLayoutSearch;
    private RecyclerView rvDropdownResults;
    private View footerPeople;

    // Pagination
    private static final int SEARCH_PAGE_SIZE = 5;
    private View paginationBar;
    private TextView tvPageIndicator;
    private ImageButton btnPrev, btnNext;
    private PaginationHelper<MultiSearchResult> paginationHelper;

    private SearchMediaAdapter mediaAdapter;
    private SearchPersonAdapter personAdapter;
    private final List<MultiSearchResult> latestMediaList = new ArrayList<>();
    private final List<MultiSearchResult> latestPersonList = new ArrayList<>();

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
        rvPopularSearches = view.findViewById(R.id.rvPopularSearches);
        etSearch = view.findViewById(R.id.etSearch);
        svSearchDefault = view.findViewById(R.id.svSearchDefault);
        pillsContainer = view.findViewById(R.id.pillsContainer);
        recentSearch = view.findViewById(R.id.recentSearch);
        clearAll = view.findViewById(R.id.btnClearAll);
        btnClose = view.findViewById(R.id.btnClose);

        // Dropdown
        cardDropdown = view.findViewById(R.id.cardDropdown);
        tabLayoutSearch = view.findViewById(R.id.tabLayoutSearch);
        rvDropdownResults = view.findViewById(R.id.rvDropdownResults);
        footerPeople = view.findViewById(R.id.footerPeople);

        // Pagination bar
        paginationBar = view.findViewById(R.id.paginationBar);
        tvPageIndicator = view.findViewById(R.id.tvPageIndicator);
        btnPrev = view.findViewById(R.id.btnPrev);
        btnNext = view.findViewById(R.id.btnNext);

        paginationHelper = new PaginationHelper<>(SEARCH_PAGE_SIZE, new ArrayList<>(),
                new PaginationHelper.PaginationCallback<MultiSearchResult>() {
                    @Override
                    public void onPageUpdated(List<MultiSearchResult> pageItems) {
                        int tab = tabLayoutSearch.getSelectedTabPosition();
                        if (tab == 2) {
                            personAdapter.submitList(new ArrayList<>(pageItems));
                        } else {
                            mediaAdapter.submitList(new ArrayList<>(pageItems));
                        }
                    }
                    @Override
                    public void onUiUpdate(int current, int total, boolean hasPrev, boolean hasNext) {
                        PaginationHelper.updatePaginationBar(paginationBar, tvPageIndicator,
                                btnPrev, btnNext, current, total, hasPrev, hasNext);
                    }
                });

        btnPrev.setOnClickListener(v -> paginationHelper.prevPage());
        btnNext.setOnClickListener(v -> paginationHelper.nextPage());

        // Restore last query
        if (savedInstanceState != null) {
            lastQuery = savedInstanceState.getString("last_query", "");
        }

        genresAdapter = new GenresAdapter(genresList, this::onGenreSelected);
        rvGenres.setAdapter(genresAdapter);
        rvGenres.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        trendingAdapter = new TrendingSearchAdapter<>(requireContext(), new ArrayList<>(), (item, v) -> onTrendingSelected(item));
        rvPopularSearches.setAdapter(trendingAdapter);

        // Dropdown adapters
        mediaAdapter = new SearchMediaAdapter(this::onMultiItemClicked);
        personAdapter = new SearchPersonAdapter(this::onPersonClicked);
        rvDropdownResults.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Add tabs: Movies (0) · TV Shows (1) · People (2)
        tabLayoutSearch.addTab(tabLayoutSearch.newTab().setText(R.string.label_movies));
        tabLayoutSearch.addTab(tabLayoutSearch.newTab().setText(R.string.label_tv_shows));
        tabLayoutSearch.addTab(tabLayoutSearch.newTab().setText(R.string.label_people));
        if (isTVTab()) {
            tabLayoutSearch.selectTab(tabLayoutSearch.getTabAt(1));
        }

        tabLayoutSearch.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { applyDropdownState(); }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        footerPeople.setVisibility(View.GONE);

        viewModel.getMultiSearchResults().observe(getViewLifecycleOwner(), results -> {
            latestMediaList.clear();
            latestPersonList.clear();
            if (results != null) {
                for (MultiSearchResult r : results) {
                    if ("person".equals(r.getMediaType())) {
                        latestPersonList.add(r);
                    } else {
                        latestMediaList.add(r);
                    }
                }
            }
            applyDropdownState();
        });

        view.findViewById(R.id.sectionSearchByOrigin).setOnClickListener(v ->
                navigateTo(SearchByOriginFragment.newInstance(isTVTab())));

        setupSearchInput();
        loadRecentSearches();

        if (isTVTab()) {
            loadGenres(apiService.getGenresTVShows(TMDBpath.genresTVShows()));
        } else {
            fetchGenres();
        }
        fetchTrending();

        clearAll.setOnClickListener(v -> clearHistory());

        if (!lastQuery.isEmpty()) {
            etSearch.setText(lastQuery);
            viewModel.searchMulti(lastQuery);
            showDropdown();
        }

        return view;
    }

    private void applyDropdownState() {
        boolean hasAny = !latestMediaList.isEmpty() || !latestPersonList.isEmpty();
        tabLayoutSearch.setVisibility(hasAny ? View.VISIBLE : View.GONE);

        int selectedTab = tabLayoutSearch.getSelectedTabPosition();
        List<MultiSearchResult> filtered = new ArrayList<>();
        if (selectedTab == 2) {
            rvDropdownResults.setAdapter(personAdapter);
            filtered.addAll(latestPersonList);
        } else {
            rvDropdownResults.setAdapter(mediaAdapter);
            for (MultiSearchResult r : latestMediaList) {
                if (selectedTab == 0 && "movie".equals(r.getMediaType())) filtered.add(r);
                else if (selectedTab == 1 && "tv".equals(r.getMediaType())) filtered.add(r);
            }
        }
        paginationHelper.updateData(filtered);
    }

    private void showDropdown() {
        cardDropdown.setVisibility(View.VISIBLE);
        svSearchDefault.setVisibility(View.GONE);
    }

    private void hideDropdown() {
        cardDropdown.setVisibility(View.GONE);
        svSearchDefault.setVisibility(View.VISIBLE);
        paginationBar.setVisibility(View.GONE);
        latestMediaList.clear();
        latestPersonList.clear();
    }

    protected abstract void fetchGenres();
    protected abstract void fetchTrending();
    protected abstract void onTrendingSelected(MediaItem item);

    protected void onGenreSelected(GenresModel genresModel) {
        if (genresModel.getId() == -1) {
            navigateTo(AllGenresFragment.newInstance(fullGenresList, isTVTab()));
            return;
        }
        Fragment target = isTVTab()
                ? TVShowsByGenresFragment.newInstance(genresModel.getId(), genresModel.getName())
                : MovieByGenresFragment.newInstance(genresModel.getId(), genresModel.getName());
        navigateTo(target);
    }

    protected void onMultiItemClicked(MultiSearchResult item, @Nullable View sharedElement) {
        Fragment fragment;
        if ("movie".equals(item.getMediaType())) {
            fragment = MovieResultDetailsFragment.newInstance(item.getMediaId());
        } else if ("tv".equals(item.getMediaType())) {
            fragment = TVShowResultDetailsFragment.newInstance(item.getMediaId());
        } else {
            return;
        }
        String transitionName = sharedElement != null ? "poster_transition" : null;
        navigateTo(fragment, sharedElement, transitionName);
    }

    protected void onPersonClicked(MultiSearchResult item) {
        navigateTo(CastDetailFragment.newInstance(item.getMediaId()));
    }

    protected void navigateToMedia(MediaItem item) {
        Fragment fragment;
        if (item instanceof emplay.entertainment.emplay.models.movie.MovieModel) {
            fragment = MovieResultDetailsFragment.newInstance(item.getMediaId());
        } else if (item instanceof emplay.entertainment.emplay.models.tvshow.TVShowModel) {
            fragment = TVShowResultDetailsFragment.newInstance(item.getMediaId());
        } else if (item instanceof MultiSearchResult) {
            onMultiItemClicked((MultiSearchResult) item, null);
            return;
        } else {
            return;
        }
        navigateTo(fragment);
    }

    protected boolean isTVTab() { return false; }

    protected void setupSearchInput() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                lastQuery = s.toString().trim();
                btnClose.setVisibility(lastQuery.isEmpty() ? View.GONE : View.VISIBLE);
                if (pendingSearch != null) searchHandler.removeCallbacks(pendingSearch);
                if (lastQuery.isEmpty()) {
                    hideDropdown();
                } else {
                    showDropdown();
                    pendingSearch = () -> viewModel.searchMulti(lastQuery);
                    searchHandler.postDelayed(pendingSearch, 400);
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

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                etSearch.setText("");
                etSearch.requestFocus();
            });
        }
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
            viewModel.searchMulti(q);
            showDropdown();
        }
    }

    protected void loadGenres(Call<GenresResponse> call) {
        safeEnqueue(call, new Callback<GenresResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<GenresResponse> call, @NonNull Response<GenresResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    genresList.clear();
                    List<GenresModel> full = response.body().getGenres();
                    if (full != null) {
                        fullGenresList.clear();
                        fullGenresList.addAll(full);
                        genresList.addAll(full.subList(0, Math.min(5, full.size())));
                        genresList.add(new GenresModel(-1, "All " + full.size() + " genres ›", 0));
                    }
                    genresAdapter.notifyDataSetChanged();
                }
            }
            @Override public void onFailure(@NonNull Call<GenresResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Failed to load genres", Toast.LENGTH_SHORT).show();
            }
        });
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