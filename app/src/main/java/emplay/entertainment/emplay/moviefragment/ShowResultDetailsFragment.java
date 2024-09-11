package emplay.entertainment.emplay.moviefragment;

import android.content.Intent;
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
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import emplay.entertainment.emplay.LanguageMapper;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.activity.TrailerActivity;
import emplay.entertainment.emplay.api.MovieApiService;
import emplay.entertainment.emplay.api.MovieCreditsResponse;
import emplay.entertainment.emplay.api.MovieDetailsResponse;
import emplay.entertainment.emplay.api.MovieSimilarResponse;
import emplay.entertainment.emplay.api.MoviesTrailerResponses;
import emplay.entertainment.emplay.api.MoviesTrailerResponses.TrailerModel;
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
        suggestionList = new ArrayList<>();

        movieResultAdapter = new MovieResultAdapter(movieList, getActivity());
        castAdapter = new CastAdapter(castList, getActivity());
        suggestionAdapter = new SuggestionAdapter(suggestionList, getContext(), this::onItemClicked);

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
                fetchTrailersForMovie();
            } else {
                Toast.makeText(getContext(), "Invalid movie ID", Toast.LENGTH_SHORT).show();
            }
        }

        return view;
    }

    private void onItemClicked(MovieModel movie) {
        if (movie != null) {
            ShowResultDetailsFragment showResultDetailsFragment = ShowResultDetailsFragment.newInstance(movie.getId());
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, showResultDetailsFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            Toast.makeText(getContext(), "Movie details are not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchTrailersForMovie() {
        Call<MoviesTrailerResponses> call = apiService.getMoviesTrailer(movieId, API_KEY);
        call.enqueue(new Callback<MoviesTrailerResponses>() {
            @Override
            public void onResponse(Call<MoviesTrailerResponses> call, Response<MoviesTrailerResponses> response) {
                if (response.isSuccessful()) {
                    MoviesTrailerResponses trailerResponses = response.body();
                    List<MoviesTrailerResponses.TrailerModel> trailers = trailerResponses != null ? trailerResponses.getResults() : null;

                    Log.d("TrailerFetchSuccess", "Raw JSON Response: " + new Gson().toJson(trailerResponses));

                    List<MoviesTrailerResponses.TrailerModel> teaserTrailers = new ArrayList<>();
                    if (trailers != null) {
                        for (MoviesTrailerResponses.TrailerModel trailer : trailers) {
                            Log.d("TrailerFetchSuccess", "Trailer Type: " + trailer.getType() + ", Key: " + trailer.getKey());
                            if ("Trailer".equalsIgnoreCase(trailer.getType())) {
                                teaserTrailers.add(trailer);
                            }
                        }
                    }

                    Log.d("TrailerFetchSuccess", "Filtered Teaser trailers size: " + teaserTrailers.size());

                    List<TrailerModel> adapterTrailers = convertToTrailerModels(teaserTrailers);
                    if (movieResultAdapter != null) {
                        movieResultAdapter.setTeaserTrailers(adapterTrailers);
                    }

                    if (!teaserTrailers.isEmpty()) {
                        MoviesTrailerResponses.TrailerModel firstTeaser = teaserTrailers.get(0);
                        firstTeaser.getKey();

                    } else {
                        Toast.makeText(getContext(), "No teaser available", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("TrailerFetchError", "Response Code: " + response.code() + ", Message: " + response.message());
                    Toast.makeText(getContext(), "Failed to retrieve trailers. Response Code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MoviesTrailerResponses> call, Throwable t) {
                Log.e("TrailerFetchError", "Error fetching trailers: " + t.getMessage());
                Toast.makeText(getContext(), "Error fetching trailers: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private List<MoviesTrailerResponses.TrailerModel> convertToTrailerModels(List<MoviesTrailerResponses.TrailerModel> apiTrailers) {
        List<MoviesTrailerResponses.TrailerModel> adapterTrailers = new ArrayList<>();

        if (apiTrailers != null) {
            for (MoviesTrailerResponses.TrailerModel apiTrailer : apiTrailers) {
                MoviesTrailerResponses.TrailerModel trailerModel = new MoviesTrailerResponses.TrailerModel(
                        apiTrailer.getKey(),
                        apiTrailer.getName());
                adapterTrailers.add(trailerModel);
            }
        }

        return adapterTrailers;
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
                                movieDetails.getBackdropPath(),
                                movieDetails.getOverview(),
                                LanguageMapper.getLanguageName(movieDetails.getOriginalLanguage()), // Language mapping
                                movieDetails.getReleaseDate(),
                                movieDetails.getRuntime(),
                                genres
                        ));
                    }
                    movieResultAdapter.notifyDataSetChanged();
                    // Set the background with the blurred poster image
                    setRecyclerViewBackground(movieDetails.getBackdropPath());

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

    private void setRecyclerViewBackground(String backdropPath) {
        if (backdropPath == null || backdropPath.isEmpty()) {
            return; // Handle case where backdropPath is null or empty
        }

        // Create the URL for the backdrop image
        String posterUrl = "https://image.tmdb.org/t/p/w500/" + backdropPath;

        // Load the image with Glide and apply the blur transformation
        Glide.with(this)
                .load(posterUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(new MultiTransformation<>(new CenterCrop(), new BlurTransformation(5)))
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        // Combine the blurred image with the gradient drawable
                        Drawable[] layers = new Drawable[2];
                        layers[0] = resource; // Blurred image

                        // Use ContextCompat.getDrawable or Resources.getDrawable with theme
                        layers[1] = ContextCompat.getDrawable(requireContext(), R.drawable.gradient_bg); // Gradient drawable

                        LayerDrawable layerDrawable = new LayerDrawable(layers);
                        detailRecyclerView.setBackground(layerDrawable);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Handle when the load is cleared
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
                                    cast.getProfilePath(),
                                    cast.getCharacter()
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
        Call<MovieSimilarResponse> call = apiService.getMovieSimilar(movieId, API_KEY);
        call.enqueue(new Callback<MovieSimilarResponse>() {
            @Override
            public void onResponse(Call<MovieSimilarResponse> call, Response<MovieSimilarResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieSimilarResponse recommendationsResponse = response.body();
                    Log.d("API Response", "Response body: " + recommendationsResponse.toString());

                    // Ensure suggestionList is initialized
                    if (suggestionList == null) {
                        suggestionList = new ArrayList<>();
                    }
                    suggestionList.clear();

                    List<MovieModel> recommendations = recommendationsResponse.getResults();
                    if (recommendations != null && !recommendations.isEmpty()) {
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
                        suggestionAdapter.notifyDataSetChanged();
                        Toast.makeText(getContext(), "Suggestions fetched successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "No suggestions available", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e("API Error", errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getContext(), "Failed to load movie recommendations", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieSimilarResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load movie recommendations: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}





