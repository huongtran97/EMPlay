package emplay.entertainment.emplay.fragment.common;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.GenresAdapter;
import emplay.entertainment.emplay.adapter.common.TrendingSearchAdapter;
import emplay.entertainment.emplay.api.common.ApiClient;
import emplay.entertainment.emplay.api.common.GenresResponse;
import emplay.entertainment.emplay.api.common.MovieApiService;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.fragment.common.TrendingSeeAllFragment;
import emplay.entertainment.emplay.models.common.GenresModel;
import emplay.entertainment.emplay.models.common.MediaItem;
import emplay.entertainment.emplay.models.common.SharedViewModel;
import emplay.entertainment.emplay.tool.BadgeHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseSearchFragment<T extends MediaItem> extends BaseFragment {

    protected GenresAdapter genresAdapter;
    protected TrendingSearchAdapter trendingAdapter;
    protected final List<GenresModel> genresList = new ArrayList<>();
    protected final List<T> trendingList = new ArrayList<>();
    protected RecyclerView rvGenres, rvSearchResults, rvTrendingSearches;
    protected MovieApiService apiService;
    protected TextInputEditText etSearch;
    protected SharedViewModel viewModel;
    protected MaterialButton btnMovie, btnTvShow;
    protected NestedScrollView svSearchDefault;
    protected FlexboxLayout pillsContainer;
    protected View recentSearch;
    protected DatabaseHelper dbHelper;
    protected String lastQuery = "";
    protected TextView clearAll, btnSeeAll;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_view, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        dbHelper  = DatabaseHelper.getInstance(requireContext());
        apiService = ApiClient.getClient().create(MovieApiService.class);

        etSearch          = view.findViewById(R.id.etSearch);
        rvSearchResults   = view.findViewById(R.id.rvSearchResults);
        rvGenres          = view.findViewById(R.id.rvGenres);
        rvTrendingSearches = view.findViewById(R.id.rvTrendingSearches);
        svSearchDefault   = view.findViewById(R.id.svSearchDefault);
        pillsContainer    = view.findViewById(R.id.pillsContainer);
        recentSearch      = view.findViewById(R.id.recentSearch);
        clearAll          = view.findViewById(R.id.btnClearAll);
        btnMovie          = view.findViewById(R.id.btnMovie);
        btnTvShow         = view.findViewById(R.id.btnTvShow);
        btnSeeAll         = view.findViewById(R.id.btnSeeAll);

        if (clearAll != null) {
            clearAll.setPaintFlags(clearAll.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            clearAll.setOnClickListener(v -> clearHistory());
        }
        btnMovie.setOnClickListener(v -> onMovieTabClick());
        btnTvShow.setOnClickListener(v -> onTVTabClick());
        btnSeeAll.setOnClickListener(v -> navigateTo(TrendingSeeAllFragment.newInstance(isTVTab())));

        genresAdapter = new GenresAdapter(genresList, this::onGenreSelected);
        rvGenres.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvGenres.setNestedScrollingEnabled(false);
        rvGenres.setAdapter(genresAdapter);

        trendingAdapter = new TrendingSearchAdapter(requireContext(), trendingList, this::onTrendingSelected);
        rvTrendingSearches.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTrendingSearches.setAdapter(trendingAdapter);

        setupSearchResultAdapter();
        setupSearchInput();
        setupResultObserver();
        loadRecentSearches();
        fetchGenres();
        fetchTrending();

        if (savedInstanceState != null) {
            etSearch.setText(savedInstanceState.getString("searchQuery", ""));
        }

        updateToggleUI();
        return view;
    }

    // Abstract hooks — subclasses provide the type-specific behavior

    protected abstract void setupSearchResultAdapter();
    protected abstract void setupResultObserver();
    protected abstract void fetchGenres();
    protected abstract void fetchTrending();
    protected abstract void performSearchQuery(String query);
    protected abstract void clearSearchResults();
    protected abstract void onGenreSelected(GenresModel genre);
    protected abstract void onTrendingSelected(MediaItem item);
    /** Returns true if this is the TV tab; controls toggle highlight and "See all" destination. */
    protected abstract boolean isTVTab();
    protected abstract void onMovieTabClick();
    protected abstract void onTVTabClick();

    // Common implementations

    private void setupSearchInput() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                    hasText = !s.isEmpty();
                }
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

    protected void loadRecentSearches() {
        pillsContainer.removeAllViews();
        List<String> searches = dbHelper.getRecentSearches();
        recentSearch.setVisibility(searches.isEmpty() ? View.GONE : View.VISIBLE);
        for (String query : searches) {
            View pillView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.search_pill_item, pillsContainer, false);
            TextView tvPill = pillView.findViewById(R.id.tvPillText);
            tvPill.setText(query);
            pillView.setOnClickListener(v -> {
                etSearch.setText(query);
                etSearch.setSelection(query.length());
            });
            pillsContainer.addView(pillView);
        }
    }

    protected void clearHistory() {
        dbHelper.clearRecentSearches();
        pillsContainer.removeAllViews();
        recentSearch.setVisibility(View.GONE);
    }

    @SuppressLint("NotifyDataSetChanged")
    protected void performSearch() {
        String query = Objects.requireNonNull(etSearch.getText()).toString().trim();
        if (!query.isEmpty()) {
            if (query.equals(lastQuery)) return;
            lastQuery = query;
            performSearchQuery(query);
        } else {
            lastQuery = "";
            clearSearchResults();
        }
    }

    // Shared genre-loading helper — subclass just passes the right API call.
    protected void loadGenres(Call<GenresResponse> call) {
        safeEnqueue(call, new Callback<GenresResponse>() {
            @Override
            public void onResponse(@NonNull Call<GenresResponse> call,
                                   @NonNull Response<GenresResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Context ctx = getContext();
                    List<GenresModel> genres = response.body().getGenres();
                    if (genres != null && !genres.isEmpty() && genresList.isEmpty()) {
                        for (GenresModel g : genres) {
                            genresList.add(new GenresModel(
                                    g.getId(), g.getName(),
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

    protected void updateToggleUI() {
        boolean tv = isTVTab();
        btnMovie.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(),
                tv ? R.color.toggle_bg : R.color.red_primary));
        btnMovie.setTextColor(tv ? Color.parseColor("#888888") : Color.WHITE);
        btnMovie.setIconTint(ContextCompat.getColorStateList(requireContext(),
                tv ? android.R.color.darker_gray : android.R.color.white));

        btnTvShow.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(),
                tv ? R.color.red_primary : R.color.toggle_bg));
        btnTvShow.setTextColor(tv ? Color.WHITE : Color.parseColor("#888888"));
        btnTvShow.setIconTint(ContextCompat.getColorStateList(requireContext(),
                tv ? android.R.color.white : android.R.color.darker_gray));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (etSearch != null) {
            outState.putString("searchQuery", Objects.requireNonNull(etSearch.getText()).toString());
        }
    }
}