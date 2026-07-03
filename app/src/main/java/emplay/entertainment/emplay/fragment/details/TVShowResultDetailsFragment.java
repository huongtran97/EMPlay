package emplay.entertainment.emplay.fragment.details;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import androidx.transition.TransitionInflater;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.CastAdapter;
import emplay.entertainment.emplay.adapter.common.ProviderAdapter;
import emplay.entertainment.emplay.adapter.tvshow.EpisodeAdapter;
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
import emplay.entertainment.emplay.api.motn.MotnShowResponse;
import emplay.entertainment.emplay.api.tvshow.SeasonDetailResponse;
import emplay.entertainment.emplay.api.tvshow.TVShowProviderResponse;
import emplay.entertainment.emplay.api.tvshow.TVShowContentRatingsResponse;
import emplay.entertainment.emplay.api.tvshow.TVShowDetailsResponse;
import emplay.entertainment.emplay.api.tvshow.TVShowSimilarResponse;
import emplay.entertainment.emplay.api.tvshow.TVShowCreditsResponses;
import emplay.entertainment.emplay.api.tvshow.TVShowsTrailerResponses;
import emplay.entertainment.emplay.auth.AuthManager;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.database.WatchlistHelper;
import emplay.entertainment.emplay.databinding.ActivityDetailTvBinding;
import emplay.entertainment.emplay.databinding.WtwReleasedViewBinding;
import emplay.entertainment.emplay.fragment.common.BaseFragment;
import emplay.entertainment.emplay.fragment.common.SeeAllFragment;
import emplay.entertainment.emplay.models.common.CastModel;
import emplay.entertainment.emplay.models.common.ProviderModel;
import emplay.entertainment.emplay.models.common.RegionProvidersModel;
import emplay.entertainment.emplay.models.tvshow.SeasonsModel;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;
import emplay.entertainment.emplay.tool.MotnHelper;
import emplay.entertainment.emplay.tool.PaginationHelper;
import emplay.entertainment.emplay.tool.ReadHelper;
import emplay.entertainment.emplay.tool.WatchProviderHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TVShowResultDetailsFragment extends BaseFragment {

    private static final String ARG_TV_ID = "TV_ID";
    private static final int PAGE_SIZE = 9;

    private int tvId;
    private final List<SeasonsModel> seasonsList = new ArrayList<>();
    private final List<CastModel> castList = new ArrayList<>();
    private final List<TVShowModel> allSuggestions = new ArrayList<>();
    private List<TVShowsTrailerResponses.TrailerModel> trailers = new ArrayList<>();
    private Map<String, RegionProvidersModel> watchProviderResults;
    private MotnShowResponse cachedMotn;

    private ActivityDetailTvBinding binding;
    private SeasonsTVAdapter seasonTabAdapter;
    private EpisodeAdapter episodeAdapter;
    private CastAdapter castAdapter;
    private SuggestionTVAdapter suggestionTVAdapter;
    private MovieApiService apiService;
    private TMDBWatchlistApiService watchlistApiService;
    private DatabaseHelper databaseHelper;
    private PaginationHelper<TVShowModel> paginationHelper;

    private boolean tmdbTVInWatchlist = false;
    private boolean isAiring = false;
    private boolean userHasSelectedRegion = false;
    private String currentWtwRegion = WatchProviderHelper.defaultRegion();
    private final ArrayList<String> tvGenreNames = new ArrayList<>();

    public static TVShowResultDetailsFragment newInstance(int tvId) {
        TVShowResultDetailsFragment fragment = new TVShowResultDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TV_ID, tvId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSharedElementEnterTransition(TransitionInflater.from(requireContext())
                .inflateTransition(android.R.transition.move));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ActivityDetailTvBinding.inflate(inflater, container, false);

        apiService = ApiClient.getClient().create(MovieApiService.class);
        watchlistApiService = ApiClient.getClient().create(TMDBWatchlistApiService.class);
        databaseHelper = DatabaseHelper.getInstance(requireContext());

        // Season tab chips (horizontal)
        seasonTabAdapter = new SeasonsTVAdapter(seasonsList, requireContext(), season -> {
            int pos = seasonsList.indexOf(season);
            seasonTabAdapter.setSelectedPosition(pos);
            fetchEpisodes(season.getSeasonNumber());
        });
        binding.rvSeasonTabs.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvSeasonTabs.setAdapter(seasonTabAdapter);

        // Episode list (vertical)
        episodeAdapter = new EpisodeAdapter(requireContext());
        binding.rvEpisodes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvEpisodes.setAdapter(episodeAdapter);

        binding.tvSeeMoreEpisodes.setOnClickListener(v -> {
            boolean expanded = !episodeAdapter.isExpanded();
            episodeAdapter.setExpanded(expanded);
            binding.tvSeeMoreEpisodes.setText(expanded ? R.string.see_less : R.string.see_more);
        });

        // Cast
        castAdapter = new CastAdapter(castList, requireContext(), cast ->
                navigateTo(CastDetailFragment.newInstance(cast.getId())));
        binding.rvCast.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvCast.setAdapter(castAdapter);

        binding.tvAllCast.setOnClickListener(v ->
                navigateTo(SeeAllFragment.newInstance(SeeAllFragment.TYPE_CAST_TV, tvId,
                        getString(R.string.detail_section_cast_crew))));

        // Suggestions
        suggestionTVAdapter = new SuggestionTVAdapter(new ArrayList<>(), requireContext(),
                (tv, view) -> navigateTo(TVShowResultDetailsFragment.newInstance(tv.getTVShowId()),
                        view, "poster_transition"));
        binding.searchResultSuggestionRecyclerview.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.searchResultSuggestionRecyclerview.setAdapter(suggestionTVAdapter);

        paginationHelper = new PaginationHelper<>(PAGE_SIZE, allSuggestions,
                new PaginationHelper.PaginationCallback<TVShowModel>() {
                    @Override public void onPageUpdated(List<TVShowModel> pageItems) {
                        suggestionTVAdapter.updateData(pageItems);
                    }
                    @Override public void onUiUpdate(int current, int total,
                                                     boolean hasPrev, boolean hasNext) {}
                });


        binding.cardNextEpisode.setVisibility(View.GONE);

        if (getArguments() != null) {
            tvId = getArguments().getInt(ARG_TV_ID, -1);
            if (tvId != -1) {
                fetchTVDetails();
                fetchTVCastList();
                fetchTVSuggestionList();
                fetchTrailers();
                fetchTVCertification();
                fetchTVProviders();
            }
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            int statusBarHeight = statusBars.top;

            v.setPadding(0, 0, 0, 0);

            ViewGroup.LayoutParams backdropParams = binding.backdropContainer.getLayoutParams();
            backdropParams.height = dpToPx(210) + statusBarHeight;
            binding.backdropContainer.setLayoutParams(backdropParams);


            return insets;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Fetch data
    private void fetchTVDetails() {
        safeEnqueue(apiService.getTVShowDetails(TMDBpath.tvShowDetails(tvId)),
                new Callback<TVShowDetailsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TVShowDetailsResponse> call,
                                           @NonNull Response<TVShowDetailsResponse> response) {
                        if (response.isSuccessful() && response.body() != null)
                            bindTVDetails(response.body());
                    }
                    @Override public void onFailure(@NonNull Call<TVShowDetailsResponse> call,
                                                    @NonNull Throwable t) {}
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void bindTVDetails(TVShowDetailsResponse tv) {
        if (binding == null) return;

        Glide.with(requireContext())
                .load(ImageUrl.of(ImageUrl.BACKDROP, tv.getBackdrop_path()))
                .placeholder(R.drawable.bg_poster_placeholder)
                .into(binding.imgBackdrop);

        binding.tvShowTitle.setText(tv.getName());
        binding.chipRating.setText(String.format(java.util.Locale.getDefault(),
                "★ %.1f", tv.getVote_average()));
        binding.chipSeasons.setText(String.format(java.util.Locale.getDefault(),
                "%d seasons", tv.getNumber_of_seasons()));

        tvGenreNames.clear();
        if (tv.getGenres() != null && !tv.getGenres().isEmpty()) {
            binding.chipGenre.setText(tv.getGenres().get(0).getName());
            binding.chipGenre.setVisibility(View.VISIBLE);
            for (TVShowDetailsResponse.Genre g : tv.getGenres()) tvGenreNames.add(g.getName());
        } else {
            binding.chipGenre.setVisibility(View.GONE);
        }

        List<TVShowDetailsResponse.ProductionCountry> countries = tv.getProduction_countries();
        if (countries != null && !countries.isEmpty()){
            binding.chipCountry.setText(countries.get(0).getName());
        }

        binding.tvOverview.setText(tv.getOverview());
        ReadHelper.setup(binding.tvOverview, binding.readMoreText, false, expanded -> {});

        isAiring = "Returning Series".equals(tv.getStatus()) || tv.isIn_production();
        if (isAiring) {
            binding.tvReturningBadge.setText(R.string.tv_badge_now_airing);
            binding.tvReturningBadge.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.airing));
        } else {
            binding.tvReturningBadge.setText(R.string.tv_badge_ended);
            binding.tvReturningBadge.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.text_3));
            binding.tvReturningBadge.setBackgroundTintList(null);
        }
        binding.tvReturningBadge.setVisibility(View.VISIBLE);

        bindNextEpisode(tv.getNext_episode_to_air());

        // Build season tabs and auto-load first real season
        seasonsList.clear();
        if (tv.getSeasons() != null) {
            for (TVShowDetailsResponse.Season s : tv.getSeasons()) {
                seasonsList.add(new SeasonsModel(s.getId(), s.getName(),
                        s.getPoster_path(), s.getEpisode_count(), s.getSeason_number()));
            }
        }
        seasonTabAdapter.updateData(seasonsList);

        int autoIdx = 0, autoSeasonNumber = 1;
        for (int i = 0; i < seasonsList.size(); i++) {
            if (seasonsList.get(i).getSeasonNumber() > 0) {
                autoIdx = i;
                autoSeasonNumber = seasonsList.get(i).getSeasonNumber();
                break;
            }
        }
        seasonTabAdapter.setSelectedPosition(autoIdx);
        fetchEpisodes(autoSeasonNumber);

        // WTW release visibility
        applyReleaseVisibility(
                tv.getFirst_air_date(),
                binding.wtwReleased.getRoot(),
                binding.wtwUnreleased.getRoot(),
                binding.comingSoonBadge,
                () -> {
                    startCountdown(LocalDate.parse(tv.getFirst_air_date()),
                            binding.wtwUnreleased, () -> {
                                if (binding == null) return;
                                binding.wtwUnreleased.getRoot().setVisibility(View.GONE);
                                binding.wtwReleased.getRoot().setVisibility(View.VISIBLE);
                                fetchTVProviders();
                            });
                }
        );

        updateWatchlistButton(tv);
    }

    private void bindNextEpisode(TVShowDetailsResponse.LastEpisodeToAir next) {
        if (next == null || next.getAir_date() == null || next.getAir_date().isEmpty()) {
            binding.cardNextEpisode.setVisibility(View.GONE);
            return;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDate airDate = LocalDate.parse(next.getAir_date());
                long daysUntil = LocalDate.now().until(airDate, ChronoUnit.DAYS);

                String title = "S" + next.getSeason_number() + " E" + next.getEpisode_number();
                if (next.getName() != null && !next.getName().isEmpty())
                    title += " · \"" + next.getName() + "\"";
                binding.tvNextEpisodeTitle.setText(title);

                if (daysUntil > 0) {
                    binding.tvNextEpisodeDays.setText(
                            daysUntil == 1 ? "Tomorrow" : "In " + daysUntil + "d");
                    binding.tvNextEpisodeDays.setVisibility(View.VISIBLE);
                } else if (daysUntil == 0) {
                    binding.tvNextEpisodeDays.setText("Today");
                    binding.tvNextEpisodeDays.setVisibility(View.VISIBLE);
                } else {
                    binding.tvNextEpisodeDays.setVisibility(View.GONE);
                }
                binding.cardNextEpisode.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            binding.cardNextEpisode.setVisibility(View.GONE);
        }
    }

    private void fetchEpisodes(int seasonNumber) {
        safeEnqueue(apiService.getTVSeasonDetails(TMDBpath.tvSeasonDetails(tvId, seasonNumber)),
                new Callback<SeasonDetailResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SeasonDetailResponse> call,
                                           @NonNull Response<SeasonDetailResponse> response) {
                        if (binding == null || !response.isSuccessful() || response.body() == null) return;
                        episodeAdapter.updateData(response.body().getEpisodes());
                        binding.tvSeeMoreEpisodes.setText(R.string.see_more);
                        binding.tvSeeMoreEpisodes.setVisibility(
                                episodeAdapter.getTotalCount() > 4 ? View.VISIBLE : View.GONE);
                    }
                    @Override public void onFailure(@NonNull Call<SeasonDetailResponse> call,
                                                    @NonNull Throwable t) {}
                });
    }

    private void fetchTVCastList() {
        safeEnqueue(apiService.getTVShowCredits(TMDBpath.tvShowCredits(tvId)),
                new Callback<TVShowCreditsResponses>() {
                    @Override
                    public void onResponse(@NonNull Call<TVShowCreditsResponses> call,
                                           @NonNull Response<TVShowCreditsResponses> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<CastModel> newCast = new ArrayList<>();
                            for (TVShowCreditsResponses.Cast c : response.body().getCast()) {
                                newCast.add(new CastModel(c.getId(), c.getName(),
                                        c.getProfilePath(), c.getCharacter()));
                            }
                            castAdapter.updateData(newCast);
                        }
                    }
                    @Override public void onFailure(@NonNull Call<TVShowCreditsResponses> call,
                                                    @NonNull Throwable t) {}
                });
    }

    private void fetchTVSuggestionList() {
        safeEnqueue(apiService.getTVShowSimilar(TMDBpath.tvShowSimilar(tvId)),
                new Callback<TVShowSimilarResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TVShowSimilarResponse> call,
                                           @NonNull Response<TVShowSimilarResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<TVShowModel> results = new ArrayList<>(response.body().getResults());
                            allSuggestions.clear();
                            allSuggestions.addAll(results);
                            int previewCount = Math.min(10, results.size());
                            suggestionTVAdapter.updateData(results.subList(0, previewCount));
                            if (binding != null && results.size() > 10) {
                                suggestionTVAdapter.setShowMoreItem(true,
                                        () -> navigateTo(SeeAllFragment.newInstance(
                                                SeeAllFragment.TYPE_SIMILAR_TV, tvId,
                                                getString(R.string.detail_section_also_like),
                                                tvGenreNames)));
                            }
                        }
                    }
                    @Override public void onFailure(@NonNull Call<TVShowSimilarResponse> call,
                                                    @NonNull Throwable t) {}
                });
    }

    private void fetchTrailers() {
        safeEnqueue(apiService.getTVShowsTrailer(TMDBpath.tvShowTrailer(tvId)),
                new Callback<TVShowsTrailerResponses>() {
                    @Override
                    public void onResponse(@NonNull Call<TVShowsTrailerResponses> call,
                                           @NonNull Response<TVShowsTrailerResponses> response) {
                        if (response.isSuccessful() && response.body() != null)
                            trailers = response.body().getResults();
                        if (binding != null) wireTrailerButton();
                    }
                    @Override public void onFailure(@NonNull Call<TVShowsTrailerResponses> call,
                                                    @NonNull Throwable t) {
                        if (binding != null) wireTrailerButton();
                    }
                });
    }

    private void wireTrailerButton() {
        binding.btnWatchTrailer.setOnClickListener(v -> {
            TVShowsTrailerResponses.TrailerModel pick = pickOfficialTrailer();
            if (pick != null) {
                TrailerBottomSheetFragment.newInstance(pick.getKey(), pick.getName())
                        .show(getChildFragmentManager(), "trailer");
            } else {
                Toast.makeText(getContext(), "No trailer available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private TVShowsTrailerResponses.TrailerModel pickOfficialTrailer() {
        if (trailers == null || trailers.isEmpty()) return null;
        TVShowsTrailerResponses.TrailerModel officialTrailer = null;
        TVShowsTrailerResponses.TrailerModel finalTrailer = null;
        TVShowsTrailerResponses.TrailerModel anyTrailer = null;
        TVShowsTrailerResponses.TrailerModel anyTeaser = null;
        for (TVShowsTrailerResponses.TrailerModel t : trailers) {
            if (!"YouTube".equals(t.getSite())) continue;
            String name = t.getName() != null ? t.getName().toLowerCase() : "";
            String type = t.getType();
            if ("Trailer".equals(type)) {
                if (t.isOfficial() && name.contains("official") && officialTrailer == null)
                    officialTrailer = t;
                else if (t.isOfficial() && name.contains("final") && finalTrailer == null)
                    finalTrailer = t;
                else if (anyTrailer == null)
                    anyTrailer = t;
            } else if ("Teaser".equals(type) && anyTeaser == null) {
                anyTeaser = t;
            }
        }
        if (officialTrailer != null) return officialTrailer;
        if (finalTrailer != null) return finalTrailer;
        if (anyTrailer != null) return anyTrailer;
        return anyTeaser;
    }

    private void fetchTVCertification() {
        safeEnqueue(apiService.getTVShowContentRatings(TMDBpath.tvShowContentRatings(tvId)),
                new Callback<TVShowContentRatingsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TVShowContentRatingsResponse> call,
                                           @NonNull Response<TVShowContentRatingsResponse> response) {
                        if (binding == null || !response.isSuccessful() || response.body() == null) return;
                        String cert = parseTVCertification(response.body());
                        if (cert != null && !cert.isEmpty()) {
                            binding.chipCertification.setText(cert);
                            binding.chipCertification.setVisibility(View.VISIBLE);
                        } else {
                            binding.chipCertification.setVisibility(View.GONE);
                        }
                    }
                    @Override public void onFailure(@NonNull Call<TVShowContentRatingsResponse> call,
                                                    @NonNull Throwable t) {}
                });
    }

    private String parseTVCertification(TVShowContentRatingsResponse response) {
        if (response.getResults() == null) return null;
        String ca = null, us = null;
        for (TVShowContentRatingsResponse.CountryRating entry : response.getResults()) {
            String rating = entry.getRating();
            if (rating == null || rating.isEmpty()) continue;
            if ("CA".equals(entry.getCountry())) ca = rating;
            else if ("US".equals(entry.getCountry()) && us == null) us = rating;
        }
        return ca != null ? ca : us;
    }

    private void fetchTVProviders() {
        safeEnqueue(apiService.getTVShowProviders(TMDBpath.tvProvider(tvId)),
                new Callback<TVShowProviderResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TVShowProviderResponse> call,
                                           @NonNull Response<TVShowProviderResponse> response) {
                        if (binding == null || !response.isSuccessful() || response.body() == null) return;
                        watchProviderResults = response.body().getResults();
                        String region = WatchProviderHelper.defaultRegion();
                        new Thread(() -> {
                            cachedMotn = MotnHelper.fromJson(
                                    databaseHelper.getCachedMotnJson(tvId, MotnHelper.SHOW_TYPE_TV));
                            if (getActivity() != null) getActivity().runOnUiThread(() -> {
                                bindTVProviders(region);
                                binding.wtwReleased.btnRegion.setOnClickListener(v ->
                                        WatchProviderHelper.showRegionPicker(requireContext(),
                                                watchProviderResults, selectedRegion -> {
                                                    userHasSelectedRegion = true;
                                                    bindTVProviders(selectedRegion);
                                                }));
                            });
                        }).start();
                    }
                    @Override public void onFailure(@NonNull Call<TVShowProviderResponse> call,
                                                    @NonNull Throwable t) {}
                });
    }

    private void bindTVProviders(String region) {
        if (binding == null || !isAdded()) return;
        currentWtwRegion = region;
        WtwReleasedViewBinding wtw = binding.wtwReleased;
        wtw.tvRegion.setText(region);
        wtw.nowInTheatersBadge.setVisibility(View.GONE);

        RegionProvidersModel regionData = watchProviderResults != null
                ? watchProviderResults.get(region) : null;
        if (regionData == null) {
            handleNoWatchProviders();
            return;
        }

        List<ProviderModel> stream = MotnHelper.filterByMotn(craveFirst(regionData.getFlatrate() != null
                ? regionData.getFlatrate() : new ArrayList<>()), cachedMotn, region);
        List<ProviderModel> rent   = MotnHelper.filterByMotn(craveFirst(regionData.getRent() != null
                ? regionData.getRent()     : new ArrayList<>()), cachedMotn, region);
        List<ProviderModel> buy    = MotnHelper.filterByMotn(craveFirst(regionData.getBuy() != null
                ? regionData.getBuy()      : new ArrayList<>()), cachedMotn, region);

        ProviderAdapter streamAdapter  = new ProviderAdapter(ProviderAdapter.TYPE_STREAM);
        ProviderAdapter rentBuyAdapter = new ProviderAdapter(ProviderAdapter.TYPE_RENT_BUY);
        streamAdapter.setOnProviderClick(provider -> handleProviderClick(provider, MotnHelper.SHOW_TYPE_TV));
        rentBuyAdapter.setOnProviderClick(provider -> handleProviderClick(provider, MotnHelper.SHOW_TYPE_TV));

        wtw.layoutWtwEmpty.setVisibility(View.GONE);
        wtw.tabLayoutWtw.setVisibility(View.VISIBLE);
        FlexboxLayoutManager flexLm = new FlexboxLayoutManager(requireContext());
        flexLm.setFlexDirection(FlexDirection.ROW);
        flexLm.setFlexWrap(FlexWrap.WRAP);
        wtw.rvProviders.setLayoutManager(flexLm);
        wtw.rvProviders.setAdapter(streamAdapter);
        streamAdapter.submitList(stream);
        applyWtwEmptyState(wtw, stream.isEmpty());

        wtw.tabLayoutWtw.clearOnTabSelectedListeners();
        wtw.tabLayoutWtw.selectTab(wtw.tabLayoutWtw.getTabAt(0));
        wtw.tabLayoutWtw.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 1:
                        rentBuyAdapter.submitList(rent);
                        wtw.rvProviders.setAdapter(rentBuyAdapter);
                        applyWtwEmptyState(wtw, rent.isEmpty());
                        break;
                    case 2:
                        rentBuyAdapter.submitList(buy);
                        wtw.rvProviders.setAdapter(rentBuyAdapter);
                        applyWtwEmptyState(wtw, buy.isEmpty());
                        break;
                    default:
                        wtw.rvProviders.setAdapter(streamAdapter);
                        applyWtwEmptyState(wtw, stream.isEmpty());
                        break;
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void handleNoWatchProviders() {
        if (binding == null) return;
        WtwReleasedViewBinding wtw = binding.wtwReleased;
        wtw.rvProviders.setVisibility(View.GONE);
        wtw.tabLayoutWtw.setVisibility(View.GONE);

        if (isAiring && !userHasSelectedRegion) {
            wtw.layoutWtwEmpty.setVisibility(View.GONE);
            wtw.nowInTheatersBadge.setVisibility(View.VISIBLE);
            wtw.nowInTheatersBadge.setText(getString(R.string.now_airing));
            wtw.nowInTheatersBadge.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.airing));
            wtw.nowInTheatersBadge.setBackgroundResource(R.drawable.bg_badge_airing);
        } else {
            wtw.nowInTheatersBadge.setVisibility(View.GONE);
            wtw.layoutWtwEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void applyWtwEmptyState(WtwReleasedViewBinding wtw, boolean isEmpty) {
        wtw.rvProviders.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        wtw.layoutWtwEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private List<ProviderModel> craveFirst(List<ProviderModel> providers) {
        List<ProviderModel> sorted = new ArrayList<>(providers);
        sorted.sort((a, b) -> {
            boolean ac = a.getProviderName() != null
                    && a.getProviderName().toLowerCase(java.util.Locale.ROOT).contains("crave");
            boolean bc = b.getProviderName() != null
                    && b.getProviderName().toLowerCase(java.util.Locale.ROOT).contains("crave");
            return ac == bc ? 0 : (ac ? -1 : 1);
        });
        return sorted;
    }

    private void handleProviderClick(ProviderModel provider, String showType) {
        new Thread(() -> {
            String cached = databaseHelper.getCachedMotnJson(tvId, showType);
            if (cached != null) {
                MotnShowResponse motn = MotnHelper.fromJson(cached);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> openMotnLink(motn, provider.getProviderName(), provider.getProviderId()));
                }
                return;
            }
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> fetchAndOpenMotnLink(tvId, showType, provider.getProviderName(), provider.getProviderId()));
            }
        }).start();
    }

    private void fetchAndOpenMotnLink(int tmdbId, String showType, String providerName, int tmdbProviderId) {
        safeEnqueue(apiService.getStreamingAvailability(tmdbId, showType),
                new Callback<MotnShowResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MotnShowResponse> call,
                                           @NonNull Response<MotnShowResponse> response) {
                        if (binding == null || !response.isSuccessful() || response.body() == null) return;
                        MotnShowResponse motn = response.body();
                        cachedMotn = motn;
                        new Thread(() -> databaseHelper.cacheMotnJson(tmdbId, showType, MotnHelper.toJson(motn))).start();
                        openMotnLink(motn, providerName, tmdbProviderId);
                    }
                    @Override public void onFailure(@NonNull Call<MotnShowResponse> call,
                                                    @NonNull Throwable t) {}
                });
    }

    private void openMotnLink(MotnShowResponse motn, String providerName, int tmdbProviderId) {
        if (!isAdded() || binding == null) return;
        String link = MotnHelper.findLink(motn, currentWtwRegion, providerName, tmdbProviderId);
        if (link == null) link = MotnHelper.findServiceHomePage(motn, providerName, tmdbProviderId);
        if (link == null) {
            Toast.makeText(requireContext(), R.string.motn_link_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(link)));
    }

    // ── Watchlist ─────────────────────────────────────────────────────────────

    private void updateWatchlistButton(TVShowDetailsResponse tv) {
        AuthManager auth = AuthManager.getInstance(requireContext());
        if (auth.getAuthType() == AuthManager.AuthType.TMDB) {
            setupTmdbWatchlistButton(auth, tv);
        } else if (auth.getAuthType() == AuthManager.AuthType.GOOGLE) {
            setupGoogleWatchlistButton(auth.getUserId(), tv);
        } else {
            binding.btnMyList.setOnClickListener(v -> showLoginPromptDialog());
        }
    }

    private void setupGoogleWatchlistButton(String userId, TVShowDetailsResponse tv) {
        updateMyListLabel(WatchlistHelper.isTVShowSaved(databaseHelper, userId, tv.getId()));
        binding.btnMyList.setOnClickListener(v -> new Thread(() -> {
            boolean wasSaved = WatchlistHelper.isTVShowSaved(databaseHelper, userId, tv.getId());
            if (wasSaved) {
                WatchlistHelper.removeTVShow(databaseHelper, userId, tv.getId());
                safeRunOnUiThread(() -> {
                    if (binding == null) return;
                    updateMyListLabel(false);
                    Toast.makeText(requireContext(), "TV Show removed from library",
                            Toast.LENGTH_SHORT).show();
                });
            } else {
                String genres = buildGenresString(tv.getGenres());
                boolean saved = WatchlistHelper.saveTVShow(databaseHelper, userId, tv.getId(),
                        tv.getName(), tv.getPoster_path(), genres,
                        tv.getVote_average()) != -1;
                safeRunOnUiThread(() -> {
                    if (binding == null) return;
                    updateMyListLabel(saved);
                    Toast.makeText(requireContext(),
                            saved ? "TV Show added to library" : "Failed to add TV Show",
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start());
    }

    private void setupTmdbWatchlistButton(AuthManager auth, TVShowDetailsResponse tv) {
        binding.btnMyList.setClickable(false);
        safeEnqueue(watchlistApiService.getAccountStates(
                TMDBpath.tvAccountStates(tv.getId()), auth.getTMDBSessionId()),
                new Callback<TMDBAccountStatesResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TMDBAccountStatesResponse> call,
                                           @NonNull Response<TMDBAccountStatesResponse> response) {
                        if (binding == null) return;
                        tmdbTVInWatchlist = response.isSuccessful()
                                && response.body() != null && response.body().watchlist;
                        updateMyListLabel(tmdbTVInWatchlist);
                        binding.btnMyList.setClickable(true);
                        binding.btnMyList.setOnClickListener(v ->
                                toggleTmdbWatchlist(auth, tv.getId()));
                    }
                    @Override
                    public void onFailure(@NonNull Call<TMDBAccountStatesResponse> call,
                                          @NonNull Throwable t) {
                        if (binding != null) binding.btnMyList.setClickable(true);
                    }
                });
    }

    private void toggleTmdbWatchlist(AuthManager auth, int mediaId) {
        binding.btnMyList.setClickable(false);
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
                            updateMyListLabel(tmdbTVInWatchlist);
                            Toast.makeText(requireContext(),
                                    addToWatchlist ? "Added to TMDB watchlist"
                                                   : "Removed from TMDB watchlist",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Failed to update watchlist",
                                    Toast.LENGTH_SHORT).show();
                        }
                        binding.btnMyList.setClickable(true);
                    }
                    @Override
                    public void onFailure(@NonNull Call<TMDBWatchlistStatusResponse> call,
                                          @NonNull Throwable t) {
                        if (binding != null) {
                            Toast.makeText(requireContext(), "Failed to update watchlist",
                                    Toast.LENGTH_SHORT).show();
                            binding.btnMyList.setClickable(true);
                        }
                    }
                });
    }

    private void updateMyListLabel(boolean saved) {
        if (binding == null) return;
        binding.tvMyList.setText(saved ? R.string.detail_saved : R.string.detail_btn_mylist);
    }

    private String buildGenresString(List<TVShowDetailsResponse.Genre> genres) {
        if (genres == null || genres.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < genres.size(); i++) {
            sb.append(genres.get(i).getName());
            if (i < genres.size() - 1) sb.append(",");
        }
        return sb.toString();
    }
}