package emplay.entertainment.emplay.fragment;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
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

import emplay.entertainment.emplay.api.TMDBpath;
import emplay.entertainment.emplay.tool.LanguageMapper;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.ApiClient;
import emplay.entertainment.emplay.api.MovieApiService;
import emplay.entertainment.emplay.api.MovieCreditsResponse;
import emplay.entertainment.emplay.api.MovieDetailsResponse;
import emplay.entertainment.emplay.api.MovieSimilarResponse;
import emplay.entertainment.emplay.api.MoviesTrailerResponses;
import emplay.entertainment.emplay.models.CastModel;
import emplay.entertainment.emplay.models.MovieModel;
import emplay.entertainment.emplay.adapter.CastAdapter;
import emplay.entertainment.emplay.adapter.MovieInformationAdapter;
import emplay.entertainment.emplay.adapter.SuggestionMovieAdapter;
import jp.wasabeef.glide.transformations.BlurTransformation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *  Detail screen for a single movie. Loads the movie info, cast row, and a
 *  paginated "more like this" grid — all in parallel.
 */
public class ShowResultDetailsFragment extends BaseFragment {

    private static final String ARG_MOVIE_ID = "MOVIE_ID";
    private static final int PAGE_SIZE = 9; // 3-column grid × 3 rows per page

    private int movieId;
    private List<MovieModel> movieList;
    private List<CastModel> castList;
    private final List<MovieModel> allSuggestions = new ArrayList<>();
    private int suggestionPage = 1;
    
    private RecyclerView detailRecyclerView;
    private RecyclerView castRecyclerView;
    private RecyclerView suggestionRecyclerView;
    private LinearLayout suggestionPaginationBar;
    private TextView suggestionPageIndicator;
    private ImageButton suggestionBtnPrev;
    private ImageButton suggestionBtnNext;
    
    private MovieInformationAdapter movieInformationAdapter;
    private CastAdapter castAdapter;
    private SuggestionMovieAdapter suggestionMovieAdapter;
    private MovieApiService apiService;

    public static ShowResultDetailsFragment newInstance(int movieId) {
        ShowResultDetailsFragment fragment = new ShowResultDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MOVIE_ID, movieId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_result_movie_view, container, false);

        detailRecyclerView = view.findViewById(R.id.search_result_recyclerview);
        castRecyclerView = view.findViewById(R.id.search_result_cast_recyclerview);
        suggestionRecyclerView = view.findViewById(R.id.search_result_suggestion_recyclerview);
        suggestionPaginationBar = view.findViewById(R.id.suggestion_pagination_bar);
        suggestionPageIndicator = view.findViewById(R.id.suggestion_page_indicator);
        suggestionBtnPrev = view.findViewById(R.id.suggestion_btn_prev);
        suggestionBtnNext = view.findViewById(R.id.suggestion_btn_next);

        detailRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        castRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        suggestionRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        movieList = new ArrayList<>();
        castList = new ArrayList<>();

