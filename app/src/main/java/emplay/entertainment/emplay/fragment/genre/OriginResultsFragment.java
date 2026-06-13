package emplay.entertainment.emplay.fragment.genre;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
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
    private static final int ITEMS_PER_PAGE = 18;
    private static final int ANIME_GENRE_ID = 16;

    private RecyclerView resultsRecyclerView;
    private MovieByGenreAdapter movieAdapter;
    private TVShowByGenreAdapter tvAdapter;
    private TextView pageIndicator;
    private ImageButton btnPrev, btnNext;
    private MaterialButton btnMovies, btnTV;
    private MovieApiService apiService;

    private String countryCode;
    private boolean isAnime;
    private boolean showingTV;
    private int currentPage = 1;
    private int totalCustomPages = 1;
    private boolean isLoading = false;
    private String currentSortBy = "popularity.desc";

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
        View view = inflater.inflate(R.layout.origin_results_view, container, false);

        resultsRecyclerView = view.findViewById(R.id.origin_results_recyclerview);
        pageIndicator       = view.findViewById(R.id.page_indicator);
        btnPrev             = view.findViewById(R.id.btn_prev);
        btnNext             = view.findViewById(R.id.btn_next);
        btnMovies           = view.findViewById(R.id.btnOriginMovies);
        btnTV               = view.findViewById(R.id.btnOriginTV);

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

        resultsRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        resultsRecyclerView.setHasFixedSize(true);

        apiService = ApiClient.getClient().create(MovieApiService.class);

        view.findViewById(R.id.btn_back).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        applyToggleState();
        fetchPage();

        btnMovies.setOnClickListener(v -> {
            if (showingTV) { showingTV = false; currentPage = 1; applyToggleState(); fetchPage(); }
        });
        btnTV.setOnClickListener(v -> {
            if (!showingTV) { showingTV = true; currentPage = 1; applyToggleState(); fetchPage(); }
        });

        btnPrev.setOnClickListener(v -> {
            if (currentPage > 1) { currentPage--; fetchPage(); resultsRecyclerView.scrollToPosition(0); }
        });
        btnNext.setOnClickListener(v -> {
            if (currentPage < totalCustomPages) { currentPage++; fetchPage(); resultsRecyclerView.scrollToPosition(0); }
        });

        return view;
    }

    private void applyToggleState() {
        if (btnMovies == null || btnTV == null) return;
        btnMovies.setBackgroundTintList(ColorStateList.valueOf(showingTV ? 0xFF171E31 : 0xFFE3B566));
        btnMovies.setTextColor(showingTV ? 0xFFB9C0D4 : 0xFF3A2A0C);
        btnMovies.setStrokeWidth(showingTV ? dpToPx(1) : 0);
        btnTV.setBackgroundTintList(ColorStateList.valueOf(showingTV ? 0xFFE3B566 : 0xFF171E31));
        btnTV.setTextColor(showingTV ? 0xFF3A2A0C : 0xFFB9C0D4);
        btnTV.setStrokeWidth(showingTV ? 0 : dpToPx(1));
        resultsRecyclerView.setAdapter(showingTV ? tvAdapter : movieAdapter);
    }

    private void fetchPage() {
        if (isLoading) return;
        isLoading = true;

        int startItem    = (currentPage - 1) * ITEMS_PER_PAGE;
        int firstTmdb    = (startItem / 20) + 1;
        int secondTmdb   = ((startItem + ITEMS_PER_PAGE - 1) / 20) + 1;

        if (showingTV) {
            fetchTVPages(startItem, firstTmdb, secondTmdb, new ArrayList<>());
        } else {
            fetchMoviePages(startItem, firstTmdb, secondTmdb, new ArrayList<>());
        }
    }

    private void fetchMoviePages(int startItem, int firstTmdb, int secondTmdb, List<MovieModel> acc) {
        safeEnqueue(apiService.getMoviesByOrigin(TMDBpath.discoverMovies(), countryCode, currentSortBy, firstTmdb),
                new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> resp) {
                        if (resp.isSuccessful() && resp.body() != null) {
                            if (resp.body().getResults() != null) acc.addAll(resp.body().getResults());
                            int total = resp.body().getTotal_results();
                            totalCustomPages = (int) Math.ceil((double) total / ITEMS_PER_PAGE);
                        }
                        if (firstTmdb != secondTmdb) {
                            fetchSecondMoviePage(startItem, secondTmdb, acc);
                        } else {
                            processMovieResults(acc, startItem);
                        }
                    }
                    @Override public void onFailure(@NonNull Call<MovieResponse> c, @NonNull Throwable t) {
                        isLoading = false;
                    }
                });
    }

    private void fetchSecondMoviePage(int startItem, int page, List<MovieModel> acc) {
        safeEnqueue(apiService.getMoviesByOrigin(TMDBpath.discoverMovies(), countryCode, currentSortBy, page),
                new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> resp) {
                        if (resp.isSuccessful() && resp.body() != null && resp.body().getResults() != null)
                            acc.addAll(resp.body().getResults());
                        processMovieResults(acc, startItem);
                    }
                    @Override public void onFailure(@NonNull Call<MovieResponse> c, @NonNull Throwable t) {
                        isLoading = false;
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void processMovieResults(List<MovieModel> acc, int startItem) {
        isLoading = false;
        int offset = startItem % 20;
        List<MovieModel> subset = new ArrayList<>();
        for (int i = offset; i < offset + ITEMS_PER_PAGE && i < acc.size(); i++) subset.add(acc.get(i));
        movieAdapter.updateData(subset);
        pageIndicator.setText("Page " + currentPage + " of " + totalCustomPages);
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalCustomPages);
    }

    private void fetchTVPages(int startItem, int firstTmdb, int secondTmdb, List<TVShowModel> acc) {
        Integer genreId = isAnime ? ANIME_GENRE_ID : null;
        safeEnqueue(apiService.getTVShowsByOrigin(TMDBpath.discoverTVShows(), countryCode, genreId, currentSortBy, firstTmdb),
                new Callback<TVShowResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TVShowResponse> call, @NonNull Response<TVShowResponse> resp) {
                        if (resp.isSuccessful() && resp.body() != null) {
                            if (resp.body().getResults() != null) acc.addAll(resp.body().getResults());
                            int total = resp.body().getTotal_results();
                            totalCustomPages = (int) Math.ceil((double) total / ITEMS_PER_PAGE);
                        }
                        if (firstTmdb != secondTmdb) {
                            fetchSecondTVPage(startItem, secondTmdb, acc, genreId);
                        } else {
                            processTVResults(acc, startItem);
                        }
                    }
                    @Override public void onFailure(@NonNull Call<TVShowResponse> c, @NonNull Throwable t) {
                        isLoading = false;
                    }
                });
    }

    private void fetchSecondTVPage(int startItem, int page, List<TVShowModel> acc, Integer genreId) {
        safeEnqueue(apiService.getTVShowsByOrigin(TMDBpath.discoverTVShows(), countryCode, genreId, currentSortBy, page),
                new Callback<TVShowResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TVShowResponse> call, @NonNull Response<TVShowResponse> resp) {
                        if (resp.isSuccessful() && resp.body() != null && resp.body().getResults() != null)
                            acc.addAll(resp.body().getResults());
                        processTVResults(acc, startItem);
                    }
                    @Override public void onFailure(@NonNull Call<TVShowResponse> c, @NonNull Throwable t) {
                        isLoading = false;
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void processTVResults(List<TVShowModel> acc, int startItem) {
        isLoading = false;
        int offset = startItem % 20;
        List<TVShowModel> subset = new ArrayList<>();
        for (int i = offset; i < offset + ITEMS_PER_PAGE && i < acc.size(); i++) subset.add(acc.get(i));
        tvAdapter.updateData(subset);
        pageIndicator.setText("Page " + currentPage + " of " + totalCustomPages);
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalCustomPages);
    }

    private void onMovieClick(MovieModel movie, View v) {
        navigateTo(MovieResultDetailsFragment.newInstance(movie.getMovieId()), v, "poster_transition");
    }

    private void onTVClick(TVShowModel tv, View v) {
        navigateTo(TVShowResultDetailsFragment.newInstance(tv.getTVShowId()), v, "poster_transition");
    }
}
