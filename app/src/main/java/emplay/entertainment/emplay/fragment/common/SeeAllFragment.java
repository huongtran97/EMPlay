package emplay.entertainment.emplay.fragment.common;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import emplay.entertainment.emplay.databinding.ActivitySeeAllBinding;
import emplay.entertainment.emplay.databinding.ItemSearchResultCastBinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import com.google.android.flexbox.FlexboxLayout;
import androidx.core.content.ContextCompat;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.TrendingSearchAdapter;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.adapter.movie.MovieByGenreAdapter;
import emplay.entertainment.emplay.adapter.tvshow.TVShowByGenreAdapter;
import emplay.entertainment.emplay.api.common.ApiClient;
import emplay.entertainment.emplay.api.common.MovieApiService;
import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.api.common.AggregateCreditsResponse;
import emplay.entertainment.emplay.api.movie.MovieCreditsResponse;
import emplay.entertainment.emplay.api.movie.MovieSimilarResponse;
import emplay.entertainment.emplay.api.movie.UpComingMovieResponse;
import emplay.entertainment.emplay.adapter.tvshow.OnAirSeeAllAdapter;
import emplay.entertainment.emplay.api.tvshow.TVShowCreditsResponses;
import emplay.entertainment.emplay.api.tvshow.TVShowDetailsResponse;
import emplay.entertainment.emplay.api.tvshow.TVShowResponse;
import emplay.entertainment.emplay.api.tvshow.TVShowSimilarResponse;
import emplay.entertainment.emplay.api.tvshow.UpComingTVShowsResponse;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.fragment.details.CastDetailFragment;
import emplay.entertainment.emplay.fragment.details.MovieResultDetailsFragment;
import emplay.entertainment.emplay.fragment.details.TVShowResultDetailsFragment;
import emplay.entertainment.emplay.models.common.CastModel;
import emplay.entertainment.emplay.models.common.MediaItem;
import emplay.entertainment.emplay.models.movie.MovieModel;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SeeAllFragment extends BaseFragment {
    public static final String TYPE_ON_AIR = "ON_AIR";
    public static final String TYPE_UPCOMING_MOVIES = "UPCOMING_MOVIES";
    public static final String TYPE_UPCOMING_TV = "UPCOMING_TV";
    public static final String TYPE_CAST_MOVIE = "CAST_MOVIE";
    public static final String TYPE_CAST_TV = "CAST_TV";
    public static final String TYPE_SIMILAR_MOVIE = "SIMILAR_MOVIE";
    public static final String TYPE_SIMILAR_TV = "SIMILAR_TV";
    private static final String ARG_TYPE = "type";
    private static final String ARG_MEDIA_ID = "media_id";
    private static final String ARG_TITLE = "title";
    private static final String ARG_GENRES = "genres";
    private ActivitySeeAllBinding binding;
    private String type;
    private int mediaId;
    private MovieApiService apiService;
    private TrendingSearchAdapter<MediaItem> trendingAdapter;
    private OnAirSeeAllAdapter onAirSeeAllAdapter;
    private final List<TVShowModel> onAirTVList = new ArrayList<>();
    private final List<TVShowModel> onAirBuffer = new ArrayList<>();
    private int onAirTmdbPage = 1;
    private int onAirTmdbTotalPages = 1;
    private boolean onAirIsLoading = false;
    private static final int ON_AIR_PAGE_SIZE = 6;
    private RecyclerView onAirRv;
    private MovieByGenreAdapter moviePosterAdapter;
    private TVShowByGenreAdapter tvPosterAdapter;
    private RecyclerView.Adapter<RecyclerView.ViewHolder> castGridAdapter;
    private RecyclerView.Adapter<RecyclerView.ViewHolder> crewGridAdapter;
    private final List<CastModel> castOnlyList = new ArrayList<>();
    private final List<CastModel> crewList = new ArrayList<>();
    private boolean castExpanded = false;
    private boolean crewExpanded = false;

    // Similar pagination state
    private int similarNextPage = 1;
    private int similarTotalPages = 1;
    private boolean similarIsLoading = false;

    // Coming Soon pagination state (shared for UPCOMING_MOVIES and UPCOMING_TV)
    private final List<MediaItem> comingSoonBuffer = new ArrayList<>();
    private int comingSoonTmdbPage = 1;
    private int comingSoonTmdbTotalPages = 1;
    private boolean comingSoonIsLoading = false;
    private static final int COMING_SOON_PAGE_SIZE = 6;
    private RecyclerView comingSoonRv;
    private String comingSoonRegion;

    public static SeeAllFragment newInstance(String type, String title) {
        return newInstance(type, -1, title);
    }

    public static SeeAllFragment newInstance(String type, int mediaId, String title) {
        return newInstance(type, mediaId, title, null);
    }

    public static SeeAllFragment newInstance(String type, int mediaId, String title,
                                             ArrayList<String> genres) {
        SeeAllFragment fragment = new SeeAllFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        args.putInt(ARG_MEDIA_ID, mediaId);
        args.putString(ARG_TITLE, title);
        if (genres != null && !genres.isEmpty()) args.putStringArrayList(ARG_GENRES, genres);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getString(ARG_TYPE, TYPE_UPCOMING_MOVIES);
            mediaId = getArguments().getInt(ARG_MEDIA_ID, -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ActivitySeeAllBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            binding.tvSectionTitle.setText(getArguments().getString(ARG_TITLE, ""));
        }
        binding.tvSubtitle.setVisibility(View.GONE);
        binding.tvLoadingMore.setVisibility(View.GONE);

        if (getArguments() != null && (TYPE_SIMILAR_MOVIE.equals(type) || TYPE_SIMILAR_TV.equals(type))) {
            ArrayList<String> genres = getArguments().getStringArrayList(ARG_GENRES);
            if (genres != null && !genres.isEmpty()) {
                populateGenreChips(genres);
            }
        }

        apiService = ApiClient.getClient().create(MovieApiService.class);

        boolean isCastType = TYPE_CAST_MOVIE.equals(type) || TYPE_CAST_TV.equals(type);
        if (isCastType) {
            binding.rvAllItems.setVisibility(View.GONE);
            binding.nsvCastCrew.setVisibility(View.VISIBLE);
            setupCastCrewAccordion();
            fetchData(binding.tvSubtitle);
            binding.nsvCastCrew.setOnScrollChangeListener(
                    (androidx.core.widget.NestedScrollView.OnScrollChangeListener)
                            (v, scrollX, scrollY, oldScrollX, oldScrollY) ->
                                    binding.fabScrollTop.setVisibility(scrollY > 0 ? View.VISIBLE : View.GONE));
            binding.fabScrollTop.setOnClickListener(v -> binding.nsvCastCrew.smoothScrollTo(0, 0));
        } else {
            binding.nsvCastCrew.setVisibility(View.GONE);
            setupRecyclerView(binding.rvAllItems);
            fetchData(binding.tvSubtitle);
            binding.rvAllItems.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    binding.fabScrollTop.setVisibility(
                            recyclerView.canScrollVertically(-1) ? View.VISIBLE : View.GONE);
                }
            });
            binding.fabScrollTop.setOnClickListener(v ->
                    binding.rvAllItems.smoothScrollToPosition(0));
        }

        return binding.getRoot();
    }

    private void setupCastCrewAccordion() {
        castGridAdapter = buildCastGridAdapter(castOnlyList);
        binding.rvCastSection.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        binding.rvCastSection.setAdapter(castGridAdapter);

        crewGridAdapter = buildCrewGridAdapter(crewList);
        binding.rvCrewSection.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        binding.rvCrewSection.setAdapter(crewGridAdapter);

        binding.rowCastToggle.setOnClickListener(v -> {
            castExpanded = !castExpanded;
            toggleSection(binding.rvCastSection, binding.icCastChevron, castExpanded);
        });
        binding.rowCrewToggle.setOnClickListener(v -> {
            crewExpanded = !crewExpanded;
            toggleSection(binding.rvCrewSection, binding.icCrewChevron, crewExpanded);
        });
    }

    private void toggleSection(RecyclerView rv, ImageView chevron, boolean expanded) {
        rv.setVisibility(expanded ? View.VISIBLE : View.GONE);
        ObjectAnimator.ofFloat(chevron, "rotation", expanded ? 0f : 90f, expanded ? 90f : 0f)
                .setDuration(200)
                .start();
    }

    @SuppressLint("NotifyDataSetChanged")
    private RecyclerView.Adapter<RecyclerView.ViewHolder> buildCastGridAdapter(List<CastModel> list) {
        return new RecyclerView.Adapter<>() {
            static class VH extends RecyclerView.ViewHolder {
                final ItemSearchResultCastBinding b;
                VH(ItemSearchResultCastBinding b) { super(b.getRoot()); this.b = b; }
            }
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new VH(ItemSearchResultCastBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false));
            }
            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                CastModel cast = list.get(position);
                ItemSearchResultCastBinding b = ((VH) holder).b;
                b.castNameProfile.setText(cast.getName());
                b.castCharacterProfile.setText(cast.getCharacter());
                String path = cast.getProfilePath();
                Glide.with(b.getRoot().getContext())
                        .load(path != null && !path.isEmpty() ? ImageUrl.of(ImageUrl.PROFILE, path) : null)
                        .placeholder(R.drawable.avatar)
                        .error(R.drawable.avatar)
                        .circleCrop()
                        .into(b.castPosterProfile);
                b.getRoot().setOnClickListener(v ->
                        navigateTo(CastDetailFragment.newInstance(cast.getId())));
            }
            @Override
            public int getItemCount() { return list.size(); }
        };
    }

    @SuppressLint("NotifyDataSetChanged")
    private RecyclerView.Adapter<RecyclerView.ViewHolder> buildCrewGridAdapter(List<CastModel> list) {
        return new RecyclerView.Adapter<>() {
            static class VH extends RecyclerView.ViewHolder {
                final ItemSearchResultCastBinding b;
                VH(ItemSearchResultCastBinding b) { super(b.getRoot()); this.b = b; }
            }
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new VH(ItemSearchResultCastBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false));
            }
            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                CastModel crew = list.get(position);
                ItemSearchResultCastBinding b = ((VH) holder).b;
                b.castNameProfile.setText(crew.getName());
                b.castCharacterProfile.setText(crew.getCharacter());
                String path = crew.getProfilePath();
                Glide.with(b.getRoot().getContext())
                        .load(path != null && !path.isEmpty() ? ImageUrl.of(ImageUrl.PROFILE, path) : null)
                        .placeholder(R.drawable.avatar)
                        .error(R.drawable.avatar)
                        .circleCrop()
                        .into(b.castPosterProfile);
                b.getRoot().setOnClickListener(v ->
                        navigateTo(CastDetailFragment.newInstance(crew.getId())));
            }
            @Override
            public int getItemCount() { return list.size(); }
        };
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        comingSoonRv = null;
        onAirRv = null;
        comingSoonBuffer.clear();
        castOnlyList.clear();
        crewList.clear();
    }

    private void setupRecyclerView(RecyclerView rv) {
        switch (type) {
            case TYPE_SIMILAR_MOVIE: {
                moviePosterAdapter = new MovieByGenreAdapter(requireContext(), new ArrayList<>(),
                        (movie, v) -> navigateTo(
                                MovieResultDetailsFragment.newInstance(movie.getMovieId()),
                                v, "poster_transition"));
                GridLayoutManager movieLm = new GridLayoutManager(requireContext(), 3);
                rv.setLayoutManager(movieLm);
                rv.setAdapter(moviePosterAdapter);
                rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        if (dy <= 0) return;
                        int total = movieLm.getItemCount();
                        int last = movieLm.findLastVisibleItemPosition();
                        if (!similarIsLoading && similarNextPage <= similarTotalPages
                                && last >= total - 10) {
                            fetchSimilarMovies(binding.tvSubtitle);
                        }
                    }
                });
                break;
            }

            case TYPE_SIMILAR_TV: {
                tvPosterAdapter = new TVShowByGenreAdapter(requireContext(), new ArrayList<>(),
                        (tv, v) -> navigateTo(
                                TVShowResultDetailsFragment.newInstance(tv.getTVShowId()),
                                v, "poster_transition"));
                GridLayoutManager tvLm = new GridLayoutManager(requireContext(), 3);
                rv.setLayoutManager(tvLm);
                rv.setAdapter(tvPosterAdapter);
                rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        if (dy <= 0) return;
                        int total = tvLm.getItemCount();
                        int last = tvLm.findLastVisibleItemPosition();
                        if (!similarIsLoading && similarNextPage <= similarTotalPages
                                && last >= total - 10) {
                            fetchSimilarTV(binding.tvSubtitle);
                        }
                    }
                });
                break;
            }

            case TYPE_ON_AIR: {
                FirebaseUser onAirUser = FirebaseAuth.getInstance().getCurrentUser();
                String onAirUserId = onAirUser != null ? onAirUser.getUid() : null;
                DatabaseHelper onAirDb = DatabaseHelper.getInstance(requireContext());
                onAirSeeAllAdapter = new OnAirSeeAllAdapter(
                        requireContext(), onAirTVList,
                        (tv, v) -> navigateTo(TVShowResultDetailsFragment.newInstance(tv.getTVShowId()),
                                v, "poster_transition"),
                        onAirDb, onAirUserId, this::showLoginPromptDialog);
                onAirRv = rv;
                rv.setLayoutManager(new LinearLayoutManager(requireContext()));
                rv.setAdapter(onAirSeeAllAdapter);
                rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        if (dy <= 0) return;
                        LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                        if (lm == null) return;
                        int lastComplete = lm.findLastCompletelyVisibleItemPosition();
                        if (lastComplete >= onAirTVList.size() - 1) {
                            loadMoreOnAir();
                        }
                    }
                });
                break;
            }

            default:
                // UPCOMING_MOVIES, UPCOMING_TV — vertical list using TrendingSearchAdapter
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String userId = user != null ? user.getUid() : null;
                DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
                trendingAdapter = new TrendingSearchAdapter<>(
                        requireContext(), new ArrayList<>(), this::onMediaClick, db, userId);
                comingSoonRv = rv;
                rv.setLayoutManager(new LinearLayoutManager(requireContext()));
                rv.setAdapter(trendingAdapter);
                rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        if (dy <= 0) return;
                        LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                        if (lm == null) return;
                        int lastComplete = lm.findLastCompletelyVisibleItemPosition();
                        if (lastComplete >= trendingAdapter.getDataItemCount() - 1) {
                            loadMoreComingSoon();
                        }
                    }
                });
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    private void bindEyebrow(TextView tv) {
        switch (type) {
            case TYPE_CAST_MOVIE:
            case TYPE_CAST_TV:
                tv.setText("CAST & CREW");
                break;
            case TYPE_SIMILAR_MOVIE:
            case TYPE_SIMILAR_TV:
                tv.setText("YOU MAY ALSO LIKE");
                break;
            default:
                tv.setText(getString(R.string.eyebrow_from_home));
                break;
        }
    }

    private void fetchData(TextView tvSubtitle) {
        switch (type) {
            case TYPE_CAST_MOVIE:      fetchMovieCast(tvSubtitle);      break;
            case TYPE_CAST_TV:         fetchTVCast(tvSubtitle);         break;
            case TYPE_SIMILAR_MOVIE:   fetchSimilarMovies(tvSubtitle);  break;
            case TYPE_SIMILAR_TV:      fetchSimilarTV(tvSubtitle);      break;
            case TYPE_ON_AIR:          fetchOnAir(tvSubtitle);          break;
            case TYPE_UPCOMING_MOVIES: fetchUpcomingMovies(tvSubtitle); break;
            case TYPE_UPCOMING_TV:     fetchUpcomingTV(tvSubtitle);     break;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchMovieCast(TextView tvSubtitle) {
        safeEnqueue(apiService.getMovieCredits(TMDBpath.movieCredits(mediaId)),
                new Callback<MovieCreditsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MovieCreditsResponse> call,
                                           @NonNull Response<MovieCreditsResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            castOnlyList.clear();
                            crewList.clear();
                            List<MovieCreditsResponse.Cast> cast = response.body().getCast();
                            if (cast != null) {
                                for (MovieCreditsResponse.Cast c : cast) {
                                    castOnlyList.add(new CastModel(c.getId(), c.getCastName(),
                                            c.getProfilePath(), c.getCharacter()));
                                }
                            }
                            List<MovieCreditsResponse.Crew> crew = response.body().getCrew();
                            if (crew != null) {
                                for (MovieCreditsResponse.Crew c : crew) {
                                    crewList.add(new CastModel(c.getId(), c.getName(),
                                            c.getProfilePath(), c.getJob()));
                                }
                            }
                            if (castGridAdapter != null) castGridAdapter.notifyDataSetChanged();
                            if (crewGridAdapter != null) crewGridAdapter.notifyDataSetChanged();
                            updateCastCrewCounts(tvSubtitle);
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<MovieCreditsResponse> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Failed to load cast", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchTVCast(TextView tvSubtitle) {
        safeEnqueue(apiService.getTVAggregateCredits(TMDBpath.tvShowAggregateCredits(mediaId)),
                new Callback<AggregateCreditsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AggregateCreditsResponse> call,
                                           @NonNull Response<AggregateCreditsResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            castOnlyList.clear();
                            crewList.clear();
                            List<AggregateCreditsResponse.Cast> cast = response.body().getCast();
                            if (cast != null) {
                                for (AggregateCreditsResponse.Cast c : cast) {
                                    castOnlyList.add(new CastModel(c.getId(), c.getName(),
                                            c.getProfilePath(), c.getCharacter()));
                                }
                            }
                            List<AggregateCreditsResponse.Crew> crew = response.body().getCrew();
                            if (crew != null) {
                                for (AggregateCreditsResponse.Crew c : crew) {
                                    crewList.add(new CastModel(c.getId(), c.getName(),
                                            c.getProfilePath(), c.getJob()));
                                }
                            }
                            if (castGridAdapter != null) castGridAdapter.notifyDataSetChanged();
                            if (crewGridAdapter != null) crewGridAdapter.notifyDataSetChanged();
                            updateCastCrewCounts(tvSubtitle);
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<AggregateCreditsResponse> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Failed to load cast", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchSimilarMovies(TextView tvSubtitle) {
        if (similarIsLoading) return;
        if (similarNextPage > similarTotalPages) return;
        similarIsLoading = true;
        int pageToFetch = similarNextPage;
        safeEnqueue(apiService.getMovieSimilarPaged(TMDBpath.movieSimilar(mediaId), pageToFetch),
                new Callback<MovieSimilarResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MovieSimilarResponse> call,
                                           @NonNull Response<MovieSimilarResponse> response) {
                        similarIsLoading = false;
                        if (response.isSuccessful() && response.body() != null) {
                            similarTotalPages = response.body().getTotal_pages();
                            List<MovieModel> results = response.body().getResults();
                            if (results == null) results = new ArrayList<>();
                            results.removeIf(m -> m.getPosterPath() == null && m.getBackdropPath() == null);
                            if (moviePosterAdapter != null) {
                                if (pageToFetch == 1) {
                                    moviePosterAdapter.updateData(results);
                                } else {
                                    moviePosterAdapter.appendData(results);
                                }
                            }
                            similarNextPage = pageToFetch + 1;
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<MovieSimilarResponse> call, @NonNull Throwable t) {
                        similarIsLoading = false;
                        Toast.makeText(getContext(), "Failed to load similar movies", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchSimilarTV(TextView tvSubtitle) {
        if (similarIsLoading) return;
        if (similarNextPage > similarTotalPages) return;
        similarIsLoading = true;
        int pageToFetch = similarNextPage;
        safeEnqueue(apiService.getTVShowSimilarPaged(TMDBpath.tvShowSimilar(mediaId), pageToFetch),
                new Callback<TVShowSimilarResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TVShowSimilarResponse> call,
                                           @NonNull Response<TVShowSimilarResponse> response) {
                        similarIsLoading = false;
                        if (response.isSuccessful() && response.body() != null) {
                            similarTotalPages = response.body().getTotal_pages();
                            List<TVShowModel> results = response.body().getResults();
                            if (results == null) results = new ArrayList<>();
                            results.removeIf(tv -> tv.getPosterPath() == null && tv.getBackdropPath() == null);
                            if (tvPosterAdapter != null) {
                                if (pageToFetch == 1) {
                                    tvPosterAdapter.updateData(results);
                                } else {
                                    tvPosterAdapter.appendData(results);
                                }
                            }
                            similarNextPage = pageToFetch + 1;
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<TVShowSimilarResponse> call, @NonNull Throwable t) {
                        similarIsLoading = false;
                        Toast.makeText(getContext(), "Failed to load similar TV shows", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchOnAir(TextView tvSubtitle) {
        safeEnqueue(apiService.getOnAirTVShows(TMDBpath.onAirTVShows(), 1),
                new Callback<TVShowResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TVShowResponse> call,
                                           @NonNull Response<TVShowResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            TVShowResponse body = response.body();
                            onAirTmdbPage = 1;
                            onAirTmdbTotalPages = body.getTotal_pages();
                            onAirBuffer.clear();
                            onAirTVList.clear();
                            List<TVShowModel> results = body.getResults();
                            if (results != null) {
                                for (TVShowModel tv : results) {
                                    if (tv.getPosterPath() != null || tv.getBackdropPath() != null) onAirBuffer.add(tv);
                                }
                            }
                            appendOnAirBatch();
                            showSubtitle(tvSubtitle, "TV Shows");
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<TVShowResponse> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Failed to load on-air shows", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void appendOnAirBatch() {
        int start = onAirTVList.size();
        int end = Math.min(start + ON_AIR_PAGE_SIZE, onAirBuffer.size());
        if (end <= start) {
            onAirIsLoading = false;
            return;
        }
        onAirTVList.addAll(onAirBuffer.subList(start, end));
        if (onAirSeeAllAdapter != null) onAirSeeAllAdapter.notifyItemRangeInserted(start, end - start);
        fetchOnAirDetailsForRange(start, end);
        // After layout: release the lock, then keep filling if items still don't overflow the screen
        if (onAirRv != null) onAirRv.post(() -> {
            onAirIsLoading = false;
            boolean hasMore = onAirTVList.size() < onAirBuffer.size()
                    || onAirTmdbPage < onAirTmdbTotalPages;
            if (hasMore && !onAirRv.canScrollVertically(1)) {
                loadMoreOnAir();
            }
        });
    }

    private void loadMoreOnAir() {
        if (onAirIsLoading) return;
        onAirIsLoading = true;
        if (onAirTVList.size() < onAirBuffer.size()) {
            appendOnAirBatch();
        } else if (onAirTmdbPage < onAirTmdbTotalPages) {
            if (onAirSeeAllAdapter != null) onAirSeeAllAdapter.setLoading(true);
            fetchOnAirTmdbPage(onAirTmdbPage + 1);
        } else {
            onAirIsLoading = false;
        }
    }

    private void fetchOnAirTmdbPage(int page) {
        safeEnqueue(apiService.getOnAirTVShows(TMDBpath.onAirTVShows(), page),
                new Callback<TVShowResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TVShowResponse> call,
                                           @NonNull Response<TVShowResponse> response) {
                        if (onAirSeeAllAdapter != null) onAirSeeAllAdapter.setLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            onAirTmdbPage = page;
                            List<TVShowModel> results = response.body().getResults();
                            if (results != null) {
                                for (TVShowModel tv : results) {
                                    if (tv.getPosterPath() != null || tv.getBackdropPath() != null) onAirBuffer.add(tv);
                                }
                            }
                            appendOnAirBatch();
                            // appendOnAirBatch's post callback releases onAirIsLoading
                        } else {
                            onAirIsLoading = false;
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<TVShowResponse> call, @NonNull Throwable t) {
                        if (onAirSeeAllAdapter != null) onAirSeeAllAdapter.setLoading(false);
                        onAirIsLoading = false;
                        Toast.makeText(getContext(), "Failed to load more shows", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchOnAirDetailsForRange(int start, int end) {
        for (int i = start; i < end && i < onAirTVList.size(); i++) {
            final int idx = i;
            final TVShowModel tv = onAirTVList.get(i);
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
                            if (onAirSeeAllAdapter != null) onAirSeeAllAdapter.notifyItemChanged(idx);
                        }
                        @Override
                        public void onFailure(@NonNull Call<TVShowDetailsResponse> call, @NonNull Throwable t) {}
                    });
        }
    }

    private void fetchUpcomingMovies(TextView tvSubtitle) {
        comingSoonRegion = PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getString("pref_region", "CA");
        safeEnqueue(apiService.getUpcomingMovies(
                        TMDBpath.discoverMovies(), TMDBpath.todayDate(), TMDBpath.thirtyDaysFromNow(),
                        "popularity.desc", false, "en-US", 1, comingSoonRegion),
                new Callback<UpComingMovieResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<UpComingMovieResponse> call,
                                           @NonNull Response<UpComingMovieResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            UpComingMovieResponse body = response.body();
                            comingSoonTmdbPage = 1;
                            comingSoonTmdbTotalPages = body.getTotal_pages();
                            comingSoonBuffer.clear();
                            for (MovieModel m : body.getResults()) {
                                if (m.getPosterPath() != null || m.getBackdropPath() != null) comingSoonBuffer.add(m);
                            }
                            appendComingSoonBatch();
                            showSubtitle(tvSubtitle, "Movies");
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<UpComingMovieResponse> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Failed to load upcoming movies", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUpcomingTV(TextView tvSubtitle) {
        comingSoonRegion = PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getString("pref_region", "CA");
        safeEnqueue(apiService.getUpcomingTVShows(
                        TMDBpath.discoverTVShows(), TMDBpath.todayDate(), TMDBpath.thirtyDaysFromNow(),
                        "popularity.desc", false, "en-US", 1, comingSoonRegion),
                new Callback<UpComingTVShowsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<UpComingTVShowsResponse> call,
                                           @NonNull Response<UpComingTVShowsResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            UpComingTVShowsResponse body = response.body();
                            comingSoonTmdbPage = 1;
                            comingSoonTmdbTotalPages = body.getTotal_pages();
                            comingSoonBuffer.clear();
                            for (TVShowModel tv : body.getResults()) {
                                if (tv.getPosterPath() != null || tv.getBackdropPath() != null) comingSoonBuffer.add(tv);
                            }
                            appendComingSoonBatch();
                            showSubtitle(tvSubtitle, "TV Shows");
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<UpComingTVShowsResponse> call,
                                          @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Failed to load upcoming TV shows", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void appendComingSoonBatch() {
        if (trendingAdapter == null) {
            comingSoonIsLoading = false;
            return;
        }
        int start = trendingAdapter.getDataItemCount();
        int end = Math.min(start + COMING_SOON_PAGE_SIZE, comingSoonBuffer.size());
        if (end <= start) {
            comingSoonIsLoading = false;
            return;
        }
        trendingAdapter.appendItems(comingSoonBuffer.subList(start, end));
        if (comingSoonRv != null) comingSoonRv.post(() -> {
            comingSoonIsLoading = false;
            boolean hasMore = trendingAdapter.getDataItemCount() < comingSoonBuffer.size()
                    || comingSoonTmdbPage < comingSoonTmdbTotalPages;
            if (hasMore && comingSoonRv != null && !comingSoonRv.canScrollVertically(1)) {
                loadMoreComingSoon();
            }
        });
    }

    private void loadMoreComingSoon() {
        if (comingSoonIsLoading) return;
        comingSoonIsLoading = true;
        if (trendingAdapter != null && trendingAdapter.getDataItemCount() < comingSoonBuffer.size()) {
            appendComingSoonBatch();
        } else if (comingSoonTmdbPage < comingSoonTmdbTotalPages) {
            if (trendingAdapter != null) trendingAdapter.setLoading(true);
            fetchComingSoonTmdbPage(comingSoonTmdbPage + 1);
        } else {
            comingSoonIsLoading = false;
        }
    }

    private void fetchComingSoonTmdbPage(int page) {
        if (TYPE_UPCOMING_MOVIES.equals(type)) {
            safeEnqueue(apiService.getUpcomingMovies(
                            TMDBpath.discoverMovies(), TMDBpath.todayDate(), TMDBpath.thirtyDaysFromNow(),
                            "popularity.desc", false, "en-US", page, comingSoonRegion),
                    new Callback<UpComingMovieResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<UpComingMovieResponse> call,
                                               @NonNull Response<UpComingMovieResponse> response) {
                            if (trendingAdapter != null) trendingAdapter.setLoading(false);
                            if (response.isSuccessful() && response.body() != null) {
                                comingSoonTmdbPage = page;
                                for (MovieModel m : response.body().getResults()) {
                                    if (m.getPosterPath() != null || m.getBackdropPath() != null) comingSoonBuffer.add(m);
                                }
                                appendComingSoonBatch();
                            } else {
                                comingSoonIsLoading = false;
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<UpComingMovieResponse> call, @NonNull Throwable t) {
                            if (trendingAdapter != null) trendingAdapter.setLoading(false);
                            comingSoonIsLoading = false;
                            Toast.makeText(getContext(), "Failed to load more movies", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            safeEnqueue(apiService.getUpcomingTVShows(
                            TMDBpath.discoverTVShows(), TMDBpath.todayDate(), TMDBpath.thirtyDaysFromNow(),
                            "popularity.desc", false, "en-US", page, comingSoonRegion),
                    new Callback<UpComingTVShowsResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<UpComingTVShowsResponse> call,
                                               @NonNull Response<UpComingTVShowsResponse> response) {
                            if (trendingAdapter != null) trendingAdapter.setLoading(false);
                            if (response.isSuccessful() && response.body() != null) {
                                comingSoonTmdbPage = page;
                                for (TVShowModel tv : response.body().getResults()) {
                                    if (tv.getPosterPath() != null || tv.getBackdropPath() != null) comingSoonBuffer.add(tv);
                                }
                                appendComingSoonBatch();
                            } else {
                                comingSoonIsLoading = false;
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<UpComingTVShowsResponse> call, @NonNull Throwable t) {
                            if (trendingAdapter != null) trendingAdapter.setLoading(false);
                            comingSoonIsLoading = false;
                            Toast.makeText(getContext(), "Failed to load more TV shows", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void populateGenreChips(List<String> genres) {
        if (binding == null) return;
        FlexboxLayout container = binding.genreChips;
        container.removeAllViews();
        float density = getResources().getDisplayMetrics().density;
        int hPad = Math.round(10 * density);
        int vPad = Math.round(3 * density);
        int endMargin = Math.round(5 * density);
        int botMargin = Math.round(5 * density);
        for (String name : genres) {
            TextView chip = new TextView(requireContext());
            chip.setText(name);
            chip.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 11);
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_2));
            chip.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_chip));
            chip.setPadding(hPad, vPad, hPad, vPad);
            FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(endMargin);
            lp.bottomMargin = botMargin;
            chip.setLayoutParams(lp);
            container.addView(chip);
        }
        container.setVisibility(View.VISIBLE);
    }

    private void showSubtitle(TextView tv, String text) {
        if (tv == null) return;
        tv.setText(text);
        tv.setVisibility(View.VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    private void updateCastCrewCounts(TextView tvSubtitle) {
        if (binding == null) return;
        int castCount = castOnlyList.size();
        int crewCount = crewList.size();

        binding.tvCastCount.setText(castCount + " people");
        binding.tvCastCount.setVisibility(View.VISIBLE);
        binding.tvCrewCount.setText(crewCount + " people");
        binding.tvCrewCount.setVisibility(View.VISIBLE);

        showSubtitle(tvSubtitle, castCount + " cast  •  " + crewCount + " crew");
    }

    private void onMediaClick(MediaItem item, View sharedElement) {
        if (item instanceof MovieModel) {
            navigateTo(MovieResultDetailsFragment.newInstance(item.getMediaId()),
                    sharedElement, "poster_transition");
        } else if (item instanceof TVShowModel) {
            navigateTo(TVShowResultDetailsFragment.newInstance(item.getMediaId()),
                    sharedElement, "poster_transition");
        }
    }
}