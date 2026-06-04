package emplay.entertainment.emplay.fragment.common;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.RecentlyAddedAdapter;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.fragment.details.MovieResultDetailsFragment;
import emplay.entertainment.emplay.fragment.details.TVShowResultDetailsFragment;
import emplay.entertainment.emplay.models.common.MediaItem;
import emplay.entertainment.emplay.models.movie.MovieModel;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;

public class RecentlyAddedFragment extends BaseFragment {

    private RecentlyAddedAdapter adapter;
    private final List<MediaItem> allItems = new ArrayList<>();
    private TextView tvItemCount, tvSortLabel;
    private DatabaseHelper dbHelper;
    private boolean isSortedAZ = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recently_added_view, container, false);

        RecyclerView rvRecentlyAdded = view.findViewById(R.id.rvRecentlyAdded);
        tvItemCount = view.findViewById(R.id.tvItemCount);
        tvSortLabel = view.findViewById(R.id.tvSortLabel);
        dbHelper = DatabaseHelper.getInstance(requireContext());

        view.findViewById(R.id.ivBack).setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        view.findViewById(R.id.llSortBtn).setOnClickListener(v -> toggleSort());

        adapter = new RecentlyAddedAdapter(requireContext(), allItems, this::onItemClick);
        rvRecentlyAdded.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecentlyAdded.setAdapter(adapter);

        loadData();

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String userId = user.getUid();

        new Thread(() -> {
            List<MovieModel> movies = dbHelper.getAllMoviesFromDatabase(userId);
            List<TVShowModel> shows = dbHelper.getSavedTVShows(userId);
            
            allItems.clear();
            allItems.addAll(movies);
            allItems.addAll(shows);
            
            // Default sort: Newest first (by timestamp)
            Collections.sort(allItems, (a, b) -> {
                if (a.getSavedTimestamp() == null || b.getSavedTimestamp() == null) return 0;
                return b.getSavedTimestamp().compareTo(a.getSavedTimestamp());
            });

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                    tvItemCount.setText(getString(R.string.items_count, allItems.size()));
                });
            }
        }).start();
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    private void toggleSort() {
        if (isSortedAZ) {
            // Sort by Newest
            Collections.sort(allItems, (a, b) -> {
                if (a.getSavedTimestamp() == null || b.getSavedTimestamp() == null) return 0;
                return b.getSavedTimestamp().compareTo(a.getSavedTimestamp());
            });
            tvSortLabel.setText("Newest first");
        } else {
            // Sort A-Z
            Collections.sort(allItems, (a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()));
            tvSortLabel.setText("A-Z");
        }
        isSortedAZ = !isSortedAZ;
        adapter.notifyDataSetChanged();
    }

    private void onItemClick(MediaItem item) {
        if (item instanceof MovieModel) {
            navigateTo(MovieResultDetailsFragment.newInstance(item.getMediaId()));
        } else {
            navigateTo(TVShowResultDetailsFragment.newInstance(item.getMediaId()));
        }
    }
}
