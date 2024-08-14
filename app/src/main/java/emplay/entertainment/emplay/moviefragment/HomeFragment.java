package emplay.entertainment.emplay.moviefragment;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.api.TVShowDetailsResponse;
import emplay.entertainment.emplay.api.TVShowResponse;
import emplay.entertainment.emplay.api.UpcomingMovieResponse;
import emplay.entertainment.emplay.models.SharedViewModel;
import emplay.entertainment.emplay.models.TVShowModel;
import emplay.entertainment.emplay.movieadapter.MovieAdapter;
import emplay.entertainment.emplay.movieadapter.MovieInformationAdapter;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.ApiClient;
import emplay.entertainment.emplay.api.MovieApiService;
import emplay.entertainment.emplay.api.MovieDetailsResponse;
import emplay.entertainment.emplay.api.MovieResponse;
import emplay.entertainment.emplay.models.MovieModel;
import emplay.entertainment.emplay.movieadapter.TVShowAdapter;
import emplay.entertainment.emplay.movieadapter.TVShowInformationAdapter;
import emplay.entertainment.emplay.movieadapter.UpcomingMovieAdapter;
import jp.wasabeef.glide.transformations.BlurTransformation;
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
    private List<MovieModel> movieList;
    private List<MovieModel> upComingList;
    private List<TVShowModel> tvList;
    private RecyclerView movieRecyclerView, tvRecyclerView2, upComingRecyclerView;
    private MovieAdapter movieAdapter;
    private TVShowAdapter tvShowAdapter;
    private UpcomingMovieAdapter upcomingMovieAdapter;
    private MovieApiService apiService;
    private static final String API_KEY = "ff3dce8592d15d036bf53cbedeca224b";

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
        View view = inflater.inflate(R.layout.movie_activity, container, false);



        // Initialize RecyclerViews
        movieRecyclerView = view.findViewById(R.id.movie_popular_recyclerview);
        tvRecyclerView2 = view.findViewById(R.id.tvshow_popular_recyclerview);
        upComingRecyclerView = view.findViewById(R.id.up_coming_movie_recyclerview);

        // Initialize movie list and adapters
        movieList = new ArrayList<>();
        movieAdapter = new MovieAdapter(getContext(), movieList, this::onItemClicked);
        movieRecyclerView.setAdapter(movieAdapter);
        movieRecyclerView.setLayoutManager((new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)));  // Use GridLayoutManager for grid view

        tvList = new ArrayList<>();
        tvShowAdapter = new TVShowAdapter(getContext(), tvList, this::onItemClicked);
        tvRecyclerView2.setAdapter(tvShowAdapter);
        tvRecyclerView2.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));//Trending movie adapter

        upComingList = new ArrayList<>();
        upcomingMovieAdapter = new UpcomingMovieAdapter(getContext(),upComingList, this::onItemClicked);
        upComingRecyclerView.setAdapter(upcomingMovieAdapter);
        upComingRecyclerView.setLayoutManager((new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)));



        // Initialize API service
        apiService = ApiClient.getClient().create(MovieApiService.class);

        // Fetch initial movie data
        fetchInitialData();
        fetchPopularTV();
        fetchUpComingMovie();

        return view;
    }

    private void fetchUpComingMovie() {
        Call<UpcomingMovieResponse> call = apiService.getUpcomingMovies(API_KEY);
        call.enqueue(new Callback<UpcomingMovieResponse>() {
            @Override
            public void onResponse(Call<UpcomingMovieResponse> call, Response<UpcomingMovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MovieModel> movieList = response.body().getResults();
                    upcomingMovieAdapter.updateData(movieList);
                } else {
                    Log.e("HomeFragment", "Failed to load movie data. Status code: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e("HomeFragment", "Error response: " + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getContext(), "Failed to load movie data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UpcomingMovieResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load movie data: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("HomeFragment", "Error fetching movie data", t);
            }
        });
    }

    /**
     * Fetches the initial list of popular movies from the API and updates the movie adapter.
     *
     * This method makes an API call to retrieve popular movies and updates the MovieAdapter with the fetched data.
     * Displays a Toast message if the data loading fails.
     */
    private void fetchInitialData() {
        Call<MovieResponse> call = apiService.getPopularMovies(API_KEY);
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MovieModel> movieList = response.body().getResults();
                    movieAdapter.updateData(movieList);
                } else {
                    Log.e("HomeFragment", "Failed to load movie data. Status code: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e("HomeFragment", "Error response: " + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getContext(), "Failed to load movie data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load movie data: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("HomeFragment", "Error fetching movie data", t);
            }
        });
    }

    private void fetchPopularTV() {
        Call<TVShowResponse> call = apiService.getPopularTVShows(API_KEY);
        call.enqueue(new Callback<TVShowResponse>() {
            @Override
            public void onResponse(Call<TVShowResponse> call, Response<TVShowResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TVShowModel> tvList = response.body().getResults();
                    tvShowAdapter.updateData(tvList);
                } else {
                    Log.e("HomeFragment", "Failed to load TV show data. Status code: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e("HomeFragment", "Error response: " + errorBody);
                    } catch (IOException e) {
                        Log.e("HomeFragment", "Error reading error body", e);
                    }
                    Toast.makeText(getContext(), "Failed to load TV show data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TVShowResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load TV show data: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("HomeFragment", "Error fetching TV show data", t);
            }
        });
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
            MovieModel movie = (MovieModel) item;
            if (movie != null) {
                Log.d("ItemClicked", "Clicked item is a Movie: " + movie.getTitle());
                ShowResultDetailsFragment showResultDetailsFragment = ShowResultDetailsFragment.newInstance(movie.getId());
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, showResultDetailsFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            } else {
                Toast.makeText(getContext(), "Movie details are not available", Toast.LENGTH_SHORT).show();
            }
        } else if (item instanceof TVShowModel) {
            TVShowModel tvShow = (TVShowModel) item;
            if (tvShow != null) {
                Log.d("ItemClicked", "Clicked item is a TV Show: " + tvShow.getName());
                ShowResultTVShowDetailsFragment showResultTVShowDetailsFragment = ShowResultTVShowDetailsFragment.newInstance(tvShow.getId());
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, showResultTVShowDetailsFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            } else {
                Toast.makeText(getContext(), "TV show details are not available", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d("ItemClicked", "Unknown item type clicked");
            Toast.makeText(getContext(), "Unknown item type", Toast.LENGTH_SHORT).show();
        }
    }



}