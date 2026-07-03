package emplay.entertainment.emplay.fragment.details;

import android.content.Intent;
import android.net.Uri;
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
import androidx.transition.TransitionInflater;

import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import android.graphics.drawable.GradientDrawable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.CastAdapter;
import emplay.entertainment.emplay.adapter.common.ProviderAdapter;
import emplay.entertainment.emplay.adapter.movie.CollectionAdapter;
import emplay.entertainment.emplay.adapter.movie.SuggestionMovieAdapter;
import emplay.entertainment.emplay.api.auth.TMDBWatchlistApiService;
import emplay.entertainment.emplay.api.auth.model.TMDBAccountStatesResponse;
import emplay.entertainment.emplay.api.auth.model.TMDBWatchlistRequest;
import emplay.entertainment.emplay.api.auth.model.TMDBWatchlistStatusResponse;
import emplay.entertainment.emplay.api.common.ApiClient;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.api.common.MovieApiService;
import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.api.motn.MotnShowResponse;
import emplay.entertainment.emplay.api.movie.CollectionResponse;
import emplay.entertainment.emplay.api.tvshow.TVShowProviderResponse;
import emplay.entertainment.emplay.api.movie.MovieCreditsResponse;
import emplay.entertainment.emplay.api.movie.MovieDetailsResponse;
import emplay.entertainment.emplay.api.movie.MovieReleaseDatesResponse;
import emplay.entertainment.emplay.api.movie.MovieSimilarResponse;
import emplay.entertainment.emplay.api.movie.MoviesTrailerResponses;
import emplay.entertainment.emplay.auth.AuthManager;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.database.WatchlistHelper;
import emplay.entertainment.emplay.databinding.ActivityDetailMovieBinding;
import emplay.entertainment.emplay.databinding.WtwReleasedViewBinding;
import emplay.entertainment.emplay.fragment.common.BaseFragment;
import emplay.entertainment.emplay.fragment.common.SeeAllFragment;
import emplay.entertainment.emplay.tool.MotnHelper;
import emplay.entertainment.emplay.tool.ReadHelper;
import emplay.entertainment.emplay.tool.WatchProviderHelper;
import emplay.entertainment.emplay.models.common.CastModel;
import emplay.entertainment.emplay.models.common.ProviderModel;
import emplay.entertainment.emplay.models.common.RegionProvidersModel;
import emplay.entertainment.emplay.models.movie.MovieModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieResultDetailsFragment extends BaseFragment {
    private static final String ARG_MOVIE_ID = "MOVIE_ID";

    private ActivityDetailMovieBinding binding;
    private int movieId;
    private final List<CastModel> castList = new ArrayList<>();
    private CastAdapter castAdapter;
    private SuggestionMovieAdapter alsoLikeAdapter;
    private CollectionAdapter collectionAdapter;
    private boolean isCollectionExpanded = false;
    private Map<String, RegionProvidersModel> watchProviderResults;
    private MotnShowResponse cachedMotn;
    private List<MoviesTrailerResponses.TrailerModel> trailers = new ArrayList<>();
    private MovieApiService apiService;
    private TMDBWatchlistApiService watchlistApiService;
    private DatabaseHelper databaseHelper;
    private boolean tmdbMovieInWatchlist = false;
    private boolean isNowPlaying = false;
    private boolean userHasSelectedRegion = false;
    private String currentWtwRegion = WatchProviderHelper.defaultRegion();
    private android.os.Handler fetchHandler;
    private final ArrayList<String> movieGenreNames = new ArrayList<>();

    public static MovieResultDetailsFragment newInstance(int movieId) {
        MovieResultDetailsFragment fragment = new MovieResultDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MOVIE_ID, movieId);
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
        binding = ActivityDetailMovieBinding.inflate(inflater, container, false);

        apiService = ApiClient.getClient().create(MovieApiService.class);
        watchlistApiService = ApiClient.getClient().create(TMDBWatchlistApiService.class);
        databaseHelper = DatabaseHelper.getInstance(requireContext());

        castAdapter = new CastAdapter(castList, requireContext(),
                cast -> navigateTo(CastDetailFragment.newInstance(cast.getId())));
        binding.rvCast.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvCast.setAdapter(castAdapter);

        binding.tvAllCast.setOnClickListener(v ->
                navigateTo(SeeAllFragment.newInstance(SeeAllFragment.TYPE_CAST_MOVIE, movieId,
                        getString(R.string.detail_section_cast_crew))));

        alsoLikeAdapter = new SuggestionMovieAdapter(new ArrayList<>(), requireContext(),
                (movie, view) -> navigateTo(MovieResultDetailsFragment.newInstance(movie.getMovieId()),
                        view, "poster_transition"));
        binding.searchResultSuggestionRecyclerview.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.searchResultSuggestionRecyclerview.setAdapter(alsoLikeAdapter);

        collectionAdapter = new CollectionAdapter(new ArrayList<>(), requireContext(),
                id -> navigateTo(MovieResultDetailsFragment.newInstance(id)));
        binding.rvCollection.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCollection.setAdapter(collectionAdapter);

        binding.layoutCollectionHeader.setOnClickListener(v -> toggleCollectionDropdown());


        if (getArguments() != null) {
            movieId = getArguments().getInt(ARG_MOVIE_ID, -1);
            if (movieId != -1) {
                fetchMovieDetails();
                fetchHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                fetchHandler.postDelayed(this::fetchCastList,       150);
                fetchHandler.postDelayed(this::fetchSuggestionList, 300);
                fetchHandler.postDelayed(this::fetchTrailers,       450);
                fetchHandler.postDelayed(this::fetchCertification,  600);
                fetchHandler.postDelayed(this::fetchMovieProviders, 750);
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
            backdropParams.height = dpToPx(200) + statusBarHeight;
            binding.backdropContainer.setLayoutParams(backdropParams);


            return insets;
        });

        binding.btnFindTheaters.setOnClickListener(v -> {
            String title = binding.tvMovieTitle.getText().toString();
            String query = Uri.encode("theaters showing " + title);
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/search?q=" + query)));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (fetchHandler != null) {
            fetchHandler.removeCallbacksAndMessages(null);
            fetchHandler = null;
        }
        binding = null;
    }

    private void fetchMovieDetails() {
        safeEnqueue(apiService.getMovieDetails(TMDBpath.movieDetails(movieId)),
                new Callback<MovieDetailsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MovieDetailsResponse> call,
                                           @NonNull Response<MovieDetailsResponse> response) {
                        if (response.isSuccessful() && response.body() != null)
                            bindMovieDetails(response.body());
                    }
                    @Override public void onFailure(@NonNull Call<MovieDetailsResponse> call,
                                                    @NonNull Throwable t) {}
                });
    }

    private void fetchCollectionCount(int collectionId) {
        safeEnqueue(apiService.getCollectionDetails(TMDBpath.collectionDetails(collectionId)),
                new Callback<CollectionResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CollectionResponse> call,
                                           @NonNull Response<CollectionResponse> response) {
                        if (binding == null || !response.isSuccessful() || response.body() == null) return;
                        List<CollectionResponse.Part> parts = response.body().getParts();
                        int count = parts != null ? parts.size() : 0;
                        if (count > 0) {
                            binding.tvCollectionCount.setText(
                                    String.format(java.util.Locale.getDefault(), "%d movie%s", count, count == 1 ? "" : "s"));
                            collectionAdapter.updateData(parts);
                        }
                    }
                    @Override public void onFailure(@NonNull Call<CollectionResponse> call,
                                                    @NonNull Throwable t) {}
                });
    }

    private void toggleCollectionDropdown() {
        isCollectionExpanded = !isCollectionExpanded;
        androidx.transition.TransitionManager.beginDelayedTransition(
                (android.view.ViewGroup) binding.cardCollection);
        binding.rvCollection.setVisibility(isCollectionExpanded ? View.VISIBLE : View.GONE);
        binding.ivCollectionChevron.animate()
                .rotation(isCollectionExpanded ? 90f : 0f)
                .setDuration(200)
                .start();
    }

    private void bindMovieDetails(MovieDetailsResponse d) {
        if (binding == null) return;

        Glide.with(requireContext())
                .load(ImageUrl.of(ImageUrl.BACKDROP, d.getBackdropPath()))
                .placeholder(R.drawable.bg_poster_placeholder)
                .into(binding.imgBackdrop);

        Glide.with(requireContext())
                .load(ImageUrl.of(ImageUrl.POSTER, d.getPosterPath()))
                .placeholder(R.drawable.bg_poster_placeholder)
                .into(binding.imgPoster);

        binding.tvMovieTitle.setText(d.getTitle());
        binding.chipRating.setText(String.format(java.util.Locale.getDefault(),
                "★ %.1f", d.getVoteAverage()));
        binding.tvOverview.setText(d.getOverview());
        ReadHelper.setup(binding.tvOverview, binding.readMoreText, false, expanded -> {});

        List<MovieDetailsResponse.Genre> genres = d.getGenres();
        movieGenreNames.clear();
        if (genres != null && !genres.isEmpty()) {
            binding.chipGenre.setText(genres.get(0).getName());
            binding.chipGenre.setVisibility(View.VISIBLE);
            for (MovieDetailsResponse.Genre g : genres) movieGenreNames.add(g.getName());
        } else {
            binding.chipGenre.setVisibility(View.GONE);
        }

        int runtime = d.getRuntime();
        if (runtime > 0) {
            binding.chipRuntime.setText(String.format(java.util.Locale.getDefault(),
                    "%dh %dm", runtime / 60, runtime % 60));
        }

        String rd = d.getReleaseDate();
        binding.chipReleaseDate.setText((rd != null && rd.length() >= 4) ? rd.substring(0, 4) : "");
        List<MovieDetailsResponse.ProductionCountry> countries = d.getProduction_countries();
        if (countries != null && !countries.isEmpty()) {
            binding.chipCountry.setText(countries.get(0).getName());
        }


        if (rd != null && !rd.isEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                LocalDate releaseDate = LocalDate.parse(rd);
                boolean isUnreleased = !releaseDate.isBefore(LocalDate.now());
                long daysOut = java.time.temporal.ChronoUnit.DAYS.between(releaseDate, LocalDate.now());
                isNowPlaying = !isUnreleased && daysOut <= 45;
                binding.tvNowInTheatersBadge.setVisibility(isNowPlaying ? View.VISIBLE : View.GONE);
                binding.btnFindTheaters.setVisibility(isNowPlaying ? View.VISIBLE : View.GONE);
                binding.wtwReleased.getRoot().setVisibility((!isUnreleased && !isNowPlaying) ? View.VISIBLE : View.GONE);
                binding.wtwUnreleased.getRoot().setVisibility(isUnreleased ? View.VISIBLE : View.GONE);
                if (isUnreleased) {
                    startCountdown(releaseDate, binding.wtwUnreleased, () -> {
                        if (binding == null) return;
                        binding.wtwUnreleased.getRoot().setVisibility(View.GONE);
                        binding.wtwReleased.getRoot().setVisibility(View.VISIBLE);
                    });
                }
            } catch (Exception ignored) {}
        }

        MovieDetailsResponse.BelongsToCollection collection = d.getBelongsToCollection();
        if (collection != null && collection.getName() != null) {
            binding.tvCollectionName.setText(collection.getName());
            binding.cardCollection.setVisibility(View.VISIBLE);
            fetchCollectionCount(collection.getId());
        }

        updateWatchlistButton(d);
    }

    private void updateWatchlistButton(MovieDetailsResponse movieDetails) {
        AuthManager auth = AuthManager.getInstance(requireContext());
        if (auth.getAuthType() == AuthManager.AuthType.TMDB) {
            setupTMDBMovieWatchlistButton(auth, movieDetails);
        } else if (auth.getAuthType() == AuthManager.AuthType.GOOGLE) {
            setupGoogleMovieWatchlistButton(auth.getUserId(), movieDetails);
        } else {
            binding.btnMyList.setOnClickListener(v -> showLoginPromptDialog());
        }
    }

    private void setupGoogleMovieWatchlistButton(String userId, MovieDetailsResponse movieDetails) {
        updateSaveBtnState(userId, movieDetails.getId());
        binding.btnMyList.setOnClickListener(v -> new Thread(() -> {
            boolean wasSaved = WatchlistHelper.isMovieSaved(databaseHelper, userId, movieDetails.getId());
            if (wasSaved) {
                WatchlistHelper.removeMovie(databaseHelper, userId, movieDetails.getId());
                safeRunOnUiThread(() -> {
                    if (binding == null) return;
                    applyMyListState(false);
                    Toast.makeText(requireContext(), "Movie removed from library", Toast.LENGTH_SHORT).show();
                });
            } else {
                String genresString = buildGenresString(movieDetails.getGenres());
                boolean saved = WatchlistHelper.saveMovie(databaseHelper, userId,
                        movieDetails.getId(), movieDetails.getTitle(),
                        movieDetails.getPosterPath(), genresString,
                        movieDetails.getVoteAverage()) != -1;
                safeRunOnUiThread(() -> {
                    if (binding == null) return;
                    if (saved) {
                        applyMyListState(true);
                        Toast.makeText(requireContext(), "Movie added to library", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to add Movie", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start());
    }

    private void setupTMDBMovieWatchlistButton(AuthManager auth, MovieDetailsResponse movieDetails) {
        binding.btnMyList.setEnabled(false);
        safeEnqueue(watchlistApiService.getAccountStates(
                TMDBpath.movieAccountStates(movieDetails.getId()), auth.getTMDBSessionId()),
                new Callback<TMDBAccountStatesResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TMDBAccountStatesResponse> call,
                                           @NonNull Response<TMDBAccountStatesResponse> response) {
                        if (binding == null) return;
                        tmdbMovieInWatchlist = response.isSuccessful()
                                && response.body() != null && response.body().watchlist;
                        applyTMDBMovieWatchlistState(auth, movieDetails.getId());
                    }
                    @Override
                    public void onFailure(@NonNull Call<TMDBAccountStatesResponse> call,
                                          @NonNull Throwable t) {
                        if (binding != null) binding.btnMyList.setEnabled(true);
                    }
                });
    }

    private void applyTMDBMovieWatchlistState(AuthManager auth, int mediaId) {
        if (binding == null) return;
        applyMyListState(tmdbMovieInWatchlist);
        binding.btnMyList.setEnabled(true);
        binding.btnMyList.setOnClickListener(v -> toggleTMDBMovieWatchlist(auth, mediaId));
    }

    private void toggleTMDBMovieWatchlist(AuthManager auth, int mediaId) {
        binding.btnMyList.setEnabled(false);
        boolean addToWatchlist = !tmdbMovieInWatchlist;
        safeEnqueue(watchlistApiService.updateWatchlist(
                TMDBpath.accountAddToWatchlist(auth.getTMDBAccountId()),
                auth.getTMDBSessionId(),
                new TMDBWatchlistRequest("movie", mediaId, addToWatchlist)),
                new Callback<TMDBWatchlistStatusResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TMDBWatchlistStatusResponse> call,
                                           @NonNull Response<TMDBWatchlistStatusResponse> response) {
                        if (binding == null) return;
                        if (response.isSuccessful() && response.body() != null) {
                            tmdbMovieInWatchlist = addToWatchlist;
                            applyMyListState(tmdbMovieInWatchlist);
                            Toast.makeText(requireContext(),
                                    addToWatchlist ? "Added to TMDB watchlist" : "Removed from TMDB watchlist",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Failed to update watchlist", Toast.LENGTH_SHORT).show();
                        }
                        binding.btnMyList.setEnabled(true);
                    }
                    @Override
                    public void onFailure(@NonNull Call<TMDBWatchlistStatusResponse> call,
                                          @NonNull Throwable t) {
                        if (binding != null) {
                            Toast.makeText(requireContext(), "Failed to update watchlist", Toast.LENGTH_SHORT).show();
                            binding.btnMyList.setEnabled(true);
                        }
                    }
                });
    }

    private void applyMyListState(boolean isSaved) {
        if (binding == null || !isAdded()) return;
        int accentColor = ContextCompat.getColor(requireContext(), R.color.accent);
        int onAccent    = ContextCompat.getColor(requireContext(), R.color.on_accent);
        int textColor1  = ContextCompat.getColor(requireContext(), R.color.text_1);
        float radius    = 8 * getResources().getDisplayMetrics().density;

        if (isSaved) {
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setCornerRadius(radius);
            bg.setColor(accentColor);
            binding.btnMyList.setBackground(bg);
        } else {
            binding.btnMyList.setBackgroundResource(R.drawable.ticket_tear_line);
        }

        binding.tvMyList.setText(isSaved ? R.string.detail_saved : R.string.detail_btn_mylist);
        binding.tvMyList.setTextColor(isSaved ? onAccent : textColor1);
        binding.tvAdmitOne.setVisibility(isSaved ? View.GONE : View.VISIBLE);
    }

    private void updateSaveBtnState(String userId, int id) {
        new Thread(() -> {
            boolean saved = WatchlistHelper.isMovieSaved(databaseHelper, userId, id);
            safeRunOnUiThread(() -> {
                if (binding == null) return;
                applyMyListState(saved);
            });
        }).start();
    }

    private String buildGenresString(List<MovieDetailsResponse.Genre> genres) {
        if (genres == null || genres.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < genres.size(); i++) {
            sb.append(genres.get(i).getName());
            if (i < genres.size() - 1) sb.append(",");
        }
        return sb.toString();
    }

    private void fetchCastList() {
        safeEnqueue(apiService.getMovieCredits(TMDBpath.movieCredits(movieId)),
                new Callback<MovieCreditsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MovieCreditsResponse> call,
                                           @NonNull Response<MovieCreditsResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<CastModel> newCast = new ArrayList<>();
                            for (MovieCreditsResponse.Cast c : response.body().getCast()) {
                                newCast.add(new CastModel(c.getId(), c.getCastName(),
                                        c.getProfilePath(), c.getCharacter()));
                            }
                            castAdapter.updateData(newCast);
                        }
                    }
                    @Override public void onFailure(@NonNull Call<MovieCreditsResponse> call,
                                                    @NonNull Throwable t) {}
                });
    }

    private void fetchSuggestionList() {
        safeEnqueue(apiService.getMovieSimilar(TMDBpath.movieSimilar(movieId)),
                new Callback<MovieSimilarResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MovieSimilarResponse> call,
                                           @NonNull Response<MovieSimilarResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<MovieModel> results = new ArrayList<>(response.body().getResults());
                            alsoLikeAdapter.updateData(results.subList(0, Math.min(10, results.size())));
                            if (binding != null && results.size() > 10) {
                                alsoLikeAdapter.setShowMoreItem(true,
                                        () -> navigateTo(SeeAllFragment.newInstance(
                                                SeeAllFragment.TYPE_SIMILAR_MOVIE, movieId,
                                                getString(R.string.detail_section_also_like),
                                                movieGenreNames)));
                            }
                        }
                    }
                    @Override public void onFailure(@NonNull Call<MovieSimilarResponse> call,
                                                    @NonNull Throwable t) {}
                });
    }

    private void fetchTrailers() {
        safeEnqueue(apiService.getMoviesTrailer(TMDBpath.movieTrailer(movieId)),
                new Callback<MoviesTrailerResponses>() {
                    @Override
                    public void onResponse(@NonNull Call<MoviesTrailerResponses> call,
                                           @NonNull Response<MoviesTrailerResponses> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            trailers = response.body().getResults();
                            updateTrailerButton();
                        }
                    }
                    @Override public void onFailure(@NonNull Call<MoviesTrailerResponses> call,
                                                    @NonNull Throwable t) {}
                });
    }

    private void updateTrailerButton() {
        if (binding == null) return;
        binding.btnWatchTrailer.setOnClickListener(v -> {
            MoviesTrailerResponses.TrailerModel pick = pickOfficialTrailer();
            if (pick != null) {
                String title = binding.tvMovieTitle.getText().toString();
                TrailerBottomSheetFragment.newInstance(pick.getKey(), pick.getName())
                        .show(getChildFragmentManager(), "trailer");
            } else {
                Toast.makeText(getContext(), "No trailer available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private MoviesTrailerResponses.TrailerModel pickOfficialTrailer() {
        if (trailers == null || trailers.isEmpty()) return null;
        MoviesTrailerResponses.TrailerModel officialTrailer = null;
        MoviesTrailerResponses.TrailerModel finalTrailer = null;
        MoviesTrailerResponses.TrailerModel anyTrailer = null;
        MoviesTrailerResponses.TrailerModel anyTeaser = null;
        for (MoviesTrailerResponses.TrailerModel t : trailers) {
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

    private void fetchCertification() {
        safeEnqueue(apiService.getMovieReleaseDates(TMDBpath.movieReleaseDates(movieId)),
                new Callback<MovieReleaseDatesResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MovieReleaseDatesResponse> call,
                                           @NonNull Response<MovieReleaseDatesResponse> response) {
                        if (binding == null || !response.isSuccessful() || response.body() == null) return;
                        String cert = parseCaCertification(response.body());
                        if (cert != null && !cert.isEmpty()) {
                            binding.chipCert.setText(cert);
                            binding.chipCert.setVisibility(View.VISIBLE);
                        }
                    }
                    @Override public void onFailure(@NonNull Call<MovieReleaseDatesResponse> call,
                                                    @NonNull Throwable t) {}
                });
    }

    private String parseCaCertification(MovieReleaseDatesResponse response) {
        if (response.getResults() == null) return null;
        String ca = null, us = null;
        for (MovieReleaseDatesResponse.CountryReleaseDates entry : response.getResults()) {
            List<MovieReleaseDatesResponse.ReleaseDate> dates = entry.getReleaseDates();
            if (dates == null || dates.isEmpty()) continue;
            String cert = firstNonEmptyCert(dates);
            if (cert == null) continue;
            if ("CA".equals(entry.getCountry())) ca = cert;
            else if ("US".equals(entry.getCountry()) && us == null) us = cert;
        }
        return ca != null ? ca : us;
    }

    private String firstNonEmptyCert(List<MovieReleaseDatesResponse.ReleaseDate> dates) {
        for (MovieReleaseDatesResponse.ReleaseDate d : dates) {
            if (d.getType() == 3 && d.getCertification() != null && !d.getCertification().isEmpty())
                return d.getCertification();
        }
        for (MovieReleaseDatesResponse.ReleaseDate d : dates) {
            if (d.getCertification() != null && !d.getCertification().isEmpty())
                return d.getCertification();
        }
        return null;
    }

    private void fetchMovieProviders() {
        safeEnqueue(apiService.getMovieProviders(TMDBpath.movieProvider(movieId)),
                new Callback<TVShowProviderResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TVShowProviderResponse> call,
                                           @NonNull Response<TVShowProviderResponse> response) {
                        if (binding == null || !response.isSuccessful() || response.body() == null) return;
                        watchProviderResults = response.body().getResults();
                        String region = WatchProviderHelper.defaultRegion();
                        new Thread(() -> {
                            cachedMotn = MotnHelper.fromJson(
                                    databaseHelper.getCachedMotnJson(movieId, MotnHelper.SHOW_TYPE_MOVIE));
                            if (getActivity() != null) getActivity().runOnUiThread(() -> {
                                bindMovieProviders(region);
                                binding.wtwReleased.chipLocation.setOnClickListener(v ->
                                        WatchProviderHelper.showRegionPicker(requireContext(), watchProviderResults,
                                                selectedRegion -> {
                                                    userHasSelectedRegion = true;
                                                    bindMovieProviders(selectedRegion);
                                                }));
                            });
                        }).start();
                    }
                    @Override public void onFailure(@NonNull Call<TVShowProviderResponse> call,
                                                    @NonNull Throwable t) {}
                });
    }

    private void bindMovieProviders(String region) {
        if (binding == null || !isAdded()) return;
        currentWtwRegion = region;
        WtwReleasedViewBinding wtw = binding.wtwReleased;
        wtw.textRegion.setText(region);

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
        streamAdapter.setOnProviderClick(provider -> handleProviderClick(provider, MotnHelper.SHOW_TYPE_MOVIE));
        rentBuyAdapter.setOnProviderClick(provider -> handleProviderClick(provider, MotnHelper.SHOW_TYPE_MOVIE));

        wtw.textEmptyState.setVisibility(View.GONE);
        wtw.tabContainer.setVisibility(View.VISIBLE);
        FlexboxLayoutManager flexLm = new FlexboxLayoutManager(requireContext());
        flexLm.setFlexDirection(FlexDirection.ROW);
        flexLm.setFlexWrap(FlexWrap.WRAP);
        wtw.recyclerProviders.setLayoutManager(flexLm);
        wtw.recyclerProviders.setAdapter(streamAdapter);
        streamAdapter.submitList(stream);
        applyWtwEmptyState(wtw, stream.isEmpty());

        wtw.tabStream.setSelected(true);
        wtw.tabRent.setSelected(false);
        wtw.tabBuy.setSelected(false);
        wtw.tabStream.setOnClickListener(v -> {
            wtw.tabStream.setSelected(true);
            wtw.tabRent.setSelected(false);
            wtw.tabBuy.setSelected(false);
            wtw.recyclerProviders.setAdapter(streamAdapter);
            applyWtwEmptyState(wtw, stream.isEmpty());
        });
        wtw.tabRent.setOnClickListener(v -> {
            wtw.tabStream.setSelected(false);
            wtw.tabRent.setSelected(true);
            wtw.tabBuy.setSelected(false);
            rentBuyAdapter.submitList(rent);
            wtw.recyclerProviders.setAdapter(rentBuyAdapter);
            applyWtwEmptyState(wtw, rent.isEmpty());
        });
        wtw.tabBuy.setOnClickListener(v -> {
            wtw.tabStream.setSelected(false);
            wtw.tabRent.setSelected(false);
            wtw.tabBuy.setSelected(true);
            rentBuyAdapter.submitList(buy);
            wtw.recyclerProviders.setAdapter(rentBuyAdapter);
            applyWtwEmptyState(wtw, buy.isEmpty());
        });
    }

    private void handleNoWatchProviders() {
        if (binding == null) return;
        WtwReleasedViewBinding wtw = binding.wtwReleased;
        wtw.recyclerProviders.setVisibility(View.GONE);
        wtw.tabContainer.setVisibility(View.GONE);
        if (isNowPlaying && !userHasSelectedRegion) {
            wtw.textEmptyState.setVisibility(View.GONE);
        } else {
            wtw.textEmptyState.setVisibility(View.VISIBLE);
            wtw.textEmptyState.setText(getString(R.string.unavailable_country));
            wtw.textEmptyState.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.text_disabled));
        }
    }

    private void applyWtwEmptyState(WtwReleasedViewBinding wtw, boolean isEmpty) {
        wtw.recyclerProviders.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        wtw.textEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
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
            String cached = databaseHelper.getCachedMotnJson(movieId, showType);
            if (cached != null) {
                MotnShowResponse motn = MotnHelper.fromJson(cached);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> openMotnLink(motn, provider.getProviderName(), provider.getProviderId()));
                }
                return;
            }
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> fetchAndOpenMotnLink(movieId, showType, provider.getProviderName(), provider.getProviderId()));
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
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
    }
}