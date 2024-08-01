package emplay.entertainment.emplay.moviefragment;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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

import emplay.entertainment.emplay.LanguageMapper;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.MovieApiService;
import emplay.entertainment.emplay.api.MovieCreditsResponse;
import emplay.entertainment.emplay.api.MovieDetailsResponse;
import emplay.entertainment.emplay.api.MovieRecommendationsResponse;
import emplay.entertainment.emplay.models.CastModel;
import emplay.entertainment.emplay.models.MovieModel;
import emplay.entertainment.emplay.movieadapter.CastAdapter;
import emplay.entertainment.emplay.movieadapter.MovieResultAdapter;
import emplay.entertainment.emplay.movieadapter.SuggestionAdapter;
import jp.wasabeef.glide.transformations.BlurTransformation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ShowResultDetailsFragment extends Fragment {

    private static final String ARG_MOVIE_ID = "MOVIE_ID";
    private static final String API_KEY = "ff3dce8592d15d036bf53cbedeca224b";
    private static final String BASE_URL = "https://api.themoviedb.org/";

    private int movieId;
    private List<MovieModel> movieList;
    private List<CastModel> castList;
    private List<MovieModel> suggestionList;
    private RecyclerView detailRecyclerView;
    private RecyclerView castRecyclerView;
    private RecyclerView suggestionRecyclerView;
    private MovieResultAdapter movieResultAdapter;
    private CastAdapter castAdapter;
    private SuggestionAdapter suggestionAdapter;
    private MovieApiService apiService;


    public static ShowResultDetailsFragment newInstance(int movieId) {
        ShowResultDetailsFragment fragment = new ShowResultDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MOVIE_ID, movieId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_result_view, container, false);

        detailRecyclerView = view.findViewById(R.id.search_result_recyclerview);
        castRecyclerView = view.findViewById(R.id.search_result_cast_recyclerview);
        suggestionRecyclerView = view.findViewById(R.id.search_result_suggestion_recyclerview);

        detailRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        castRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        suggestionRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        movieList = new ArrayList<>();
        castList = new ArrayList<>();
        suggestionList = new ArrayList<>(); // Initialize suggestionList here

        movieResultAdapter = new MovieResultAdapter(movieList, getActivity());
        castAdapter = new CastAdapter(castList, getActivity());
        suggestionAdapter = new SuggestionAdapter(suggestionList, getActivity()); // Pass suggestionList

        detailRecyclerView.setAdapter(movieResultAdapter);
        castRecyclerView.setAdapter(castAdapter);
        suggestionRecyclerView.setAdapter(suggestionAdapter);

        // Initialize Retrofit and MovieApiService
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(MovieApiService.class);

        if (getArguments() != null) {
            movieId = getArguments().getInt(ARG_MOVIE_ID, -1);
            if (movieId != -1) {
                fetchMovieDetails();
                fetchCastList();
                fetchSuggestionList();
            } else {
                Toast.makeText(getContext(), "Invalid movie ID", Toast.LENGTH_SHORT).show();
            }
        }

        return view;
    }

    private void fetchMovieDetails() {
        Call<MovieDetailsResponse> call = apiService.getMovieDetails(movieId, API_KEY);
        call.enqueue(new Callback<MovieDetailsResponse>() {
            @Override
            public void onResponse(Call<MovieDetailsResponse> call, Response<MovieDetailsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieDetailsResponse movieDetails = response.body();


                    List<String> genres = new ArrayList<>();
                    if (movieDetails.getGenres() != null) {
                        for (MovieDetailsResponse.Genre genre : movieDetails.getGenres()) {
                            genres.add(genre.getName());
                        }
                    }

                    movieList.clear();
                    if (VERSION.SDK_INT >= VERSION_CODES.N) {
                        movieList.add(new MovieModel(
                                movieDetails.getId(),
                                movieDetails.getTitle(),
                                movieDetails.getVoteAverage(),
                                movieDetails.getPosterPath(),
                                movieDetails.getOverview(),
                                LanguageMapper.getLanguageName(movieDetails.getOriginalLanguage()), // Language mapping here
                                movieDetails.getReleaseDate(),
                                movieDetails.getRuntime(),
                                genres
                        ));
                    }
                    movieResultAdapter.notifyDataSetChanged();
                    // Set the background with the blurred poster image
                    setRecyclerViewBackground(movieDetails.getPosterPath());

                    Toast.makeText(getContext(), "Movie details fetched successfully", Toast.LENGTH_SHORT).show();
                } else {

                    Toast.makeText(getContext(), "Failed to retrieve movie details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieDetailsResponse> call, Throwable t) {

                Toast.makeText(getContext(), "Error fetching movie details: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                    detailRecyclerView.setBackground(layerDrawable);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                    // Handle when the load is cleared, if needed
                }
            });
}

    private void fetchCastList() {

        Call<MovieCreditsResponse> call = apiService.getMovieCredits(movieId, API_KEY);
        call.enqueue(new Callback<MovieCreditsResponse>() {
            @Override
            public void onResponse(Call<MovieCreditsResponse> call, Response<MovieCreditsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieCreditsResponse movieCreditsResponse = response.body();


                    List<MovieCreditsResponse.Cast> castList = movieCreditsResponse.getCast();
                    if (castList != null && !castList.isEmpty()) {
                        List<CastModel> castModels = new ArrayList<>();
                        for (MovieCreditsResponse.Cast cast : castList) {
                            CastModel castModel = new CastModel(
                                    cast.getCastId(),
                                    cast.getCastName(),
                                    cast.getProfilePath()
                            );
                            castModels.add(castModel);
                        }
                        ShowResultDetailsFragment.this.castList.clear();
                        ShowResultDetailsFragment.this.castList.addAll(castModels);
                        castAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Cast list is empty", Toast.LENGTH_SHORT).show();
                    }
                } else {

                    Toast.makeText(getContext(), "Failed to retrieve cast list", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieCreditsResponse> call, Throwable t) {

                Toast.makeText(getContext(), "Error fetching cast list: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchSuggestionList() {
        Call<MovieRecommendationsResponse> call = apiService.getMovieRecommendations(movieId, API_KEY);
        call.enqueue(new Callback<MovieRecommendationsResponse>() {
            @Override
            public void onResponse(Call<MovieRecommendationsResponse> call, Response<MovieRecommendationsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieRecommendationsResponse recommendationsResponse = response.body();
                    Log.d("API Response", "Response body: " + recommendationsResponse.toString()); // Log the full response

                    suggestionList.clear();
                    List<MovieModel> recommendations = recommendationsResponse.getResults();
                    if (recommendations != null) {
                        for (MovieModel movie : recommendations) {
                            suggestionList.add(new MovieModel(
                                    movie.getId(),
                                    movie.getTitle(),
                                    movie.getVoteAverage(),
                                    movie.getPosterPath(),
                                    movie.getOverview(),
                                    movie.getOriginalLanguage(),
                                    movie.getReleaseDate()
                            ));
                        }
                        Log.d("Suggestion List", "Movies added: " + suggestionList.toString()); // Log the suggestion list
                    } else {
                        Log.d("API Response", "Recommendations are null");
                    }
                    suggestionAdapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Suggestions fetched successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("ShowResultDetailsFragment", "Failed to load movie recommendations. Status code: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e("ShowResultDetailsFragment", "Error response: " + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getContext(), "Failed to load movie recommendations", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieRecommendationsResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load movie recommendations: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ShowResultDetailsFragment", "Error fetching movie recommendations", t);
            }
        });
    }
}





