package emplay.entertainment.emplay.fragment.layout;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import emplay.entertainment.emplay.tool.RecyclerViewHelper;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.activity.LoginActivity;
import emplay.entertainment.emplay.activity.MainActivity;
import emplay.entertainment.emplay.adapter.common.WatchlistGridAdapter;
import emplay.entertainment.emplay.api.auth.TMDBWatchlistApiService;
import emplay.entertainment.emplay.api.auth.model.TMDBMovieWatchlistResponse;
import emplay.entertainment.emplay.api.auth.model.TMDBTVWatchlistResponse;
import emplay.entertainment.emplay.api.common.ApiClient;
import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.auth.AuthManager;
import emplay.entertainment.emplay.databinding.LayoutWatchlistBinding;
import emplay.entertainment.emplay.fragment.common.BaseFragment;
import emplay.entertainment.emplay.fragment.details.MovieResultDetailsFragment;
import emplay.entertainment.emplay.fragment.details.TVShowResultDetailsFragment;
import emplay.entertainment.emplay.models.common.MediaItem;
import emplay.entertainment.emplay.models.movie.MovieModel;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WatchlistFragment extends BaseFragment {

    private static final int SORT_DATE = 0;
    private static final int SORT_TITLE = 1;
    private static final int SORT_RATING = 2;

    private static final int FILTER_ALL = 0;
    private static final int FILTER_MOVIES = 1;
    private static final int FILTER_TV = 2;

    private LayoutWatchlistBinding binding;
    private WatchlistGridAdapter adapter;
    private TMDBWatchlistApiService watchlistApiService;

    private final List<MediaItem> allItems = new ArrayList<>();
    private int currentFilter = FILTER_ALL;
    private int currentSort = SORT_DATE;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutWatchlistBinding.inflate(inflater, container, false);
        watchlistApiService = ApiClient.getClient().create(TMDBWatchlistApiService.class);

        setupRecyclerView();
        setupFilterChips();
        setupSortButton();
        loadData();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupRecyclerView() {
        adapter = new WatchlistGridAdapter(requireContext(), this::onItemClick);
        RecyclerViewHelper.setupGrid(binding.rvWatchlist, requireContext(), 3, adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION || binding == null) return;

                MediaItem removed = adapter.getItem(pos);
                adapter.removeItem(pos);
                allItems.remove(removed);
                updateCountLabel();

                Snackbar.make(binding.getRoot(), R.string.removed_from_list, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo, v -> {
                            allItems.add(removed);
                            applyFilterAndSort();
                        })
                        .show();
            }
        }).attachToRecyclerView(binding.rvWatchlist);
    }

    private void setupFilterChips() {
        selectChip(binding.chipAll);
        binding.chipAll.setOnClickListener(v -> setFilter(FILTER_ALL, (TextView) v));
        binding.chipMovies.setOnClickListener(v -> setFilter(FILTER_MOVIES, (TextView) v));
        binding.chipTV.setOnClickListener(v -> setFilter(FILTER_TV, (TextView) v));
    }

    private void setFilter(int filter, TextView chip) {
        currentFilter = filter;
        selectChip(chip);
        applyFilterAndSort();
    }

    private void selectChip(TextView selected) {
        binding.chipAll.setSelected(false);
        binding.chipMovies.setSelected(false);
        binding.chipTV.setSelected(false);
        selected.setSelected(true);
    }

    private void setupSortButton() {
        binding.llSortBtn.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), v);
            popup.getMenu().add(0, SORT_DATE, 0, R.string.sort_date_added);
            popup.getMenu().add(0, SORT_TITLE, 1, R.string.sort_title_az);
            popup.getMenu().add(0, SORT_RATING, 2, R.string.sort_rating);
            popup.setOnMenuItemClickListener(item -> {
                currentSort = item.getItemId();
                binding.tvSortLabel.setText(item.getTitle());
                applyFilterAndSort();
                return true;
            });
            popup.show();
        });
    }

    private void loadData() {
        AuthManager auth = AuthManager.getInstance(requireContext());
        switch (auth.getAuthType()) {
            case TMDB:
                loadFromTmdb(auth.getTMDBAccountId(), auth.getTMDBSessionId());
                break;
            case GOOGLE:
                loadFromSqlite(auth.getUserId());
                break;
            default:
                showEmptyState(true);
                break;
        }
    }

    private void loadFromSqlite(String userId) {
        new Thread(() -> {
            List<MediaItem> combined = new ArrayList<>();
            combined.addAll(getDbHelper().getAllMoviesFromDatabase(userId));
            combined.addAll(getDbHelper().getSavedTVShows(userId));
            safeRunOnUiThread(() -> onDataLoaded(combined));
        }).start();
    }

    private void loadFromTmdb(String accountId, String sessionId) {
        List<MediaItem> combined = new ArrayList<>();
        int[] pending = {2};

        safeEnqueue(
                watchlistApiService.getWatchlistMovies(TMDBpath.accountWatchlistMovies(accountId), sessionId, 1),
                new Callback<TMDBMovieWatchlistResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TMDBMovieWatchlistResponse> call, @NonNull Response<TMDBMovieWatchlistResponse> response) {
                        if (response.isSuccessful() && response.body() != null)
                            combined.addAll(response.body().results);
                        if (--pending[0] == 0) onDataLoaded(combined);
                    }
                    @Override
                    public void onFailure(@NonNull Call<TMDBMovieWatchlistResponse> call, @NonNull Throwable t) {
                        if (--pending[0] == 0) onDataLoaded(combined);
                    }
                });

        safeEnqueue(
                watchlistApiService.getWatchlistTV(TMDBpath.accountWatchlistTVShows(accountId), sessionId, 1),
                new Callback<TMDBTVWatchlistResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TMDBTVWatchlistResponse> call, @NonNull Response<TMDBTVWatchlistResponse> response) {
                        if (response.isSuccessful() && response.body() != null)
                            combined.addAll(response.body().results);
                        if (--pending[0] == 0) onDataLoaded(combined);
                    }
                    @Override
                    public void onFailure(@NonNull Call<TMDBTVWatchlistResponse> call, @NonNull Throwable t) {
                        if (--pending[0] == 0) onDataLoaded(combined);
                    }
                });
    }

    private void onDataLoaded(List<MediaItem> items) {
        allItems.clear();
        allItems.addAll(items);
        applyFilterAndSort();
    }

    private void applyFilterAndSort() {
        if (binding == null) return;

        List<MediaItem> filtered = new ArrayList<>();
        for (MediaItem item : allItems) {
            if (currentFilter == FILTER_ALL
                    || (currentFilter == FILTER_MOVIES && item instanceof MovieModel)
                    || (currentFilter == FILTER_TV && item instanceof TVShowModel)) {
                filtered.add(item);
            }
        }

        switch (currentSort) {
            case SORT_TITLE:
                Collections.sort(filtered, (a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()));
                break;
            case SORT_RATING:
                Collections.sort(filtered, (a, b) -> Double.compare(b.getVoteAverage(), a.getVoteAverage()));
                break;
            default:
                break;
        }

        boolean empty = filtered.isEmpty();
        binding.rvWatchlist.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            showEmptyState(false);
        } else {
            binding.emptyLayout.getRoot().setVisibility(View.GONE);
        }

        adapter.setData(filtered);
        updateCountLabel();
    }

    private void showEmptyState(boolean isGuest) {
        if (binding == null) return;
        binding.rvWatchlist.setVisibility(View.GONE);
        binding.emptyLayout.getRoot().setVisibility(View.VISIBLE);

        if (isGuest) {
            binding.emptyLayout.tvEmptyTitle.setText(R.string.guest_empty_list_title);
            binding.emptyLayout.btnExplore.setText(R.string.guest_sign_in_cta);
            binding.emptyLayout.btnExplore.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), LoginActivity.class)));
        } else {
            binding.emptyLayout.tvEmptyTitle.setText(R.string.empty_watchlist_title);
            binding.emptyLayout.btnExplore.setText(R.string.empty_watchlist_cta);
            binding.emptyLayout.btnExplore.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    getActivity().findViewById(R.id.nav_home).performClick();
                }
            });
        }

        updateCountLabel();
    }

    private void updateCountLabel() {
        if (binding == null) return;
        int count = adapter.getItemCount();
        binding.tvItemCount.setText(getResources().getQuantityString(R.plurals.count_item, count, count));
    }

    private void onItemClick(MediaItem item, View sharedElement) {
        if (item instanceof MovieModel) {
            navigateTo(MovieResultDetailsFragment.newInstance(item.getMediaId()), sharedElement, "poster_transition");
        } else if (item instanceof TVShowModel) {
            navigateTo(TVShowResultDetailsFragment.newInstance(item.getMediaId()), sharedElement, "poster_transition");
        }
    }
}