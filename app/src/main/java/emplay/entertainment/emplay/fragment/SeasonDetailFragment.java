package emplay.entertainment.emplay.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.EpisodeAdapter;
import emplay.entertainment.emplay.api.ApiClient;
import emplay.entertainment.emplay.api.MovieApiService;
import emplay.entertainment.emplay.api.SeasonDetailResponse;
import emplay.entertainment.emplay.api.TMDBpath;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SeasonDetailFragment extends Fragment {

    private static final String ARG_TV_ID = "TV_ID";
    private static final String ARG_SEASON_NUMBER = "SEASON_NUMBER";

    private int tvId;
    private int seasonNumber;
    private MovieApiService apiService;

    private ImageView posterImage;
    private TextView seasonName;
    private TextView airDate;
    private TextView overview;
    private TextView episodeCount;
    private RecyclerView episodesRecyclerView;
    private EpisodeAdapter episodeAdapter;
    private List<SeasonDetailResponse.Episode> episodeList;

    public static SeasonDetailFragment newInstance(int tvId, int seasonNumber) {
        SeasonDetailFragment fragment = new SeasonDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TV_ID, tvId);
        args.putInt(ARG_SEASON_NUMBER, seasonNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.season_details_fragment, container, false);

        posterImage = view.findViewById(R.id.season_detail_poster);
        seasonName = view.findViewById(R.id.season_detail_name);
        airDate = view.findViewById(R.id.season_detail_air_date);
        overview = view.findViewById(R.id.season_detail_overview);
        episodeCount = view.findViewById(R.id.season_detail_episode_count);
        episodesRecyclerView = view.findViewById(R.id.season_detail_episodes_recyclerview);

        episodeList = new ArrayList<>();
        episodeAdapter = new EpisodeAdapter(episodeList, getContext());
        episodesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        episodesRecyclerView.setAdapter(episodeAdapter);
        episodesRecyclerView.setNestedScrollingEnabled(false);

        apiService = ApiClient.getClient().create(MovieApiService.class);

        if (getArguments() != null) {
            tvId = getArguments().getInt(ARG_TV_ID, -1);
            seasonNumber = getArguments().getInt(ARG_SEASON_NUMBER, 1);
            if (tvId != -1) {
                fetchSeasonDetails();
            }
        }

        return view;
    }

    private void fetchSeasonDetails() {
        Call<SeasonDetailResponse> call = apiService.getTVSeasonDetails(
                TMDBpath.tvSeasonDetails(tvId, seasonNumber));
        call.enqueue(new Callback<SeasonDetailResponse>() {
            @Override
            public void onResponse(Call<SeasonDetailResponse> call,
                                   Response<SeasonDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SeasonDetailResponse season = response.body();

                    seasonName.setText(season.getName());
                    airDate.setText(season.getAirDate() != null ? season.getAirDate() : "");
                    overview.setText(season.getOverview() != null
                            && !season.getOverview().isEmpty()
                            ? season.getOverview() : "No overview available.");

                    if (season.getEpisodes() != null) {
                        episodeCount.setText(season.getEpisodes().size() + " Episodes");
                        episodeList.clear();
                        episodeList.addAll(season.getEpisodes());
                        episodeAdapter.notifyDataSetChanged();
                    }

                    if (season.getPosterPath() != null) {
                        Glide.with(requireContext())
                                .load("https://image.tmdb.org/t/p/w500" + season.getPosterPath())
                                .placeholder(R.drawable.placeholder_image)
                                .into(posterImage);
                    }
                }
            }

            @Override
            public void onFailure(Call<SeasonDetailResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load season details", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
