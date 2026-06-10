package emplay.entertainment.emplay.fragment.layout;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

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

import java.util.Locale;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import emplay.entertainment.emplay.adapter.movie.TrendingBannerAdapter;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.api.tvshow.TVShowResponse;
import emplay.entertainment.emplay.api.movie.UpComingMovieResponse;
import emplay.entertainment.emplay.api.tvshow.UpComingTVShowsResponse;
import emplay.entertainment.emplay.fragment.common.BaseFragment;
import emplay.entertainment.emplay.fragment.common.WhatsNewFragment;
import emplay.entertainment.emplay.fragment.details.MovieResultDetailsFragment;
import emplay.entertainment.emplay.fragment.details.TVShowResultDetailsFragment;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;
import emplay.entertainment.emplay.adapter.movie.WhatsNewMovieAdapter;
import emplay.entertainment.emplay.adapter.tvshow.WhatsNewTVAdapter;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.ApiClient;
import emplay.entertainment.emplay.api.common.MovieApiService;
import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.tool.BadgeHelper;
import emplay.entertainment.emplay.api.movie.MovieResponse;
import emplay.entertainment.emplay.models.movie.MovieModel;
import emplay.entertainment.emplay.adapter.tvshow.UpComingTVAdapter;
import emplay.entertainment.emplay.adapter.movie.UpcomingMovieAdapter;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import emplay.entertainment.emplay.api.tvshow.TVShowProviderResponse;
import emplay.entertainment.emplay.models.common.RegionProvidersModel;

import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Call;

