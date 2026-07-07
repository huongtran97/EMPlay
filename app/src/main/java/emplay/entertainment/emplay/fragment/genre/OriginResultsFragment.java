package emplay.entertainment.emplay.fragment.genre;

import android.content.res.ColorStateList;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.movie.MovieByGenreAdapter;
import emplay.entertainment.emplay.adapter.tvshow.TVShowByGenreAdapter;
import emplay.entertainment.emplay.api.common.ApiClient;
import emplay.entertainment.emplay.api.common.MovieApiService;
import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.api.movie.MovieResponse;
import emplay.entertainment.emplay.api.tvshow.TVShowResponse;
import emplay.entertainment.emplay.fragment.common.BaseFragment;
import emplay.entertainment.emplay.fragment.details.MovieResultDetailsFragment;
import emplay.entertainment.emplay.fragment.details.TVShowResultDetailsFragment;
import emplay.entertainment.emplay.models.movie.MovieModel;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OriginResultsFragment extends BaseFragment {
    private static final String ARG_COUNTRY_CODE = "COUNTRY_CODE";
    private static final String ARG_ORIGIN_NAME  = "ORIGIN_NAME";
    private static final String ARG_GLYPH        = "GLYPH";
    private static final String ARG_IS_ANIME     = "IS_ANIME";
    private static final String ARG_DEFAULT_TV   = "DEFAULT_TV";
    private static final int ANIME_GENRE_ID = 16;
    private RecyclerView resultsRecyclerView;
    private MovieByGenreAdapter movieAdapter;
    private TVShowByGenreAdapter tvAdapter;
    private ProgressBar loadingIndicator;
    private MaterialButton btnMovies, btnTV;
    private TextView chipPopularity, chipRating, chipNewest;
    private MovieApiService apiService;
    private String countryCode;
    private boolean isAnime;
    private boolean showingTV;
    private String sortToken    = FilterBottomSheetFragment.SORT_POPULAR;
    private int filterYearFrom  = 1940;
    private int filterYearTo    = Calendar.getInstance().get(Calendar.YEAR);
    private int nextTmdbPage    = 1;
    private int totalTmdbPages  = 1;
    private boolean isLoading   = false;

    public static OriginResultsFragment newInstance(String countryCode, String originName,
                                                     String glyph, boolean isAnime, boolean defaultTV) {
        OriginResultsFragment f = new OriginResultsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COUNTRY_CODE, countryCode);
        args.putString(ARG_ORIGIN_NAME, originName);
        args.putString(ARG_GLYPH, glyph);
        args.putBoolean(ARG_IS_ANIME, isAnime);
        args.putBoolean(ARG_DEFAULT_TV, defaultTV);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_origin_results, container, false);

        resultsRecyclerView = view.findViewById(R.id.origin_results_recyclerview);
        loadingIndicator    = view.findViewById(R.id.loadingIndicator);
        btnMovies           = view.findViewById(R.id.btnOriginMovies);
        btnTV               = view.findViewById(R.id.btnOriginTV);
        chipPopularity      = view.findViewById(R.id.chipPopularity);
        chipRating          = view.findViewById(R.id.chipRating);
        chipNewest          = view.findViewById(R.id.chipNewest);

        Bundle args = getArguments();
        countryCode = args != null ? args.getString(ARG_COUNTRY_CODE, "KR") : "KR";
        String originName = args != null ? args.getString(ARG_ORIGIN_NAME, "") : "";
        String glyph      = args != null ? args.getString(ARG_GLYPH, "") : "";
        isAnime           = args != null && args.getBoolean(ARG_IS_ANIME, false);
        showingTV         = args == null || args.getBoolean(ARG_DEFAULT_TV, true);

        TextView tvTitle = view.findViewById(R.id.tvOriginTitle);
        if (tvTitle != null) tvTitle.setText(originName);
        TextView tvGhostGlyph = view.findViewById(R.id.tvOriginGhostGlyph);
        if (tvGhostGlyph != null && !glyph.isEmpty()) tvGhostGlyph.setText(glyph);

        movieAdapter = new MovieByGenreAdapter(requireContext(), new ArrayList<>(), this::onMovieClick);
        tvAdapter    = new TVShowByGenreAdapter(requireContext(), new ArrayList<>(), this::onTVClick);

        GridLayoutManager lm = new GridLayoutManager(requireContext(), 3);
        resultsRecyclerView.setLayoutManager(lm);
        resultsRecyclerView.setHasFixedSize(true);
        resultsRecyclerView.addOnScrollListener(infiniteScrollListener(lm));

        apiService = ApiClient.getClient().create(MovieApiService.class);

        view.findViewById(R.id.btnFilter).setOnClickListener(v -> openFilterSheet());

        updateChipSelectionUi();
        chipPopularity.setOnClickListener(v -> applySortChip(FilterBottomSheetFragment.SORT_POPULAR));
        chipRating.setOnClickListener(v -> applySortChip(FilterBottomSheetFragment.SORT_RATING));
        chipNewest.setOnClickListener(v -> applySortChip(FilterBottomSheetFragment.SORT_NEWEST));

        btnMovies.setOnClickListener(v -> {
            if (showingTV) { showingTV = false; applyToggleState(); fetchPage(true); }
        });
        btnTV.setOnClickListener(v -> {
            if (!showingTV) { showingTV = true; applyToggleState(); fetchPage(true); }
        });

        applyToggleState();
        fetchPage(true);
        return view;
    }

    private void applySortChip(String token) {
        if (token.equals(sortToken)) return;
        sortToken = token;
        updateChipSelectionUi();
        fetchPage(true);
    }

    private void updateChipSelectionUi() {
        chipPopularity.setSelected(FilterBottomSheetFragment.SORT_POPULAR.equals(sortToken));
        chipRating.setSelected(FilterBottomSheetFragment.SORT_RATING.equals(sortToken));
        chipNewest.setSelected(FilterBottomSheetFragment.SORT_NEWEST.equals(sortToken));
    }

    private void openFilterSheet() {
        FilterBottomSheetFragment sheet = FilterBottomSheetFragment.newInstance(
                sortToken, filterYearFrom, filterYearTo);
        sheet.setFilterListener((token, from, to) -> {
            sortToken      = token;
            filterYearFrom = from;
            filterYearTo   = to;
            updateChipSelectionUi();
            fetchPage(true);
        });
        sheet.show(getParentFragmentManager(), "filter");
    }

    private RecyclerView.OnScrollListener infiniteScrollListener(GridLayoutManager lm) {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (dy <= 0) return;
                int totalItems  = lm.getItemCount();
                int lastVisible = lm.findLastVisibleItemPosition();
                if (!isLoading && nextTmdbPage <= totalTmdbPages && lastVisible >= totalItems - 10) {
                    fetchPage(false);
                }
            }
        };
    }

    private void applyToggleState() {
        if (btnMovies == null || btnTV == null) return;
        int accent = ContextCompat.getColor(requireContext(), R.color.accent);
        int surface = ContextCompat.getColor(requireContext(), R.color.bg_surface);
        int onAccent = ContextCompat.getColor(requireContext(), R.color.on_accent);
        int text2 = ContextCompat.getColor(requireContext(), R.color.text_2);
        btnMovies.setBackgroundTintList(ColorStateList.valueOf(showingTV ? surface : accent));
        btnMovies.setTextColor(showingTV ? text2 : onAccent);
        btnMovies.setStrokeWidth(showingTV ? dpToPx(1) : 0);
        btnTV.setBackgroundTintList(ColorStateList.valueOf(showingTV ? accent : surface));
        btnTV.setTextColor(showingTV ? onAccent : text2);
        btnTV.setStrokeWidth(showingTV ? 0 : dpToPx(1));
        resultsRecyclerView.setAdapter(showingTV ? tvAdapter : movieAdapter);
    }

    private void fetchPage(boolean reset) {
        if (isLoading) return;
        if (!reset && nextTmdbPage > totalTmdbPages) return;
        isLoading = true;
        if (reset) {
            nextTmdbPage = 1;
            totalTmdbPages = 1;
        }
        loadingIndicator.setVisibility(View.VISIBLE);

        String sortBy = MovieByGenresFragment.resolveApiSort(sortToken, showingTV);
        String gteField = filterYearFrom + "-01-01";
        String lteField = filterYearTo + "-12-31";
        String gteYear = filterYearFrom > 1940 ? gteField : null;
        String lteYear = filterYearTo < Calendar.getInstance().get(Calendar.YEAR) ? lteField : null;

        int pageToFetch = nextTmdbPage;
        if (showingTV) {
            Integer genreId = isAnime ? ANIME_GENRE_ID : null;
            safeEnqueue(apiService.getTVShowsByOrigin(
                    TMDBpath.discoverTVShows(), countryCode, genreId, sortBy, gteYear, lteYear, pageToFetch),
                    new Callback<TVShowResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<TVShowResponse> call,
                                               @NonNull Response<TVShowResponse> resp) {
                            isLoading = false;
                            loadingIndicator.setVisibility(View.GONE);
                            if (resp.isSuccessful() && resp.body() != null) {
                                totalTmdbPages = resp.body().getTotal_pages();
                                List<TVShowModel> results = resp.body().getResults();
                                if (results != null) results.removeIf(tv -> tv.getPosterPath() == null && tv.getBackdropPath() == null);
                                if (reset) {
                                    tvAdapter.updateData(results != null ? results : new ArrayList<>());
                                    resultsRecyclerView.scrollToPosition(0);
                                } else {
                                    if (results != null) tvAdapter.appendData(results);
                                }
                                nextTmdbPage = pageToFetch + 1;
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<TVShowResponse> c, @NonNull Throwable t) {
                            isLoading = false;
                            loadingIndicator.setVisibility(View.GONE);
                        }
                    });
        } else {
            safeEnqueue(apiService.getMoviesByOrigin(
                    TMDBpath.discoverMovies(), countryCode, sortBy, gteYear, lteYear, pageToFetch),
                    new Callback<MovieResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<MovieResponse> call,
                                               @NonNull Response<MovieResponse> resp) {
                            isLoading = false;
                            loadingIndicator.setVisibility(View.GONE);
                            if (resp.isSuccessful() && resp.body() != null) {
                                totalTmdbPages = resp.body().getTotal_pages();
                                List<MovieModel> results = resp.body().getResults();
                                if (results != null) results.removeIf(m -> m.getPosterPath() == null && m.getBackdropPath() == null);
                                if (reset) {
                                    movieAdapter.updateData(results != null ? results : new ArrayList<>());
                                    resultsRecyclerView.scrollToPosition(0);
                                } else {
                                    if (results != null) movieAdapter.appendData(results);
                                }
                                nextTmdbPage = pageToFetch + 1;
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<MovieResponse> c, @NonNull Throwable t) {
                            isLoading = false;
                            loadingIndicator.setVisibility(View.GONE);
                        }
                    });
        }
    }

    private void onMovieClick(MovieModel movie, View v) {
        navigateTo(MovieResultDetailsFragment.newInstance(movie.getMovieId()), v, "poster_transition");
    }

    private void onTVClick(TVShowModel tv, View v) {
        navigateTo(TVShowResultDetailsFragment.newInstance(tv.getTVShowId()), v, "poster_transition");
    }
}