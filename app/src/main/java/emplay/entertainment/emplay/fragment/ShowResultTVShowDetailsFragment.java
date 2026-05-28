package emplay.entertainment.emplay.fragment;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
import java.util.ArrayList;
import java.util.List;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.ApiClient;
import emplay.entertainment.emplay.api.MovieApiService;
import emplay.entertainment.emplay.api.TMDBpath;
import emplay.entertainment.emplay.api.TVShowCreditsResponses;
import emplay.entertainment.emplay.api.TVShowDetailsResponse;
import emplay.entertainment.emplay.api.TVShowSimilarResponse;
import emplay.entertainment.emplay.api.MoviesTrailerResponses;
import emplay.entertainment.emplay.api.TVShowsTrailerResponses;
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

/**
 *  Detail screen for a single TV show. Loads info, seasons row, cast row, and
 *  a paginated "more like this" grid — all in parallel since they're independent.
 */
public class ShowResultTVShowDetailsFragment extends BaseFragment {

    private static final String ARG_TV_ID = "TV_ID";
    private static final int PAGE_SIZE = 9; // 3-column grid × 3 rows per page
    private int tvId;
    private List<TVShowModel> tvInformationList;
    private List<SeasonsModel> seasonsList;
    private List<CastModel> castList;
    private List<TVShowModel> allSuggestions = new ArrayList<>();
    private int suggestionPage = 1;
    private RecyclerView detailRecyclerView;
    private RecyclerView seasonsRecyclerview;
    private RecyclerView castRecyclerView;
    private RecyclerView suggestionRecyclerView;
    private LinearLayout suggestionPaginationBar;
    private TextView suggestionPageIndicator;
    private ImageButton suggestionBtnPrev;
    private ImageButton suggestionBtnNext;
    private TVShowInformationAdapter tvAdapter;
    private SeasonsTVAdapter seasonAdapter;
    private CastAdapter castAdapter;
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
        View view = inflater.inflate(R.layout.search_result_tv_view, container, false);

        detailRecyclerView = view.findViewById(R.id.search_result_recyclerview);
        seasonsRecyclerview = view.findViewById(R.id.search_result_seasons_recyclerview);
        castRecyclerView = view.findViewById(R.id.search_result_cast_recyclerview);
        suggestionRecyclerView = view.findViewById(R.id.search_result_suggestion_recyclerview);
        suggestionPaginationBar = view.findViewById(R.id.suggestion_pagination_bar);
        suggestionPageIndicator = view.findViewById(R.id.suggestion_page_indicator);
        suggestionBtnPrev = view.findViewById(R.id.suggestion_btn_prev);
        suggestionBtnNext = view.findViewById(R.id.suggestion_btn_next);

        detailRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        seasonsRecyclerview.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        castRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        suggestionRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        tvInformationList = new ArrayList<>();
        seasonsList = new ArrayList<>();
        castList = new ArrayList<>();

        tvAdapter = new TVShowInformationAdapter(tvInformationList, requireActivity());

