package emplay.entertainment.emplay.fragment.common;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import emplay.entertainment.emplay.databinding.ActivitySeeAllBinding;
import emplay.entertainment.emplay.databinding.SearchResultCastItemBinding;

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

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.TrendingSearchAdapter;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.adapter.movie.MovieByGenreAdapter;
import emplay.entertainment.emplay.adapter.tvshow.TVShowByGenreAdapter;
import emplay.entertainment.emplay.api.common.ApiClient;
import emplay.entertainment.emplay.api.common.MovieApiService;
import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.api.movie.MovieCreditsResponse;
import emplay.entertainment.emplay.api.movie.MovieSimilarResponse;
import emplay.entertainment.emplay.api.movie.UpComingMovieResponse;
import emplay.entertainment.emplay.api.tvshow.TVShowCreditsResponses;
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

    private ActivitySeeAllBinding binding;
    private String type;
    private int mediaId;
    private MovieApiService apiService;
    private TrendingSearchAdapter<MediaItem> trendingAdapter;
    private MovieByGenreAdapter moviePosterAdapter;
    private TVShowByGenreAdapter tvPosterAdapter;
    private RecyclerView.Adapter<RecyclerView.ViewHolder> castGridAdapter;
    private final List<MediaItem> mediaList = new ArrayList<>();
    private final List<CastModel> castList = new ArrayList<>();

    public static SeeAllFragment newInstance(String type, String title) {
        return newInstance(type, -1, title);
    }

    public static SeeAllFragment newInstance(String type, int mediaId, String title) {
        SeeAllFragment fragment = new SeeAllFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        args.putInt(ARG_MEDIA_ID, mediaId);
        args.putString(ARG_TITLE, title);
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

        binding.btnBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        if (getArguments() != null) {
            binding.tvSectionTitle.setText(getArguments().getString(ARG_TITLE, ""));
        }
        bindEyebrow(binding.tvEyebrow);
        binding.tvSubtitle.setVisibility(View.GONE);
        binding.tvLoadingMore.setVisibility(View.GONE);

        apiService = ApiClient.getClient().create(MovieApiService.class);

        setupRecyclerView(binding.rvAllItems);
        fetchData(binding.tvSubtitle);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupRecyclerView(RecyclerView rv) {
        switch (type) {
            case TYPE_CAST_MOVIE:
            case TYPE_CAST_TV:
                castGridAdapter = new RecyclerView.Adapter<>() {
                    static class CastViewHolder extends RecyclerView.ViewHolder {
                        final SearchResultCastItemBinding b;
                        CastViewHolder(SearchResultCastItemBinding b) {
                            super(b.getRoot());
                            this.b = b;
                        }
                    }
                    @NonNull
                    @Override
                    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        return new CastViewHolder(SearchResultCastItemBinding.inflate(
                                LayoutInflater.from(parent.getContext()), parent, false));
                    }
                    @Override
                    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                        CastModel cast = castList.get(position);
                        SearchResultCastItemBinding b = ((CastViewHolder) holder).b;

                        b.castNameProfile.setText(cast.getName());
                        b.castCharacterProfile.setText(cast.getCharacter());

                        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) b.getRoot().getLayoutParams();
                        params.setMarginStart(dpToPx(16));
                        b.getRoot().setLayoutParams(params);

                        Glide.with(b.getRoot().getContext())
                                .load(ImageUrl.of(ImageUrl.PROFILE, cast.getProfilePath()))
                                .placeholder(R.drawable.avatar)
                                .circleCrop()
                                .into(b.castPosterProfile);

                        b.getRoot().setOnClickListener(v ->
                                navigateTo(CastDetailFragment.newInstance(cast.getId())));
                    }
                    @Override
                    public int getItemCount() { return castList.size(); }
                };
                rv.setLayoutManager(new GridLayoutManager(requireContext(), 3));
                rv.setAdapter(castGridAdapter);
                break;

            case TYPE_SIMILAR_MOVIE:
                // genre_item layout (movie_by_genre_item), 3-column grid
                moviePosterAdapter = new MovieByGenreAdapter(requireContext(), new ArrayList<>(),
                        (movie, v) -> navigateTo(
                                MovieResultDetailsFragment.newInstance(movie.getMovieId()),
                                v, "poster_transition"));
                rv.setLayoutManager(new GridLayoutManager(requireContext(), 3));
                rv.setAdapter(moviePosterAdapter);
                break;

            case TYPE_SIMILAR_TV:
                // genre_item layout (tv_by_genre_item), 3-column grid
                tvPosterAdapter = new TVShowByGenreAdapter(requireContext(), new ArrayList<>(),
                        (tv, v) -> navigateTo(
                                TVShowResultDetailsFragment.newInstance(tv.getTVShowId()),
                                v, "poster_transition"));
                rv.setLayoutManager(new GridLayoutManager(requireContext(), 3));
                rv.setAdapter(tvPosterAdapter);
                break;

            default:
                // ON_AIR, UPCOMING_MOVIES — vertical list using TrendingSearchAdapter
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String userId = user != null ? user.getUid() : null;
                DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
                trendingAdapter = new TrendingSearchAdapter<>(
                        requireContext(), mediaList, this::onMediaClick, db, userId);
                rv.setLayoutManager(new LinearLayoutManager(requireContext()));
                rv.setAdapter(trendingAdapter);
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
                            castList.clear();
                            for (MovieCreditsResponse.Cast c : response.body().getCast()) {
                                castList.add(new CastModel(c.getId(), c.getCastName(),
                                        c.getProfilePath(), c.getCharacter()));
                            }
                            if (castGridAdapter != null) castGridAdapter.notifyDataSetChanged();
                            showSubtitle(tvSubtitle, castList.size() + " members");
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
        safeEnqueue(apiService.getTVShowCredits(TMDBpath.tvShowCredits(mediaId)),
                new Callback<TVShowCreditsResponses>() {
                    @Override
                    public void onResponse(@NonNull Call<TVShowCreditsResponses> call,
                                           @NonNull Response<TVShowCreditsResponses> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            castList.clear();
                            for (TVShowCreditsResponses.Cast c : response.body().getCast()) {
                                castList.add(new CastModel(c.getId(), c.getName(),
                                        c.getProfilePath(), c.getCharacter()));
                            }
                            if (castGridAdapter != null) castGridAdapter.notifyDataSetChanged();
                            showSubtitle(tvSubtitle, castList.size() + " members");
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<TVShowCreditsResponses> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Failed to load cast", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchSimilarMovies(TextView tvSubtitle) {
        safeEnqueue(apiService.getMovieSimilar(TMDBpath.movieSimilar(mediaId)),
                new Callback<MovieSimilarResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MovieSimilarResponse> call,
                                           @NonNull Response<MovieSimilarResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<MovieModel> results = response.body().getResults();
                            if (results == null) results = new ArrayList<>();
                            // Pass results directly — updateData clears mData then adds from newData;
                            // passing the same list reference would result in adding from an empty list.
                            if (moviePosterAdapter != null) moviePosterAdapter.updateData(results);
                            showSubtitle(tvSubtitle, results.size() + " titles");
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<MovieSimilarResponse> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Failed to load similar movies", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchSimilarTV(TextView tvSubtitle) {
        safeEnqueue(apiService.getTVShowSimilar(TMDBpath.tvShowSimilar(mediaId)),
                new Callback<TVShowSimilarResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TVShowSimilarResponse> call,
                                           @NonNull Response<TVShowSimilarResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<TVShowModel> results = response.body().getResults();
                            if (results == null) results = new ArrayList<>();
                            if (tvPosterAdapter != null) tvPosterAdapter.updateData(results);
                            showSubtitle(tvSubtitle, results.size() + " titles");
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<TVShowSimilarResponse> call, @NonNull Throwable t) {
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
                            mediaList.clear();
                            List<TVShowModel> results = response.body().getResults();
                            if (results != null) {
                                for (TVShowModel tv : results) {
                                    if (tv.getBackdropPath() != null) mediaList.add(tv);
                                }
                            }
                            if (trendingAdapter != null) trendingAdapter.updateData(mediaList);
                            showSubtitle(tvSubtitle, "TV Shows · " + mediaList.size() + " titles");
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<TVShowResponse> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Failed to load on-air shows", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUpcomingMovies(TextView tvSubtitle) {
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
                            mediaList.clear();
                            for (MovieModel m : response.body().getResults()) {
                                if (m.getPosterPath() != null) mediaList.add(m);
                            }
                            if (trendingAdapter != null) trendingAdapter.updateData(mediaList);
                            showSubtitle(tvSubtitle, "Movies · " + mediaList.size() + " titles");
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<UpComingMovieResponse> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Failed to load upcoming movies", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUpcomingTV(TextView tvSubtitle) {
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
                            mediaList.clear();
                            for (TVShowModel tv : response.body().getResults()) {
                                if (tv.getPosterPath() != null) mediaList.add(tv);
                            }
                            if (trendingAdapter != null) trendingAdapter.updateData(mediaList);
                            showSubtitle(tvSubtitle, "TV Shows · " + mediaList.size() + " titles");
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<UpComingTVShowsResponse> call,
                                          @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Failed to load upcoming TV shows", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showSubtitle(TextView tv, String text) {
        if (tv == null) return;
        tv.setText(text);
        tv.setVisibility(View.VISIBLE);
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