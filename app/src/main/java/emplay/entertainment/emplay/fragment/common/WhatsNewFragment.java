package emplay.entertainment.emplay.fragment.common;

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
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.movie.WhatsNewMovieAdapter;
import emplay.entertainment.emplay.adapter.tvshow.WhatsNewTVAdapter;
import emplay.entertainment.emplay.api.common.ApiClient;
import emplay.entertainment.emplay.api.common.MovieApiService;
import emplay.entertainment.emplay.api.movie.MovieResponse;
import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.api.tvshow.TVShowResponse;
import emplay.entertainment.emplay.fragment.details.MovieResultDetailsFragment;
import emplay.entertainment.emplay.fragment.details.TVShowResultDetailsFragment;
import emplay.entertainment.emplay.models.movie.MovieModel;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;
import emplay.entertainment.emplay.tool.BadgeHelper;
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
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        TextView tvTitle = view.findViewById(R.id.trendNow);
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
        WhatsNewTVAdapter adapter = new WhatsNewTVAdapter(requireContext(), new ArrayList<>(), apiService, this::onTVShowClicked);
        recyclerView.setAdapter(adapter);

        safeEnqueue(apiService.getTrendingTVShows(TMDBpath.trendingTVShows()), new Callback<TVShowResponse>() {
            @Override
            public void onResponse(@NonNull Call<TVShowResponse> call, @NonNull Response<TVShowResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TVShowModel> all = new ArrayList<>();
                    for (TVShowModel tv : response.body().getResults()) {
                        if (tv.getPosterPath() != null) all.add(tv);
                    }
                    adapter.updateData(all);
                }
            }

            @Override
            public void onFailure(@NonNull Call<TVShowResponse> call, @NonNull Throwable t) {
                Log.e("WhatsNewFragment", "Failed to fetch TV shows", t);
            }
        });
    }

    private void fetchMovies() {
        WhatsNewMovieAdapter adapter = new WhatsNewMovieAdapter(requireContext(), new ArrayList<>(), this::onMovieClicked);
        recyclerView.setAdapter(adapter);

        safeEnqueue(apiService.getTrendingMovies(TMDBpath.trendingMovies()), new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MovieModel> filtered = new ArrayList<>();
                    for (MovieModel m : response.body().getResults()) {
                        if (m.getPosterPath() != null && BadgeHelper.isNotOlderThan(m.getReleaseDate(), 30)) {
                            filtered.add(m);
                        }
                    }
                    adapter.updateData(filtered);
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                Log.e("WhatsNewFragment", "Failed to fetch movies", t);
            }
        });
    }

    private void onTVShowClicked(TVShowModel tvShow) {
        navigateTo(TVShowResultDetailsFragment.newInstance(tvShow.getTVShowId()));
    }

    private void onMovieClicked(MovieModel movie) {
        navigateTo(MovieResultDetailsFragment.newInstance(movie.getMovieId()));
    }
}
