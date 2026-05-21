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
import emplay.entertainment.emplay.adapter.TVShowByGenreAdapter;
import emplay.entertainment.emplay.api.ApiClient;
import emplay.entertainment.emplay.api.MovieApiService;
import emplay.entertainment.emplay.api.TMDBpath;
import emplay.entertainment.emplay.api.TVShowResponse;
import emplay.entertainment.emplay.models.TVShowModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TVShowsByGenresFragment extends Fragment {

    private static final String ARG_GENRE_ID = "GENRE_ID";
    private static final String ARG_GENRE_NAME = "GENRE_NAME";
    private RecyclerView tvByGenreRecyclerview;
    private TVShowByGenreAdapter tvByGenreAdapter;
    private TextView genreName;
    private TextView pageIndicator;
    private ImageButton btnPrev;
    private ImageButton btnNext;
    private List<TVShowModel> tvByGenreList;
    private MovieApiService apiService;
    private int genreId;
    private int currentPage = 1;
    private int totalPages = 1;
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
        genreName = view.findViewById(R.id.tv_genres);
        pageIndicator = view.findViewById(R.id.page_indicator);
        btnPrev = view.findViewById(R.id.btn_prev);
        btnNext = view.findViewById(R.id.btn_next);

        tvByGenreList = new ArrayList<>();

        tvByGenreAdapter = new TVShowByGenreAdapter(tvByGenreList, getContext(), this::onItemClick);

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
            if (currentPage < totalPages) {
                currentPage++;
                fetchTVShowsByGenre(genreId);
                tvByGenreRecyclerview.scrollToPosition(0);
            }
        });

        apiService = ApiClient.getClient().create(MovieApiService.class);

        if (getArguments() != null) {
            genreId = getArguments().getInt(ARG_GENRE_ID, -1);
            String genreNameString = getArguments().getString(ARG_GENRE_NAME, "Unknown Genre");
            genreName.setText(genreNameString);

            if (genreId != -1) {
                fetchTVShowsByGenre(genreId);
            } else {
                Toast.makeText(getContext(), "Invalid genre ID", Toast.LENGTH_SHORT).show();
            }
        }

        return view;
    }

    private void onItemClick(TVShowModel tvShowModel) {
        ShowResultTVShowDetailsFragment fragment = ShowResultTVShowDetailsFragment.newInstance(tvShowModel.getTVShowId());
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void fetchTVShowsByGenre(int genreId) {
        if (isLoading) return;
        isLoading = true;

        Call<TVShowResponse> call = apiService.getTVShowsByGenre(TMDBpath.discoverTVShows(), genreId, currentPage);
        call.enqueue(new Callback<TVShowResponse>() {
            @Override
            public void onResponse(@NonNull Call<TVShowResponse> call, @NonNull Response<TVShowResponse> response) {
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    TVShowResponse body = response.body();
                    totalPages = body.getTotal_pages();

                    List<TVShowModel> tv = body.getResults();
                    if (tv != null) {
                        tvByGenreAdapter.updateData(tv);
                    }

                    pageIndicator.setText("Page " + currentPage + " of " + totalPages);
                    btnPrev.setEnabled(currentPage > 1);
                    btnNext.setEnabled(currentPage < totalPages);
                }
            }

            @Override
            public void onFailure(@NonNull Call<TVShowResponse> call, @NonNull Throwable t) {
                isLoading = false;
                Toast.makeText(getContext(), "Failed to load TV shows.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
