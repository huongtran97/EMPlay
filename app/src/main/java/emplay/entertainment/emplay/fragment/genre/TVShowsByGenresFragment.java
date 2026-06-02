package emplay.entertainment.emplay.fragment.genre;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.tvshow.TVShowByGenreAdapter;
import emplay.entertainment.emplay.api.common.ApiClient;
import emplay.entertainment.emplay.api.common.MovieApiService;
import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.api.tvshow.TVShowResponse;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;
import emplay.entertainment.emplay.fragment.details.TVShowResultDetailsFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *  TV show genre browser — same 18-item pagination trick as MovieByGenresFragment.
 */
public class TVShowsByGenresFragment extends Fragment {
    private static final String ARG_GENRE_ID = "GENRE_ID";
    private static final String ARG_GENRE_NAME = "GENRE_NAME";
    private static final int ITEMS_PER_PAGE = 18; // 6 rows of 3 items
    private RecyclerView tvByGenreRecyclerview;
    private TVShowByGenreAdapter tvByGenreAdapter;
    private TextView pageIndicator;
    private ImageButton btnPrev;
    private ImageButton btnNext;
    private MovieApiService apiService;
    private int genreId;
    private int currentPage = 1;
    private int totalCustomPages = 1;
    private boolean isLoading = false;

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tv_by_genre_view, container, false);

        tvByGenreRecyclerview = view.findViewById(R.id.tv_by_genre_recyclerview);
        TextView genreNameHeader = view.findViewById(R.id.tv_genres);
        pageIndicator = view.findViewById(R.id.page_indicator);
        btnPrev = view.findViewById(R.id.btn_prev);
        btnNext = view.findViewById(R.id.btn_next);

        tvByGenreAdapter = new TVShowByGenreAdapter(new ArrayList<>(), getContext(), this::onItemClick);

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        tvByGenreRecyclerview.setLayoutManager(layoutManager);
        tvByGenreRecyclerview.setAdapter(tvByGenreAdapter);

        btnPrev.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                fetchTVShowsByGenre(genreId);
                tvByGenreRecyclerview.scrollToPosition(0);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentPage < totalCustomPages) {
                currentPage++;
                fetchTVShowsByGenre(genreId);
                tvByGenreRecyclerview.scrollToPosition(0);
            }
        });

        apiService = ApiClient.getClient().create(MovieApiService.class);

        if (getArguments() != null) {
            genreId = getArguments().getInt(ARG_GENRE_ID, -1);
            String genreNameString = getArguments().getString(ARG_GENRE_NAME, "Unknown Genre");
            genreNameHeader.setText(genreNameString);

            if (genreId != -1) {
                fetchTVShowsByGenre(genreId);
            } else {
                Toast.makeText(getContext(), "Invalid genre ID", Toast.LENGTH_SHORT).show();
            }
        }

        return view;
    }

    private void onItemClick(TVShowModel tvShowModel) {
        TVShowResultDetailsFragment fragment = TVShowResultDetailsFragment.newInstance(tvShowModel.getTVShowId());
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void fetchTVShowsByGenre(int genreId) {
        if (isLoading) return;
        isLoading = true;

        // Work out which TMDB pages overlap with the 18 items.
        // If the slice spans two TMDB pages, fetch both and trim to our 18 items window.
        int startItemIndex = (currentPage - 1) * ITEMS_PER_PAGE;
        int firstTmdbPage = (startItemIndex / 20) + 1;
        int secondTmdbPage = ((startItemIndex + ITEMS_PER_PAGE - 1) / 20) + 1;

        List<TVShowModel> combinedResults = new ArrayList<>();

        fetchTmdbPage(genreId, firstTmdbPage, combinedResults, () -> {
            if (firstTmdbPage != secondTmdbPage) {
                fetchTmdbPage(genreId, secondTmdbPage, combinedResults, () -> {
                    processResults(combinedResults, startItemIndex);
                });
            } else {
                processResults(combinedResults, startItemIndex);
            }
        });
    }

    private void fetchTmdbPage(int genreId, int page, List<TVShowModel> accumulator, Runnable onDone) {
        Call<TVShowResponse> call = apiService.getTVShowsByGenre(TMDBpath.discoverTVShows(), genreId, page);
        call.enqueue(new Callback<TVShowResponse>() {
            @Override
            public void onResponse(@NonNull Call<TVShowResponse> call, @NonNull Response<TVShowResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TVShowResponse body = response.body();
                    // Total results helps us calculate total custom pages
                    int totalResults = body.getTotal_results();
                    totalCustomPages = (int) Math.ceil((double) totalResults / ITEMS_PER_PAGE);

                    if (body.getResults() != null) {
                        accumulator.addAll(body.getResults());
                    }
                }
                onDone.run();
            }

            @Override
            public void onFailure(@NonNull Call<TVShowResponse> call, @NonNull Throwable t) {
                onDone.run();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void processResults(List<TVShowModel> combinedResults, int startItemIndex) {
        isLoading = false;
        // combinedResults starts from the beginning of firstTmdbPage, so we offset into it
        // to find where our 18-item window actually starts.
        int offsetInFirstPage = startItemIndex % 20;
        
        List<TVShowModel> subset = new ArrayList<>();
        for (int i = offsetInFirstPage; i < offsetInFirstPage + ITEMS_PER_PAGE && i < combinedResults.size(); i++) {
            subset.add(combinedResults.get(i));
        }

        tvByGenreAdapter.updateData(subset);
        pageIndicator.setText("Page " + currentPage + " of " + totalCustomPages);
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalCustomPages);
    }
}
