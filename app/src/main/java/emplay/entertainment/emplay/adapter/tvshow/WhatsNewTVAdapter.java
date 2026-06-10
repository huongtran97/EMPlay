package emplay.entertainment.emplay.adapter.tvshow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.api.common.MovieApiService;
import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.api.tvshow.TVShowDetailsResponse;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;
import emplay.entertainment.emplay.tool.BadgeHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WhatsNewTVAdapter extends RecyclerView.Adapter<WhatsNewTVAdapter.ViewHolder> {

    private final Context context;
    private final List<TVShowModel> tvShowList;
    private final MovieApiService apiService;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(TVShowModel tvShow);
    }

    public WhatsNewTVAdapter(Context context, List<TVShowModel> tvShowList,
                             MovieApiService apiService, OnItemClickListener listener) {
        this.context = context;
        this.tvShowList = tvShowList;
        this.apiService = apiService;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_whats_new_tvshow, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TVShowModel tvShow = tvShowList.get(position);
        holder.tvTitle.setText(tvShow.getName());

        String firstAirDate = tvShow.getFirstAirDate();
        BadgeHelper.applyTVShowBadge(holder.tvBadge, firstAirDate);

        String type = BadgeHelper.getTVShowType(firstAirDate);

        String dateForMeta = tvShow.getDisplayDate() != null ? tvShow.getDisplayDate() : firstAirDate;
        String rel = BadgeHelper.formatRelativeDate(dateForMeta);
        String seasonInfo = tvShow.getDisplaySeasonInfo();
        String meta;
        if (seasonInfo != null) {
            meta = seasonInfo;
        } else {
            meta = (firstAirDate != null && firstAirDate.length() >= 4) ? firstAirDate.substring(0, 4) : "";
        }
        if (!rel.isEmpty()) meta += " · " + rel;
        holder.tvMeta.setText(meta);

        // Image: episode still for "New episode", season poster for "New season", backdrop for "New TV show"
        String imageUrl;
        if (tvShow.getDisplayImagePath() != null) {
            imageUrl = tvShow.getDisplayImagePath();
        } else {
            imageUrl = ImageUrl.THUMBNAIL + tvShow.getBackdropPath();
            if (!"New TV show".equals(type) && !tvShow.isDetailsFetched()) {
                fetchDetails(tvShow, type);
            }
        }

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.bg_poster_placeholder)
                .into(holder.ivThumb);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(tvShow));
    }

    private void fetchDetails(TVShowModel tvShow, String type) {
        tvShow.setDetailsFetched(true);
        apiService.getTVShowDetails(TMDBpath.tvShowDetails(tvShow.getTVShowId()))
                .enqueue(new Callback<TVShowDetailsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TVShowDetailsResponse> call,
                                           @NonNull Response<TVShowDetailsResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) return;
                        TVShowDetailsResponse details = response.body();

                        if ("SEASON".equals(type)) {
                            List<TVShowDetailsResponse.Season> seasons = details.getSeasons();
                            if (seasons != null) {
                                TVShowDetailsResponse.Season latest = null;
                                for (TVShowDetailsResponse.Season s : seasons) {
                                    if (s.getSeason_number() > 0 && s.getPoster_path() != null) {
                                        if (latest == null || s.getSeason_number() > latest.getSeason_number()) {
                                            latest = s;
                                        }
                                    }
                                }
                                if (latest != null) {
                                    if (!BadgeHelper.isNotOlderThan(latest.getAir_date(), 30)) {
                                        int pos = tvShowList.indexOf(tvShow);
                                        if (pos != -1) {
                                            tvShowList.remove(pos);
                                            notifyItemRemoved(pos);
                                        }
                                        return;
                                    }
                                    tvShow.setDisplayImagePath(ImageUrl.POSTER + latest.getPoster_path());
                                    String airDate = latest.getAir_date();
                                    if (airDate != null) {
                                        tvShow.setDisplayDate(airDate);
                                        String year = airDate.length() >= 4 ? airDate.substring(0, 4) : "";
                                        tvShow.setDisplaySeasonInfo(year + " · S" + latest.getSeason_number());
                                    }
                                }
                            }
                        } else {
                            TVShowDetailsResponse.LastEpisodeToAir ep = details.getLast_episode_to_air();
                            if (ep != null) {
                                if (!BadgeHelper.isNotOlderThan(ep.getAir_date(), 30)) {
                                    int pos = tvShowList.indexOf(tvShow);
                                    if (pos != -1) {
                                        tvShowList.remove(pos);
                                        notifyItemRemoved(pos);
                                    }
                                    return;
                                }
                                if (ep.getStill_path() != null) {
                                    tvShow.setDisplayImagePath(ImageUrl.THUMBNAIL + ep.getStill_path());
                                }
                                String airDate = ep.getAir_date();
                                if (airDate != null) {
                                    String year = airDate.length() >= 4 ? airDate.substring(0, 4) : "";
                                    tvShow.setDisplaySeasonInfo(year + " · S" + ep.getSeason_number());
                                    tvShow.setDisplayDate(airDate);
                                }
                            }
                        }

                        int pos = tvShowList.indexOf(tvShow);
                        if (pos != -1) notifyItemChanged(pos);
                    }

                    @Override
                    public void onFailure(@NonNull Call<TVShowDetailsResponse> call, @NonNull Throwable t) {
                        // keep defaults
                    }
                });
    }

    @Override
    public int getItemCount() {
        return tvShowList.size();
    }

    public void updateData(List<TVShowModel> newList) {
        List<TVShowModel> oldList = new ArrayList<>(tvShowList);
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return oldList.size(); }
            @Override public int getNewListSize() { return newList != null ? newList.size() : 0; }
            @Override public boolean areItemsTheSame(int o, int n) {
                return oldList.get(o).getTVShowId() == newList.get(n).getTVShowId();
            }
            @Override public boolean areContentsTheSame(int o, int n) {
                TVShowModel a = oldList.get(o), b = newList.get(n);
                return Objects.equals(a.getName(), b.getName())
                        && Objects.equals(a.getPosterPath(), b.getPosterPath());
            }
        });
        tvShowList.clear();
        if (newList != null) tvShowList.addAll(newList);
        diff.dispatchUpdatesTo(this);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvTitle, tvMeta, tvBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.ivEpisodeThumb);
            tvTitle = itemView.findViewById(R.id.tvShowTitle);
            tvMeta = itemView.findViewById(R.id.tvEpisodeMeta);
            tvBadge = itemView.findViewById(R.id.tvEpisodeBadge);
        }
    }
}