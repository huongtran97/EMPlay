package emplay.entertainment.emplay.fragment.common;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.TrendingSearchAdapter;
import emplay.entertainment.emplay.api.common.ApiClient;
import emplay.entertainment.emplay.api.common.MovieApiService;
import emplay.entertainment.emplay.api.movie.MovieResponse;
import emplay.entertainment.emplay.api.tvshow.TVShowResponse;
import emplay.entertainment.emplay.api.common.TMDBpath;
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
    private TrendingSearchAdapter adapter;
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.trending_see_all_view, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        TextView tvTitle = view.findViewById(R.id.trendNow);
        recyclerView = view.findViewById(R.id.rvTrendingFull);

        btnBack.setOnClickListener(v -> {
            if (getFragmentManager() != null) {
                getFragmentManager().popBackStack();
            }
        });
        
        tvTitle.setText(isTV ? "Trending TV Shows" : "Trending Movies");

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TrendingSearchAdapter(getContext(), trendingList, this::onItemClick);
        recyclerView.setAdapter(adapter);

        apiService = ApiClient.getClient().create(MovieApiService.class);
        fetchTrendingData();

        return view;
    }

    private void fetchTrendingData() {
        if (isTV) {
            safeEnqueue(apiService.getTrendingTVShows(TMDBpath.trendingTVShows()), new Callback<TVShowResponse>() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onResponse(@NonNull Call<TVShowResponse> call, @NonNull Response<TVShowResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        trendingList.clear();
                        List<TVShowModel> results = response.body().getResults();
                        if (results != null) {
                            Collections.sort(results, (t1, t2) -> Double.compare(t2.getVoteAverage(), t1.getVoteAverage()));
                            trendingList.addAll(results);
                        }
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<TVShowResponse> call, @NonNull Throwable t) {
                    Toast.makeText(getContext(), "Failed to load trending TV shows", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            safeEnqueue(apiService.getTrendingMovies(TMDBpath.trendingMovies()), new Callback<MovieResponse>() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        trendingList.clear();
                        List<MovieModel> results = response.body().getResults();
                        if (results != null) {
                            Collections.sort(results, (m1, m2) -> Double.compare(m2.getVoteAverage(), m1.getVoteAverage()));
                            trendingList.addAll(results);
                        }
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                    Toast.makeText(getContext(), "Failed to load trending movies", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void onItemClick(MediaItem item) {
        if (item instanceof MovieModel) {
            navigateTo(MovieResultDetailsFragment.newInstance(item.getMediaId()));
        } else if (item instanceof TVShowModel) {
            navigateTo(TVShowResultDetailsFragment.newInstance(item.getMediaId()));
        }
    }
}
