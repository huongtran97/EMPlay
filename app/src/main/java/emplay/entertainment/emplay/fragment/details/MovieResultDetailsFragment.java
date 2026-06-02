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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.activity.TrailerActivity;
import emplay.entertainment.emplay.adapter.common.CastAdapter;
import emplay.entertainment.emplay.adapter.movie.SuggestionMovieAdapter;
import emplay.entertainment.emplay.api.tvshow.TVShowProviderResponse;
import emplay.entertainment.emplay.api.tvshow.TVShowsTrailerResponses;
import emplay.entertainment.emplay.models.common.RegionProvidersModel;
import emplay.entertainment.emplay.api.common.ApiClient;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.api.common.MovieApiService;
import emplay.entertainment.emplay.api.movie.MovieCreditsResponse;
import emplay.entertainment.emplay.api.movie.MovieDetailsResponse;
import emplay.entertainment.emplay.api.movie.MovieSimilarResponse;
import emplay.entertainment.emplay.api.movie.MoviesTrailerResponses;
import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.databinding.SearchResultMovieViewBinding;
import emplay.entertainment.emplay.fragment.common.BaseFragment;
import emplay.entertainment.emplay.models.common.CastModel;
import emplay.entertainment.emplay.models.movie.MovieModel;
import emplay.entertainment.emplay.tool.LanguageMapper;
import emplay.entertainment.emplay.tool.PaginationHelper;
import emplay.entertainment.emplay.tool.ReadHelper;
import emplay.entertainment.emplay.tool.UiUtils;
import emplay.entertainment.emplay.tool.WatchProviderHelper;
import emplay.entertainment.emplay.database.WatchlistHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieResultDetailsFragment extends BaseFragment {
    private static final String ARG_MOVIE_ID = "MOVIE_ID";
    private static final int PAGE_SIZE = 9;
    private SearchResultMovieViewBinding binding;
    private int movieId;
    private final List<CastModel> castList = new ArrayList<>();
    private final List<MovieModel> allSuggestions = new ArrayList<>();
    private List<MoviesTrailerResponses.TrailerModel> trailers = new ArrayList<>();
    private CastAdapter castAdapter;
    private SuggestionMovieAdapter suggestionMovieAdapter;
    private MovieApiService apiService;
    private DatabaseHelper databaseHelper;
    private FirebaseAuth mAuth;
    private Map<String, RegionProvidersModel> watchProviderResults;
    private PaginationHelper<MovieModel> paginationHelper;

    public static MovieResultDetailsFragment newInstance(int movieId) {
        MovieResultDetailsFragment fragment = new MovieResultDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MOVIE_ID, movieId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SearchResultMovieViewBinding.inflate(inflater, container, false);

        databaseHelper = DatabaseHelper.getInstance(requireActivity());
        mAuth = FirebaseAuth.getInstance();
        apiService = ApiClient.getClient().create(MovieApiService.class);

        castAdapter = new CastAdapter(castList, requireActivity(), cast -> navigateTo(CastDetailFragment.newInstance(cast.getId())));
        suggestionMovieAdapter = new SuggestionMovieAdapter(new ArrayList<>(), getContext(), movie -> {
            if (movie != null) navigateTo(MovieResultDetailsFragment.newInstance(movie.getMovieId()));
        });

        binding.searchResultCastRecyclerview.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.searchResultCastRecyclerview.setAdapter(castAdapter);

        binding.searchResultSuggestionRecyclerview.setLayoutManager(new GridLayoutManager(getContext(), 3));
        binding.searchResultSuggestionRecyclerview.setAdapter(suggestionMovieAdapter);
        binding.searchResultSuggestionRecyclerview.setNestedScrollingEnabled(false);

        paginationHelper = new PaginationHelper<>(PAGE_SIZE, allSuggestions, new PaginationHelper.PaginationCallback<MovieModel>() {
            @Override
            public void onPageUpdated(List<MovieModel> pageItems) {
                suggestionMovieAdapter.updateData(pageItems);
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

        binding.movieInfo.btnBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        if (getArguments() != null) {
            movieId = getArguments().getInt(ARG_MOVIE_ID, -1);
            if (movieId != -1) {
                fetchMovieDetails();
                fetchCastList();
                fetchSuggestionList();
                fetchTrailers();
                fetchWatchProviders();
            }
        }

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().getWindow().setStatusBarColor(Color.parseColor("#0D0D12"));
    }

    @Override
    public void onPause() {
        super.onPause();
        requireActivity().getWindow().setStatusBarColor(Color.BLACK);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cancelCountdown();
        binding = null;
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void bindMovieDetails(MovieDetailsResponse mDetails) {
        if (binding == null) return;

        // Title & share
        binding.movieInfo.movieTitle.setText(mDetails.getTitle());
        binding.movieInfo.btnShare.setOnClickListener(v -> {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, mDetails.getTitle());
            startActivity(Intent.createChooser(share, "Share via"));
        });

        // Rating
        binding.movieInfo.tvPosterRating.setText(String.format("★ %.1f", mDetails.getVoteAverage()));

        // Images
        String backdropPath = mDetails.getBackdropPath();
        Glide.with(this)
                .load(backdropPath != null && !backdropPath.isEmpty()
                        ? ImageUrl.BACKDROP + backdropPath
                        : ImageUrl.POSTER + mDetails.getPosterPath())
                .error(R.drawable.placeholder_image)
                .into(binding.movieInfo.backdropView);

        // Released date only display "year"
        String rd = mDetails.getReleaseDate();
        binding.movieInfo.movieReleaseDate.setText((rd != null && rd.length() >= 4) ? rd.substring(0, 4) : "");

        // Language
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.movieInfo.movieLanguage.setText("Language: " + LanguageMapper.getLanguageName(mDetails.getOriginalLanguage()));
        }

        // Runtime
        binding.movieInfo.movieRuntime.setText(mDetails.getRuntime() + " min");

        // Production country
        List<MovieDetailsResponse.ProductionCountry> countries = mDetails.getProduction_countries();
        if (countries != null && !countries.isEmpty()) {
            String countryNames = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                countryNames = countries.stream()
                        .map(MovieDetailsResponse.ProductionCountry::getName)
                        .collect(Collectors.joining(", "));
            }
            binding.movieInfo.movieProductCountry.setText(countryNames);
        } else {
            binding.movieInfo.movieProductCountry.setVisibility(View.GONE);
        }

        // Genre chips
        List<String> genres = new ArrayList<>();
        for (MovieDetailsResponse.Genre g : mDetails.getGenres()) genres.add(g.getName());

        buildGenreChips(binding.movieInfo.movieInfoGenres, genres);

        // Overview and read more/less
        binding.movieInfo.searchResultOverview.setText(mDetails.getOverview());
        ReadHelper.setup(binding.movieInfo.searchResultOverview, binding.movieInfo.readMoreText, false, null);

        // Images
        Glide.with(this)
                .load(ImageUrl.POSTER + mDetails.getPosterPath())
                .error(R.drawable.placeholder_image)
                .into(binding.movieInfo.imageView);

        // Watchlist button
        updateWatchlistButton(mDetails);

        // Coming soon / released visibility
        applyReleaseVisibility(
                mDetails.getReleaseDate(),
                binding.movieInfo.wtwReleased.getRoot(),
                binding.movieInfo.wtwUnreleased.getRoot(),
                binding.movieInfo.comingSoonBadge,
                () -> startCountdown(LocalDate.parse(mDetails.getReleaseDate()),
                        binding.movieInfo.wtwUnreleased, () -> {
                            binding.movieInfo.wtwUnreleased.getRoot().setVisibility(View.GONE);
                            binding.movieInfo.wtwReleased.getRoot().setVisibility(View.VISIBLE);
                            fetchWatchProviders();
                        })
        );
        // Blurred background
        UiUtils.setupBlurredBackground(this, mDetails.getBackdropPath(), mDetails.getPosterPath(), binding.searchResultFragment);
    }

    private void updateWatchlistButton(MovieDetailsResponse movieDetails) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            binding.movieInfo.addToLibraryBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_watchlist, 0, 0, 0);
            binding.movieInfo.addToLibraryBtn.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "You must be logged in to save it!", Toast.LENGTH_SHORT).show());
        } else {
            String userId = currentUser.getUid();
            updateSaveBtnState(userId, movieDetails.getId());

            binding.movieInfo.addToLibraryBtn.setOnClickListener(v -> {
                if (WatchlistHelper.isMovieSaved(databaseHelper, userId, movieDetails.getId())) {
                    WatchlistHelper.removeMovie(databaseHelper, userId, movieDetails.getId());
                    updateSaveBtnState(userId, movieDetails.getId());
                    Toast.makeText(requireContext(), "Movie removed from library", Toast.LENGTH_SHORT).show();
                } else {
                    StringBuilder genresBuilder = new StringBuilder();
                    if (movieDetails.getGenres() != null) {
                        for (MovieDetailsResponse.Genre genre : movieDetails.getGenres()) {
                            if (genresBuilder.length() > 0) genresBuilder.append(",");
                            genresBuilder.append(genre.getName());
                        }
                    }
                    String genresString = genresBuilder.toString();
                    long result = WatchlistHelper.saveMovie(databaseHelper, userId, movieDetails.getId(), 
                            movieDetails.getTitle(), movieDetails.getPosterPath(), genresString, movieDetails.getVoteAverage());
                    if (result != -1) {
                        updateSaveBtnState(userId, movieDetails.getId());
                        Toast.makeText(requireContext(), "Movie added to library", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to add Movie", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void updateSaveBtnState(String userId, int id) {
        int iconRes = WatchlistHelper.isMovieSaved(databaseHelper, userId, id) ? R.drawable.ic_check : R.drawable.ic_watchlist;
        binding.movieInfo.addToLibraryBtn.setIcon(ContextCompat.getDrawable(requireContext(), iconRes));
    }

    private void updateTrailerButton(List<MoviesTrailerResponses.TrailerModel> trailers) {
        this.trailers = trailers != null ? trailers : new ArrayList<>();
        binding.movieInfo.trailerBtn.setOnClickListener(v -> {
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
        binding.movieInfo.wtwReleased.tvRegion.setText(region);
        binding.movieInfo.wtwReleased.layoutWtwEmpty.setVisibility(View.GONE);
        binding.movieInfo.wtwReleased.rvProviders.setVisibility(View.VISIBLE);
        binding.movieInfo.wtwReleased.tabLayoutWtw.setVisibility(View.VISIBLE);
        RegionProvidersModel providers = watchProviderResults.get(region);
        if (providers != null) {
            WatchProviderHelper.setupProviderTabs(requireContext(),
                    binding.movieInfo.wtwReleased.tabLayoutWtw,
                    binding.movieInfo.wtwReleased.rvProviders, providers);
        } else {
            handleNoWatchProviders();
        }
    }

    private void handleNoWatchProviders() {
        handleNoWatchProviders(
                binding.movieInfo.wtwReleased.layoutWtwEmpty,
                binding.movieInfo.wtwReleased.rvProviders,
                binding.movieInfo.wtwReleased.tabLayoutWtw
        );
    }

    private void fetchMovieDetails() {
        safeEnqueue(apiService.getMovieDetails(TMDBpath.movieDetails(movieId)), new Callback<MovieDetailsResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieDetailsResponse> call, @NonNull Response<MovieDetailsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    bindMovieDetails(response.body());
                } else if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to retrieve movie details", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<MovieDetailsResponse> call, @NonNull Throwable t) {
                if (getContext() != null) Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTrailers() {
        safeEnqueue(apiService.getMoviesTrailer(TMDBpath.movieTrailer(movieId)), new Callback<MoviesTrailerResponses>() {
            @Override
            public void onResponse(@NonNull Call<MoviesTrailerResponses> call, @NonNull Response<MoviesTrailerResponses> response) {
                if (response.isSuccessful()) {
                    MoviesTrailerResponses trailerResponses = response.body();
                    List<MoviesTrailerResponses.TrailerModel> allTrailers = trailerResponses != null ? trailerResponses.getResults() : null;

                    if (allTrailers == null || allTrailers.isEmpty()) {
                        updateTrailerButton(new ArrayList<>());
                        return;
                    }

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
                    updateTrailerButton(filtered);
                }
            }

            @Override
            public void onFailure(@NonNull Call<MoviesTrailerResponses> call, @NonNull Throwable t) {
                Log.e("MovieResultDetails", "Trailer fetch failed", t);
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchCastList() {
        safeEnqueue(apiService.getMovieCredits(TMDBpath.movieCredits(movieId)), new Callback<MovieCreditsResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieCreditsResponse> call, @NonNull Response<MovieCreditsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MovieCreditsResponse.Cast> raw = response.body().getCast();
                    if (raw != null) {
                        castList.clear();
                        for (MovieCreditsResponse.Cast c : raw) {
                            castList.add(new CastModel(c.getId(), c.getCastName(), c.getProfilePath(), c.getCharacter()));
                        }
                        castAdapter.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<MovieCreditsResponse> call, @NonNull Throwable t) {
                if (getContext() != null) Toast.makeText(getContext(), "Error fetching cast: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchSuggestionList() {
        safeEnqueue(apiService.getMovieSimilar(TMDBpath.movieSimilar(movieId)), new Callback<MovieSimilarResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieSimilarResponse> call, @NonNull Response<MovieSimilarResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MovieModel> results = response.body().getResults();
                    List<MovieModel> suggestions = new ArrayList<>();
                    if (results != null) {
                        for (MovieModel m : results) {
                            if (m.getPosterPath() != null) suggestions.add(m);
                        }
                    }
                    paginationHelper.updateData(suggestions);
                }
            }
            @Override
            public void onFailure(@NonNull Call<MovieSimilarResponse> call, @NonNull Throwable t) {
                if (getContext() != null) Toast.makeText(getContext(), "Failed to load recommendations", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchWatchProviders() {
        String defaultRegion = Locale.getDefault().getCountry();
        safeEnqueue(apiService.getMovieProviders(TMDBpath.movieProvider(movieId)), new Callback<TVShowProviderResponse>() {
            @Override
            public void onResponse(@NonNull Call<TVShowProviderResponse> call, @NonNull Response<TVShowProviderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    watchProviderResults = response.body().getResults();
                    String region = watchProviderResults.containsKey(defaultRegion)
                            ? defaultRegion
                            : watchProviderResults.isEmpty() ? null : watchProviderResults.keySet().iterator().next();
                    if (region != null) {
                        applyWatchProviders(region);
                        binding.movieInfo.wtwReleased.btnRegion.setOnClickListener(v -> 
                                WatchProviderHelper.showRegionPicker(requireContext(), watchProviderResults, MovieResultDetailsFragment.this::applyWatchProviders));
                    } else {
                        handleNoWatchProviders();
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<TVShowProviderResponse> call, @NonNull Throwable t) {
                Log.e("MovieDetails", "Provider fetch failed", t);
                handleNoWatchProviders();
            }
        });
    }
}