public class HomeFragment extends BaseFragment {
    private ViewPager2 vpTrendingBanner;  // ViewPager2 that hosts the hero banner cards
    private LinearLayout llHeroDots;
    private TextSwitcher tvHeroTitle;
    private TextView tvHeroYear, tvHeroRating;
    private RecyclerView rvWhatsNew;
    private WhatsNewTVAdapter whatsNewTVAdapter;
    private WhatsNewMovieAdapter whatsNewMovieAdapter;
    private UpcomingMovieAdapter upcomingMovieAdapter;
    private UpComingTVAdapter upComingTVAdapter;
    private MovieApiService apiService;
    private TrendingBannerAdapter trendingAdapter;
    private MaterialButton btnWhatsNewTvShow, btnWhatsNewMovie;
    private View heroSection;
    private android.graphics.drawable.GradientDrawable heroSectionFill; // The inner fill layer of the hero background — animated to match poster palette color
    private int heroBgColor = 0xFF111111; // Tracks current animated background color for status bar sync and animator start value
    private final android.util.SparseIntArray bannerColors = new android.util.SparseIntArray(); // Cache of palette-extracted colors keyed by banner position — avoids re-extracting on swipe back
    private boolean isWhatsNewShowingTV = true;
    private final List<TVShowModel> cachedTVShows = new ArrayList<>();
    private final List<MovieModel> cachedMovies = new ArrayList<>();
    private final List<MovieModel> nowPlayingPool = new ArrayList<>(); // Source pool for the hero banner — populated once from API, never reshuffled after initial load
    private final Random random = new Random();
    private long lastFetchTime = 0;
    private static final long CACHE_DURATION_MS = 30 * 60 * 1000; // 30 minutes

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_view, container, false);

        heroSection = view.findViewById(R.id.heroSection);


        // Layer 1 is the solid fill — layer 0 is typically a border or shadow
        heroSectionFill = (GradientDrawable) ContextCompat.getDrawable(
                requireContext(), R.drawable.bg_hero_section).mutate();
        heroSection.setBackground(heroSectionFill);

        heroSection.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(View view, android.graphics.Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), dpToPx(24));
            }
        });
        heroSection.setClipToOutline(true);

        vpTrendingBanner = view.findViewById(R.id.vpTrendingBanner);
        btnWhatsNewTvShow = view.findViewById(R.id.btnWhatsNewTvShow);
        btnWhatsNewMovie  = view.findViewById(R.id.btnWhatsNewMovie);

        // Set up adapter with empty list — data is loaded asynchronously via fetchNowPlayingMovies()
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(requireActivity());
        trendingAdapter = new TrendingBannerAdapter(
                requireContext(),
                new ArrayList<>(),
                dbHelper,
                new TrendingBannerAdapter.CardGestureListener() {
                    @Override
                    public void onSingleTap(MovieModel movie) {
                        onItemClicked(movie);
                    }

                    @Override
                    public void onDoubleTap(MovieModel movie) {
                        trendingAdapter.triggerWatchlist(movie);
                    }
                }
        );
        vpTrendingBanner.setAdapter(trendingAdapter);

        // When Palette finishes extracting a color for a banner position, cache it.
        // If the user is already on that page, animate the background immediately.
        trendingAdapter.setPaletteColorListener((position, color) -> {
            bannerColors.put(position, color);
            if (position == vpTrendingBanner.getCurrentItem()) {
                animateBackground(color);
            }
        });

        llHeroDots  = view.findViewById(R.id.llHeroDots);
        tvHeroTitle = view.findViewById(R.id.tvHeroTitle);
        tvHeroYear  = view.findViewById(R.id.tvHeroYear);
        tvHeroRating = view.findViewById(R.id.tvHeroRating);

        // TextSwitcher factory — each call to setText() cross-fades between two TextView instances
        tvHeroTitle.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView tv = new TextView(requireContext());
                tv.setTextSize(22f);
                tv.setTypeface(androidx.core.content.res.ResourcesCompat
                        .getFont(requireContext(), R.font.bebas_neue));
                tv.setTextColor(Color.WHITE);
                tv.setGravity(Gravity.CENTER);
                tv.setLayoutParams(new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT));
                return tv;
            }
        });
        // Fade in/out animations for the title when the banner page changes
        AlphaAnimation heroInAnim = new AlphaAnimation(0f, 1f);
        heroInAnim.setDuration(200);
        AlphaAnimation heroOutAnim = new AlphaAnimation(1f, 0f);
        heroOutAnim.setDuration(200);
        tvHeroTitle.setInAnimation(heroInAnim);
        tvHeroTitle.setOutAnimation(heroOutAnim);

        // Keep adjacent pages in memory so swipe feels instant (no reload on swipe back)
        vpTrendingBanner.setOffscreenPageLimit(2);

        // Custom page transformer: stack/fan effect with scale + parallax + depth
        vpTrendingBanner.setPageTransformer((page, position) -> {
            float absPos   = Math.abs(position);
            float pageWidth = page.getWidth();
            if (pageWidth == 0f) return;

            // Parallax: move page slower than default to create depth
            page.setTranslationX(-pageWidth * position * 0.9f);
            // Z ordering: active page floats above neighbors
            page.setTranslationZ(1f - absPos);
            // Scale: active page is full size, neighbors shrink to 88%
            float t = Math.max(0f, 1f - absPos);
            page.setScaleX(0.88f + 0.12f * t);
            page.setScaleY(0.88f + 0.12f * t);
            page.setAlpha(absPos <= 2f ? 1f : 0f);

            // Watchlist button fades out quickly on non-active pages
            // so it only appears clearly on the centered card
            View btn = page.findViewById(R.id.btnHeroWatchlist);
            if (btn != null) {
                btn.setAlpha(Math.max(0f, 1f - absPos * 4f));
                btn.setVisibility(absPos < 0.5f ? View.VISIBLE : View.INVISIBLE);
            }
        });

        vpTrendingBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            // Fade out meta text while scrolling between pages for a cleaner transition
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                float dist  = Math.min(positionOffset, 1f - positionOffset);
                float alpha = Math.max(0.1f, 1f - dist * 3f);
                tvHeroTitle.setAlpha(alpha);
                tvHeroYear.setAlpha(alpha);
                tvHeroRating.setAlpha(alpha);
            }

            // Update title, meta, dots, and background when a new page is selected
            @Override
            public void onPageSelected(int position) {
                MovieModel movie = trendingAdapter.getMovie(position);
                if (movie == null) return;

                // Restore full opacity after scroll transition
                tvHeroTitle.setAlpha(1f);
                tvHeroYear.setAlpha(1f);
                tvHeroRating.setAlpha(1f);

                activateDot(position);
                tvHeroTitle.setText(movie.getTitle());

                // Build "Language • dd/mm/yyyy" format
                String lang = movie.getOriginalLanguage();
                String date = movie.getReleaseDate();
                String formatted = (date != null && date.length() == 10)
                        ? date.substring(8, 10) + "/" + date.substring(5, 7) + "/" + date.substring(0, 4)
                        : "";
                String langStr = (lang != null) ? lang.toUpperCase(Locale.ROOT) : "";
                tvHeroYear.setText((!langStr.isEmpty() && !formatted.isEmpty())
                        ? langStr + " • " + formatted : langStr + formatted);

                tvHeroRating.setText(getString(R.string.rating_format, movie.getVoteAverage()));

                // Use cached palette color if already extracted, otherwise wait for Palette callback
                int cached = bannerColors.get(position, 0);
                if (cached != 0) animateBackground(cached);
            }
        });

        int touchSlop = ViewConfiguration.get(requireContext()).getScaledTouchSlop();

        GestureDetector overlayDetector = new GestureDetector(requireContext(),
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onDown(@NonNull MotionEvent e) { return true; }

                    @Override
                    public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                        MovieModel movie = trendingAdapter.getMovie(vpTrendingBanner.getCurrentItem());
                        if (movie != null) onItemClicked(movie);
                        return true;
                    }

                    @Override
                    public boolean onDoubleTap(@NonNull MotionEvent e) {
                        MovieModel movie = trendingAdapter.getMovie(vpTrendingBanner.getCurrentItem());
                        if (movie != null) trendingAdapter.triggerWatchlist(movie);
                        return true;
                    }
                });

        // Single touch listener on heroSection covers the entire hero area —
