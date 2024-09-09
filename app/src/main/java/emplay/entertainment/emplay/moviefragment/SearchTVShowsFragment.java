package emplay.entertainment.emplay.moviefragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.LanguageMapper;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.ApiClient;
import emplay.entertainment.emplay.api.MovieApiService;
import emplay.entertainment.emplay.api.TVShowResponse;
import emplay.entertainment.emplay.models.SharedViewModel;
import emplay.entertainment.emplay.models.TVShowModel;
import emplay.entertainment.emplay.movieadapter.SearchTVAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchTVShowsFragment extends Fragment {
    private static final String API_KEY = "ff3dce8592d15d036bf53cbedeca224b";

    private SearchTVAdapter searchAdapter;
    private List<TVShowModel> searchTVList;
    private MovieApiService apiService;
    private EditText inputSearch;
    private SharedViewModel viewModel;
    private LinearLayout searchMovieLayout;
    private LinearLayout searchTVShowLayout;
    private boolean isMoviesSearch = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.movie_search_view, container, false);

        // Initialize the ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Initialize UI elements
        inputSearch = view.findViewById(R.id.input_title);
        RecyclerView recyclerView = view.findViewById(R.id.search_recycler_view);
        ImageButton searchButton = view.findViewById(R.id.search_movie_btn);
        searchMovieLayout = view.findViewById(R.id.menu_movie_layout);
        searchTVShowLayout = view.findViewById(R.id.menu_tvshow_layout);

        // Initialize menu item clicks
        searchMovieLayout.setOnClickListener(v -> onMovieClick());
        searchTVShowLayout.setOnClickListener(v -> onTVShowClick());

        searchTVList = new ArrayList<>();
        searchAdapter = new SearchTVAdapter(searchTVList, this::showTVDetails);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(searchAdapter);

        apiService = ApiClient.getClient().create(MovieApiService.class);

        // Set up TextWatcher for real-time search
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Empty action
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Perform the search as text changes
                performSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Empty action
            }
        });

        // Set up OnClickListener for Search Button
        searchButton.setOnClickListener(v -> {
            performSearch();
            hideKeyboard(); // Hide the keyboard when search button is clicked
        });

        // Set up OnEditorActionListener to hide the keyboard when Enter is pressed
        inputSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                hideKeyboard();
                return true;
            }
            return false;
        });

        // Restore state if needed
        if (savedInstanceState != null) {
            isMoviesSearch = savedInstanceState.getBoolean("isMoviesSearch", false);
            inputSearch.setText(savedInstanceState.getString("searchQuery", ""));
        }

        // Set initial selected state
        updateMenuSelection();

        // Observe LiveData from ViewModel
        setupObservers();

        return view;
    }

    private void updateMenuSelection() {
        if (viewModel.getIsTVShowSearch().getValue() != null && viewModel.getIsTVShowSearch().getValue()) {
            searchMovieLayout.setSelected(false);
            searchTVShowLayout.setSelected(true);
        } else {
            searchMovieLayout.setSelected(true);
            searchTVShowLayout.setSelected(false);
        }
    }

    private void onTVShowClick() {
        // Handle the TV Show item click
        viewModel.setIsTVShowSearch(true);
        updateMenuSelection();
        replaceFragment(new SearchTVShowsFragment(), "SearchTVShowsFragment");
    }

    private void onMovieClick() {
        // Handle the movie item click
        viewModel.setIsTVShowSearch(false);
        updateMenuSelection();
        replaceFragment(new SearchMoviesFragment(), "SearchMoviesFragment");
    }

    private void performSearch() {
        String query = inputSearch.getText().toString().trim();
        if (!query.isEmpty()) {
            if (isMoviesSearch) {
                Fragment tvShowsFragment = getParentFragmentManager().findFragmentByTag("SearchTVShowsFragment");
                if (tvShowsFragment instanceof SearchTVShowsFragment) {
                    ((SearchTVShowsFragment) tvShowsFragment).searchTVShows(query);
                } else {
                    Log.e("PerformSearch", "SearchTVShowsFragment is not found or not of the correct type");
                }
            } else {
                // Call searchTVShows method
                searchTVShows(query);
            }
            viewModel.setLastSearchWasTVShow(isMoviesSearch); // Update ViewModel with the correct search type
        } else {
            // Handle empty query case
            searchTVList.clear();
            searchAdapter.notifyDataSetChanged();
        }
    }

    void searchTVShows(String query) {
        Call<TVShowResponse> call = apiService.searchTVShows(API_KEY, query);
        call.enqueue(new Callback<TVShowResponse>() {
            @Override
            public void onResponse(Call<TVShowResponse> call, Response<TVShowResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    searchTVList.clear();
                    List<TVShowModel> tvShows = response.body().getResults();
                    if (tvShows != null && !tvShows.isEmpty()) {
                        for (TVShowModel tv : tvShows) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                tv.setOriginalLanguage(LanguageMapper.getLanguageName(tv.getOriginalLanguage()));
                            }
                        }
                        searchTVList.addAll(tvShows);
                        searchAdapter.notifyDataSetChanged(); // Notify adapter of data change
                        viewModel.setSearchTVResults(tvShows); // Update ViewModel
                    } else {
                        Toast.makeText(getContext(), "No results found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("SearchTVShows", "Failed to get results. Status code: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e("SearchTVShows", "Error response: " + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getContext(), "Failed to get results", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TVShowResponse> call, Throwable t) {
                Log.e("SearchTVShows", "Failed to load TV shows", t);
                Toast.makeText(getContext(), "Failed to load TV shows", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showTVDetails(TVShowModel tv) {
        if (tv != null) {
            ShowResultTVShowDetailsFragment showResultTVShowDetailsFragment = ShowResultTVShowDetailsFragment.newInstance(tv.getId());
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, showResultTVShowDetailsFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            Toast.makeText(getContext(), "TV show details are not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupObservers() {
        viewModel.getSearchTVResults().observe(getViewLifecycleOwner(), tvShows -> {
            searchTVList.clear();
            searchTVList.addAll(tvShows);
            searchAdapter.notifyDataSetChanged();
        });

        viewModel.getIsTVShowSearch().observe(getViewLifecycleOwner(), isTVShowSearch -> {
            this.isMoviesSearch = !isTVShowSearch;
        });
    }

    private void replaceFragment(Fragment fragment, String tag) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment,tag);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
