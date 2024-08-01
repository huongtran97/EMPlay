package emplay.entertainment.emplay.movieadapter;

import android.os.Bundle;
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

import android.util.Log;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.models.MovieModel;


public class MovieResultAdapter extends RecyclerView.Adapter<MovieResultAdapter.MovieResultViewHolder> {

    private List<MovieModel> movieList;
    private FragmentActivity fragmentActivity;

    // Constructor
    public MovieResultAdapter(List<MovieModel> movieList, FragmentActivity fragmentActivity) {
        this.movieList = movieList;
        this.fragmentActivity = fragmentActivity;
    }

    @NonNull
    @Override
    public MovieResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_result_recyclerview_item, parent, false);
        return new MovieResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieResultViewHolder holder, int position) {
        MovieModel movieModel = movieList.get(position);
        if (movieModel != null) {
            Log.d("MovieResultAdapter", "Binding data for: " + movieModel.getTitle());

            holder.resultTitle.setText(movieModel.getTitle());
            holder.resultReleaseDate.setText("Release Date: " + movieModel.getReleaseDate());
            holder.resultLanguage.setText("\u2022    Language: " + movieModel.getOriginalLanguage());
            holder.resultRatingBar.setRating((float) (movieModel.getVoteAverage() / 2));
            holder.resultOverview.setText("Overview: " + movieModel.getOverview());
            holder.resultRuntime.setText("\u2022    Runtime: " + movieModel.getRuntime() + " min");

            List<String> genres = movieModel.getGenres() != null ? movieModel.getGenres() : new ArrayList<>();
            StringBuilder genreNames = new StringBuilder();
            for (String genre : genres) {
                if (genreNames.length() > 0) {
                    genreNames.append(" | ");
                }
                genreNames.append(genre);
            }
            holder.genreNames.setText(genreNames.toString());

            Glide.with(holder.itemView.getContext())
                    .load("https://image.tmdb.org/t/p/w500" + movieModel.getPosterPath())
                    .into(holder.resultPoster);

        }
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public static class MovieResultViewHolder extends RecyclerView.ViewHolder {
        TextView resultTitle;
        TextView resultReleaseDate;
        TextView resultLanguage;
        RatingBar resultRatingBar;
        TextView genreNames;
        ImageView resultPoster;
        TextView resultOverview;
        TextView resultRuntime;

        public MovieResultViewHolder(@NonNull View itemView) {
            super(itemView);
            resultTitle = itemView.findViewById(R.id.search_result_title);
            resultReleaseDate = itemView.findViewById(R.id.search_result_release_date);
            resultLanguage = itemView.findViewById(R.id.search_result_language);
            resultRatingBar = itemView.findViewById(R.id.ratingBar);
            genreNames = itemView.findViewById(R.id.search_result_genres);
            resultPoster = itemView.findViewById(R.id.imageView);
            resultOverview = itemView.findViewById(R.id.search_result_overview);
            resultRuntime = itemView.findViewById(R.id.search_result_runtime);
        }
    }
}
