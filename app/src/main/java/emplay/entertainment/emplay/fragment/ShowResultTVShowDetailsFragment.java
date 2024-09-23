package emplay.entertainment.emplay.fragment;


import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
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

import emplay.entertainment.emplay.BuildConfig;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.MovieApiService;

import emplay.entertainment.emplay.api.TVShowCreditsResponses;
import emplay.entertainment.emplay.api.TVShowDetailsResponse;
import emplay.entertainment.emplay.api.TVShowSimilarResponse;
import emplay.entertainment.emplay.api.TVShowsTrailerResponses;
import emplay.entertainment.emplay.api.TVShowsTrailerResponses.TrailerModel;
import emplay.entertainment.emplay.models.CastModel;
import emplay.entertainment.emplay.models.SeasonsModel;
import emplay.entertainment.emplay.models.TVShowModel;
import emplay.entertainment.emplay.adapter.CastAdapter;
import emplay.entertainment.emplay.adapter.SeasonsTVAdapter;
import emplay.entertainment.emplay.adapter.SuggestionTVAdapter;
import emplay.entertainment.emplay.adapter.TVShowInformationAdapter;
import jp.wasabeef.glide.transformations.BlurTransformation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class ShowResultTVShowDetailsFragment extends Fragment {

    private static final String ARG_TV_ID = "TV_ID";

    private static final String BASE_URL = "https://api.themoviedb.org/";

    private int tvId;
    private List<TVShowModel> tvInformationList;
    private List<SeasonsModel> seasonsList;
    private List<CastModel> castList;
    private List<TVShowModel> suggestionList;
    private RecyclerView detailRecyclerView;
    private RecyclerView seasonsRecyclerview;
    private RecyclerView castRecyclerView;
    private RecyclerView suggestionRecyclerView;
    private TVShowInformationAdapter tvAdapter;
    private SeasonsTVAdapter seasonAdapter;
    private CastAdapter castAdapter;
    private SuggestionTVAdapter suggestionAdapter;
    private MovieApiService apiService;

    public String useApiKey() {
        String apiKey = BuildConfig.API_KEY;
        System.out.println("API Key: " + apiKey);
        return apiKey;
    }

    public static ShowResultTVShowDetailsFragment newInstance(int movieId) {
        ShowResultTVShowDetailsFragment fragment = new ShowResultTVShowDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TV_ID, movieId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_result_tv_view, container, false);

        // Initialize RecyclerViews
        detailRecyclerView = view.findViewById(R.id.search_result_recyclerview);
        seasonsRecyclerview = view.findViewById(R.id.search_result_seasons_recyclerview);
        castRecyclerView = view.findViewById(R.id.search_result_cast_recyclerview);
        suggestionRecyclerView = view.findViewById(R.id.search_result_suggestion_recyclerview);

        // Set layout managers for RecyclerViews
        detailRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        seasonsRecyclerview.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        castRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        suggestionRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        // Initialize lists for data
        tvInformationList = new ArrayList<>();
        seasonsList = new ArrayList<>();
        castList = new ArrayList<>();
        suggestionList = new ArrayList<>();

        // Initialize adapters and pass the lists
        tvAdapter = new TVShowInformationAdapter(tvInformationList, getActivity());
        seasonAdapter = new SeasonsTVAdapter(seasonsList, getContext());
        castAdapter = new CastAdapter(castList, getContext());
        suggestionAdapter = new SuggestionTVAdapter(suggestionList, getContext(), this::onItemClicked);

        // Set adapters to RecyclerViews
        detailRecyclerView.setAdapter(tvAdapter);
        seasonsRecyclerview.setAdapter(seasonAdapter);
        castRecyclerView.setAdapter(castAdapter);
        suggestionRecyclerView.setAdapter(suggestionAdapter);

        // Initialize API service
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(MovieApiService.class);

        if (getArguments() != null) {
            tvId = getArguments().getInt(ARG_TV_ID, -1);
            if (tvId != -1) {
                fetchTVDetails();
                fetchTVSeasons();
                fetchTVCastList();
                fetchTVSuggestionList();
                fetchTrailersForMovie();
            } else {
                Toast.makeText(getContext(), "Invalid movie ID", Toast.LENGTH_SHORT).show();
            }
        }

        return view;
    }

    private void onItemClicked(TVShowModel tvShow) {
        if (tvShow != null) {
            ShowResultTVShowDetailsFragment showResultTVShowDetailsFragment = ShowResultTVShowDetailsFragment.newInstance(tvShow.getId());
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, showResultTVShowDetailsFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            Toast.makeText(getContext(), "TV show details are not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchTrailersForMovie() {
        Call<TVShowsTrailerResponses> call = apiService.getTVShowsTrailer(tvId, useApiKey());
        call.enqueue(new Callback<TVShowsTrailerResponses>() {
            @Override
            public void onResponse(Call<TVShowsTrailerResponses> call, Response<TVShowsTrailerResponses> response) {
                if (response.isSuccessful()) {
                    TVShowsTrailerResponses trailerResponses = response.body();
                    List<TVShowsTrailerResponses.TrailerModel> trailers = trailerResponses != null ? trailerResponses.getResults() : null;

                    Log.d("TrailerFetchSuccess", "Raw JSON Response: " + new Gson().toJson(trailerResponses));

                    List<TVShowsTrailerResponses.TrailerModel> teaserTrailers = new ArrayList<>();
                    if (trailers != null) {
                        for (TVShowsTrailerResponses.TrailerModel trailer : trailers) {
                            Log.d("TrailerFetchSuccess", "Trailer Type: " + trailer.getType() + ", Key: " + trailer.getKey());
                            if ("Trailer".equalsIgnoreCase(trailer.getType())) {
                                teaserTrailers.add(trailer);
                            } else {

                            }
                        }
                    }

                    Log.d("TrailerFetchSuccess", "Filtered Teaser trailers size: " + teaserTrailers.size());

                    // Update the adapter with the filtered teaser trailers
                    List<TrailerModel> adapterTrailers = convertToTrailerModels(teaserTrailers);
                    if (tvAdapter != null) {
                        tvAdapter.setTeaserTrailers(adapterTrailers);
                    }

                    // Handle the first trailer, if available
                    if (!teaserTrailers.isEmpty()) {
                        TVShowsTrailerResponses.TrailerModel firstTeaser = teaserTrailers.get(0);
                        firstTeaser.getKey();
                    } else {
                        Toast.makeText(getContext(), "No trailer available", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("TrailerFetchError", "Response Code: " + response.code() + ", Message: " + response.message());
                    Toast.makeText(getContext(), "Failed to retrieve trailers. Response Code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TVShowsTrailerResponses> call, Throwable t) {
                Log.e("TrailerFetchError", "Error fetching trailers: " + t.getMessage());
                Toast.makeText(getContext(), "Error fetching trailers: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<TVShowsTrailerResponses.TrailerModel> convertToTrailerModels(List<TVShowsTrailerResponses.TrailerModel> apiTrailers) {
        List<TVShowsTrailerResponses.TrailerModel> adapterTrailers = new ArrayList<>();

        if (apiTrailers != null) {
            for (TVShowsTrailerResponses.TrailerModel apiTrailer : apiTrailers) {
                TVShowsTrailerResponses.TrailerModel trailerModel = new TVShowsTrailerResponses.TrailerModel(
                        apiTrailer.getKey(),
                        apiTrailer.getName());
                adapterTrailers.add(trailerModel);
            }
        }

        return adapterTrailers;
    }


    private void fetchTVDetails() {
        Call<TVShowDetailsResponse> call = apiService.getTVShowDetails(tvId, useApiKey());
        call.enqueue(new Callback<TVShowDetailsResponse>() {
            @Override
            public void onResponse(Call<TVShowDetailsResponse> call, Response<TVShowDetailsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TVShowDetailsResponse tvDetails = response.body();

                    List<String> genres = new ArrayList<>();
                    if (tvDetails.getGenres() != null) {
                        for (TVShowDetailsResponse.Genre genre : tvDetails.getGenres()) {
                            genres.add(genre.getName());
                        }
                    }
                    List<String> productionCountries = new ArrayList<>();
                    if (tvDetails.getProduction_countries() != null) {
                        for (TVShowDetailsResponse.ProductionCountry productionCountry : tvDetails.getProduction_countries()) {
                            productionCountries.add(productionCountry.getName());
                        }
                    }

                    tvInformationList.clear();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        tvInformationList.add(new TVShowModel(
                                tvDetails.getId(),
                                tvDetails.getName(),
                                tvDetails.getVote_average(),
                                tvDetails.getPoster_path(),
                                tvDetails.getBackdrop_path(),
                                tvDetails.getOverview(),
                                tvDetails.getOriginal_language(),
                                tvDetails.getFirst_air_date(),
                                tvDetails.getNumber_of_seasons(),
                                tvDetails.getNumber_of_episodes(),
                                productionCountries,
                                genres
                        ));
                    }
                    tvAdapter.notifyDataSetChanged();
                    // Set the background with the blurred poster image
                    setRecyclerViewBackground(tvDetails.getBackdrop_path());

                } else {
                    Toast.makeText(getContext(), "Failed to retrieve TV show details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TVShowDetailsResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error fetching TV show details: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTVSeasons() {
        Call<TVShowDetailsResponse> call = apiService.getTVShowDetails(tvId, useApiKey());
        call.enqueue(new Callback<TVShowDetailsResponse>() {
            @Override
            public void onResponse(Call<TVShowDetailsResponse> call, Response<TVShowDetailsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TVShowDetailsResponse tvDetails = response.body();

                    List<TVShowDetailsResponse.Season> seasonList = tvDetails.getSeasons();
                    if (seasonList != null && !seasonList.isEmpty()) {
                        seasonsList.clear();

                        for (TVShowDetailsResponse.Season season : seasonList) {
                            SeasonsModel seasonsModel = new SeasonsModel(
                                    season.getId(),
                                    season.getName(),
                                    season.getPoster_path(),
                                    season.getEpisode_count()
                            );
                            seasonsList.add(seasonsModel);
                        }
                        seasonAdapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to retrieve TV show seasons", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TVShowDetailsResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error fetching TV show seasons: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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


    private void fetchTVCastList() {
        Call<TVShowCreditsResponses> call = apiService.getTVShowCredits(tvId, useApiKey());
        call.enqueue(new Callback<TVShowCreditsResponses>() {
            @Override
            public void onResponse(Call<TVShowCreditsResponses> call, Response<TVShowCreditsResponses> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TVShowCreditsResponses tvCreditsResponse = response.body();

                    List<TVShowCreditsResponses.Cast> castList = tvCreditsResponse.getCast();
                    if (castList != null && !castList.isEmpty()) {
                        List<CastModel> castModels = new ArrayList<>();
                        for (TVShowCreditsResponses.Cast cast : castList) {
                            CastModel castModel = new CastModel(
                                    cast.getCastId(),
                                    cast.getName(),
                                    cast.getProfilePath(),
                                    cast.getCharacter()
                            );
                            castModels.add(castModel);
                        }
                        ShowResultTVShowDetailsFragment.this.castList.clear();
                        ShowResultTVShowDetailsFragment.this.castList.addAll(castModels);
                        castAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(requireContext(), "Cast list is empty", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to retrieve cast list", Toast.LENGTH_SHORT).show();
                    Log.e("TVShowDetails", "Response Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<TVShowCreditsResponses> call, Throwable t) {
                Toast.makeText(requireContext(), "Error fetching cast list: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("TVShowDetails", "Fetch Error", t);
            }
        });
    }


    private void fetchTVSuggestionList() {
        Call<TVShowSimilarResponse> call = apiService.getTVShowSimilar(tvId, useApiKey());
        call.enqueue(new Callback<TVShowSimilarResponse>() {
            @Override
            public void onResponse(Call<TVShowSimilarResponse> call, Response<TVShowSimilarResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TVShowSimilarResponse suggestionResponse = response.body();

                    suggestionList.clear();
                    List<TVShowModel> suggestion = suggestionResponse.getResults();
                    if (suggestion != null) {
                        // Filter out TV shows with null posterPath
                        List<TVShowModel> filteredSuggestions = new ArrayList<>();
                        for (TVShowModel tv : suggestion) {
                            if (tv.getPosterPath() != null) {
                                filteredSuggestions.add(new TVShowModel(
                                        tv.getId(),
                                        tv.getName(),
                                        tv.getVoteAverage(),
                                        tv.getPosterPath(),
                                        tv.getOverview(),
                                        tv.getOriginalLanguage(),
                                        tv.getFirstAirDate()
                                ));
                                // Log each item added to the filtered suggestion list
                                Log.d("Suggestion Item", "TV Show added: " + tv.getName());
                            }
                        }
                        suggestionList.addAll(filteredSuggestions);
                        // Log the size of the suggestion list
                        Log.d("Suggestion List", "Total TV Shows added: " + suggestionList.size());
                    } else {
                        Log.d("API Response", "Recommendations are null");
                    }
                    suggestionAdapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Suggestions fetched successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("ShowResultTVShowDetailsFragment", "Failed to load TV show recommendations. Status code: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e("ShowResultTVShowDetailsFragment", "Error response: " + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getContext(), "Failed to load TV show recommendations", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TVShowSimilarResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load TV show recommendations: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ShowResultTVShowDetailsFragment", "Error fetching TV show recommendations", t);
            }
        });
    }



}

