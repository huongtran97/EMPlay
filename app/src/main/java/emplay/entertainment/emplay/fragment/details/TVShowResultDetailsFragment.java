package emplay.entertainment.emplay.fragment.details;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.activity.TrailerActivity;
import emplay.entertainment.emplay.adapter.common.CastAdapter;
import emplay.entertainment.emplay.adapter.tvshow.SeasonsTVAdapter;
import emplay.entertainment.emplay.adapter.tvshow.SuggestionTVAdapter;
import emplay.entertainment.emplay.api.auth.TMDBWatchlistApiService;
import emplay.entertainment.emplay.api.auth.model.TMDBAccountStatesResponse;
import emplay.entertainment.emplay.api.auth.model.TMDBWatchlistRequest;
import emplay.entertainment.emplay.api.auth.model.TMDBWatchlistStatusResponse;
import emplay.entertainment.emplay.api.common.ApiClient;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.api.common.MovieApiService;
import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.api.tvshow.TVShowCreditsResponses;
import emplay.entertainment.emplay.api.tvshow.TVShowDetailsResponse;
import emplay.entertainment.emplay.api.tvshow.TVShowProviderResponse;
import emplay.entertainment.emplay.api.tvshow.TVShowSimilarResponse;
import emplay.entertainment.emplay.api.tvshow.TVShowsTrailerResponses;
import emplay.entertainment.emplay.auth.AuthManager;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.databinding.SearchResultTvViewBinding;
import emplay.entertainment.emplay.fragment.common.BaseFragment;
import emplay.entertainment.emplay.models.common.CastModel;
import emplay.entertainment.emplay.models.common.RegionProvidersModel;
import emplay.entertainment.emplay.models.tvshow.SeasonsModel;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;
import emplay.entertainment.emplay.tool.LanguageMapper;
import emplay.entertainment.emplay.tool.PaginationHelper;
import emplay.entertainment.emplay.tool.ReadHelper;
import emplay.entertainment.emplay.tool.UiUtils;
import emplay.entertainment.emplay.tool.WatchProviderHelper;
import emplay.entertainment.emplay.database.WatchlistHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TVShowResultDetailsFragment extends BaseFragment {

    private static final String ARG_TV_ID = "TV_ID";
    private static final int PAGE_SIZE = 9;
    private int tvId;
    private List<SeasonsModel> seasonsList;
    private List<CastModel> castList;
    private Map<String, RegionProvidersModel> watchProviderResults;
    private final List<TVShowModel> allSuggestions = new ArrayList<>();
    private List<TVShowsTrailerResponses.TrailerModel> trailers = new ArrayList<>();
    private SearchResultTvViewBinding binding;
    private SeasonsTVAdapter seasonAdapter;
    private CastAdapter castAdapter;
    private SuggestionTVAdapter suggestionTVAdapter;
    private MovieApiService apiService;
    private TMDBWatchlistApiService watchlistApiService;
    private DatabaseHelper databaseHelper;
    private PaginationHelper<TVShowModel> paginationHelper;
    private boolean tmdbTVInWatchlist = false;

    public static TVShowResultDetailsFragment newInstance(int tvId) {
        TVShowResultDetailsFragment fragment = new TVShowResultDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TV_ID, tvId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SearchResultTvViewBinding.inflate(inflater, container, false);

        databaseHelper = DatabaseHelper.getInstance(requireContext());
        apiService = ApiClient.getClient().create(MovieApiService.class);
        watchlistApiService = ApiClient.getClient().create(TMDBWatchlistApiService.class);

        seasonsList = new ArrayList<>();
        castList = new ArrayList<>();

        seasonAdapter = new SeasonsTVAdapter(seasonsList, requireActivity(), season -> navigateTo(SeasonDetailFragment.newInstance(tvId, season.getSeasonNumber())));
        castAdapter = new CastAdapter(castList, requireActivity(), cast -> navigateTo(CastDetailFragment.newInstance(cast.getId())));
        suggestionTVAdapter = new SuggestionTVAdapter(new ArrayList<>(), requireContext(), tv -> {
            if(tv != null) navigateTo(TVShowResultDetailsFragment.newInstance(tv.getTVShowId()));
        });

        binding.searchResultSeasonsRecyclerview.setAdapter(seasonAdapter);
        binding.searchResultSeasonsRecyclerview.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.searchResultCastRecyclerview.setAdapter(castAdapter);
        binding.searchResultCastRecyclerview.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.searchResultSuggestionRecyclerview.setAdapter(suggestionTVAdapter);
        binding.searchResultSuggestionRecyclerview.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.searchResultSuggestionRecyclerview.setNestedScrollingEnabled(false);

        paginationHelper = new PaginationHelper<>(PAGE_SIZE, allSuggestions, new PaginationHelper.PaginationCallback<TVShowModel>() {
            @Override
            public void onPageUpdated(List<TVShowModel> pageItems) {
                suggestionTVAdapter.updateData(pageItems);
            }

            @Override
            public void onUiUpdate(int currentPage, int totalPages, boolean hasPrev, boolean hasNext) {
                PaginationHelper.updatePaginationBar(binding.suggestionPaginationBar, 
                        binding.suggestionPageIndicator, 
                        binding.suggestionBtnPrev, 
                        binding.suggestionBtnNext, 
                        currentPage, totalPages, hasPrev, hasNext);
            }
        });

        binding.suggestionBtnPrev.setOnClickListener(v -> {
            paginationHelper.prevPage();
            binding.searchResultSuggestionRecyclerview.scrollToPosition(0);
        });
        binding.suggestionBtnNext.setOnClickListener(v -> {
            paginationHelper.nextPage();
            binding.searchResultSuggestionRecyclerview.scrollToPosition(0);
        });
        binding.tvInfoCard.btnBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        if (getArguments() != null) {
            tvId = getArguments().getInt(ARG_TV_ID, -1);
            if (tvId != -1) {
                fetchTVDetails();
                fetchWatchProviders();
                fetchTVCastList();
                fetchTVSuggestionList();
                fetchTrailers();
            } else {
                Toast.makeText(requireContext(), "Invalid TV show ID", Toast.LENGTH_SHORT).show();
            }
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Edge-to-edge hero: clear root top padding, push buttons below status bar instead
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), 0, v.getPaddingRight(), v.getPaddingBottom());
            binding.tvInfoCard.topButtonsRow.setPadding(0, bars.top, 0, 0);
            return insets;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setDarkStatusBar();
    }

    @Override
    public void onPause() {
        super.onPause();
        requireActivity().getWindow().setStatusBarColor(0xFF0A0A0A);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cancelCountdown();
        binding = null;
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n", "DefaultLocale"})
    private void bindTVDetails(TVShowDetailsResponse tvDetails) {
        if (binding == null) return;

        // Title & share
        binding.tvInfoCard.tvshowTitle.setText(tvDetails.getName());
        binding.tvInfoCard.btnShare.setOnClickListener(v -> {
            String slug = tvDetails.getName().toLowerCase(Locale.ROOT).replace(" ", "-");
            String url = "https://www.themoviedb.org/tv/" + tvDetails.getId() + "-" + slug;
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, tvDetails.getName() + "\n" + url);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

        // Rating
        binding.tvInfoCard.tvPosterRating.setText(String.format("★ %.1f", tvDetails.getVote_average()));

        // First air date — year only
        String airDate = tvDetails.getFirst_air_date();
        binding.tvInfoCard.movieReleaseDate.setText((airDate != null && airDate.length() >= 4) ? airDate.substring(0, 4) : "");

        // Seasons & episodes
        binding.tvInfoCard.chipSeasons.setText("Seasons: " + tvDetails.getNumber_of_seasons());
        binding.tvInfoCard.chipEpisodes.setText("Episodes: " + tvDetails.getNumber_of_episodes());

        // Language
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.tvInfoCard.movieLanguage.setText(" " + LanguageMapper.getLanguageName(tvDetails.getOriginal_language()));
        }

        // Production country
        List<TVShowDetailsResponse.ProductionCountry> countries = tvDetails.getProduction_countries();
        if (countries != null && !countries.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                String countryNames = countries.stream()
                        .map(TVShowDetailsResponse.ProductionCountry::getName)
                        .collect(Collectors.joining(", "));
                binding.tvInfoCard.movieProductCountry.setText(countryNames);
            }
        } else {
            binding.tvInfoCard.movieProductCountry.setVisibility(View.GONE);
        }

        // Overview and read more/less
        binding.tvInfoCard.tvshowResultOverview.setText(tvDetails.getOverview());
        ReadHelper.setup(binding.tvInfoCard.tvshowResultOverview, binding.tvInfoCard.readMoreText, false, null);

        // Size chip icons to match text
        sizeChipIcon(binding.tvInfoCard.movieReleaseDate);
        sizeChipIcon(binding.tvInfoCard.chipSeasons);
        sizeChipIcon(binding.tvInfoCard.chipEpisodes);
        sizeChipIcon(binding.tvInfoCard.movieLanguage);
        sizeChipIcon(binding.tvInfoCard.movieProductCountry);

        // Genre chips
        List<String> genres = new ArrayList<>();
        for (TVShowDetailsResponse.Genre g : tvDetails.getGenres()) genres.add(g.getName());
        buildGenreChips(binding.tvInfoCard.tvshowInfoGenres, genres);

        // Seasons list
        List<TVShowDetailsResponse.Season> apiSeasons = tvDetails.getSeasons();
        if (apiSeasons != null && !apiSeasons.isEmpty()) {
            List<SeasonsModel> seasonsModels = new ArrayList<>();
            for (TVShowDetailsResponse.Season season : apiSeasons) {
                seasonsModels.add(new SeasonsModel(
                        season.getId(),
                        season.getName(),
                        season.getPoster_path(),
                        season.getEpisode_count(),
                        season.getSeason_number()
                ));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                seasonsModels.sort((a, b) -> Integer.compare(a.getSeasonNumber(), b.getSeasonNumber()));
            }
            seasonAdapter.updateData(seasonsModels);
        }

        // Images
        Glide.with(requireContext())
                .load(ImageUrl.POSTER + tvDetails.getPoster_path())
                .error(R.drawable.placeholder_image)
                .into(binding.tvInfoCard.imageView);

        String backdropPath = tvDetails.getBackdrop_path();
        Glide.with(requireContext())
                .load(backdropPath != null && !backdropPath.isEmpty()
                        ? ImageUrl.BACKDROP + backdropPath
                        : ImageUrl.POSTER + tvDetails.getPoster_path())
                .error(R.drawable.placeholder_image)
                .into(binding.tvInfoCard.backdropView);

        // Watchlist button
        updateWatchlistButton(tvDetails);

        // Coming soon / released
        applyReleaseVisibility(
                tvDetails.getFirst_air_date(),
                binding.tvInfoCard.wtwReleased.getRoot(),
                binding.tvInfoCard.wtwUnreleased.getRoot(),
                binding.tvInfoCard.comingSoonBadge,
                () -> startCountdown(LocalDate.parse(tvDetails.getFirst_air_date()),
                        binding.tvInfoCard.wtwUnreleased, () -> {
                            binding.tvInfoCard.wtwUnreleased.getRoot().setVisibility(View.GONE);
                            binding.tvInfoCard.wtwReleased.getRoot().setVisibility(View.VISIBLE);
                            fetchWatchProviders();
                        })
        );

        // Blurred background
        UiUtils.setupBlurredBackground(this, tvDetails.getBackdrop_path(), tvDetails.getPoster_path(), binding.searchResultFragment);

    }

    private void updateWatchlistButton(TVShowDetailsResponse tvDetails) {
        AuthManager auth = AuthManager.getInstance(requireContext());
        switch (auth.getAuthType()) {
            case NONE:
            case GUEST:
                binding.tvInfoCard.icLibrary.setImageResource(R.drawable.ic_watchlist);
                binding.tvInfoCard.addToLibraryBtn.setOnClickListener(v -> showLoginPromptDialog());
                break;
            case GOOGLE:
                setupGoogleTVWatchlistButton(auth.getUserId(), tvDetails);
                break;
            case TMDB:
                setupTmdbTVWatchlistButton(auth, tvDetails);
                break;
        }
    }

    private void setupGoogleTVWatchlistButton(String userId, TVShowDetailsResponse tvDetails) {
        updateSaveBtnIcon(userId, tvDetails.getId());
        binding.tvInfoCard.addToLibraryBtn.setOnClickListener(v -> new Thread(() -> {
            if (WatchlistHelper.isTVShowSaved(databaseHelper, userId, tvDetails.getId())) {
                WatchlistHelper.removeTVShow(databaseHelper, userId, tvDetails.getId());
                safeRunOnUiThread(() -> {
                    binding.tvInfoCard.icLibrary.setImageResource(R.drawable.ic_watchlist);
                    Toast.makeText(requireContext(), "TV Show removed from library", Toast.LENGTH_SHORT).show();
                });
            } else {
                String genresString = buildGenresString(tvDetails.getGenres());
                boolean saved = WatchlistHelper.saveTVShow(databaseHelper, userId, tvDetails.getId(),
                        tvDetails.getName(), tvDetails.getPoster_path(), genresString,
                        tvDetails.getVote_average()) != -1;
                safeRunOnUiThread(() -> {
                    binding.tvInfoCard.icLibrary.setImageResource(
                            saved ? R.drawable.ic_check : R.drawable.ic_watchlist);
                    Toast.makeText(requireContext(),
                            saved ? "TV Show added to library" : "Failed to add TV Show",
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start());
    }

    private void setupTmdbTVWatchlistButton(AuthManager auth, TVShowDetailsResponse tvDetails) {
        binding.tvInfoCard.icLibrary.setImageResource(R.drawable.ic_watchlist);
        binding.tvInfoCard.addToLibraryBtn.setEnabled(false);

        safeEnqueue(watchlistApiService.getAccountStates(
                TMDBpath.tvAccountStates(tvDetails.getId()), auth.getTMDBSessionId()),
                new Callback<TMDBAccountStatesResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TMDBAccountStatesResponse> call,
                                           @NonNull Response<TMDBAccountStatesResponse> response) {
                        if (binding == null) return;
                        tmdbTVInWatchlist = response.isSuccessful()
                                && response.body() != null && response.body().watchlist;
                        applyTmdbTVWatchlistState(auth, tvDetails.getId());
                    }

                    @Override
                    public void onFailure(@NonNull Call<TMDBAccountStatesResponse> call, @NonNull Throwable t) {
                        if (binding != null) binding.tvInfoCard.addToLibraryBtn.setEnabled(true);
                    }
                });
    }

    private void applyTmdbTVWatchlistState(AuthManager auth, int mediaId) {
        if (binding == null) return;
        binding.tvInfoCard.icLibrary.setImageResource(tmdbTVInWatchlist ? R.drawable.ic_check : R.drawable.ic_watchlist);
        binding.tvInfoCard.addToLibraryBtn.setEnabled(true);
        binding.tvInfoCard.addToLibraryBtn.setOnClickListener(v -> toggleTmdbTVWatchlist(auth, mediaId));
    }

    private void toggleTmdbTVWatchlist(AuthManager auth, int mediaId) {
        binding.tvInfoCard.addToLibraryBtn.setEnabled(false);
        boolean addToWatchlist = !tmdbTVInWatchlist;

        safeEnqueue(watchlistApiService.updateWatchlist(
                TMDBpath.accountAddToWatchlist(auth.getTMDBAccountId()),
                auth.getTMDBSessionId(),
                new TMDBWatchlistRequest("tv", mediaId, addToWatchlist)),
                new Callback<TMDBWatchlistStatusResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TMDBWatchlistStatusResponse> call,
                                           @NonNull Response<TMDBWatchlistStatusResponse> response) {
                        if (binding == null) return;
                        if (response.isSuccessful() && response.body() != null) {
                            tmdbTVInWatchlist = addToWatchlist;
                            Toast.makeText(requireContext(),
                                    addToWatchlist ? "Added to TMDB watchlist" : "Removed from TMDB watchlist",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Failed to update watchlist", Toast.LENGTH_SHORT).show();
                        }
                        applyTmdbTVWatchlistState(auth, mediaId);
                    }

                    @Override
                    public void onFailure(@NonNull Call<TMDBWatchlistStatusResponse> call, @NonNull Throwable t) {
                        if (binding != null) {
                            Toast.makeText(requireContext(), "Failed to update watchlist", Toast.LENGTH_SHORT).show();
                            applyTmdbTVWatchlistState(auth, mediaId);
                        }
                    }
                });
    }

    private void updateSaveBtnIcon(String userId, int id) {
        new Thread(() -> {
            boolean saved = WatchlistHelper.isTVShowSaved(databaseHelper, userId, id);
            safeRunOnUiThread(() ->
                    binding.tvInfoCard.icLibrary.setImageResource(
                            saved ? R.drawable.ic_check : R.drawable.ic_watchlist));
        }).start();
    }

    private String buildGenresString(List<TVShowDetailsResponse.Genre> genres) {
        if (genres == null) return "";
        StringBuilder sb = new StringBuilder();
        for (TVShowDetailsResponse.Genre g : genres) {
            if (sb.length() > 0) sb.append(",");
            sb.append(g.getName());
        }
        return sb.toString();
    }

    private void updateTrailerButton(List<TVShowsTrailerResponses.TrailerModel> trailers) {
        this.trailers = trailers != null ? trailers : new ArrayList<>();
        binding.tvInfoCard.trailerBtn.setOnClickListener(v -> {
            if (!this.trailers.isEmpty()) {
                String videoKey = this.trailers.get(0).getKey();
                Intent intent = new Intent(getContext(), TrailerActivity.class);
                intent.putExtra("TRAILER_ID", videoKey);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "No trailer available", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void applyWatchProviders(String region) {
        if (binding == null) return;
        binding.tvInfoCard.wtwReleased.tvRegion.setText(region);
        binding.tvInfoCard.wtwReleased.layoutWtwEmpty.setVisibility(View.GONE);
        binding.tvInfoCard.wtwReleased.rvProviders.setVisibility(View.VISIBLE);
        binding.tvInfoCard.wtwReleased.tabLayoutWtw.setVisibility(View.VISIBLE);
        RegionProvidersModel providers = watchProviderResults.get(region);
        if (providers != null) {
            WatchProviderHelper.setupProviderTabs(requireContext(),
                    binding.tvInfoCard.wtwReleased.tabLayoutWtw,
                    binding.tvInfoCard.wtwReleased.rvProviders, providers);
        } else {
            handleNoWatchProviders();
        }
    }

    private void handleNoWatchProviders() {
        handleNoWatchProviders(
                binding.tvInfoCard.wtwReleased.layoutWtwEmpty,
                binding.tvInfoCard.wtwReleased.rvProviders,
                binding.tvInfoCard.wtwReleased.tabLayoutWtw
        );
    }

    private void fetchTrailers() {
        safeEnqueue(apiService.getTVShowsTrailer(TMDBpath.tvShowTrailer(tvId)), new Callback<TVShowsTrailerResponses>() {
            @Override
            public void onResponse(@NonNull Call<TVShowsTrailerResponses> call, @NonNull Response<TVShowsTrailerResponses> response) {
                if (response.isSuccessful()) {
                    TVShowsTrailerResponses trailerResponses = response.body();
                    List<TVShowsTrailerResponses.TrailerModel> allTrailers = trailerResponses != null ? trailerResponses.getResults() : null;

                    if (allTrailers == null || allTrailers.isEmpty()) {
                        updateTrailerButton(new ArrayList<>());
                        return;
                    }

                    List<TVShowsTrailerResponses.TrailerModel> filtered = new ArrayList<>();
                    for (TVShowsTrailerResponses.TrailerModel trailer : allTrailers) {
                        if ("YouTube".equalsIgnoreCase(trailer.getSite()) && "Trailer".equalsIgnoreCase(trailer.getType())) {
                            filtered.add(trailer);
                        }
                    }
                    if (filtered.isEmpty()) {
                        for (TVShowsTrailerResponses.TrailerModel trailer : allTrailers) {
                            if ("YouTube".equalsIgnoreCase(trailer.getSite())) filtered.add(trailer);
                        }
                    }
                    updateTrailerButton(filtered);
                }
            }

            @Override
            public void onFailure(@NonNull Call<TVShowsTrailerResponses> call, @NonNull Throwable t) {
                Log.e("TVShowResultDetails", "Trailer fetch failed", t);
            }
        });
    }

    private void fetchWatchProviders() {
        String defaultRegion = Locale.getDefault().getCountry();
        safeEnqueue(apiService.getTVShowProviders(TMDBpath.tvProvider(tvId)), new Callback<TVShowProviderResponse>() {
            @Override
            public void onResponse(@NonNull Call<TVShowProviderResponse> call, @NonNull Response<TVShowProviderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    watchProviderResults = response.body().getResults();
                    String region = watchProviderResults.containsKey(defaultRegion)
                            ? defaultRegion
                            : watchProviderResults.isEmpty() ? null : watchProviderResults.keySet().iterator().next();
                    if (region != null) {
                        applyWatchProviders(region);
                        binding.tvInfoCard.wtwReleased.btnRegion.setOnClickListener(v -> 
                                WatchProviderHelper.showRegionPicker(requireContext(), watchProviderResults, TVShowResultDetailsFragment.this::applyWatchProviders));
                    } else {
                        handleNoWatchProviders();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<TVShowProviderResponse> call, @NonNull Throwable t) {
                Log.e("TVShowResultDetails", "Provider fetch failed", t);
                handleNoWatchProviders();
            }
        });
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n", "DefaultLocale"})
    private void fetchTVDetails() {
        safeEnqueue(apiService.getTVShowDetails(TMDBpath.tvShowDetails(tvId)), new Callback<TVShowDetailsResponse>() {
            @Override
            public void onResponse(@NonNull Call<TVShowDetailsResponse> call, @NonNull Response<TVShowDetailsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    bindTVDetails(response.body());
                } else if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to retrieve TV show details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TVShowDetailsResponse> call, @NonNull Throwable t) {
                if (binding == null) return;
                if (getContext() != null) Toast.makeText(getContext(), "Error fetching TV show details: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchTVCastList() {
        safeEnqueue(apiService.getTVShowCredits(TMDBpath.tvShowCredits(tvId)), new Callback<TVShowCreditsResponses>() {
            @Override
            public void onResponse(@NonNull Call<TVShowCreditsResponses> call, @NonNull Response<TVShowCreditsResponses> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TVShowCreditsResponses tvCreditsResponse = response.body();
                    List<TVShowCreditsResponses.Cast> raw = tvCreditsResponse.getCast();
                    if (raw != null && !raw.isEmpty()) {
                        List<CastModel> newCast = new ArrayList<>();
                        for (TVShowCreditsResponses.Cast cast : raw) {
                            newCast.add(new CastModel(cast.getId(), cast.getName(), cast.getProfilePath(), cast.getCharacter()));
                        }
                        castAdapter.updateData(newCast);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<TVShowCreditsResponses> call, @NonNull Throwable t) {
                if (getContext() != null) Toast.makeText(getContext(), "Error fetching cast list: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTVSuggestionList() {
        safeEnqueue(apiService.getTVShowSimilar(TMDBpath.tvShowSimilar(tvId)), new Callback<TVShowSimilarResponse>() {
            @Override
            public void onResponse(@NonNull Call<TVShowSimilarResponse> call, @NonNull Response<TVShowSimilarResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TVShowModel> results = response.body().getResults();
                    List<TVShowModel> suggestions = new ArrayList<>();
                    if (results != null) {
                        for (TVShowModel tv : results) {
                            if (tv.getPosterPath() != null) suggestions.add(tv);
                        }
                    }
                    paginationHelper.updateData(suggestions);
                } else if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load TV show recommendations", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TVShowSimilarResponse> call, @NonNull Throwable t) {
                if (getContext() != null) Toast.makeText(getContext(), "Failed to load TV show recommendations: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
