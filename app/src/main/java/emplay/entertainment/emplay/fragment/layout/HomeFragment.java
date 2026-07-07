package emplay.entertainment.emplay.fragment.layout;

import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.auth.AuthManager;
import emplay.entertainment.emplay.adapter.movie.MovieAdapter;
import emplay.entertainment.emplay.adapter.movie.TopRatedMovieAdapter;
import emplay.entertainment.emplay.adapter.movie.TrendingBannerAdapter;
import emplay.entertainment.emplay.adapter.movie.UpcomingMovieAdapter;
import emplay.entertainment.emplay.adapter.tvshow.OnAirTVAdapter;
import emplay.entertainment.emplay.adapter.tvshow.TVShowAdapter;
import emplay.entertainment.emplay.adapter.tvshow.UpComingTVAdapter;
import emplay.entertainment.emplay.api.common.ApiClient;
import emplay.entertainment.emplay.api.common.MovieApiService;
import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.api.movie.MovieReleaseDatesResponse;
import emplay.entertainment.emplay.api.movie.MovieResponse;
import emplay.entertainment.emplay.api.movie.UpComingMovieResponse;
import emplay.entertainment.emplay.api.tvshow.TVShowDetailsResponse;
import emplay.entertainment.emplay.api.tvshow.TVShowProviderResponse;
import emplay.entertainment.emplay.api.tvshow.TVShowResponse;
import emplay.entertainment.emplay.api.tvshow.UpComingTVShowsResponse;
import emplay.entertainment.emplay.fragment.common.BaseFragment;
import emplay.entertainment.emplay.fragment.common.SeeAllFragment;
import emplay.entertainment.emplay.fragment.details.MovieResultDetailsFragment;
import emplay.entertainment.emplay.fragment.details.TVShowResultDetailsFragment;
import emplay.entertainment.emplay.models.common.RegionProvidersModel;
import emplay.entertainment.emplay.models.movie.MovieModel;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends BaseFragment {
    private ViewPager2 vpTrendingBanner;
    private LinearLayout llHeroDots;
    private UpcomingMovieAdapter upcomingMovieAdapter;
    private UpComingTVAdapter upComingTVAdapter;
    private MovieApiService apiService;
    private TrendingBannerAdapter trendingAdapter;
    private View heroSection;
    private final List<MovieModel> nowPlayingPool = new ArrayList<>();
    private final List<MovieModel> heroBatch = new ArrayList<>();
    private final Set<Integer> nowPlayingMovieIds = new java.util.HashSet<>();
    private boolean nowPlayingIdsFetched = false;
    private List<MovieModel> rawUpcomingMovies = null;
    private final Set<Integer> onAirTVIds = new java.util.HashSet<>();
    private boolean onAirTVIdsFetched = false;
    private List<TVShowModel> rawUpcomingTVShows = null;
    private final Random random = new Random();
    private long lastFetchTime = 0;
    private static final long CACHE_DURATION_MS = 30 * 60 * 1000;
    private static final String PROVIDER_CACHE_PREFS = "provider_cache";
    private static final long PROVIDER_CACHE_TTL_MS = 24 * 60 * 60 * 1000L;
    private android.os.Handler providerCheckHandler;
    private TopRatedMovieAdapter topRatedMovieAdapter;
    private MovieAdapter trendingMoviesAdapter;
    private TVShowAdapter trendingTVAdapter;
    private OnAirTVAdapter onAirTVAdapter;
    private TextView btnTrendingMovies, btnTrendingTV;
    private final List<MovieModel> allTrendingMovies = new ArrayList<>();
    private final List<TVShowModel> allTrendingTVShows = new ArrayList<>();
    private boolean isTrendingShowingTV = false;

    private android.os.Handler autoAdvanceHandler;
    private boolean isBannerDragging = false;
    private static final long AUTO_ADVANCE_MS = 5_000L;

    private View btnBell;
    private TextView tvBellBadge;

    private com.facebook.shimmer.ShimmerFrameLayout shimmerHero;
    private com.facebook.shimmer.ShimmerFrameLayout shimmerTrending;
    private com.facebook.shimmer.ShimmerFrameLayout shimmerOnAir;
    private com.facebook.shimmer.ShimmerFrameLayout shimmerTopRated;
    private com.facebook.shimmer.ShimmerFrameLayout shimmerUpcomingMovies;
    private com.facebook.shimmer.ShimmerFrameLayout shimmerUpcomingTV;
    private RecyclerView rvTrending;
    private RecyclerView rvOnAir;
    private RecyclerView rvTopRated;
    private RecyclerView rvUpcomingMovies;
    private RecyclerView rvUpcomingTvShows;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_home, container, false);

        heroSection = view.findViewById(R.id.heroSection);
        vpTrendingBanner = view.findViewById(R.id.vpTrendingBanner);
        llHeroDots = view.findViewById(R.id.llHeroDots);

        shimmerHero = view.findViewById(R.id.shimmerHero);
        shimmerTrending = view.findViewById(R.id.shimmerTrending);
        shimmerOnAir = view.findViewById(R.id.shimmerOnAir);
        shimmerTopRated = view.findViewById(R.id.shimmerTopRated);
        shimmerUpcomingMovies = view.findViewById(R.id.shimmerUpcomingMovies);
        shimmerUpcomingTV = view.findViewById(R.id.shimmerUpcomingTV);

        if (shimmerHero != null) shimmerHero.startShimmer();
        if (shimmerTrending != null) shimmerTrending.startShimmer();
        if (shimmerOnAir != null) shimmerOnAir.startShimmer();
        if (shimmerTopRated != null) shimmerTopRated.startShimmer();
        if (shimmerUpcomingMovies != null) shimmerUpcomingMovies.startShimmer();
        if (shimmerUpcomingTV != null) shimmerUpcomingTV.startShimmer();

        trendingAdapter = new TrendingBannerAdapter(
                requireContext(),
                new ArrayList<>(),
                new TrendingBannerAdapter.CardGestureListener() {
                    @Override
                    public void onSingleTap(MovieModel movie) {
                        onItemClicked(movie, null);
                    }

                }
        );
        vpTrendingBanner.setAdapter(trendingAdapter);
        vpTrendingBanner.setOffscreenPageLimit(1);

        vpTrendingBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                activateDot(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    isBannerDragging = true;
                    stopAutoAdvance();
                } else if (state == ViewPager2.SCROLL_STATE_IDLE && isBannerDragging) {
                    isBannerDragging = false;
                    startAutoAdvance();
                }
            }
        });

        apiService = ApiClient.getClient().create(MovieApiService.class);

        btnBell = view.findViewById(R.id.btnBell);
        tvBellBadge = view.findViewById(R.id.tvBellBadge);
        if (btnBell != null) {
            btnBell.setOnClickListener(v -> {
                AuthManager auth = AuthManager.getInstance(requireContext());
                if (!auth.isLoggedIn()) return;
                ReleaseAlertsBottomSheet sheet = new ReleaseAlertsBottomSheet();
                sheet.setOnAlertsChangedListener(new ReleaseAlertsBottomSheet.OnAlertsChangedListener() {
                    @Override
                    public void onAlertsChanged() {
                        refreshBellBadge();
                    }
                    @Override
                    public void onNavigateTo(androidx.fragment.app.Fragment fragment) {
                        navigateTo(fragment);
                    }
                });
                sheet.show(getChildFragmentManager(), "release_alerts");
            });
        }

        rvUpcomingMovies = view.findViewById(R.id.rvUpcomingMovies);
        upcomingMovieAdapter = new UpcomingMovieAdapter(requireContext(), new ArrayList<>(), this::onItemClicked);
        rvUpcomingMovies.setAdapter(upcomingMovieAdapter);
        rvUpcomingMovies.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        rvUpcomingTvShows = view.findViewById(R.id.rvUpcomingTvShows);
        upComingTVAdapter = new UpComingTVAdapter(requireContext(), new ArrayList<>(), this::onItemClicked);
        rvUpcomingTvShows.setAdapter(upComingTVAdapter);
        rvUpcomingTvShows.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        rvTopRated = view.findViewById(R.id.rvTopRated);
        topRatedMovieAdapter = new TopRatedMovieAdapter(requireContext(), new ArrayList<>(), this::onItemClicked);
        rvTopRated.setAdapter(topRatedMovieAdapter);
        rvTopRated.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        rvTrending = view.findViewById(R.id.rvTrending);
        trendingMoviesAdapter = new MovieAdapter(requireContext(), new ArrayList<>(), this::onItemClicked);
        trendingTVAdapter = new TVShowAdapter(requireContext(), new ArrayList<>(), this::onItemClicked);
        rvTrending.setAdapter(trendingMoviesAdapter);
        rvTrending.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        rvOnAir = view.findViewById(R.id.rvOnAir);
        onAirTVAdapter = new OnAirTVAdapter(requireContext(), new ArrayList<>(), this::onItemClicked);
        rvOnAir.setAdapter(onAirTVAdapter);
        rvOnAir.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        btnTrendingMovies = view.findViewById(R.id.btnTrendingMovies);
        btnTrendingTV = view.findViewById(R.id.btnTrendingTV);
        if (btnTrendingMovies != null) {
            btnTrendingMovies.setOnClickListener(v -> switchTrending(false, rvTrending));
        }
        if (btnTrendingTV != null) {
            btnTrendingTV.setOnClickListener(v -> switchTrending(true, rvTrending));
        }
        applyTrendingButtonState(false);


        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, bars.top, 0, 0);
            return insets;
        });

        fetchTrendingMovies();
        fetchTrendingTVShows();
        fetchUpComingMovie();
        fetchTopRatedMovies();
        fetchOnAirTVShows();
        fetchUpcomingTVShows();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        long now = System.currentTimeMillis();
        if (nowPlayingPool.isEmpty() || (now - lastFetchTime) > CACHE_DURATION_MS) {
            nowPlayingPool.clear();
            trendingAdapter.updateData(new ArrayList<>());
            fetchNowPlayingMovies();
            lastFetchTime = now;
        } else {
            showNowPlayingBanner();
            if (!nowPlayingIdsFetched) {
                nowPlayingIdsFetched = true;
                applyUpcomingMovieFilter();
            }
            if (!onAirTVIdsFetched) {
                onAirTVIdsFetched = true;
                applyUpcomingTVFilter();
            }
        }
        refreshBellBadge();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoAdvance();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAutoAdvance();
        autoAdvanceHandler = null;
        if (providerCheckHandler != null) {
            providerCheckHandler.removeCallbacksAndMessages(null);
            providerCheckHandler = null;
        }
        heroBatch.clear();
        nowPlayingMovieIds.clear();
        nowPlayingIdsFetched = false;
        rawUpcomingMovies = null;
        onAirTVIds.clear();
        onAirTVIdsFetched = false;
        rawUpcomingTVShows = null;
        heroSection = null;
        btnBell = null;
        tvBellBadge = null;
        shimmerHero = null;
        shimmerTrending = null;
        shimmerOnAir = null;
        shimmerTopRated = null;
        shimmerUpcomingMovies = null;
        shimmerUpcomingTV = null;
        rvTrending = null;
        rvOnAir = null;
        rvTopRated = null;
        rvUpcomingMovies = null;
        rvUpcomingTvShows = null;
    }

    private void switchTrending(boolean showTV, RecyclerView rvTrending) {
        isTrendingShowingTV = showTV;
        rvTrending.setAdapter(showTV ? trendingTVAdapter : trendingMoviesAdapter);
        applyTrendingButtonState(showTV);
    }

    private void applyTrendingButtonState(boolean showTV) {
        if (btnTrendingMovies == null || btnTrendingTV == null) return;
        btnTrendingMovies.setSelected(!showTV);
        btnTrendingTV.setSelected(showTV);
    }

    private void fetchNowPlayingMovies() {
        String region = PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getString("pref_region", "CA");

        AtomicInteger pagesRemaining = new AtomicInteger(2);
        List<MovieModel> allResults = new ArrayList<>();

        Callback<MovieResponse> pageCallback = new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call,
                                   @NonNull Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MovieModel> results = response.body().getResults();
                    if (results != null) allResults.addAll(results);
                }
                if (pagesRemaining.decrementAndGet() == 0) processNowPlayingResults(allResults);
            }

            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                if (pagesRemaining.decrementAndGet() == 0) processNowPlayingResults(allResults);
            }
        };

        safeEnqueue(apiService.getNowPlayingMovies(TMDBpath.nowPlayingMovies(), 1, region), pageCallback);
        safeEnqueue(apiService.getNowPlayingMovies(TMDBpath.nowPlayingMovies(), 2, region), pageCallback);
    }

    private void processNowPlayingResults(List<MovieModel> allResults) {
        nowPlayingMovieIds.clear();
        for (MovieModel m : allResults) nowPlayingMovieIds.add(m.getMovieId());
        nowPlayingIdsFetched = true;
        applyUpcomingMovieFilter();

        if (allResults.isEmpty()) return;

        List<MovieModel> candidates = new ArrayList<>();
        for (MovieModel m : allResults) {
            if (m.getPosterPath() != null) candidates.add(m);
        }
        if (candidates.isEmpty()) return;

        List<MovieModel> englishOnly = new ArrayList<>();
        for (MovieModel m : candidates) {
            if ("en".equals(m.getOriginalLanguage())) englishOnly.add(m);
        }
        List<MovieModel> finalCandidates = englishOnly.size() >= 5 ? englishOnly : candidates;

        filterByTheatricalRelease(finalCandidates);
    }

    private void filterByTheatricalRelease(List<MovieModel> candidates) {
        SharedPreferences regionPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String region = regionPrefs.getString("pref_region", "CA");
        SharedPreferences rdCache = requireContext()
                .getSharedPreferences(PROVIDER_CACHE_PREFS, android.content.Context.MODE_PRIVATE);
        long now = System.currentTimeMillis();

        AtomicInteger remaining = new AtomicInteger(candidates.size());
        List<MovieModel> qualified = new ArrayList<>();

        providerCheckHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        int apiCallIndex = 0;

        for (MovieModel movie : candidates) {
            boolean rdCached = (now - rdCache.getLong("rd_ts_" + movie.getMovieId() + "_" + region, 0L))
                    < PROVIDER_CACHE_TTL_MS;

            if (!rdCached) {
                int delay = apiCallIndex++ * 200;
                providerCheckHandler.postDelayed(
                        () -> fetchReleaseDates(movie, region, qualified, remaining, candidates, rdCache),
                        delay);
                continue;
            }

            boolean hasType3 = rdCache.getBoolean("rd_has3_" + movie.getMovieId() + "_" + region, false);
            if (!hasType3) {
                onBothChecksDone(remaining, candidates, qualified);
                continue;
            }

            String d = rdCache.getString("rd_date3_" + movie.getMovieId() + "_" + region, null);
            if (d != null && !d.isEmpty()) movie.setTheatricalDate(d);
            String c = rdCache.getString("rd_cert3_" + movie.getMovieId() + "_" + region, null);
            if (c != null && !c.isEmpty()) movie.setCertification(c);

            boolean pCached = (now - rdCache.getLong("pt_" + movie.getMovieId() + "_" + region, 0L))
                    < PROVIDER_CACHE_TTL_MS;

            if (!pCached) {
                int delay = apiCallIndex++ * 200;
                providerCheckHandler.postDelayed(
                        () -> fetchProviders(movie, region, qualified, remaining, candidates, rdCache),
                        delay);
            } else {
                if (rdCache.getBoolean("p_" + movie.getMovieId() + "_" + region, true))
                    qualified.add(movie);
                onBothChecksDone(remaining, candidates, qualified);
            }
        }
    }

    private void fetchReleaseDates(MovieModel movie, String region,
                                   List<MovieModel> qualified,
                                   AtomicInteger remaining,
                                   List<MovieModel> candidates,
                                   SharedPreferences rdCache) {
        safeEnqueue(apiService.getMovieReleaseDates(TMDBpath.movieReleaseDates(movie.getMovieId())),
                new Callback<MovieReleaseDatesResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MovieReleaseDatesResponse> call,
                                           @NonNull Response<MovieReleaseDatesResponse> response) {
                        boolean hasType3 = false;
                        String theatricalDate = null;
                        String certification = null;

                        if (response.isSuccessful() && response.body() != null) {
                            List<MovieReleaseDatesResponse.CountryReleaseDates> results =
                                    response.body().getResults();
                            if (results != null) {
                                MovieReleaseDatesResponse.CountryReleaseDates countryEntry = null;
                                MovieReleaseDatesResponse.CountryReleaseDates usEntry = null;
                                for (MovieReleaseDatesResponse.CountryReleaseDates entry : results) {
                                    if (region.equals(entry.getCountry())) countryEntry = entry;
                                    if ("US".equals(entry.getCountry())) usEntry = entry;
                                }
                                MovieReleaseDatesResponse.CountryReleaseDates target =
                                        countryEntry != null ? countryEntry : usEntry;
                                if (target != null && target.getReleaseDates() != null) {
                                    for (MovieReleaseDatesResponse.ReleaseDate rd : target.getReleaseDates()) {
                                        if (rd.getType() == 3) {
                                            hasType3 = true;
                                            String raw = rd.getReleaseDate();
                                            theatricalDate = (raw != null && raw.length() >= 10)
                                                    ? raw.substring(0, 10) : null;
                                            certification = rd.getCertification();
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        rdCache.edit()
                                .putBoolean("rd_has3_" + movie.getMovieId() + "_" + region, hasType3)
                                .putString("rd_date3_" + movie.getMovieId() + "_" + region,
                                        theatricalDate != null ? theatricalDate : "")
                                .putString("rd_cert3_" + movie.getMovieId() + "_" + region,
                                        certification != null ? certification : "")
                                .putLong("rd_ts_" + movie.getMovieId() + "_" + region, System.currentTimeMillis())
                                .apply();

                        if (!hasType3) {
                            onBothChecksDone(remaining, candidates, qualified);
                            return;
                        }
                        if (theatricalDate != null) movie.setTheatricalDate(theatricalDate);
                        if (certification != null && !certification.isEmpty())
                            movie.setCertification(certification);
                        fetchProviders(movie, region, qualified, remaining, candidates, rdCache);
                    }

                    @Override
                    public void onFailure(@NonNull Call<MovieReleaseDatesResponse> call, @NonNull Throwable t) {
                        onBothChecksDone(remaining, candidates, qualified);
                    }
                });
    }

    private void fetchProviders(MovieModel movie, String region,
                                 List<MovieModel> qualified,
                                 AtomicInteger remaining,
                                 List<MovieModel> candidates,
                                 SharedPreferences cache) {
        safeEnqueue(apiService.getMovieProviders(TMDBpath.movieProvider(movie.getMovieId())),
                new Callback<TVShowProviderResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TVShowProviderResponse> call,
                                           @NonNull Response<TVShowProviderResponse> response) {
                        boolean hasDigital = false;
                        if (response.isSuccessful() && response.body() != null) {
                            Map<String, RegionProvidersModel> providerMap = response.body().getResults();
                            if (providerMap != null && providerMap.containsKey(region)) {
                                RegionProvidersModel r = providerMap.get(region);
                                if (r != null) {
                                    hasDigital = (r.getFlatrate() != null && !r.getFlatrate().isEmpty())
                                            || (r.getRent() != null && !r.getRent().isEmpty())
                                            || (r.getBuy() != null && !r.getBuy().isEmpty());
                                }
                            }
                        }
                        boolean isTheaterOnly = !hasDigital;
                        cache.edit()
                                .putBoolean("p_" + movie.getMovieId() + "_" + region, isTheaterOnly)
                                .putLong("pt_" + movie.getMovieId() + "_" + region, System.currentTimeMillis())
                                .apply();
                        if (isTheaterOnly) qualified.add(movie);
                        onBothChecksDone(remaining, candidates, qualified);
                    }

                    @Override
                    public void onFailure(@NonNull Call<TVShowProviderResponse> call, @NonNull Throwable t) {
                        qualified.add(movie);
                        onBothChecksDone(remaining, candidates, qualified);
                    }
                });
    }

    private void onBothChecksDone(AtomicInteger remaining, List<MovieModel> candidates,
                                   List<MovieModel> qualified) {
        if (remaining.decrementAndGet() != 0) return;
        Set<Integer> qualifiedIds = new java.util.HashSet<>();
        for (MovieModel m : qualified) qualifiedIds.add(m.getMovieId());
        nowPlayingPool.clear();
        for (MovieModel m : candidates) {
            if (qualifiedIds.contains(m.getMovieId())) nowPlayingPool.add(m);
        }
        if (!nowPlayingPool.isEmpty()) showNowPlayingBanner();
    }

    private void showNowPlayingBanner() {
        if (nowPlayingPool.isEmpty() || !isAdded()) return;

        if (heroBatch.isEmpty()) {
            heroBatch.addAll(pickHeroBatch());
        }
        if (heroBatch.isEmpty()) return;

        hideShimmer(shimmerHero, vpTrendingBanner);
        trendingAdapter.updateData(new ArrayList<>(heroBatch));
        buildDots(heroBatch.size());
        vpTrendingBanner.setCurrentItem(0, false);
        startAutoAdvance();
    }

    private void startAutoAdvance() {
        stopAutoAdvance();
        if (autoAdvanceHandler == null) {
            autoAdvanceHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        }
        autoAdvanceHandler.postDelayed(this::doAutoAdvance, AUTO_ADVANCE_MS);
    }

    private void doAutoAdvance() {
        if (vpTrendingBanner == null || !isAdded()) return;
        int count = trendingAdapter.getItemCount();
        if (count > 1) {
            int next = (vpTrendingBanner.getCurrentItem() + 1) % count;
            vpTrendingBanner.setCurrentItem(next, true);
        }
        if (autoAdvanceHandler != null) {
            autoAdvanceHandler.postDelayed(this::doAutoAdvance, AUTO_ADVANCE_MS);
        }
    }

    private void stopAutoAdvance() {
        if (autoAdvanceHandler != null) {
            autoAdvanceHandler.removeCallbacksAndMessages(null);
        }
    }

    private void hideShimmer(com.facebook.shimmer.ShimmerFrameLayout shimmer, View content) {
        if (shimmer == null) return;
        shimmer.stopShimmer();
        shimmer.setVisibility(View.GONE);
        if (content != null) content.setVisibility(View.VISIBLE);
    }

    private List<MovieModel> pickHeroBatch() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("hero_prefs", 0);

        Map<Integer, MovieModel> poolMap = new LinkedHashMap<>();
        for (MovieModel m : nowPlayingPool) poolMap.put(m.getMovieId(), m);
        List<Integer> poolIds = new ArrayList<>(poolMap.keySet());

        String currentPoolStr = idsToString(poolIds);
        List<Integer> unseenIds = parseIds(prefs.getString("hero_unseen_ids", ""));
        List<Integer> lastBatchIds = parseIds(prefs.getString("hero_last_batch_ids", ""));

        if (!currentPoolStr.equals(prefs.getString("hero_pool_ids", ""))) {
            unseenIds = new ArrayList<>(poolIds);
            Collections.shuffle(unseenIds, random);
            lastBatchIds.clear();
        } else {
            unseenIds.retainAll(poolIds);
        }

        if (unseenIds.size() < 5) {
            List<Integer> newCycle = new ArrayList<>(poolIds);
            Collections.shuffle(newCycle, random);
            if (!lastBatchIds.isEmpty() && newCycle.size() > lastBatchIds.size()) {
                for (int i = 0; i < newCycle.size(); i++) {
                    List<Integer> nextBatch = newCycle.subList(0, Math.min(5, newCycle.size()));
                    boolean hasOverlap = false;
                    for (int id : nextBatch) {
                        if (lastBatchIds.contains(id)) { hasOverlap = true; break; }
                    }
                    if (!hasOverlap) break;
                    Collections.rotate(newCycle, 1);
                }
            }
            Set<Integer> deduped = new LinkedHashSet<>(unseenIds);
            deduped.addAll(newCycle);
            unseenIds = new ArrayList<>(deduped);
        }

        int take = Math.min(5, unseenIds.size());
        List<Integer> batch = new ArrayList<>(unseenIds.subList(0, take));
        List<Integer> remaining = new ArrayList<>(unseenIds.subList(take, unseenIds.size()));

        prefs.edit()
                .putString("hero_pool_ids", currentPoolStr)
                .putString("hero_unseen_ids", idsToString(remaining))
                .putString("hero_last_batch_ids", idsToString(batch))
                .apply();

        List<MovieModel> result = new ArrayList<>();
        for (int id : batch) {
            MovieModel m = poolMap.get(id);
            if (m != null) result.add(m);
        }
        return result;
    }

    private static String idsToString(List<Integer> ids) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(ids.get(i));
        }
        return sb.toString();
    }

    private static List<Integer> parseIds(String s) {
        List<Integer> ids = new ArrayList<>();
        if (s == null || s.isEmpty()) return ids;
        for (String part : s.split(",")) {
            try { ids.add(Integer.parseInt(part.trim())); } catch (NumberFormatException ignored) {}
        }
        return ids;
    }

    private void fetchTrendingMovies() {
        safeEnqueue(apiService.getTrendingMovies(TMDBpath.trendingMovies()),
                new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MovieResponse> call,
                                           @NonNull Response<MovieResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<MovieModel> results = response.body().getResults();
                            if (results == null || results.isEmpty()) return;
                            allTrendingMovies.clear();
                            for (MovieModel movie : results) {
                                if (movie.getPosterPath() == null && movie.getBackdropPath() == null) continue;
                                allTrendingMovies.add(movie);
                            }
                            trendingMoviesAdapter.updateData(new ArrayList<>(allTrendingMovies));
                            hideShimmer(shimmerTrending, rvTrending);
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                        Log.e("HomeFragment", "Failed to fetch trending movies", t);
                    }
                });
    }

    private void fetchTrendingTVShows() {
        safeEnqueue(apiService.getTrendingTVShows(TMDBpath.trendingTVShows()),
                new Callback<TVShowResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TVShowResponse> call,
                                           @NonNull Response<TVShowResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<TVShowModel> results = response.body().getResults();
                            if (results == null) return;
                            allTrendingTVShows.clear();
                            for (TVShowModel tv : results) {
                                if (tv.getPosterPath() == null && tv.getBackdropPath() == null) continue;
                                allTrendingTVShows.add(tv);
                            }
                            trendingTVAdapter.updateData(new ArrayList<>(allTrendingTVShows));
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<TVShowResponse> call, @NonNull Throwable t) {
                        Log.e("HomeFragment", "Failed to fetch trending TV shows", t);
                    }
                });
    }

    private void fetchTopRatedMovies() {
        safeEnqueue(apiService.getTopRatedMovies(TMDBpath.topRatedMovies(), 1),
                new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MovieResponse> call,
                                           @NonNull Response<MovieResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<MovieModel> results = response.body().getResults();
                            if (results != null) {
                                results.removeIf(m -> m.getPosterPath() == null && m.getBackdropPath() == null);
                                topRatedMovieAdapter.updateData(results.subList(0, Math.min(10, results.size())));
                                hideShimmer(shimmerTopRated, rvTopRated);
                            }
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                        Log.e("HomeFragment", "Failed to fetch top rated movies", t);
                    }
                });
    }

    private void fetchUpComingMovie() {
        String region = PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getString("pref_region", "CA");
        safeEnqueue(apiService.getUpcomingMovies(
                        TMDBpath.discoverMovies(), TMDBpath.todayDate(), TMDBpath.thirtyDaysFromNow(),
                        "popularity.desc", false, "en-US", 1, region),
                new Callback<UpComingMovieResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<UpComingMovieResponse> call,
                                           @NonNull Response<UpComingMovieResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<MovieModel> raw = new ArrayList<>();
                            for (MovieModel movie : response.body().getResults()) {
                                if (movie.getPosterPath() != null || movie.getBackdropPath() != null) raw.add(movie);
                            }
                            rawUpcomingMovies = raw;
                            applyUpcomingMovieFilter();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<UpComingMovieResponse> call, @NonNull Throwable t) {
                        Log.e("HomeFragment", "Failed to fetch upcoming movies", t);
                    }
                });
    }

    private void applyUpcomingMovieFilter() {
        if (rawUpcomingMovies == null || upcomingMovieAdapter == null) return;
        if (!nowPlayingIdsFetched) return;

        List<MovieModel> toUpcoming = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (MovieModel m : rawUpcomingMovies) {
            if (nowPlayingMovieIds.contains(m.getMovieId())) continue;
            String rd = m.getReleaseDate();
            if (rd != null && rd.length() == 10) {
                try { if (!LocalDate.parse(rd).isAfter(today)) continue; } catch (Exception ignored) {}
            }
            toUpcoming.add(m);
        }
        List<MovieModel> upcomingPreview = toUpcoming.size() > 10
                ? toUpcoming.subList(0, 10) : toUpcoming;
        upcomingMovieAdapter.updateData(upcomingPreview);
        if (toUpcoming.size() > 10) {
            upcomingMovieAdapter.setShowMoreItem(true, () -> navigateTo(
                    SeeAllFragment.newInstance(SeeAllFragment.TYPE_UPCOMING_MOVIES,
                            getString(R.string.home_section_coming_soon))));
        }
        hideShimmer(shimmerUpcomingMovies, rvUpcomingMovies);
    }

    private void fetchOnAirTVShows() {
        safeEnqueue(apiService.getOnAirTVShows(TMDBpath.onAirTVShows(), 1),
                new Callback<TVShowResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TVShowResponse> call,
                                           @NonNull Response<TVShowResponse> response) {
                        onAirTVIds.clear();
                        List<TVShowModel> onAirList = new ArrayList<>();
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getResults() != null) {
                            for (TVShowModel tv : response.body().getResults()) {
                                onAirTVIds.add(tv.getTVShowId());
                                if (tv.getPosterPath() != null || tv.getBackdropPath() != null) onAirList.add(tv);
                            }
                        }
                        List<TVShowModel> onAirPreview = onAirList.size() > 10
                                ? onAirList.subList(0, 10) : onAirList;
                        if (onAirTVAdapter != null) {
                            onAirTVAdapter.updateData(onAirPreview);
                            if (onAirList.size() > 10) {
                                onAirTVAdapter.setShowMoreItem(true, () -> navigateTo(
                                        SeeAllFragment.newInstance(SeeAllFragment.TYPE_ON_AIR,
                                                getString(R.string.home_section_on_air))));
                            }
                        }
                        hideShimmer(shimmerOnAir, rvOnAir);
                        onAirTVIdsFetched = true;
                        applyUpcomingTVFilter();
                        fetchOnAirDetails(onAirList);
                    }
                    @Override
                    public void onFailure(@NonNull Call<TVShowResponse> call, @NonNull Throwable t) {
                        onAirTVIdsFetched = true;
                        applyUpcomingTVFilter();
                        Log.e("HomeFragment", "Failed to fetch on-air TV shows", t);
                    }
                });
    }

    private void fetchOnAirDetails(List<TVShowModel> onAirList) {
        for (int i = 0; i < onAirList.size(); i++) {
            final int idx = i;
            final TVShowModel tv = onAirList.get(i);
            safeEnqueue(apiService.getTVShowDetails(TMDBpath.tvShowDetails(tv.getTVShowId())),
                    new Callback<TVShowDetailsResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<TVShowDetailsResponse> call,
                                               @NonNull Response<TVShowDetailsResponse> response) {
                            if (!response.isSuccessful() || response.body() == null) return;
                            TVShowDetailsResponse d = response.body();
                            TVShowDetailsResponse.LastEpisodeToAir ep = d.getLast_episode_to_air();
                            if (ep != null) {
                                tv.setOnAirEpisodeNumber(ep.getEpisode_number());
                                tv.setOnAirEpisodeAirDate(ep.getAir_date());
                                int sNum = ep.getSeason_number();
                                if (d.getSeasons() != null) {
                                    for (TVShowDetailsResponse.Season s : d.getSeasons()) {
                                        if (s.getSeason_number() == sNum) {
                                            tv.setOnAirSeasonName(s.getName());
                                            break;
                                        }
                                    }
                                }
                            }
                            if (onAirTVAdapter != null) onAirTVAdapter.notifyItemChanged(idx);
                        }
                        @Override
                        public void onFailure(@NonNull Call<TVShowDetailsResponse> call, @NonNull Throwable t) {
                            Log.e("HomeFragment", "Failed to fetch details for on-air TV show id=" + tv.getTVShowId(), t);
                        }
                    });
        }
    }

    private void fetchUpcomingTVShows() {
        String region = PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getString("pref_region", "CA");
        safeEnqueue(apiService.getUpcomingTVShows(
                        TMDBpath.discoverTVShows(), TMDBpath.todayDate(), TMDBpath.thirtyDaysFromNow(),
                        "popularity.desc", false, "en-US", 1, region),
                new Callback<UpComingTVShowsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<UpComingTVShowsResponse> call,
                                           @NonNull Response<UpComingTVShowsResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<TVShowModel> raw = new ArrayList<>();
                            for (TVShowModel tv : response.body().getResults()) {
                                if (tv.getPosterPath() != null || tv.getBackdropPath() != null) raw.add(tv);
                            }
                            rawUpcomingTVShows = raw;
                            applyUpcomingTVFilter();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<UpComingTVShowsResponse> call, @NonNull Throwable t) {
                        Log.e("HomeFragment", "Failed to fetch upcoming TV shows", t);
                    }
                });
    }

    private void applyUpcomingTVFilter() {
        if (rawUpcomingTVShows == null || upComingTVAdapter == null) return;
        if (!onAirTVIdsFetched) return;
        LocalDate today = LocalDate.now();
        List<TVShowModel> filtered = new ArrayList<>();
        for (TVShowModel tv : rawUpcomingTVShows) {
            if (onAirTVIds.contains(tv.getTVShowId())) continue;
            String fd = tv.getFirstAirDate();
            if (fd != null && fd.length() == 10) {
                try {
                    if (!LocalDate.parse(fd).isAfter(today)) continue;
                } catch (Exception ignored) {}
            }
            filtered.add(tv);
        }
        List<TVShowModel> tvPreview = filtered.size() > 10 ? filtered.subList(0, 10) : filtered;
        upComingTVAdapter.updateData(tvPreview);
        if (filtered.size() > 10) {
            upComingTVAdapter.setShowMoreItem(true, () -> {
                if (isAdded()) navigateTo(SeeAllFragment.newInstance(
                        SeeAllFragment.TYPE_UPCOMING_TV, getString(R.string.home_section_coming_soon)));
            });
        }
        hideShimmer(shimmerUpcomingTV, rvUpcomingTvShows);
    }

    private void buildDots(int total) {
        if (llHeroDots.getChildCount() == total) return;
        llHeroDots.removeAllViews();
        for (int i = 0; i < total; i++) {
            View dot = new View(requireContext());
            dot.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.dot_inactive));
            int size = dpToPx(8);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(0, 0, dpToPx(6), 0);
            dot.setLayoutParams(params);
            llHeroDots.addView(dot);
        }
        activateDot(0);
    }

    private void activateDot(int active) {
        int activePx   = dpToPx(22);
        int inactivePx = dpToPx(8);

        for (int i = 0; i < llHeroDots.getChildCount(); i++) {
            View dot     = llHeroDots.getChildAt(i);
            int targetW  = (i == active) ? activePx : inactivePx;
            int targetBg = (i == active) ? R.drawable.dot_active : R.drawable.dot_inactive;

            ValueAnimator animator = ValueAnimator.ofInt(dot.getLayoutParams().width, targetW);
            animator.setDuration(300);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addUpdateListener(anim -> {
                ViewGroup.LayoutParams lp = dot.getLayoutParams();
                lp.width = (int) anim.getAnimatedValue();
                dot.setLayoutParams(lp);
            });
            animator.start();
            dot.setBackground(ContextCompat.getDrawable(requireContext(), targetBg));
        }
    }

    private void refreshBellBadge() {
        AuthManager auth = AuthManager.getInstance(requireContext());
        if (!auth.isLoggedIn() || tvBellBadge == null) return;
        new Thread(() -> {
            int count = getDbHelper().getUnreadReleasedCount(auth.getUserId());
            safeRunOnUiThread(() -> {
                if (tvBellBadge == null) return;
                if (count > 0) {
                    tvBellBadge.setText(count > 9 ? "9+" : String.valueOf(count));
                    tvBellBadge.setVisibility(View.VISIBLE);
                } else {
                    tvBellBadge.setVisibility(View.GONE);
                }
            });
        }).start();
    }

    public void onItemClicked(Object item, @Nullable View sharedElement) {
        Fragment fragment;
        String transitionName = null;
        if (item instanceof MovieModel) {
            fragment = MovieResultDetailsFragment.newInstance(((MovieModel) item).getMovieId());
            if (sharedElement != null) transitionName = "poster_transition";
        } else if (item instanceof TVShowModel) {
            fragment = TVShowResultDetailsFragment.newInstance(((TVShowModel) item).getTVShowId());
            if (sharedElement != null) transitionName = "poster_transition";
        } else {
            return;
        }
        navigateTo(fragment, sharedElement, transitionName);
    }

}
