package emplay.entertainment.emplay.moviefragment;


import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.LanguageMapper;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.MovieApiService;

import emplay.entertainment.emplay.api.TVShowCreditsResponses;
import emplay.entertainment.emplay.api.TVShowDetailsResponse;
import emplay.entertainment.emplay.api.TVShowSimilarResponse;
import emplay.entertainment.emplay.models.CrewModel;
import emplay.entertainment.emplay.models.TVShowModel;
import emplay.entertainment.emplay.movieadapter.CrewAdapter;
import emplay.entertainment.emplay.movieadapter.SuggestionTVAdapter;
import emplay.entertainment.emplay.movieadapter.TVShowInformationAdapter;
import jp.wasabeef.glide.transformations.BlurTransformation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class ShowResultTVShowDetailsFragment extends Fragment {

    private static final String ARG_TV_ID = "TV_ID";
    private static final String API_KEY = "ff3dce8592d15d036bf53cbedeca224b";
    private static final String BASE_URL = "https://api.themoviedb.org/";

    private int tvId;
    private List<TVShowModel> tvInformationList;
    private List<CrewModel> crewList;
    private List<TVShowModel> suggestionList;
    private RecyclerView detailRecyclerView;
    private RecyclerView castRecyclerView;
    private RecyclerView suggestionRecyclerView;
    private TVShowInformationAdapter tvAdapter;
    private CrewAdapter crewAdapter;
    private SuggestionTVAdapter suggestionAdapter;
    private MovieApiService apiService;

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
        View view = inflater.inflate(R.layout.search_result_view, container, false);

        // Initialize RecyclerViews
        detailRecyclerView = view.findViewById(R.id.search_result_recyclerview);
        castRecyclerView = view.findViewById(R.id.search_result_cast_recyclerview);
        suggestionRecyclerView = view.findViewById(R.id.search_result_suggestion_recyclerview);

        // Set layout managers for RecyclerViews
        detailRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        castRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        suggestionRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        // Initialize lists for data
        tvInformationList = new ArrayList<>();
        crewList = new ArrayList<>();
        suggestionList = new ArrayList<>();

        // Initialize adapters and pass the lists
        tvAdapter = new TVShowInformationAdapter(tvInformationList, getActivity());
        crewAdapter = new CrewAdapter(crewList, getContext());
//        suggestionAdapter = new SuggestionTVAdapter(suggestionList, getActivity());
        suggestionAdapter = new SuggestionTVAdapter(suggestionList, getContext(), this::onItemClicked);



        // Set adapters to RecyclerViews
        detailRecyclerView.setAdapter(tvAdapter);
        castRecyclerView.setAdapter(crewAdapter);
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
                fetchTVCastList();
                fetchTVSuggestionList();
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

    private void fetchTVDetails() {
        Call<TVShowDetailsResponse> call = apiService.getTVShowDetails(tvId, API_KEY);
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
                    List<String> seasons = new ArrayList<>();
                    if (tvDetails.getSeasons() != null) {
                        for (TVShowDetailsResponse.Season season : tvDetails.getSeasons()) {
                            seasons.add(season.getName());
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
                                tvDetails.getOverview(),
                                LanguageMapper.getLanguageName(tvDetails.getOriginal_language()), // Language mapping here
                                tvDetails.getFirst_air_date(),
                                seasons,
                                tvDetails.getNumber_of_episodes(),
                                productionCountries,
                                genres
                        ));
                    }
                    tvAdapter.notifyDataSetChanged();
                    // Set the background with the blurred poster image
                    setRecyclerViewBackground(tvDetails.getPoster_path());

                    Toast.makeText(getContext(), "TV show details fetched successfully", Toast.LENGTH_SHORT).show();
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

    private void fetchTVCastList() {
        Call<TVShowCreditsResponses> call = apiService.getTVShowCredits(tvId, API_KEY);
        call.enqueue(new Callback<TVShowCreditsResponses>() {
            @Override
            public void onResponse(Call<TVShowCreditsResponses> call, Response<TVShowCreditsResponses> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TVShowCreditsResponses tvCreditsResponse = response.body();

                    List<TVShowCreditsResponses.Cast> crewList = tvCreditsResponse.getCast();
                    if (crewList != null && !crewList.isEmpty()) {
                        List<CrewModel> crewModels = new ArrayList<>();
                        for (TVShowCreditsResponses.Cast cast : crewList) {
                            CrewModel crewModel = new CrewModel(
                                    cast.getCrewId(),
                                    cast.getName(),
                                    cast.getProfilePath()
                            );
                            crewModels.add(crewModel);
                        }
                        ShowResultTVShowDetailsFragment.this.crewList.clear();
                        ShowResultTVShowDetailsFragment.this.crewList.addAll(crewModels);
                        crewAdapter.notifyDataSetChanged();
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
        Call<TVShowSimilarResponse> call = apiService.getTVShowSimilar(tvId, API_KEY);
        call.enqueue(new Callback<TVShowSimilarResponse>() {
            @Override
            public void onResponse(Call<TVShowSimilarResponse> call, Response<TVShowSimilarResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TVShowSimilarResponse recommendationsResponse = response.body();
                    Log.d("API Response", "Response body: " + recommendationsResponse.toString()); // Log the full response

                    suggestionList.clear();
                    List<TVShowModel> recommendations = recommendationsResponse.getResults();
                    if (recommendations != null) {
                        for (TVShowModel tv : recommendations) {
                            suggestionList.add(new TVShowModel(
                                    tv.getId(),
                                    tv.getName(),
                                    tv.getVoteAverage(),
                                    tv.getPosterPath(),
                                    tv.getOverview(),
                                    tv.getOriginalLanguage(),
                                    tv.getFirstAirDate()
                            ));
                        }
                        Log.d("Suggestion List", "TV Shows added: " + suggestionList.toString()); // Log the suggestion list
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

