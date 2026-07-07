package emplay.entertainment.emplay.fragment.common;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import emplay.entertainment.emplay.tool.RecyclerViewHelper;
import emplay.entertainment.emplay.tool.ToastHelper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.TrendingSearchAdapter;
import emplay.entertainment.emplay.api.common.ApiClient;
import emplay.entertainment.emplay.api.common.MovieApiService;
import emplay.entertainment.emplay.api.movie.MovieResponse;
import emplay.entertainment.emplay.api.tvshow.TVShowResponse;
import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.fragment.details.MovieResultDetailsFragment;
import emplay.entertainment.emplay.fragment.details.TVShowResultDetailsFragment;
import emplay.entertainment.emplay.models.common.MediaItem;
import emplay.entertainment.emplay.models.movie.MovieModel;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrendingSeeAllFragment extends BaseFragment {

    private static final String ARG_IS_TV = "is_tv";
    private boolean isTV;
    private RecyclerView recyclerView;
    private TrendingSearchAdapter<?> adapter;
    private final List<MediaItem> trendingList = new ArrayList<>();
    private MovieApiService apiService;

    public static TrendingSeeAllFragment newInstance(boolean isTV) {
        TrendingSeeAllFragment fragment = new TrendingSeeAllFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_TV, isTV);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isTV = getArguments().getBoolean(ARG_IS_TV);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_trending_see_all, container, false);

        TextView tvTitle = view.findViewById(R.id.trendNow);
        recyclerView     = view.findViewById(R.id.rvTrendingFull);

        tvTitle.setText(isTV ? "Trending TV Shows" : "Trending Movies");

        // Resolve Firebase user for My List inline actions
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : null;
        DatabaseHelper dbHelper = getDbHelper();

        adapter = new TrendingSearchAdapter<>(requireContext(), trendingList, this::onItemClick,
                dbHelper, userId);
        RecyclerViewHelper.setupVertical(recyclerView, requireContext(), adapter);

        apiService = ApiClient.getClient().create(MovieApiService.class);
        fetchTrendingData();

        return view;
    }

    private void fetchTrendingData() {
        if (isTV) {
            safeEnqueue(apiService.getTrendingTVShows(TMDBpath.trendingTVShows()),
                    new Callback<TVShowResponse>() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onResponse(@NonNull Call<TVShowResponse> call,
                                               @NonNull Response<TVShowResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                trendingList.clear();
                                List<TVShowModel> results = response.body().getResults();
                                if (results != null) {
                                    results.removeIf(tv -> tv.getPosterPath() == null && tv.getBackdropPath() == null);
                                    trendingList.addAll(results);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<TVShowResponse> call, @NonNull Throwable t) {
                            ToastHelper.show(getContext(), "Failed to load trending TV shows");
                        }
                    });
        } else {
            safeEnqueue(apiService.getTrendingMovies(TMDBpath.trendingMovies()),
                    new Callback<MovieResponse>() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onResponse(@NonNull Call<MovieResponse> call,
                                               @NonNull Response<MovieResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                trendingList.clear();
                                List<MovieModel> results = response.body().getResults();
                                if (results != null) {
                                    results.removeIf(m -> m.getPosterPath() == null && m.getBackdropPath() == null);
                                    trendingList.addAll(results);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                            ToastHelper.show(getContext(), "Failed to load trending movies");
                        }
                    });
        }
    }

    private void onItemClick(MediaItem item, View sharedElement) {
        if (item instanceof MovieModel) {
            navigateTo(MovieResultDetailsFragment.newInstance(item.getMediaId()), sharedElement, "poster_transition");
        } else if (item instanceof TVShowModel) {
            navigateTo(TVShowResultDetailsFragment.newInstance(item.getMediaId()), sharedElement, "poster_transition");
        }
    }
}