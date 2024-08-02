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
import androidx.recyclerview.widget.GridLayoutManager;
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

import emplay.entertainment.emplay.movieadapter.MovieAdapter;
import emplay.entertainment.emplay.movieadapter.MovieInformationAdapter;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.ApiClient;
import emplay.entertainment.emplay.api.MovieApiService;
import emplay.entertainment.emplay.api.MovieDetailsResponse;
import emplay.entertainment.emplay.api.MovieResponse;
import emplay.entertainment.emplay.models.MovieModel;
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
    private RecyclerView movieRecyclerView, movieRecyclerView1, movieRecyclerView3;
    private MovieAdapter adapter;
    private MovieInformationAdapter adapter1;
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
        movieRecyclerView1 = view.findViewById(R.id.movie_information_details);
        movieRecyclerView3 = view.findViewById(R.id.movie_trending_recyclerview);//Working on movie Trending

        // Initialize movie list and adapters
        movieList = new ArrayList<>();
        adapter = new MovieAdapter(getContext(), movieList, this::onItemClicked);
        movieRecyclerView.setAdapter(adapter);
        movieRecyclerView.setLayoutManager((new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)));  // Use GridLayoutManager for grid view

        adapter1 = new MovieInformationAdapter(new ArrayList<>());
        movieRecyclerView1.setAdapter(adapter1);
        movieRecyclerView1.setLayoutManager(new LinearLayoutManager(getContext()));  // Use LinearLayoutManager for list view

        // Initialize API service
        apiService = ApiClient.getClient().create(MovieApiService.class);

        // Fetch initial movie data
        fetchInitialData();

        return view;
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
                    adapter.updateData(movieList);
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

    /**
     * Fetches detailed information about a specific movie from the API and updates the movie information adapter.
     *
     * This method makes an API call to retrieve details for the specified movie and updates the MovieInformationAdapter with the fetched data.
     * Displays a Toast message if the data loading fails.
     *
     * @param itemId The ID of the movie for which details are to be fetched.
     */
    private void fetchNewData(String itemId) {
        Call<MovieDetailsResponse> call = apiService.getMovieDetails(Integer.parseInt(itemId), API_KEY);
        call.enqueue(new Callback<MovieDetailsResponse>() {
            @Override
            public void onResponse(Call<MovieDetailsResponse> call, Response<MovieDetailsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieDetailsResponse movieDetails = response.body();
                    List<MovieDetailsResponse> detailList = new ArrayList<>();
                    detailList.add(movieDetails);
                    adapter1.updateData(detailList);
                    setRecyclerViewBackground(movieDetails.getPosterPath());
                } else {
                    Log.e("HomeFragment", "Failed to load movie details. Status code: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e("HomeFragment", "Error response: " + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getContext(), "Failed to load movie details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieDetailsResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load movie details: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("HomeFragment", "Error fetching movie details", t);
            }
        });
    }

    private void setRecyclerViewBackground(String posterPath) {
        // Create the URL for the poster image
        String posterUrl = "https://image.tmdb.org/t/p/w500" + posterPath; // Adjust the size as needed

        // Load the image with Glide and apply the blur transformation
        Glide.with(this)
                .load(posterUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(new MultiTransformation<>(new CenterCrop(), new BlurTransformation(25)))
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        // Combine the blurred image with the gradient drawable
                        Drawable[] layers = new Drawable[2];
                        layers[0] = resource; // Blurred image
                        layers[1] = getResources().getDrawable(R.drawable.gradient_bg); // Gradient drawable

                        LayerDrawable layerDrawable = new LayerDrawable(layers);
                        movieRecyclerView1.setBackground(layerDrawable);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Handle when the load is cleared, if needed
                    }
                });
    }

    /**
     * Handles item clicks in the movie list.
     * <p>
     * When a movie item is clicked, this method fetches detailed information about the selected movie.
     *
     * @param movie The MovieModel object representing the clicked movie.
     */
    public void onItemClicked(MovieModel movie) {
        fetchNewData(String.valueOf(movie.getId()));
    }
}