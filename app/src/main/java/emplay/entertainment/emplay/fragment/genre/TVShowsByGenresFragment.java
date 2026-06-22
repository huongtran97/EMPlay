package emplay.entertainment.emplay.fragment.genre;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.tvshow.TVShowByGenreAdapter;
import emplay.entertainment.emplay.api.common.ApiClient;
import emplay.entertainment.emplay.api.common.MovieApiService;
import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.api.tvshow.TVShowResponse;
import emplay.entertainment.emplay.fragment.common.BaseFragment;
import emplay.entertainment.emplay.fragment.details.TVShowResultDetailsFragment;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TVShowsByGenresFragment extends BaseFragment {

    private static final String ARG_GENRE_ID   = "GENRE_ID";
    private static final String ARG_GENRE_NAME = "GENRE_NAME";

    private RecyclerView recyclerView;
    private TVShowByGenreAdapter adapter;
    private ProgressBar loadingIndicator;
    private TextView chipPopularity, chipRating, chipNewest;
    private MovieApiService apiService;

    private int genreId;
    private String sortToken = FilterBottomSheetFragment.SORT_POPULAR;
    private int filterYearFrom = 1940;
    private int filterYearTo   = Calendar.getInstance().get(Calendar.YEAR);
    private int nextTmdbPage   = 1;
    private int totalTmdbPages = 1;
    private boolean isLoading  = false;

    public static TVShowsByGenresFragment newInstance(int genreId, String genreName) {
        TVShowsByGenresFragment fragment = new TVShowsByGenresFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_GENRE_ID, genreId);
        args.putString(ARG_GENRE_NAME, genreName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tv_by_genre_view, container, false);

        recyclerView     = view.findViewById(R.id.tv_by_genre_recyclerview);
        loadingIndicator = view.findViewById(R.id.loadingIndicator);
        chipPopularity   = view.findViewById(R.id.chipPopularity);
        chipRating       = view.findViewById(R.id.chipRating);
        chipNewest       = view.findViewById(R.id.chipNewest);

        TextView genreNameHeader = view.findViewById(R.id.tv_genres);
        TextView tvGhostGlyph   = view.findViewById(R.id.tvGhostGlyph);

        adapter = new TVShowByGenreAdapter(requireContext(), new ArrayList<>(), this::onItemClick);
        GridLayoutManager lm = new GridLayoutManager(requireContext(), 3);
        recyclerView.setLayoutManager(lm);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(infiniteScrollListener(lm));

        apiService = ApiClient.getClient().create(MovieApiService.class);

        if (getArguments() != null) {
            genreId = getArguments().getInt(ARG_GENRE_ID, -1);
            String genreName = getArguments().getString(ARG_GENRE_NAME, "");
            genreNameHeader.setText(genreName);
            if (tvGhostGlyph != null && !genreName.isEmpty()) {
                tvGhostGlyph.setText(String.valueOf(genreName.charAt(0)).toUpperCase());
            }
        }

        updateChipSelectionUi();
        chipPopularity.setOnClickListener(v -> applySortChip(FilterBottomSheetFragment.SORT_POPULAR));
        chipRating.setOnClickListener(v -> applySortChip(FilterBottomSheetFragment.SORT_RATING));
        chipNewest.setOnClickListener(v -> applySortChip(FilterBottomSheetFragment.SORT_NEWEST));

        view.findViewById(R.id.btnFilter).setOnClickListener(v -> openFilterSheet());

        if (genreId != -1) fetchPage(true);
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

    private void fetchPage(boolean reset) {
        if (isLoading) return;
        if (!reset && nextTmdbPage > totalTmdbPages) return;
        isLoading = true;
        if (reset) {
            nextTmdbPage = 1;
            totalTmdbPages = 1;
        }
        loadingIndicator.setVisibility(View.VISIBLE);

        String sortBy = MovieByGenresFragment.resolveApiSort(sortToken, true);
        String gteYear = filterYearFrom > 1940 ? filterYearFrom + "-01-01" : null;
        String lteYear = filterYearTo < Calendar.getInstance().get(Calendar.YEAR)
                ? filterYearTo + "-12-31" : null;

        int pageToFetch = nextTmdbPage;
        Call<TVShowResponse> call = apiService.getTVShowsByGenre(
                TMDBpath.discoverTVShows(), genreId, sortBy, gteYear, lteYear, pageToFetch);
        safeEnqueue(call, new Callback<TVShowResponse>() {
            @Override
            public void onResponse(@NonNull Call<TVShowResponse> call,
                                   @NonNull Response<TVShowResponse> response) {
                isLoading = false;
                loadingIndicator.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    totalTmdbPages = response.body().getTotal_pages();
                    List<TVShowModel> results = response.body().getResults();
                    if (reset) {
                        adapter.updateData(results != null ? results : new ArrayList<>());
                        recyclerView.scrollToPosition(0);
                    } else {
                        if (results != null) adapter.appendData(results);
                    }
                    nextTmdbPage = pageToFetch + 1;
                }
            }

            @Override
            public void onFailure(@NonNull Call<TVShowResponse> call, @NonNull Throwable t) {
                isLoading = false;
                loadingIndicator.setVisibility(View.GONE);
            }
        });
    }

    private void onItemClick(TVShowModel tv, View sharedElement) {
        navigateTo(TVShowResultDetailsFragment.newInstance(tv.getTVShowId()), sharedElement, "poster_transition");
    }
}