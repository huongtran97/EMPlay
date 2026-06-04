package emplay.entertainment.emplay.fragment.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.WatchlistGridAdapter;
import emplay.entertainment.emplay.api.auth.TMDBWatchlistApiService;
import emplay.entertainment.emplay.api.auth.model.TMDBMovieWatchlistResponse;
import emplay.entertainment.emplay.api.auth.model.TMDBTVWatchlistResponse;
import emplay.entertainment.emplay.api.common.ApiClient;
import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.auth.AuthManager;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.fragment.details.MovieResultDetailsFragment;
import emplay.entertainment.emplay.fragment.details.TVShowResultDetailsFragment;
import emplay.entertainment.emplay.fragment.layout.WatchlistFragment;
import emplay.entertainment.emplay.models.common.MediaItem;
import emplay.entertainment.emplay.models.movie.MovieModel;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WatchlistTabFragment extends Fragment {

    private static final String ARG_TYPE = "media_type";
    private String type;
    private RecyclerView rvWatchlist;
    private WatchlistGridAdapter adapter;
    private DatabaseHelper dbHelper;
    private TMDBWatchlistApiService watchlistApiService;

    public static WatchlistTabFragment newInstance(String type) {
        WatchlistTabFragment fragment = new WatchlistTabFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getString(ARG_TYPE);
        }
        dbHelper = DatabaseHelper.getInstance(requireContext());
        watchlistApiService = ApiClient.getClient().create(TMDBWatchlistApiService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.watchlist_tab_recyclerview, container, false);
        rvWatchlist = view.findViewById(R.id.rvWatchlist);

        adapter = new WatchlistGridAdapter(requireContext(), this::onItemClick);
        rvWatchlist.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        rvWatchlist.setAdapter(adapter);

        loadData();

        return view;
    }

    private void loadData() {
        AuthManager auth = AuthManager.getInstance(requireContext());
        switch (auth.getAuthType()) {
            case GOOGLE:
                if ("movie".equals(type)) loadMoviesFromSqlite(auth.getUserId());
                else loadTVFromSqlite(auth.getUserId());
                break;
            case TMDB:
                if ("movie".equals(type)) loadMoviesFromTmdb(auth.getTMDBAccountId(), auth.getTMDBSessionId());
                else loadTVFromTmdb(auth.getTMDBAccountId(), auth.getTMDBSessionId());
                break;
            default:
                updateUI(new ArrayList<>());
                break;
        }
    }

    private void loadMoviesFromSqlite(String userId) {
        new Thread(() -> {
            List<MovieModel> movies = dbHelper.getAllMoviesFromDatabase(userId);
            List<MediaItem> items = new ArrayList<>(movies);
            Collections.sort(items, (a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()));
            if (isAdded()) requireActivity().runOnUiThread(() -> updateUI(items));
        }).start();
    }

    private void loadTVFromSqlite(String userId) {
        new Thread(() -> {
            List<TVShowModel> shows = dbHelper.getSavedTVShows(userId);
            List<MediaItem> items = new ArrayList<>(shows);
            Collections.sort(items, (a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()));
            if (isAdded()) requireActivity().runOnUiThread(() -> updateUI(items));
        }).start();
    }

    private void loadMoviesFromTmdb(String accountId, String sessionId) {
        watchlistApiService.getWatchlistMovies(
                TMDBpath.accountWatchlistMovies(accountId), sessionId, 1)
                .enqueue(new Callback<TMDBMovieWatchlistResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TMDBMovieWatchlistResponse> call,
                                           @NonNull Response<TMDBMovieWatchlistResponse> response) {
                        if (!isAdded()) return;
                        List<MediaItem> items = new ArrayList<>();
                        if (response.isSuccessful() && response.body() != null
                                && response.body().results != null) {
                            items.addAll(response.body().results);
                        }
                        requireActivity().runOnUiThread(() -> updateUI(items));
                    }

                    @Override
                    public void onFailure(@NonNull Call<TMDBMovieWatchlistResponse> call, @NonNull Throwable t) {
                        if (isAdded()) requireActivity().runOnUiThread(() -> updateUI(new ArrayList<>()));
                    }
                });
    }

    private void loadTVFromTmdb(String accountId, String sessionId) {
        watchlistApiService.getWatchlistTV(
                TMDBpath.accountWatchlistTVShows(accountId), sessionId, 1)
                .enqueue(new Callback<TMDBTVWatchlistResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TMDBTVWatchlistResponse> call,
                                           @NonNull Response<TMDBTVWatchlistResponse> response) {
                        if (!isAdded()) return;
                        List<MediaItem> items = new ArrayList<>();
                        if (response.isSuccessful() && response.body() != null
                                && response.body().results != null) {
                            items.addAll(response.body().results);
                        }
                        requireActivity().runOnUiThread(() -> updateUI(items));
                    }

                    @Override
                    public void onFailure(@NonNull Call<TMDBTVWatchlistResponse> call, @NonNull Throwable t) {
                        if (isAdded()) requireActivity().runOnUiThread(() -> updateUI(new ArrayList<>()));
                    }
                });
    }

    private void updateUI(List<MediaItem> items) {
        adapter.setData(items);
        if (getParentFragment() instanceof WatchlistFragment) {
            ((WatchlistFragment) getParentFragment()).updateItemCount(items.size(), type);
        }
    }

    private void onItemClick(MediaItem item) {
        Fragment fragment;
        if (item instanceof MovieModel) {
            fragment = MovieResultDetailsFragment.newInstance(item.getMediaId());
        } else {
            fragment = TVShowResultDetailsFragment.newInstance(item.getMediaId());
        }

        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}