package emplay.entertainment.emplay.movieadapter;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.LanguageMapper;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.TVShowDetailsResponse;
import emplay.entertainment.emplay.models.TVShowModel;

public class TVShowInformationAdapter extends RecyclerView.Adapter<TVShowInformationAdapter.TVShowInformationViewHolder> {
    private List<TVShowModel> tvInformationList;
    private FragmentActivity activity;

    // Constructor
    public TVShowInformationAdapter(List<TVShowModel> tvInformationList, FragmentActivity activity) {
        this.tvInformationList = tvInformationList != null ? tvInformationList : new ArrayList<>();
        this.activity = activity;
    }

    @NonNull
    @Override
    public TVShowInformationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tvshow_information, parent, false);
        return new TVShowInformationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TVShowInformationViewHolder holder, int position) {
        TVShowModel tv = tvInformationList.get(position);
        holder.bind(tv);
    }

    @Override
    public int getItemCount() {
        return tvInformationList.size();
    }

    // Method to update data in the adapter
    public void updateData(List<TVShowModel> newTVShow) {
        if (newTVShow != null) {
            tvInformationList.clear();
            tvInformationList.addAll(newTVShow);
            notifyDataSetChanged();
        }
    }

    // ViewHolder class to hold and bind views
    public class TVShowInformationViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView overView;
        private final TextView language;
        private final TextView firstAirDate;
        private final TextView genre;
        private final TextView season;
        private final TextView episodes;
        private final TextView productCountry;
        private final ImageView poster;
        private final RatingBar ratingBar;


        public TVShowInformationViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvshow_information_title);
            overView = itemView.findViewById(R.id.tvshow_result_overview);
            language = itemView.findViewById(R.id.tvshow_result_language);
            firstAirDate = itemView.findViewById(R.id.tvshow_information_first_air_date);
            genre = itemView.findViewById(R.id.tvshow_result_genres);
            season = itemView.findViewById(R.id.tvshow_information_seasons);
            episodes = itemView.findViewById(R.id.tvshow_result_episodes);
            productCountry = itemView.findViewById(R.id.tvshow_result_product_country);
            poster = itemView.findViewById(R.id.imageView);
            ratingBar = itemView.findViewById(R.id.tvshow_ratingBar);
        }

        public void bind(TVShowModel tv) {
            name.setText(tv.getName() != null ? tv.getName() : "N/A");
            ratingBar.setRating((float) (tv.getVoteAverage() / 2));
            overView.setText(tv.getOverview());

            if (VERSION.SDK_INT >= VERSION_CODES.N) {
                language.setText("Language: " +  LanguageMapper.getLanguageName(tv.getOriginalLanguage()));
            }

            firstAirDate.setText("Date: " + (tv.getFirstAirDate() != null ? tv.getFirstAirDate() : "N/A"));

            // Set genres
            genre.setText(tv.getGenres() != null && !tv.getGenres().isEmpty() ? String.join(" | ", tv.getGenres()) : "N/A");

            // Set seasons
            season.setText(tv.getSeasons() != null && !tv.getSeasons().isEmpty() ? "Seasons: " + String.join(" \u2022 ", tv.getSeasons()) : "Seasons: N/A");

            // Set episodes
            episodes.setText("Episodes: " + tv.getNumberOfEpisodes());

            productCountry.setText(tv.getProductionCountries() != null && !tv.getProductionCountries().isEmpty() ? "Product from:  " + String.join(", ",tv.getProductionCountries()) : "Product Country: N/A");

            // Load the poster image with Glide
            Glide.with(itemView.getContext())
                    .load("https://image.tmdb.org/t/p/w500/" + (tv.getPosterPath() != null ? tv.getPosterPath() : ""))
                    .placeholder(R.drawable.placeholder_image) // Optional placeholder image
                    .error(R.drawable.error_image) // Optional error image
                    .into(poster);

            // Handle click events
            itemView.setOnClickListener(v -> {
                //handle item video trailer
            });
        }

    }
}