        movieInformationAdapter = new MovieInformationAdapter(movieList, requireActivity());
        castAdapter = new CastAdapter(castList, requireActivity(), cast -> {
            CastDetailFragment fragment = CastDetailFragment.newInstance(cast.getId());
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
        suggestionMovieAdapter = new SuggestionMovieAdapter(new ArrayList<>(), getContext(), this::onItemClicked);

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

        detailRecyclerView.setAdapter(movieInformationAdapter);
        castRecyclerView.setAdapter(castAdapter);
        suggestionRecyclerView.setAdapter(suggestionMovieAdapter);

        detailRecyclerView.setNestedScrollingEnabled(false);
        suggestionRecyclerView.setNestedScrollingEnabled(false);

        apiService = ApiClient.getClient().create(MovieApiService.class);

        if (getArguments() != null) {
            movieId = getArguments().getInt(ARG_MOVIE_ID, -1);
            if (movieId != -1) {
                fetchMovieDetails();
                fetchCastList();
                fetchSuggestionList();
                fetchTrailersForMovie();
            } else {
                Toast.makeText(getContext(), "Invalid movie ID", Toast.LENGTH_SHORT).show();
            }
        }

        return view;
    }

    private void onItemClicked(MovieModel movie) {
        if (movie != null) {
            navigateTo(ShowResultDetailsFragment.newInstance(movie.getId()));
        } else {
            Toast.makeText(getContext(), "Movie details are not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchTrailersForMovie() {
        safeEnqueue(apiService.getMoviesTrailer(TMDBpath.movieTrailer(movieId)), new Callback<MoviesTrailerResponses>() {
            @Override
            public void onResponse(@NonNull Call<MoviesTrailerResponses> call, @NonNull Response<MoviesTrailerResponses> response) {
                if (response.isSuccessful()) {
                    MoviesTrailerResponses trailerResponses = response.body();
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
            public void onFailure(@NonNull Call<MoviesTrailerResponses> call, @NonNull Throwable t) {
                Log.e("ShowResultDetails", "Failed to fetch trailers", t);
            }
        });
    }

    private void updateAdapterWithTrailers(List<MoviesTrailerResponses.TrailerModel> trailers) {
        if (movieInformationAdapter != null) {
            movieInformationAdapter.setTeaserTrailers(trailers);
        }
    }

    private void fetchMovieDetails() {
        safeEnqueue(apiService.getMovieDetails(TMDBpath.movieDetails(movieId)), new Callback<MovieDetailsResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<MovieDetailsResponse> call, @NonNull Response<MovieDetailsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieDetailsResponse movieDetails = response.body();
                    List<String> genres = new ArrayList<>();
                    if (movieDetails.getGenres() != null) {
                        for (MovieDetailsResponse.Genre genre : movieDetails.getGenres()) {
                            genres.add(genre.getName());
                        }
                    }

                    movieList.clear();
                    if (VERSION.SDK_INT >= VERSION_CODES.N) {
                        movieList.add(new MovieModel(
                                movieDetails.getId(),
                                movieDetails.getTitle(),
                                movieDetails.getVoteAverage(),
                                movieDetails.getPosterPath(),
                                movieDetails.getBackdropPath(),
                                movieDetails.getOverview(),
                                LanguageMapper.getLanguageName(movieDetails.getOriginalLanguage()),
                                movieDetails.getReleaseDate(),
                                movieDetails.getRuntime(),
                                genres
                        ));
                    }
                    movieInformationAdapter.notifyDataSetChanged();
                    setRecyclerViewBackground(movieDetails.getBackdropPath(), movieDetails.getPosterPath());

                } else {
                    Toast.makeText(getContext(), "Failed to retrieve movie details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovieDetailsResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error fetching movie details: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Blurs the backdrop (or poster if no backdrop) and layers it behind the detail card
     * to give the screen a themed background without obscuring the content.
     */
    private void setRecyclerViewBackground(String backdropPath, String posterPath) {
        Object imageSource;
        if (backdropPath != null && !backdropPath.isEmpty()) {
            imageSource = "https://image.tmdb.org/t/p/w500/" + backdropPath;
        } else if (posterPath != null && !posterPath.isEmpty()) {
            imageSource = "https://image.tmdb.org/t/p/w500/" + posterPath;
        } else {
            imageSource = R.drawable.placeholder_image;
        }

        Glide.with(this)
                .load(imageSource)
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

    private void fetchCastList() {
        safeEnqueue(apiService.getMovieCredits(TMDBpath.movieCredits(movieId)), new Callback<MovieCreditsResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<MovieCreditsResponse> call, @NonNull Response<MovieCreditsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieCreditsResponse movieCreditsResponse = response.body();
                    List<MovieCreditsResponse.Cast> castList = movieCreditsResponse.getCast();
                    if (castList != null && !castList.isEmpty()) {
                        List<CastModel> castModels = new ArrayList<>();
                        for (MovieCreditsResponse.Cast cast : castList) {
                            CastModel castModel = new CastModel(
                                    cast.getId(),
                                    cast.getCastName(),
                                    cast.getProfilePath(),
                                    cast.getCharacter()
                            );
                            castModels.add(castModel);
                        }
                        ShowResultDetailsFragment.this.castList.clear();
                        ShowResultDetailsFragment.this.castList.addAll(castModels);
                        castAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Cast list is empty", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to retrieve cast list", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovieCreditsResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error fetching cast list: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchSuggestionList() {
        safeEnqueue(apiService.getMovieSimilar(TMDBpath.movieSimilar(movieId)), new Callback<MovieSimilarResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieSimilarResponse> call, @NonNull Response<MovieSimilarResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MovieModel> recommendations = response.body().getResults();
                    allSuggestions.clear();
                    if (recommendations != null) {
                        for (MovieModel movie : recommendations) {
                            if (movie.getPosterPath() != null) {
                                allSuggestions.add(movie);
                            }
                        }
                    }
                    suggestionPage = 1;
                    showSuggestionPage();
                } else {
                    Toast.makeText(getContext(), "Failed to load movie recommendations", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovieSimilarResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Failed to load movie recommendations: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
            suggestionMovieAdapter.updateData(new ArrayList<>(allSuggestions.subList(fromIndex, toIndex)));
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
