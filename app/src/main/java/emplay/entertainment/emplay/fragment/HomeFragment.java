package emplay.entertainment.emplay.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.api.TVShowResponse;
import emplay.entertainment.emplay.api.UpComingTVShowsResponse;
import emplay.entertainment.emplay.api.UpComingMovieResponse;
import emplay.entertainment.emplay.models.TVShowModel;
import emplay.entertainment.emplay.adapter.MovieAdapter;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.ApiClient;
import emplay.entertainment.emplay.api.MovieApiService;
import emplay.entertainment.emplay.api.TMDBpath;
import emplay.entertainment.emplay.api.MovieResponse;
import emplay.entertainment.emplay.models.MovieModel;
import emplay.entertainment.emplay.adapter.TVShowAdapter;
import emplay.entertainment.emplay.adapter.UpComingTVAdapter;
import emplay.entertainment.emplay.adapter.UpcomingMovieAdapter;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Call;

public class HomeFragment extends Fragment {

    private RecyclerView movieRecyclerView, tvRecyclerView2, upComingRecyclerView, upComingTVRecyclerview;
    private MovieAdapter movieAdapter;
    private TVShowAdapter tvShowAdapter;
    private UpcomingMovieAdapter upcomingMovieAdapter;
    private UpComingTVAdapter upComingTVAdapter;
    private MovieApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main, container, false);

        movieRecyclerView = view.findViewById(R.id.movie_popular_recyclerview);
        tvRecyclerView2 = view.findViewById(R.id.tvshow_popular_recyclerview);
        upComingRecyclerView = view.findViewById(R.id.up_coming_movie_recyclerview);
        upComingTVRecyclerview = view.findViewById(R.id.up_coming_tv_recyclerview);

        movieAdapter = new MovieAdapter(getContext(), new ArrayList<>(), this::onItemClicked);
        movieRecyclerView.setAdapter(movieAdapter);
        movieRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        tvShowAdapter = new TVShowAdapter(getContext(), new ArrayList<>(), this::onItemClicked);
        tvRecyclerView2.setAdapter(tvShowAdapter);
        tvRecyclerView2.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        upcomingMovieAdapter = new UpcomingMovieAdapter(getContext(), new ArrayList<>(), this::onItemClicked);
        upComingRecyclerView.setAdapter(upcomingMovieAdapter);
        upComingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        upComingTVAdapter = new UpComingTVAdapter(getContext(), new ArrayList<>(), this::onItemClicked);
        upComingTVRecyclerview.setAdapter(upComingTVAdapter);
        upComingTVRecyclerview.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        apiService = ApiClient.getClient().create(MovieApiService.class);

        fetchPopularMovies();
        fetchPopularTV();
        fetchUpComingMovie();
        fetchUpComingTV();

        return view;
    }

    private void fetchPopularMovies() {
        Call<MovieResponse> call = apiService.getTrendingMovies(TMDBpath.trendingMovies());
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MovieModel> filtered = new ArrayList<>();
                    for (MovieModel movie : response.body().getResults()) {
                        if (movie.getPosterPath() != null) filtered.add(movie);
                    }
                    movieAdapter.updateData(filtered);
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Log.e("HomeFragment", "Failed to fetch trending movies", t);
            }
        });
    }

    private void fetchPopularTV() {
        Call<TVShowResponse> call = apiService.getTrendingTVShows(TMDBpath.trendingTVShows());
        call.enqueue(new Callback<TVShowResponse>() {
            @Override
            public void onResponse(Call<TVShowResponse> call, Response<TVShowResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TVShowModel> filtered = new ArrayList<>();
                    for (TVShowModel tv : response.body().getResults()) {
                        if (tv.getPosterPath() != null) filtered.add(tv);
                    }
                    tvShowAdapter.updateData(filtered);
                }
            }

            @Override
            public void onFailure(Call<TVShowResponse> call, Throwable t) {
                Log.e("HomeFragment", "Failed to fetch trending TV shows", t);
            }
        });
    }

    private void fetchUpComingMovie() {
        Call<UpComingMovieResponse> call = apiService.getUpcomingMovies(
                TMDBpath.discoverMovies(), TMDBpath.todayDate(), TMDBpath.thirtyDaysFromNow(),
                "popularity.desc", false, "en-US", 1);
        call.enqueue(new Callback<UpComingMovieResponse>() {
            @Override
            public void onResponse(Call<UpComingMovieResponse> call, Response<UpComingMovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MovieModel> filtered = new ArrayList<>();
                    for (MovieModel movie : response.body().getResults()) {
                        if (movie.getPosterPath() != null) filtered.add(movie);
                    }
                    upcomingMovieAdapter.updateData(filtered);
                }
            }

            @Override
            public void onFailure(Call<UpComingMovieResponse> call, Throwable t) {
                Log.e("HomeFragment", "Failed to fetch upcoming movies", t);
            }
        });
    }

    private void fetchUpComingTV() {
        Call<UpComingTVShowsResponse> call = apiService.getUpcomingTVShows(
                TMDBpath.discoverTVShows(), TMDBpath.todayDate(), TMDBpath.thirtyDaysFromNow(),
                "popularity.desc", false, "en-US", 1);
        call.enqueue(new Callback<UpComingTVShowsResponse>() {
            @Override
            public void onResponse(Call<UpComingTVShowsResponse> call, Response<UpComingTVShowsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TVShowModel> filtered = new ArrayList<>();
                    for (TVShowModel tv : response.body().getResults()) {
                        if (tv.getPosterPath() != null) filtered.add(tv);
                    }
                    upComingTVAdapter.updateData(filtered);
                }
            }

            @Override
            public void onFailure(Call<UpComingTVShowsResponse> call, Throwable t) {
                Log.e("HomeFragment", "Failed to fetch upcoming TV shows", t);
            }
        });
    }

    public void onItemClicked(Object item) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        Fragment fragment;

        if (item instanceof MovieModel) {
            fragment = ShowResultDetailsFragment.newInstance(((MovieModel) item).getId());
        } else if (item instanceof TVShowModel) {
            fragment = ShowResultTVShowDetailsFragment.newInstance(((TVShowModel) item).getTVShowId());
        } else {
            return;
        }

        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
