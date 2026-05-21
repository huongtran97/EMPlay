package emplay.entertainment.emplay.fragment;

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
import emplay.entertainment.emplay.adapter.MovieByGenreAdapter;
import emplay.entertainment.emplay.api.ApiClient;
import emplay.entertainment.emplay.api.MovieApiService;
import emplay.entertainment.emplay.api.TMDBpath;
import emplay.entertainment.emplay.api.MovieResponse;
import emplay.entertainment.emplay.models.MovieModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieByGenresFragment extends Fragment {

    private static final String ARG_GENRE_ID = "GENRE_ID";
    private static final String ARG_GENRE_NAME = "GENRE_NAME";

    private RecyclerView movieByGenreRecyclerview;
    private MovieByGenreAdapter movieByGenreAdapter;
    private TextView genreName;
    private TextView pageIndicator;
    private ImageButton btnPrev;
    private ImageButton btnNext;
    private List<MovieModel> movieByGenreList;
    private MovieApiService apiService;
    private int genreId;
    private int currentPage = 1;
    private int totalPages = 1;
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
        genreName = view.findViewById(R.id.movie_genres);
        pageIndicator = view.findViewById(R.id.page_indicator);
        btnPrev = view.findViewById(R.id.btn_prev);
        btnNext = view.findViewById(R.id.btn_next);

        movieByGenreList = new ArrayList<>();
        movieByGenreAdapter = new MovieByGenreAdapter(movieByGenreList, getContext(), this::onItemClick);

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
            if (currentPage < totalPages) {
                currentPage++;
                fetchMoviesByGenre(genreId);
                movieByGenreRecyclerview.scrollToPosition(0);
            }
        });

        apiService = ApiClient.getClient().create(MovieApiService.class);

        if (getArguments() != null) {
            genreId = getArguments().getInt(ARG_GENRE_ID, -1);
            genreName.setText(getArguments().getString(ARG_GENRE_NAME, "Unknown Genre"));
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

        Call<MovieResponse> call = apiService.getMoviesByGenre(TMDBpath.discoverMovies(), genreId, currentPage);
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    MovieResponse body = response.body();
                    totalPages = body.getTotal_pages();
                    if (body.getResults() != null) {
                        movieByGenreAdapter.updateData(body.getResults());
                    }
                    pageIndicator.setText("Page " + currentPage + " of " + totalPages);
                    btnPrev.setEnabled(currentPage > 1);
                    btnNext.setEnabled(currentPage < totalPages);
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                isLoading = false;
                Toast.makeText(getContext(), "Failed to load movies.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
