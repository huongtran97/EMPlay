package emplay.entertainment.emplay.fragment.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.RecentlyAddedAdapter;
import emplay.entertainment.emplay.auth.AuthManager;
import emplay.entertainment.emplay.tool.SwipeToDeleteCallback;
import emplay.entertainment.emplay.fragment.details.MovieResultDetailsFragment;
import emplay.entertainment.emplay.fragment.details.TVShowResultDetailsFragment;
import emplay.entertainment.emplay.models.common.MediaItem;
import emplay.entertainment.emplay.models.movie.MovieModel;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;

public class RecentlyAddedFragment extends BaseFragment {

    private RecentlyAddedAdapter adapter;
    private final List<MediaItem> allItems = new ArrayList<>();
    private TextView tvItemCount, tvSortLabel;
    private boolean isSortedAZ = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_recently_added, container, false);

        RecyclerView rvRecentlyAdded = view.findViewById(R.id.rvRecentlyAdded);
        tvItemCount = view.findViewById(R.id.tvItemCount);
        tvSortLabel = view.findViewById(R.id.tvSortLabel);
        view.findViewById(R.id.llSortBtn).setOnClickListener(v -> toggleSort());

        AuthManager auth = AuthManager.getInstance(requireContext());
        String userId = auth.getAuthType() == AuthManager.AuthType.GOOGLE ? auth.getUserId() : null;

        adapter = new RecentlyAddedAdapter(requireContext(), allItems, this::onItemClick, getDbHelper(), userId);
        adapter.setOnItemRemovedListener(count ->
                tvItemCount.setText(getResources().getQuantityString(R.plurals.items_count, count, count)));
        rvRecentlyAdded.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecentlyAdded.setHasFixedSize(true);
        rvRecentlyAdded.setAdapter(adapter);
        new ItemTouchHelper(new SwipeToDeleteCallback(rvRecentlyAdded)).attachToRecyclerView(rvRecentlyAdded);

        loadData();

        return view;
    }

    private void loadData() {
        AuthManager auth = AuthManager.getInstance(requireContext());
        if (auth.getAuthType() != AuthManager.AuthType.GOOGLE) return;
        String userId = auth.getUserId();

        new Thread(() -> {
            List<MovieModel> movies = getDbHelper().getAllMoviesFromDatabase(userId);
            List<TVShowModel> shows = getDbHelper().getSavedTVShows(userId);

            List<MediaItem> loaded = new ArrayList<>();
            loaded.addAll(movies);
            loaded.addAll(shows);
            Collections.sort(loaded, (a, b) -> {
                if (a.getSavedTimestamp() == null || b.getSavedTimestamp() == null) return 0;
                return b.getSavedTimestamp().compareTo(a.getSavedTimestamp());
            });

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    isSortedAZ = false;
                    tvSortLabel.setText(R.string.sort_newest_first);
                    adapter.updateData(loaded);
                    int count = allItems.size();
                    tvItemCount.setText(getResources().getQuantityString(R.plurals.items_count, count, count));
                });
            }
        }).start();
    }


    private void toggleSort() {
        List<MediaItem> sorted = new ArrayList<>(allItems);
        if (isSortedAZ) {
            Collections.sort(sorted, (a, b) -> {
                if (a.getSavedTimestamp() == null || b.getSavedTimestamp() == null) return 0;
                return b.getSavedTimestamp().compareTo(a.getSavedTimestamp());
            });
            tvSortLabel.setText(R.string.sort_newest_first);
        } else {
            Collections.sort(sorted, (a, b) -> {
                String ta = a.getTitle() != null ? a.getTitle() : "";
                String tb = b.getTitle() != null ? b.getTitle() : "";
                return ta.compareToIgnoreCase(tb);
            });
            tvSortLabel.setText(R.string.sort_a_z);
        }
        isSortedAZ = !isSortedAZ;
        adapter.updateData(sorted);
    }

    private void onItemClick(MediaItem item) {
        if (item instanceof MovieModel) {
            navigateTo(MovieResultDetailsFragment.newInstance(item.getMediaId()));
        } else {
            navigateTo(TVShowResultDetailsFragment.newInstance(item.getMediaId()));
        }
    }
}
