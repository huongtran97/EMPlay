package emplay.entertainment.emplay.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.MovieByGenreAdapter;
import emplay.entertainment.emplay.api.ApiClient;
import emplay.entertainment.emplay.api.MovieApiService;
import emplay.entertainment.emplay.api.TMDBpath;
import emplay.entertainment.emplay.api.MovieResponse;
import emplay.entertainment.emplay.models.MovieModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Shows movies filtered by a single genre in a paginated 3-column grid.
 * Display 18 per page so items land on clean 3-column boundaries.
 */
public class MovieByGenresFragment extends Fragment {

    private static final String ARG_GENRE_ID = "GENRE_ID";
    private static final String ARG_GENRE_NAME = "GENRE_NAME";
    private static final int ITEMS_PER_PAGE = 18; // 6 rows of 3 items
    private RecyclerView movieByGenreRecyclerview;
    private MovieByGenreAdapter movieByGenreAdapter;
    private TextView pageIndicator;
    private ImageButton btnPrev;
    private ImageButton btnNext;
    private MovieApiService apiService;
    private int genreId;
    private int currentPage = 1;
    private int totalCustomPages = 1;
    private boolean isLoading = false;

    public static MovieByGenresFragment newInstance(int genreId, String genreName) {
        MovieByGenresFragment fragment = new MovieByGenresFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_GENRE_ID, genreId);
        args.putString(ARG_GENRE_NAME, genreName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.movie_by_genres_view, container, false);

        movieByGenreRecyclerview = view.findViewById(R.id.movie_by_genre_recyclerview);
        TextView genreNameHeader = view.findViewById(R.id.movie_genres);
        pageIndicator = view.findViewById(R.id.page_indicator);
        btnPrev = view.findViewById(R.id.btn_prev);
        btnNext = view.findViewById(R.id.btn_next);

        movieByGenreAdapter = new MovieByGenreAdapter(new ArrayList<>(), getContext(), this::onItemClick);

        movieByGenreRecyclerview.setLayoutManager(new GridLayoutManager(getContext(), 3));
        movieByGenreRecyclerview.setAdapter(movieByGenreAdapter);

        btnPrev.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                fetchMoviesByGenre(genreId);
                movieByGenreRecyclerview.scrollToPosition(0);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentPage < totalCustomPages) {
                currentPage++;
                fetchMoviesByGenre(genreId);
                movieByGenreRecyclerview.scrollToPosition(0);
            }
        });

        apiService = ApiClient.getClient().create(MovieApiService.class);

        if (getArguments() != null) {
            genreId = getArguments().getInt(ARG_GENRE_ID, -1);
            genreNameHeader.setText(getArguments().getString(ARG_GENRE_NAME, "Unknown Genre"));
            if (genreId != -1) fetchMoviesByGenre(genreId);
        }

        return view;
    }

    private void onItemClick(MovieModel movieModel) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, ShowResultDetailsFragment.newInstance(movieModel.getId()));
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void fetchMoviesByGenre(int genreId) {
        if (isLoading) return;
        isLoading = true;

        // Work out which TMDB pages overlap with the 18 items.
        // If the slice spans two TMDB pages, fetch both and trim to our 18 items window.
        int startItemIndex = (currentPage - 1) * ITEMS_PER_PAGE;
        int firstTmdbPage = (startItemIndex / 20) + 1;
        int secondTmdbPage = ((startItemIndex + ITEMS_PER_PAGE - 1) / 20) + 1;

        List<MovieModel> combinedResults = new ArrayList<>();

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

    private void fetchTmdbPage(int genreId, int page, List<MovieModel> accumulator, Runnable onDone) {
        Call<MovieResponse> call = apiService.getMoviesByGenre(TMDBpath.discoverMovies(), genreId, page);
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieResponse body = response.body();
                    int totalResults = body.getTotal_results();
                    totalCustomPages = (int) Math.ceil((double) totalResults / ITEMS_PER_PAGE);

                    if (body.getResults() != null) {
                        accumulator.addAll(body.getResults());
                    }
                }
                onDone.run();
            }

            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                onDone.run();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void processResults(List<MovieModel> combinedResults, int startItemIndex) {
        isLoading = false;
        int offsetInFirstPage = startItemIndex % 20;
        
        List<MovieModel> subset = new ArrayList<>();
        for (int i = offsetInFirstPage; i < offsetInFirstPage + ITEMS_PER_PAGE && i < combinedResults.size(); i++) {
            subset.add(combinedResults.get(i));
        }

        movieByGenreAdapter.updateData(subset);
        pageIndicator.setText("Page " + currentPage + " of " + totalCustomPages);
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalCustomPages);
    }
}
