package emplay.entertainment.emplay.fragment.details;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.tvshow.EpisodeAdapter;
import emplay.entertainment.emplay.api.common.ApiClient;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.api.common.MovieApiService;
import emplay.entertainment.emplay.api.tvshow.SeasonDetailResponse;
import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.fragment.common.BaseFragment;
import emplay.entertainment.emplay.tool.ReadHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *  Shows the detail page for a single TV season: poster, overview (with Read More/Less),
 *  and the full episode list below.
 */
public class SeasonDetailFragment extends BaseFragment {

    private static final String ARG_TV_ID = "TV_ID";
    private static final String ARG_SEASON_NUMBER = "SEASON_NUMBER";

    private int tvId;
    private int seasonNumber;
    private MovieApiService apiService;

    private ImageView posterImage;
    private TextView seasonName;
    private TextView airDate;
    private TextView overview;
    private TextView readMoreText;
    private TextView episodeCount;
    private RecyclerView episodesRecyclerView;
    private EpisodeAdapter episodeAdapter;
    private boolean isExpanded = false;

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
        View view = inflater.inflate(R.layout.layout_season_details, container, false);

        posterImage = view.findViewById(R.id.season_detail_poster);
        seasonName = view.findViewById(R.id.season_detail_name);
        airDate = view.findViewById(R.id.season_detail_air_date);
        overview = view.findViewById(R.id.season_detail_overview);
        readMoreText = view.findViewById(R.id.read_more_text);
        episodeCount = view.findViewById(R.id.season_detail_episode_count);
        episodesRecyclerView = view.findViewById(R.id.season_detail_episodes_recyclerview);

        episodeAdapter = new EpisodeAdapter(requireContext());
        episodesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        episodesRecyclerView.setAdapter(episodeAdapter);
        episodesRecyclerView.setNestedScrollingEnabled(false);

        // Read More / Less
        ReadHelper.setup(overview, readMoreText, isExpanded, expanded -> isExpanded = expanded);

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
        safeEnqueue(apiService.getTVSeasonDetails(
                TMDBpath.tvSeasonDetails(tvId, seasonNumber)), new Callback<SeasonDetailResponse>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<SeasonDetailResponse> call,
                                   @NonNull Response<SeasonDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SeasonDetailResponse season = response.body();

                    seasonName.setText(season.getName());
                    airDate.setText(season.getAirDate() != null ? season.getAirDate() : "");
                    overview.setText(season.getOverview() != null
                            && !season.getOverview().isEmpty()
                            ? season.getOverview() : "No overview available.");

                    ReadHelper.bind(overview, readMoreText, isExpanded);

                    if (season.getEpisodes() != null) {
                        episodeCount.setText(season.getEpisodes().size() + " Episodes");
                        episodeAdapter.updateData(season.getEpisodes());
                    }

                    if (season.getPosterPath() != null) {
                        Glide.with(SeasonDetailFragment.this)
                                .load(ImageUrl.POSTER + season.getPosterPath())
                                .placeholder(R.drawable.placeholder_image)
                                .into(posterImage);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<SeasonDetailResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Failed to load season details", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
