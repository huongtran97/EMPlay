package emplay.entertainment.emplay.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.BuildConfig;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.TVShowByGenreAdapter;
import emplay.entertainment.emplay.api.ApiClient;
import emplay.entertainment.emplay.api.MovieApiService;
import emplay.entertainment.emplay.api.TVShowResponse;
import emplay.entertainment.emplay.models.GenresModel;
import emplay.entertainment.emplay.models.SharedViewModel;
import emplay.entertainment.emplay.models.TVShowModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TVShowsByGenresFragment extends Fragment {

    private static final String ARG_GENRE_ID = "GENRE_ID";
    private static final String ARG_GENRE_NAME = "GENRE_NAME";
    private RecyclerView tvByGenreRecyclerview;
    private SharedViewModel viewModel;
    private TVShowByGenreAdapter tvByGenreAdapter;
    private TextView genreName;
    private List<TVShowModel> tvByGenreList;
    private List<GenresModel> genresList;
    private MovieApiService apiService;
    private int genreId;

    public String useApiKey() {
        String apiKey = BuildConfig.API_KEY;
        return apiKey;
    }

    public static TVShowsByGenresFragment newInstance(int genreId, String genreName) {
        TVShowsByGenresFragment fragment = new TVShowsByGenresFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_GENRE_ID, genreId);
        args.putString(ARG_GENRE_NAME, genreName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("MovieByGenresFragment", "onCreateView called");
        View view = inflater.inflate(R.layout.tv_by_genre_view, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        tvByGenreRecyclerview = view.findViewById(R.id.tv_by_genre_recyclerview);
        genreName = view.findViewById(R.id.tv_genres);

        tvByGenreList = new ArrayList<>();
        genresList = new ArrayList<>();

        tvByGenreAdapter = new TVShowByGenreAdapter(tvByGenreList, getContext(), this::onItemClick);

        tvByGenreRecyclerview.setLayoutManager(new GridLayoutManager(getContext(), 3));
        tvByGenreRecyclerview.setAdapter(tvByGenreAdapter);

        apiService = ApiClient.getClient().create(MovieApiService.class);
        Log.d("MovieByGenresFragment", "ViewModel setup complete");

        if (getArguments() != null) {
            genreId = getArguments().getInt(ARG_GENRE_ID, -1);
            String genreNameString = getArguments().getString(ARG_GENRE_NAME, "Unknown Genre");
            genreName.setText(genreNameString);

            if (genreId != -1) {
                fetchTVShowsByGenre(genreId);
            } else {
                Toast.makeText(getContext(), "Invalid genre ID", Toast.LENGTH_SHORT).show();
            }
        }

        return view;
    }

    private void onItemClick(TVShowModel tvShowModel) {
        ShowResultTVShowDetailsFragment showResultTVShowDetailsFragment = ShowResultTVShowDetailsFragment.newInstance(tvShowModel.getId());
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, showResultTVShowDetailsFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void fetchTVShowsByGenre(int genreId) {
        Call<TVShowResponse> call = apiService.getTVShowsByGenre(useApiKey(), genreId);
        Log.d("MovieByGenresFragment", "Request URL: " + call.request().url());

        call.enqueue(new Callback<TVShowResponse>() {
            @Override
            public void onResponse(Call<TVShowResponse> call, Response<TVShowResponse> response) {
                Log.d("MovieByGenresFragment", "API Response received. Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    List<TVShowModel> tv = response.body().getResults();
                    Log.d("MovieByGenresFragment", "Fetched movies count: " + (tv != null ? tv.size() : 0));
                    if (tv != null && !tv.isEmpty()) {
                        tvByGenreAdapter.updateData(tv);
                        Log.d("MovieByGenresFragment", "Movies updated in adapter");
                    } else {
                        Log.d("MovieByGenresFragment", "No movies found for this genre");
                    }
                } else {
                    Log.e("MovieByGenresFragment", "Error in response: " + response.code() + ", Message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<TVShowResponse> call, Throwable t) {
                Log.e("MovieByGenresFragment", "API call failed: " + t.getMessage());
                Toast.makeText(getContext(), "Failed to load movies. Please try again.", Toast.LENGTH_SHORT).show();
                handleApiFailure("MovieByGenresFragment", t);
            }
        });
    }

    private void handleApiFailure(String tag, Throwable t) {
        Log.e(tag, "API call failed: " + t.getMessage());
        Toast.makeText(getContext(), "Failed to load data. Please try again.", Toast.LENGTH_SHORT).show();
    }
}
