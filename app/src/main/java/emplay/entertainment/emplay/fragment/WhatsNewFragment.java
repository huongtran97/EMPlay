package emplay.entertainment.emplay.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.WhatsNewMovieAdapter;
import emplay.entertainment.emplay.adapter.WhatsNewTVAdapter;
import emplay.entertainment.emplay.api.ApiClient;
import emplay.entertainment.emplay.api.MovieApiService;
import emplay.entertainment.emplay.api.MovieResponse;
import emplay.entertainment.emplay.api.TMDBpath;
import emplay.entertainment.emplay.api.TVShowResponse;
import emplay.entertainment.emplay.models.MovieModel;
import emplay.entertainment.emplay.models.TVShowModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WhatsNewFragment extends BaseFragment {

    private static final String ARG_IS_TV = "is_tv";
    private boolean isTV;
    private RecyclerView recyclerView;
    private MovieApiService apiService;

    public static WhatsNewFragment newInstance(boolean isTV) {
        WhatsNewFragment fragment = new WhatsNewFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_TV, isTV);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isTV = getArguments().getBoolean(ARG_IS_TV);
        }
        apiService = ApiClient.getClient().create(MovieApiService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.whats_new_view, container, false);

        recyclerView = view.findViewById(R.id.rvWhatsNewFull);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(isTV ? "What's New in TV Shows" : "What's New in Movies");

        view.findViewById(R.id.btnBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        if (isTV) {
            fetchTVShows();
        } else {
            fetchMovies();
        }

        return view;
    }

    private void fetchTVShows() {
        WhatsNewTVAdapter adapter = new WhatsNewTVAdapter(getContext(), new ArrayList<>(), this::onTVShowClicked);
        recyclerView.setAdapter(adapter);

        safeEnqueue(apiService.getTrendingTVShows(TMDBpath.trendingTVShows()), new Callback<TVShowResponse>() {
            @Override
            public void onResponse(@NonNull Call<TVShowResponse> call, @NonNull Response<TVShowResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateData(response.body().getResults());
                }
            }

            @Override
            public void onFailure(@NonNull Call<TVShowResponse> call, @NonNull Throwable t) {
                Log.e("WhatsNewFragment", "Failed to fetch TV shows", t);
            }
        });
    }

    private void fetchMovies() {
        WhatsNewMovieAdapter adapter = new WhatsNewMovieAdapter(getContext(), new ArrayList<>(), this::onMovieClicked);
        recyclerView.setAdapter(adapter);

        safeEnqueue(apiService.getTrendingMovies(TMDBpath.trendingMovies()), new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateData(response.body().getResults());
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                Log.e("WhatsNewFragment", "Failed to fetch movies", t);
            }
        });
    }

    private void onTVShowClicked(TVShowModel tvShow) {
        navigateTo(ShowResultTVShowDetailsFragment.newInstance(tvShow.getTVShowId()));
    }

    private void onMovieClicked(MovieModel movie) {
        navigateTo(ShowResultDetailsFragment.newInstance(movie.getId()));
    }
}