        seasonAdapter = new SeasonsTVAdapter(seasonsList, requireActivity(), season -> {
            SeasonDetailFragment fragment = SeasonDetailFragment.newInstance(
                    tvId, season.getSeasonNumber());
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        castAdapter = new CastAdapter(castList, requireActivity(), cast -> {
            CastDetailFragment fragment = CastDetailFragment.newInstance(cast.getId());
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        suggestionAdapter = new SuggestionTVAdapter(new ArrayList<>(), getContext(), this::onItemClicked);

        suggestionBtnPrev.setOnClickListener(v -> {
            suggestionPage--;
            showSuggestionPage();
            suggestionRecyclerView.scrollToPosition(0);
        });
        suggestionBtnNext.setOnClickListener(v -> {
            suggestionPage++;
            showSuggestionPage();
            suggestionRecyclerView.scrollToPosition(0);
        });

        detailRecyclerView.setAdapter(tvAdapter);
        seasonsRecyclerview.setAdapter(seasonAdapter);
        castRecyclerView.setAdapter(castAdapter);
        suggestionRecyclerView.setAdapter(suggestionAdapter);

        detailRecyclerView.setNestedScrollingEnabled(false);
        suggestionRecyclerView.setNestedScrollingEnabled(false);

        apiService = ApiClient.getClient().create(MovieApiService.class);

        if (getArguments() != null) {
            tvId = getArguments().getInt(ARG_TV_ID, -1);
            if (tvId != -1) {
                fetchTVDetailsAndSeasons();
                fetchTVCastList();
                fetchTVSuggestionList();
                fetchTrailersForMovie();
            } else {
                Toast.makeText(getContext(), "Invalid TV show ID", Toast.LENGTH_SHORT).show();
            }
        }

        return view;
    }

    private void onItemClicked(TVShowModel tvShow) {
        if (tvShow != null) {
            navigateTo(ShowResultTVShowDetailsFragment.newInstance(tvShow.getTVShowId()));
        } else {
            Toast.makeText(getContext(), "TV show details are not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchTrailersForMovie() {
        safeEnqueue(apiService.getTVShowsTrailer(TMDBpath.tvShowTrailer(tvId)), new Callback<TVShowsTrailerResponses>() {
            @Override
            public void onResponse(@NonNull Call<TVShowsTrailerResponses> call, @NonNull Response<TVShowsTrailerResponses> response) {
                if (response.isSuccessful()) {
                    TVShowsTrailerResponses trailerResponses = response.body();
                    List<MoviesTrailerResponses.TrailerModel> allTrailers = trailerResponses != null ? trailerResponses.getResults() : null;

                    if (allTrailers == null || allTrailers.isEmpty()) {
                        updateAdapterWithTrailers(new ArrayList<>());
                        return;
                    }

                    // Prefer official "Trailer" videos; fall back to any YouTube video if none found.
                    List<MoviesTrailerResponses.TrailerModel> filtered = new ArrayList<>();
                    for (MoviesTrailerResponses.TrailerModel trailer : allTrailers) {
                        if ("YouTube".equalsIgnoreCase(trailer.getSite()) && "Trailer".equalsIgnoreCase(trailer.getType())) {
                            filtered.add(trailer);
                        }
                    }
                    if (filtered.isEmpty()) {
                        for (MoviesTrailerResponses.TrailerModel trailer : allTrailers) {
                            if ("YouTube".equalsIgnoreCase(trailer.getSite())) filtered.add(trailer);
                        }
                    }
                    updateAdapterWithTrailers(filtered);
                }
            }

            @Override
            public void onFailure(@NonNull Call<TVShowsTrailerResponses> call, @NonNull Throwable t) {
            }
        });
    }

    private void updateAdapterWithTrailers(List<MoviesTrailerResponses.TrailerModel> trailers) {
        if (tvAdapter != null) {
            tvAdapter.setTeaserTrailers(trailers);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchTVDetailsAndSeasons() {
        safeEnqueue(apiService.getTVShowDetails(TMDBpath.tvShowDetails(tvId)), new Callback<TVShowDetailsResponse>() {
            @Override
            public void onResponse(@NonNull Call<TVShowDetailsResponse> call, @NonNull Response<TVShowDetailsResponse> response) {
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
                        for (TVShowDetailsResponse.ProductionCountry pc : tvDetails.getProduction_countries()) {
                            productionCountries.add(pc.getName());
                        }
                    }
                    
                    List<TVShowDetailsResponse.Season> apiSeasons = tvDetails.getSeasons();
                    List<SeasonsModel> seasonsModels = new ArrayList<>();
                    if (apiSeasons != null && !apiSeasons.isEmpty()) {
                        for (TVShowDetailsResponse.Season season : apiSeasons) {
                            SeasonsModel sm = new SeasonsModel(
                                    season.getId(),
                                    season.getName(),
                                    season.getPoster_path(),
                                    season.getEpisode_count(),
                                    season.getSeason_number()
                            );
                            seasonsModels.add(sm);
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            seasonsModels.sort((a, b) -> Integer.compare(a.getSeasonNumber(), b.getSeasonNumber()));
                        }

                        ShowResultTVShowDetailsFragment.this.seasonsList.clear();
                        ShowResultTVShowDetailsFragment.this.seasonsList.addAll(seasonsModels);
                        seasonAdapter.notifyDataSetChanged();
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
                                genres,
                                seasonsModels
                        ));
                    }
                    tvAdapter.notifyDataSetChanged();
                    setRecyclerViewBackground(tvDetails.getBackdrop_path());

                } else {
                    Toast.makeText(getContext(), "Failed to retrieve TV show details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TVShowDetailsResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error fetching TV show details: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     *  Same blurred-backdrop treatment as the movie detail screen.
     */
    private void setRecyclerViewBackground(String backdropPath) {
        if (backdropPath == null || backdropPath.isEmpty()) {
            return;
        }

        Glide.with(this)
                .load("https://image.tmdb.org/t/p/w500/" + backdropPath)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(new MultiTransformation<>(new CenterCrop(), new BlurTransformation(5)))
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        if (isAdded()) {
                            LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{
                                    resource,
                                    ContextCompat.getDrawable(requireContext(), R.drawable.gradient_bg)
                            });
                            detailRecyclerView.setBackground(layerDrawable);
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) { }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchTVCastList() {
        safeEnqueue(apiService.getTVShowCredits(TMDBpath.tvShowCredits(tvId)), new Callback<TVShowCreditsResponses>() {
            @Override
            public void onResponse(@NonNull Call<TVShowCreditsResponses> call, @NonNull Response<TVShowCreditsResponses> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TVShowCreditsResponses tvCreditsResponse = response.body();

                    List<TVShowCreditsResponses.Cast> castList = tvCreditsResponse.getCast();
                    if (castList != null && !castList.isEmpty()) {
                        List<CastModel> castModels = new ArrayList<>();
                        for (TVShowCreditsResponses.Cast cast : castList) {
                            CastModel castModel = new CastModel(
                                    cast.getId(),
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
                }
            }

            @Override
            public void onFailure(@NonNull Call<TVShowCreditsResponses> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Error fetching cast list: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTVSuggestionList() {
        safeEnqueue(apiService.getTVShowSimilar(TMDBpath.tvShowSimilar(tvId)), new Callback<TVShowSimilarResponse>() {
            @Override
            public void onResponse(@NonNull Call<TVShowSimilarResponse> call, @NonNull Response<TVShowSimilarResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TVShowModel> results = response.body().getResults();
                    allSuggestions.clear();
                    if (results != null) {
                        for (TVShowModel tv : results) {
                            if (tv.getPosterPath() != null) {
                                allSuggestions.add(tv);
                            }
                        }
                    }
                    suggestionPage = 1;
                    showSuggestionPage();
                } else {
                    Toast.makeText(getContext(), "Failed to load TV show recommendations", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TVShowSimilarResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Failed to load TV show recommendations: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Slices the full suggestion list into pages and updates the grid + nav buttons.
     * Pagination bar is hidden entirely when there's only one page.
     */
    @SuppressLint("SetTextI18n")
    private void showSuggestionPage() {
        int total = allSuggestions.size();
        int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);
        int fromIndex = (suggestionPage - 1) * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, total);

        if (fromIndex < total) {
            suggestionAdapter.updateData(new ArrayList<>(allSuggestions.subList(fromIndex, toIndex)));
        }

        if (totalPages > 1) {
            suggestionPaginationBar.setVisibility(View.VISIBLE);
            suggestionPageIndicator.setText("Page " + suggestionPage + " of " + totalPages);
            suggestionBtnPrev.setEnabled(suggestionPage > 1);
            suggestionBtnNext.setEnabled(suggestionPage < totalPages);
        } else {
            suggestionPaginationBar.setVisibility(View.GONE);
        }
    }
}
