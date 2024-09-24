package emplay.entertainment.emplay.fragment;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;



import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.BuildConfig;

import emplay.entertainment.emplay.api.TVShowResponse;
import emplay.entertainment.emplay.api.UpComingTVShowsResponse;
import emplay.entertainment.emplay.api.UpComingMovieResponse;
import emplay.entertainment.emplay.models.TVShowModel;
import emplay.entertainment.emplay.adapter.MovieAdapter;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.ApiClient;
import emplay.entertainment.emplay.api.MovieApiService;
import emplay.entertainment.emplay.api.MovieResponse;
import emplay.entertainment.emplay.models.MovieModel;
import emplay.entertainment.emplay.adapter.TVShowAdapter;
import emplay.entertainment.emplay.adapter.UpComingTVAdapter;
import emplay.entertainment.emplay.adapter.UpcomingMovieAdapter;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Call;

/**
 *  * @author Tran Ngoc Que Huong
 *  * @version 1.0
 *
 * Fragment that displays a list of popular movies and movie details.
 */

public class HomeFragment extends Fragment {
    private RecyclerView movieRecyclerView, tvRecyclerView2, upComingRecyclerView, upComingTVRecyclerview;
    private MovieAdapter movieAdapter;
    private TVShowAdapter tvShowAdapter;
    private UpcomingMovieAdapter upcomingMovieAdapter;
    private UpComingTVAdapter upComingTVAdapter;
    private MovieApiService apiService;


    public String useApiKey() {
        String apiKey = BuildConfig.API_KEY;

        return apiKey;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * Initializes the RecyclerView components, adapters, and API service.
     * Starts fetching initial movie data from the API.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The view for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main, container, false);


        // Initialize RecyclerViews
        movieRecyclerView = view.findViewById(R.id.movie_popular_recyclerview);
        tvRecyclerView2 = view.findViewById(R.id.tvshow_popular_recyclerview);
        upComingRecyclerView = view.findViewById(R.id.up_coming_movie_recyclerview);
        upComingTVRecyclerview = view.findViewById(R.id.up_coming_tv_recyclerview);

        // Initialize movie list and adapters with the filtered movie list
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

        // Initialize API service
        apiService = ApiClient.getClient().create(MovieApiService.class);

        // Fetch initial movie data
        fetchPopularMovies();
        fetchPopularTV();
        fetchUpComingMovie();
        fetchUpComingTV();

        return view;
    }


    private List<MovieModel> fetchUpComingMovie() {
        Call<UpComingMovieResponse> call = apiService.getUpcomingMovies(useApiKey());
        call.enqueue(new Callback<UpComingMovieResponse>() {
            @Override
            public void onResponse(Call<UpComingMovieResponse> call, Response<UpComingMovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Filter movies with non-null posterPath
                    List<MovieModel> movieList = response.body().getResults();
                    List<MovieModel> filteredMovies = new ArrayList<>();
                    for (MovieModel movie : movieList) {
                        if (movie.getPosterPath() != null) {
                            filteredMovies.add(movie);
                        }
                    }
                    upcomingMovieAdapter.updateData(filteredMovies);
                }
            }

            @Override
            public void onFailure(Call<UpComingMovieResponse> call, Throwable t) {
                Log.e("HomeFragment", "Error fetching movie data", t);
            }
        });
        return null;
    }

    private List<TVShowModel> fetchUpComingTV() {
        Call<UpComingTVShowsResponse> call = apiService.getUpcomingTVShows(useApiKey());
        call.enqueue(new Callback<UpComingTVShowsResponse>() {
            @Override
            public void onResponse(Call<UpComingTVShowsResponse> call, Response<UpComingTVShowsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Filter TV shows with non-null posterPath
                    List<TVShowModel> tvList = response.body().getResults();
                    List<TVShowModel> filteredTVShows = new ArrayList<>();
                    for (TVShowModel tv : tvList) {
                        if (tv.getPosterPath() != null) {
                            filteredTVShows.add(tv);
                        }
                    }
                    upComingTVAdapter.updateData(filteredTVShows);
                }
            }

            @Override
            public void onFailure(Call<UpComingTVShowsResponse> call, Throwable t) {
                Log.e("HomeFragment", "Error fetching TV show data", t);
            }
        });
        return null;
    }

    /**
     * Fetches the initial list of popular movies from the API and updates the movie adapter.
     * <p>
     * This method makes an API call to retrieve popular movies and updates the MovieAdapter with the fetched data.
     * Displays a Toast message if the data loading fails.
     *
     * @return
     */
    private void fetchPopularMovies() { // Change the return type to void
        Call<MovieResponse> call = apiService.getTrendingMovies(useApiKey());
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        // Filter popular movies with non-null posterPath
                        List<MovieModel> movieList = response.body().getResults();
                        List<MovieModel> filteredMovies = new ArrayList<>();
                        for (MovieModel movie : movieList) {
                            if (movie.getPosterPath() != null) {
                                filteredMovies.add(movie);
                            }
                        }
                        movieAdapter.updateData(filteredMovies);
                    } else {
                        Log.e("HomeFragment", "Response body is null");
                    }
                } else {
                    try {
                        // Log the error response if not successful
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e("HomeFragment", "Error fetching movies: " + response.code() + " - " + errorBody);
                    } catch (IOException e) {
                        Log.e("HomeFragment", "Error reading error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Log.e("HomeFragment", "Error fetching movie data", t);
            }
        });
    }


    private List<TVShowModel> fetchPopularTV() {
        Call<TVShowResponse> call = apiService.getTrendingTVShows(useApiKey());
        call.enqueue(new Callback<TVShowResponse>() {
            @Override
            public void onResponse(Call<TVShowResponse> call, Response<TVShowResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Filter popular TV shows with non-null posterPath
                    List<TVShowModel> tvList = response.body().getResults();
                    List<TVShowModel> filteredTVShows = new ArrayList<>();
                    for (TVShowModel tv : tvList) {
                        if (tv.getPosterPath() != null) {
                            filteredTVShows.add(tv);
                        }
                    }
                    tvShowAdapter.updateData(filteredTVShows);
                }
            }

            @Override
            public void onFailure(Call<TVShowResponse> call, Throwable t) {
                Log.e("HomeFragment", "Error fetching TV show data", t);
            }
        });
        return null;
    }



    /**
     * Handles item clicks in the movie list.
     * <p>
     * When a Object item is clicked, this method fetches detailed information about the selected movie.
     *
     * @param item The MovieModel object representing the clicked movie.
     */
    public void onItemClicked(Object item) {
        if (item instanceof MovieModel) {
            MovieModel selectedMovie = (MovieModel) item;

            // Start a new fragment or activity to display movie details
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            ShowResultDetailsFragment movieDetailsFragment = ShowResultDetailsFragment.newInstance(selectedMovie.getId()); // Assume you have a method to pass movie ID
            transaction.replace(R.id.fragment_container, movieDetailsFragment);  // Replace with the appropriate container ID
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (item instanceof TVShowModel) {
            TVShowModel selectedTVShow = (TVShowModel) item;

            // Start a new fragment or activity to display TV show details
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            ShowResultTVShowDetailsFragment tvShowDetailsFragment = ShowResultTVShowDetailsFragment.newInstance(selectedTVShow.getId()); // Assume you have a method to pass TV show ID
            transaction.replace(R.id.fragment_container, tvShowDetailsFragment);  // Replace with the appropriate container ID
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

}