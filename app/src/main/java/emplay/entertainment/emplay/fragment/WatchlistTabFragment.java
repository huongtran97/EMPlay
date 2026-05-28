package emplay.entertainment.emplay.fragment;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.WatchlistGridAdapter;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.models.MediaItem;
import emplay.entertainment.emplay.models.MovieModel;
import emplay.entertainment.emplay.models.TVShowModel;

public class WatchlistTabFragment extends Fragment {

    private static final String ARG_TYPE = "media_type";
    private String type;
    private RecyclerView rvWatchlist;
    private WatchlistGridAdapter adapter;
    private DatabaseHelper dbHelper;

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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_watchlist_tab, container, false);
        rvWatchlist = view.findViewById(R.id.rvWatchlist);

        adapter = new WatchlistGridAdapter(requireContext(), this::onItemClick);
        rvWatchlist.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        rvWatchlist.setAdapter(adapter);

        loadData();

        return view;
    }

    private void loadData() {
        if ("movie".equals(type)) {
            loadSavedMovies();
        } else if ("tv".equals(type)) {
            loadSavedTV();
        }
    }

    private void loadSavedMovies() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String userId = user.getUid();

        new Thread(() -> {
            List<MovieModel> movies = dbHelper.getAllMoviesFromDatabase(userId);
            List<MediaItem> items = new ArrayList<>(movies);
            Collections.sort(items, (a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()));
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> updateUI(items));
            }
        }).start();
    }

    private void loadSavedTV() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String userId = user.getUid();

        new Thread(() -> {
            List<TVShowModel> shows = dbHelper.getSavedTVShows(userId);
            List<MediaItem> items = new ArrayList<>(shows);
            Collections.sort(items, (a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()));
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> updateUI(items));
            }
        }).start();
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
            fragment = ShowResultDetailsFragment.newInstance(item.getMediaId());
        } else {
            fragment = ShowResultTVShowDetailsFragment.newInstance(item.getMediaId());
        }
        
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