// VP region lets ViewPager2 handle natively, overlay region uses fakeDrag
        heroSection.setOnTouchListener(new View.OnTouchListener() {
            private float startX, startY, lastX;
            private boolean dragging;
            private boolean forwardToVP;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // On DOWN, check if touch landed inside vpTrendingBanner bounds
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    int[] vpLoc = new int[2];
                    vpTrendingBanner.getLocationOnScreen(vpLoc);
                    float absX = event.getRawX();
                    float absY = event.getRawY();
                    forwardToVP = absX >= vpLoc[0]
                            && absX <= vpLoc[0] + vpTrendingBanner.getWidth()
                            && absY >= vpLoc[1]
                            && absY <= vpLoc[1] + vpTrendingBanner.getHeight();
                }

                // Touch is inside VP area — don't intercept, let ViewPager2 + card handle it
                if (forwardToVP) return false;

                // Touch is in overlay area (dots, title, meta, badge, fade view)
                // Feed to gesture detector for tap/double-tap detection
                overlayDetector.onTouchEvent(event);

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = lastX = event.getX();
                        startY = event.getY();
                        dragging = false;
                        // Consume DOWN to keep receiving MOVE + UP
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getX() - startX;
                        float dy = event.getY() - startY;
                        // Start fake drag when horizontal motion exceeds touchSlop
                        if (!dragging && Math.abs(dx) > Math.abs(dy)
                                && Math.abs(dx) > touchSlop) {
                            dragging = true;
                            if (!vpTrendingBanner.isFakeDragging())
                                vpTrendingBanner.beginFakeDrag();
                        }
                        if (dragging && vpTrendingBanner.isFakeDragging()) {
                            vpTrendingBanner.fakeDragBy(event.getX() - lastX);
                            lastX = event.getX();
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (dragging && vpTrendingBanner.isFakeDragging())
                            vpTrendingBanner.endFakeDrag();
                        dragging = false;
                        return true;
                }
                return false;
            }
        });

        TextView tvWhatsNewSeeAll = view.findViewById(R.id.tvWhatsNewSeeAll);
        if (tvWhatsNewSeeAll != null) {
            tvWhatsNewSeeAll.setPaintFlags(
                    tvWhatsNewSeeAll.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            tvWhatsNewSeeAll.setOnClickListener(
                    v -> navigateTo(WhatsNewFragment.newInstance(isWhatsNewShowingTV)));
        }

        apiService = ApiClient.getClient().create(MovieApiService.class);

        rvWhatsNew = view.findViewById(R.id.rvWhatsNew);
        whatsNewTVAdapter    = new WhatsNewTVAdapter(requireContext(), new ArrayList<>(), apiService, this::onItemClicked);
        whatsNewMovieAdapter = new WhatsNewMovieAdapter(requireContext(), new ArrayList<>(), this::onItemClicked);
        rvWhatsNew.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvWhatsNew.setNestedScrollingEnabled(false);
        rvWhatsNew.setAdapter(isWhatsNewShowingTV ? whatsNewTVAdapter : whatsNewMovieAdapter);

        btnWhatsNewTvShow.setOnClickListener(v -> switchWhatsNew(true));
        btnWhatsNewMovie.setOnClickListener(v -> switchWhatsNew(false));
        applyWhatsNewButtonState(isWhatsNewShowingTV);

        RecyclerView rvUpcomingMovies = view.findViewById(R.id.rvUpcomingMovies);
        upcomingMovieAdapter = new UpcomingMovieAdapter(requireContext(), new ArrayList<>(), this::onItemClicked);
        rvUpcomingMovies.setAdapter(upcomingMovieAdapter);
        rvUpcomingMovies.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvUpcomingMovies.setHasFixedSize(true);

        RecyclerView rvUpcomingTvShows = view.findViewById(R.id.rvUpcomingTvShows);
        upComingTVAdapter = new UpComingTVAdapter(requireContext(), new ArrayList<>(), this::onItemClicked);
        rvUpcomingTvShows.setAdapter(upComingTVAdapter);
        rvUpcomingTvShows.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvUpcomingTvShows.setHasFixedSize(true);

        RecyclerView.RecycledViewPool sharedPool = new RecyclerView.RecycledViewPool();
        rvUpcomingMovies.setRecycledViewPool(sharedPool);
        rvUpcomingTvShows.setRecycledViewPool(sharedPool);

        // heroSection extends behind the transparent status bar (edge-to-edge).
        // Push the banner content below the status bar; the animated background shows through.
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            heroSection.setPadding(0, bars.top, 0, 0);
            return new WindowInsetsCompat.Builder(insets)
                    .setInsets(
                            WindowInsetsCompat.Type.systemBars(),
                            Insets.of(bars.left, 0, bars.right, bars.bottom)
                    )
                    .build();
        });
        fetchTrendingMovies();
        fetchWhatsNewTVShows();
        fetchUpComingMovie();
        fetchUpcomingTVShows();

        return view;
    }

    // Animates heroSectionFill and status bar color from current to target palette color
    @Override
    protected void animateBackground(int toColor) {
        if (heroSectionFill == null || !isAdded()) return;
        ValueAnimator animator = ValueAnimator.ofArgb(heroBgColor, toColor);
        animator.setDuration(500);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a -> {
            int color = (int) a.getAnimatedValue();
            if (heroSectionFill != null) heroSectionFill.setColor(color);
            heroBgColor = color;
        });
        animator.start();
    }

    // Resets background to near-black when leaving the hero section (e.g. on destroy)
    @Override
    protected void resetBackground() {
        heroBgColor = 0xFF0A0A0A;
        if (heroSectionFill != null) heroSectionFill.setColor(heroBgColor);
    }

    @Override
    public void onResume() {
        super.onResume();
        long now = System.currentTimeMillis();
        if (nowPlayingPool.isEmpty() || (now - lastFetchTime) > CACHE_DURATION_MS) {
            nowPlayingPool.clear();
            bannerColors.clear();
            trendingAdapter.updateData(new ArrayList<>());
            fetchNowPlayingMovies();
            lastFetchTime = now;
        } else {
            showNowPlayingBanner();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        resetBackground();
        // Null out view references to avoid memory leaks after the view hierarchy is destroyed
        heroSection         = null;
        heroSectionFill     = null;
    }

    private void switchWhatsNew(boolean showTV) {
        if (isWhatsNewShowingTV != showTV) {
            isWhatsNewShowingTV = showTV;
            rvWhatsNew.setAdapter(showTV ? whatsNewTVAdapter : whatsNewMovieAdapter);
        }
        applyWhatsNewButtonState(showTV);
    }

    // Updates button tint and text color to reflect active/inactive toggle state
    private void applyWhatsNewButtonState(boolean showTV) {
        btnWhatsNewTvShow.setBackgroundTintList(ColorStateList.valueOf(showTV ? 0xFFE50914 : 0xFF1E1E1E));
        btnWhatsNewTvShow.setTextColor(showTV ? 0xFFFFFFFF : 0xFF888888);
        btnWhatsNewTvShow.setIconTint(ColorStateList.valueOf(showTV ? 0xFFFFFFFF : 0xFF888888));

        btnWhatsNewMovie.setBackgroundTintList(ColorStateList.valueOf(showTV ? 0xFF1E1E1E : 0xFFE50914));
        btnWhatsNewMovie.setTextColor(showTV ? 0xFF888888 : 0xFFFFFFFF);
        btnWhatsNewMovie.setIconTint(ColorStateList.valueOf(showTV ? 0xFF888888 : 0xFFFFFFFF));
    }

    // Fetches now-playing movies, date-filters them, then checks each for streaming providers.
    // Only movies with NO flatrate streaming in the user's region reach the hero banner.
    private void fetchNowPlayingMovies() {
        safeEnqueue(apiService.getNowPlayingMovies(TMDBpath.nowPlayingMovies()),
                new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MovieResponse> call,
                                           @NonNull Response<MovieResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<MovieModel> results = response.body().getResults();
                            if (results == null || results.isEmpty()) return;
                            List<MovieModel> candidates = new ArrayList<>();
                            LocalDate today = LocalDate.now();
                            for (MovieModel m : results) {
                                if (m.getPosterPath() == null) continue;
                                String rd = m.getReleaseDate();
                                if (rd == null || rd.isEmpty()) continue;
                                try {
                                    long days = ChronoUnit.DAYS.between(LocalDate.parse(rd), today);
                                    if (days < 0 || days > 45) continue;
                                } catch (Exception ignored) {
                                    continue;
                                }
                                candidates.add(m);
                            }
                            if (!candidates.isEmpty()) filterByTheatersOnly(candidates);
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                        Log.e("HomeFragment", "Failed to fetch now playing movies", t);
                    }
                });
    }

    // For each candidate, check if it has flatrate streaming providers in the user's region.
    // Movies without flatrate providers are still exclusively in theaters.
    private void filterByTheatersOnly(List<MovieModel> candidates) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String region = prefs.getString("pref_region", "US");
        AtomicInteger remaining = new AtomicInteger(candidates.size());
        List<MovieModel> theaterOnly = Collections.synchronizedList(new ArrayList<>());

        for (MovieModel movie : candidates) {
            safeEnqueue(apiService.getMovieProviders(TMDBpath.movieProvider(movie.getMovieId())),
                    new Callback<TVShowProviderResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<TVShowProviderResponse> call,
                                               @NonNull Response<TVShowProviderResponse> response) {
                            boolean hasDigital = false;
                            if (response.isSuccessful() && response.body() != null) {
                                Map<String, RegionProvidersModel> providerMap = response.body().getResults();
                                if (providerMap != null && providerMap.containsKey(region)) {
                                    RegionProvidersModel regionData = providerMap.get(region);
                                    if (regionData != null) {
                                        // Filter: subscription + rent + buy
                                        boolean hasFlatrate = regionData.getFlatrate() != null
                                                && !regionData.getFlatrate().isEmpty();
                                        boolean hasRent = regionData.getRent() != null
                                                && !regionData.getRent().isEmpty();
                                        boolean hasBuy = regionData.getBuy() != null
                                                && !regionData.getBuy().isEmpty();
                                        hasDigital = hasFlatrate || hasRent || hasBuy;
                                    }
                                }
                            }
                            if (!hasDigital) theaterOnly.add(movie);
                            onProviderCheckDone(remaining, candidates, theaterOnly);
                        }

                        @Override
                        public void onFailure(@NonNull Call<TVShowProviderResponse> call,
                                              @NonNull Throwable t) {
                            theaterOnly.add(movie);
                            onProviderCheckDone(remaining, candidates, theaterOnly);
                        }
                    });
        }
    }

    private void onProviderCheckDone(AtomicInteger remaining, List<MovieModel> candidates,
                                     List<MovieModel> theaterOnly) {
        if (remaining.decrementAndGet() != 0) return;
        nowPlayingPool.clear();
        for (MovieModel m : candidates) { // preserve original now_playing order
            if (theaterOnly.contains(m)) {
                nowPlayingPool.add(m);
                if (nowPlayingPool.size() == 6) break;
            }
        }
        if (!nowPlayingPool.isEmpty()) showNowPlayingBanner();
    }

    // Randomizes order and loads banner — called once from fetchNowPlayingMovies,
    // and from onResume only if the adapter is still empty
    private void showNowPlayingBanner() {
        if (nowPlayingPool.isEmpty() || !isAdded()) return;
        bannerColors.clear();

        // Randomly promote one movie to position 0 for variety on each app launch
        List<MovieModel> ordered = new ArrayList<>(nowPlayingPool);
        Collections.swap(ordered, 0, random.nextInt(ordered.size()));

        trendingAdapter.updateData(ordered);
        buildDots(ordered.size());
        vpTrendingBanner.setCurrentItem(0, false);

        // Set initial meta without animation (setCurrentText bypasses TextSwitcher transition)
        MovieModel first = ordered.get(0);
        tvHeroTitle.setCurrentText(first.getTitle());

        // Convert TMDB date format "yyyy-MM-dd" → "dd/MM/yyyy"
        String date = first.getReleaseDate();
        String formatted = (date != null && date.length() == 10)
                ? date.substring(8, 10) + "/" + date.substring(5, 7) + "/" + date.substring(0, 4)
                : "";

        // Build "EN • 15/03/2024" — omit separator if either part is missing
        String lang    = first.getOriginalLanguage();
        String langStr = (lang != null) ? lang.toUpperCase(Locale.ROOT) : "";
        tvHeroYear.setText((!langStr.isEmpty() && !formatted.isEmpty())
                ? langStr + " • " + formatted : langStr + formatted);

        tvHeroRating.setText(getString(R.string.rating_format, first.getVoteAverage()));
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
                            cachedMovies.clear();
                            // Only include movies released within the last 30 days
                            for (MovieModel movie : results) {
                                if (movie.getPosterPath() != null
                                        && BadgeHelper.isNotOlderThan(movie.getReleaseDate(), 30)) {
                                    cachedMovies.add(movie);
                                }
                            }
                            List<MovieModel> limited = cachedMovies.subList(
                                    0, Math.min(cachedMovies.size(), 4));
                            whatsNewMovieAdapter.updateData(new ArrayList<>(limited));
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                        Log.e("HomeFragment", "Failed to fetch trending movies", t);
                    }
                });
    }

    private void fetchWhatsNewTVShows() {
        safeEnqueue(apiService.getTrendingTVShows(TMDBpath.trendingTVShows()),
                new Callback<TVShowResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TVShowResponse> call,
                                           @NonNull Response<TVShowResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<TVShowModel> results = response.body().getResults();
                            if (results == null) return;
                            cachedTVShows.clear();
                            for (TVShowModel tv : results) {
                                if (tv.getPosterPath() != null) cachedTVShows.add(tv);
                            }
                            // Limit to 4 items for the Home screen
                            List<TVShowModel> limited = cachedTVShows.subList(
                                    0, Math.min(cachedTVShows.size(), 4));
                            whatsNewTVAdapter.updateData(new ArrayList<>(limited));
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<TVShowResponse> call, @NonNull Throwable t) {
                        Log.e("HomeFragment", "Failed to fetch trending TV shows", t);
                    }
                });
    }

    private void fetchUpComingMovie() {
        safeEnqueue(apiService.getUpcomingMovies(
                        TMDBpath.discoverMovies(), TMDBpath.todayDate(), TMDBpath.thirtyDaysFromNow(),
                        "popularity.desc", false, "en-US", 1),
                new Callback<UpComingMovieResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<UpComingMovieResponse> call,
                                           @NonNull Response<UpComingMovieResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<MovieModel> filtered = new ArrayList<>();
                            for (MovieModel movie : response.body().getResults()) {
                                if (movie.getPosterPath() != null) filtered.add(movie);
                            }
                            upcomingMovieAdapter.updateData(filtered);
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<UpComingMovieResponse> call, @NonNull Throwable t) {
                        Log.e("HomeFragment", "Failed to fetch upcoming movies", t);
                    }
                });
    }

    private void fetchUpcomingTVShows() {
        safeEnqueue(apiService.getUpcomingTVShows(
                        TMDBpath.discoverTVShows(), TMDBpath.todayDate(), TMDBpath.thirtyDaysFromNow(),
                        "popularity.desc", false, "en-US", 1),
                new Callback<UpComingTVShowsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<UpComingTVShowsResponse> call,
                                           @NonNull Response<UpComingTVShowsResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<TVShowModel> filtered = new ArrayList<>();
                            for (TVShowModel tv : response.body().getResults()) {
                                if (tv.getPosterPath() != null) filtered.add(tv);
                            }
                            upComingTVAdapter.updateData(filtered);
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<UpComingTVShowsResponse> call, @NonNull Throwable t) {
                        Log.e("HomeFragment", "Failed to fetch upcoming TV shows", t);
                    }
                });
    }

    // Rebuilds dot indicators when the banner data changes — skips if count is unchanged
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

    // Animates the active dot to a wider pill shape and resets all others to circle
    private void activateDot(int active) {
        int activePx   = dpToPx(22);
        int inactivePx = dpToPx(8);

        for (int i = 0; i < llHeroDots.getChildCount(); i++) {
            View dot     = llHeroDots.getChildAt(i);
            int targetW  = (i == active) ? activePx : inactivePx;
            int targetBg = (i == active) ? R.drawable.dot_active : R.drawable.dot_inactive;

            // Smoothly animate width change so the dot pill grows/shrinks
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


    // Routes item clicks to the correct detail fragment based on model type
    public void onItemClicked(Object item) {
        Fragment fragment;
        if (item instanceof MovieModel) {
            fragment = MovieResultDetailsFragment.newInstance(((MovieModel) item).getMovieId());
        } else if (item instanceof TVShowModel) {
            fragment = TVShowResultDetailsFragment.newInstance(((TVShowModel) item).getTVShowId());
        } else {
            return;
        }
        navigateTo(fragment);
    }
}
