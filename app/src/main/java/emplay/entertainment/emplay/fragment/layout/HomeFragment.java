package emplay.entertainment.emplay.fragment.layout;

import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.Locale;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

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
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Call;

public class HomeFragment extends BaseFragment {
    private ViewPager2 vpTrendingBanner;
    private LinearLayout llHeroDots;
    private TextSwitcher tvHeroTitle;
    private TextView tvHeroYear, tvHeroRating;
    private RecyclerView rvWhatsNew, rvUpcomingMovies;
    private WhatsNewTVAdapter whatsNewTVAdapter;
    private WhatsNewMovieAdapter whatsNewMovieAdapter;
    private UpcomingMovieAdapter upcomingMovieAdapter;
    private UpComingTVAdapter upComingTVAdapter;
    private MovieApiService apiService;
    private TrendingBannerAdapter trendingAdapter;
    private MaterialButton btnWhatsNewTvShow, btnWhatsNewMovie;
    private View heroBannerContainer;
    private int heroBgColor = 0xFF111111;
    private final android.util.SparseIntArray bannerColors = new android.util.SparseIntArray();
    private boolean isWhatsNewShowingTV = true;
    private final List<TVShowModel> cachedTVShows = new ArrayList<>();
    private final List<MovieModel> cachedMovies = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_view, container, false);

        heroBannerContainer = view.findViewById(R.id.heroBannerContainer);
        vpTrendingBanner = view.findViewById(R.id.vpTrendingBanner);
        btnWhatsNewTvShow = view.findViewById(R.id.btnWhatsNewTvShow);
        btnWhatsNewMovie = view.findViewById(R.id.btnWhatsNewMovie);

        // ViewPager2 for trending banner
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(requireActivity());
        trendingAdapter = new TrendingBannerAdapter(requireContext(), new ArrayList<>(),
                dbHelper, this::onItemClicked);
        vpTrendingBanner.setAdapter(trendingAdapter);
        trendingAdapter.setPaletteColorListener((position, color) -> {
            bannerColors.put(position, color);
            if (position == vpTrendingBanner.getCurrentItem()) {
                animateBackground(color);
            }
        });


        // Hero banner overlays
        llHeroDots = view.findViewById(R.id.llHeroDots);
        tvHeroTitle = view.findViewById(R.id.tvHeroTitle);
        tvHeroYear = view.findViewById(R.id.tvHeroYear);
        tvHeroRating = view.findViewById(R.id.tvHeroRating);

        tvHeroTitle.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView tv = new TextView(requireContext());
                tv.setTextSize(22f);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setTextColor(Color.WHITE);
                tv.setGravity(Gravity.CENTER);
                tv.setLayoutParams(new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
                return tv;
            }
        });
        AlphaAnimation heroInAnim = new AlphaAnimation(0f, 1f);
        heroInAnim.setDuration(200);
        AlphaAnimation heroOutAnim = new AlphaAnimation(1f, 0f);
        heroOutAnim.setDuration(200);
        tvHeroTitle.setInAnimation(heroInAnim);
        tvHeroTitle.setOutAnimation(heroOutAnim);

        vpTrendingBanner.setOffscreenPageLimit(2);
        vpTrendingBanner.setPageTransformer((page, position) -> {
            float absPos = Math.abs(position);
            float pageWidth = page.getWidth();
            if (pageWidth == 0f) return;
            page.setTranslationX(-pageWidth * position * 0.9f);
            page.setTranslationZ(1f - absPos);
            float t = Math.max(0f, 1f - absPos);
            page.setScaleX(0.88f + 0.12f * t);
            page.setScaleY(0.88f + 0.12f * t);
            page.setAlpha(absPos <= 2f ? 1f : 0f);
            // Watchlist button only visible on the active center card
            View btn = page.findViewById(R.id.btnHeroWatchlist);
            if (btn != null) {
                btn.setAlpha(Math.max(0f, 1f - absPos * 4f));
                btn.setVisibility(absPos < 0.5f ? View.VISIBLE : View.INVISIBLE);
            }
        });
        vpTrendingBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                float dist = Math.min(positionOffset, 1f - positionOffset);
                float alpha = Math.max(0.1f, 1f - dist * 3f);
                tvHeroTitle.setAlpha(alpha);
                tvHeroYear.setAlpha(alpha);
                tvHeroRating.setAlpha(alpha);
            }

            public void onPageSelected(int position) {
                MovieModel movie = trendingAdapter.getMovie(position);
                if (movie == null) return;
                tvHeroTitle.setAlpha(1f);
                tvHeroYear.setAlpha(1f);
                tvHeroRating.setAlpha(1f);
                activateDot(position);
                tvHeroTitle.setText(movie.getTitle());
                tvHeroYear.setText(movie.getOriginalLanguage().toUpperCase(Locale.ROOT));
                tvHeroRating.setText(getString(R.string.rating_format, movie.getVoteAverage()));
                int cached = bannerColors.get(position, 0);
                if (cached != 0) animateBackground(cached);
            }
        });

        TextView tvWhatsNewSeeAll = view.findViewById(R.id.tvWhatsNewSeeAll);
        if (tvWhatsNewSeeAll != null) {
            tvWhatsNewSeeAll.setPaintFlags(tvWhatsNewSeeAll.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            tvWhatsNewSeeAll.setOnClickListener(v -> navigateTo(WhatsNewFragment.newInstance(isWhatsNewShowingTV)));
        }

        apiService = ApiClient.getClient().create(MovieApiService.class);

        // What's New toggle
        rvWhatsNew = view.findViewById(R.id.rvWhatsNew);
        whatsNewTVAdapter = new WhatsNewTVAdapter(requireContext(), new ArrayList<>(), apiService, this::onItemClicked);
        whatsNewMovieAdapter = new WhatsNewMovieAdapter(requireContext(), new ArrayList<>(), this::onItemClicked);
        rvWhatsNew.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvWhatsNew.setNestedScrollingEnabled(false);
        rvWhatsNew.setAdapter(isWhatsNewShowingTV ? whatsNewTVAdapter : whatsNewMovieAdapter);

        btnWhatsNewTvShow.setOnClickListener(v -> switchWhatsNew(true));
        btnWhatsNewMovie.setOnClickListener(v -> switchWhatsNew(false));
        applyWhatsNewButtonState(isWhatsNewShowingTV);

        // Upcoming Movies
        rvUpcomingMovies = view.findViewById(R.id.rvUpcomingMovies);
        upcomingMovieAdapter = new UpcomingMovieAdapter(requireContext(), new ArrayList<>(), this::onItemClicked);
        rvUpcomingMovies.setAdapter(upcomingMovieAdapter);
        rvUpcomingMovies.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        // Upcoming TV Shows
        RecyclerView rvUpcomingTvShows = view.findViewById(R.id.rvUpcomingTvShows);
        upComingTVAdapter = new UpComingTVAdapter(requireContext(), new ArrayList<>(), this::onItemClicked);
        rvUpcomingTvShows.setAdapter(upComingTVAdapter);
        rvUpcomingTvShows.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        fetchTrendingMovies();
        fetchWhatsNewTVShows();
        fetchUpComingMovie();
        fetchUpcomingTVShows();

        return view;
    }

    @Override
    protected void animateBackground(int toColor) {
        if (heroBannerContainer == null || !isAdded()) return;
        ValueAnimator animator = ValueAnimator.ofArgb(heroBgColor, toColor);
        animator.setDuration(500);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a -> {
            int color = (int) a.getAnimatedValue();
            if (heroBannerContainer != null) heroBannerContainer.setBackgroundColor(color);
            heroBgColor = color;
        });
        animator.start();
    }

    @Override
    protected void resetBackground() {
        heroBgColor = 0xFF111111;
        if (heroBannerContainer != null) heroBannerContainer.setBackgroundColor(heroBgColor);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        resetBackground();
        heroBannerContainer = null;
    }

    private void switchWhatsNew(boolean showTV) {
        if (isWhatsNewShowingTV != showTV) {
            isWhatsNewShowingTV = showTV;
            rvWhatsNew.setAdapter(showTV ? whatsNewTVAdapter : whatsNewMovieAdapter);
        }
        applyWhatsNewButtonState(showTV);
    }

    private void applyWhatsNewButtonState(boolean showTV) {
        btnWhatsNewTvShow.setBackgroundTintList(ColorStateList.valueOf(showTV ? 0xFFE50914 : 0xFF1E1E1E));
        btnWhatsNewTvShow.setTextColor(showTV ? 0xFFFFFFFF : 0xFF888888);
        btnWhatsNewTvShow.setIconTint(ColorStateList.valueOf(showTV ? 0xFFFFFFFF : 0xFF888888));

        btnWhatsNewMovie.setBackgroundTintList(ColorStateList.valueOf(showTV ? 0xFF1E1E1E : 0xFFE50914));
        btnWhatsNewMovie.setTextColor(showTV ? 0xFF888888 : 0xFFFFFFFF);
        btnWhatsNewMovie.setIconTint(ColorStateList.valueOf(showTV ? 0xFF888888 : 0xFFFFFFFF));
    }

    private void fetchTrendingMovies() {
        safeEnqueue(apiService.getTrendingMovies(TMDBpath.trendingMovies()), new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MovieModel> results = response.body().getResults();
                    if (results == null || results.isEmpty()) return;
                    trendingAdapter.updateData(new ArrayList<>(results.subList(0, Math.min(results.size(), 5))));
                    buildDots(trendingAdapter.getItemCount());
                    MovieModel first = trendingAdapter.getMovie(0);
                    if (first != null) {
                        tvHeroTitle.setCurrentText(first.getTitle());
                        tvHeroYear.setText(first.getOriginalLanguage().toUpperCase(Locale.ROOT));
                        tvHeroRating.setText(getString(R.string.rating_format, first.getVoteAverage()));
                    }
                    cachedMovies.clear();
                    for (MovieModel movie : results) {
                        if (movie.getPosterPath() != null && BadgeHelper.isNotOlderThan(movie.getReleaseDate(), 30)) {
                            cachedMovies.add(movie);
                        }
                    }
                    List<MovieModel> limited = cachedMovies.subList(0, Math.min(cachedMovies.size(), 4));
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
        safeEnqueue(apiService.getTrendingTVShows(TMDBpath.trendingTVShows()), new Callback<TVShowResponse>() {
            @Override
            public void onResponse(@NonNull Call<TVShowResponse> call, @NonNull Response<TVShowResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TVShowModel> results = response.body().getResults();
                    if (results == null) return;
                    cachedTVShows.clear();
                    for (TVShowModel tv : results) {
                        if (tv.getPosterPath() != null) cachedTVShows.add(tv);
                    }
                    // Limit to 4 items for the Home screen
                    List<TVShowModel> limited = cachedTVShows.subList(0, Math.min(cachedTVShows.size(), 4));
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
                    public void onResponse(@NonNull Call<UpComingMovieResponse> call, @NonNull Response<UpComingMovieResponse> response) {
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
                    public void onResponse(@NonNull Call<UpComingTVShowsResponse> call, @NonNull Response<UpComingTVShowsResponse> response) {
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
            View dot = llHeroDots.getChildAt(i);
            int targetW  = (i == active) ? activePx : inactivePx;
            int targetBg = (i == active) ? R.drawable.dot_active : R.drawable.dot_inactive;

            // Animate width: current → target
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

    // Helper
    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

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
