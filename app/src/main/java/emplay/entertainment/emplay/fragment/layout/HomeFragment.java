package emplay.entertainment.emplay.fragment.layout;

import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.adapter.movie.TrendingBannerAdapter;
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
import emplay.entertainment.emplay.api.movie.MovieResponse;
import emplay.entertainment.emplay.models.movie.MovieModel;
import emplay.entertainment.emplay.adapter.tvshow.UpComingTVAdapter;
import emplay.entertainment.emplay.adapter.movie.UpcomingMovieAdapter;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Call;

public class HomeFragment extends BaseFragment {
    private ViewPager2 vpTrendingBanner;
    private RecyclerView rvWhatsNew, rvUpcomingMovies;
    private WhatsNewTVAdapter whatsNewTVAdapter;
    private WhatsNewMovieAdapter whatsNewMovieAdapter;
    private UpcomingMovieAdapter upcomingMovieAdapter;
    private UpComingTVAdapter upComingTVAdapter;
    private MovieApiService apiService;
    private TrendingBannerAdapter trendingAdapter;
    private Runnable bannerRunnable;
    private MaterialButton btnWhatsNewTvShow, btnWhatsNewMovie;
    private boolean isWhatsNewShowingTV = true;
    private final List<TVShowModel> cachedTVShows = new ArrayList<>();
    private final List<MovieModel> cachedMovies = new ArrayList<>();
    private final Handler bannerHandler = new Handler();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main, container, false);

        vpTrendingBanner = view.findViewById(R.id.vpTrendingBanner);
        btnWhatsNewTvShow = view.findViewById(R.id.btnWhatsNewTvShow);
        btnWhatsNewMovie = view.findViewById(R.id.btnWhatsNewMovie);

        // ViewPager2 for trending banner
        trendingAdapter = new TrendingBannerAdapter(getContext(), new ArrayList<>(), 
                this::onItemClicked, this::navigateToWatchlist);
        vpTrendingBanner.setAdapter(trendingAdapter);

        // Auto scroll setup
        setupAutoScroll();

        TextView tvWhatsNewSeeAll = view.findViewById(R.id.tvWhatsNewSeeAll);
        if (tvWhatsNewSeeAll != null) {
            tvWhatsNewSeeAll.setPaintFlags(tvWhatsNewSeeAll.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            tvWhatsNewSeeAll.setOnClickListener(v -> navigateTo(WhatsNewFragment.newInstance(isWhatsNewShowingTV)));
        }

        // What's New toggle
        rvWhatsNew = view.findViewById(R.id.rvWhatsNew);
        whatsNewTVAdapter = new WhatsNewTVAdapter(getContext(), new ArrayList<>(), this::onItemClicked);
        whatsNewMovieAdapter = new WhatsNewMovieAdapter(getContext(), new ArrayList<>(), this::onItemClicked);
        rvWhatsNew.setLayoutManager(new LinearLayoutManager(getContext()));
        rvWhatsNew.setNestedScrollingEnabled(false);
        rvWhatsNew.setAdapter(whatsNewTVAdapter);

        btnWhatsNewTvShow.setOnClickListener(v -> switchWhatsNew(true));
        btnWhatsNewMovie.setOnClickListener(v -> switchWhatsNew(false));

        // Upcoming Movies
        rvUpcomingMovies = view.findViewById(R.id.rvUpcomingMovies);
        upcomingMovieAdapter = new UpcomingMovieAdapter(getContext(), new ArrayList<>(), this::onItemClicked);
        rvUpcomingMovies.setAdapter(upcomingMovieAdapter);
        rvUpcomingMovies.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Upcoming TV Shows
        RecyclerView rvUpcomingTvShows = view.findViewById(R.id.rvUpcomingTvShows);
        upComingTVAdapter = new UpComingTVAdapter(getContext(), new ArrayList<>(), this::onItemClicked);
        rvUpcomingTvShows.setAdapter(upComingTVAdapter);
        rvUpcomingTvShows.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        apiService = ApiClient.getClient().create(MovieApiService.class);

        fetchHeroMovie();
        fetchWhatsNewTVShows();
        fetchWhatsNewMovies();
        fetchUpComingMovie();
        fetchUpcomingTVShows();

        return view;
    }

    private void setupAutoScroll() {
        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                if (trendingAdapter != null && trendingAdapter.getItemCount() > 0) {
                    int current = vpTrendingBanner.getCurrentItem();
                    int total = trendingAdapter.getItemCount();
                    vpTrendingBanner.setCurrentItem(current < total - 1 ? current + 1 : 0);
                }
                bannerHandler.postDelayed(this, 4000);
            }
        };
        bannerHandler.postDelayed(bannerRunnable, 4000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bannerHandler.removeCallbacks(bannerRunnable);
    }

    private void switchWhatsNew(boolean showTV) {
        if (isWhatsNewShowingTV == showTV) return;
        isWhatsNewShowingTV = showTV;
        rvWhatsNew.setAdapter(showTV ? whatsNewTVAdapter : whatsNewMovieAdapter);

        btnWhatsNewTvShow.setBackgroundTintList(ColorStateList.valueOf(showTV ? 0xFFE50914 : 0xFF1E1E1E));
        btnWhatsNewTvShow.setTextColor(showTV ? 0xFFFFFFFF : 0xFF888888);
        btnWhatsNewTvShow.setIconTint(ColorStateList.valueOf(showTV ? 0xFFFFFFFF : 0xFF888888));

        btnWhatsNewMovie.setBackgroundTintList(ColorStateList.valueOf(showTV ? 0xFF1E1E1E : 0xFFE50914));
        btnWhatsNewMovie.setTextColor(showTV ? 0xFF888888 : 0xFFFFFFFF);
        btnWhatsNewMovie.setIconTint(ColorStateList.valueOf(showTV ? 0xFF888888 : 0xFFFFFFFF));
    }

    /**
     * Data fetching
     */
    private void fetchHeroMovie() {
        safeEnqueue(apiService.getTrendingMovies(TMDBpath.trendingMovies()), new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().getResults().isEmpty()) {
                    List<MovieModel> results = response.body().getResults();
                    // Show top 5 movies in banner
                    List<MovieModel> bannerMovies = results.subList(0, Math.min(results.size(), 5));
                    trendingAdapter.updateData(bannerMovies);
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                Log.e("HomeFragment", "Failed to fetch hero movie", t);
            }
        });
    }

    private void fetchWhatsNewTVShows() {
        safeEnqueue(apiService.getTrendingTVShows(TMDBpath.trendingTVShows()), new Callback<TVShowResponse>() {
            @Override
            public void onResponse(@NonNull Call<TVShowResponse> call, @NonNull Response<TVShowResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cachedTVShows.clear();
                    for (TVShowModel tv : response.body().getResults()) {
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

    private void fetchWhatsNewMovies() {
        safeEnqueue(apiService.getTrendingMovies(TMDBpath.trendingMovies()), new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cachedMovies.clear();
                    for (MovieModel movie : response.body().getResults()) {
                        if (movie.getPosterPath() != null) cachedMovies.add(movie);
                    }
                    // Limit to 4 items for the Home screen
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

    private void navigateToWatchlist() {
        navigateTo(WatchlistFragment.newInstance(0));
    }
}
