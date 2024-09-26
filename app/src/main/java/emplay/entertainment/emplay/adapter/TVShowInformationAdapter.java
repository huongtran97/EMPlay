package emplay.entertainment.emplay.adapter;

import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_POSTER_PATH;
import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_TITLE;
import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_TVSHOW_ID;
import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_USER_ID;
import static emplay.entertainment.emplay.database.DatabaseHelper.TABLE_USER_TVSHOWS;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.tool.LanguageMapper;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.activity.TrailerActivity;
import emplay.entertainment.emplay.api.TVShowsTrailerResponses;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.models.TVShowModel;

public class TVShowInformationAdapter extends RecyclerView.Adapter<TVShowInformationAdapter.TVShowInformationViewHolder> {
    private List<TVShowModel> tvInformationList;
    private FragmentActivity fragmentActivity;
    private DatabaseHelper databaseHelper;
    private FirebaseAuth mAuth;
    private List<TVShowsTrailerResponses.TrailerModel> trailers = new ArrayList<>();

    public TVShowInformationAdapter(List<TVShowModel> tvInformationList, FragmentActivity fragmentActivity) {
        this.tvInformationList = tvInformationList;
        this.fragmentActivity = fragmentActivity;
        this.databaseHelper = new DatabaseHelper(fragmentActivity);
        this.mAuth = FirebaseAuth.getInstance();
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

        holder.name.setText(tv.getName() != null ? tv.getName() : "N/A");
        holder.ratingBar.setRating((float) (tv.getVoteAverage() / 2));
        holder.overView.setText(tv.getOverview() != null ? tv.getOverview() : "N/A");

        // Handle language display
        if (VERSION.SDK_INT >= VERSION_CODES.N) {
            holder.language.setText("Language: " + LanguageMapper.getLanguageName(tv.getOriginalLanguage()));
        }
        holder.firstAirDate.setText("Date: " + (tv.getFirstAirDate() != null ? tv.getFirstAirDate() : "N/A"));
        holder.genre.setText(tv.getGenres() != null && !tv.getGenres().isEmpty() ? String.join(" | ", tv.getGenres()) : "N/A");
        holder.season.setText("Number of seasons: " + tv.getNumberOfSeasons());
        holder.episodes.setText("Episodes: " + tv.getNumberOfEpisodes());
        holder.productCountry.setText(tv.getProductionCountries() != null && !tv.getProductionCountries().isEmpty() ?
                "Product from: " + String.join(", ", tv.getProductionCountries()) : "Product Country: N/A");

        // Handle poster loading
        String posterPath = tv.getPosterPath() != null && !tv.getPosterPath().isEmpty() ? tv.getPosterPath() : tv.getBackdropPath();
        Glide.with(fragmentActivity)
                .load(posterPath != null ? "https://image.tmdb.org/t/p/w500/" + posterPath : R.drawable.placeholder_image)
                .placeholder(R.drawable.placeholder_image)
                .into(holder.poster);

        handleAddToLibrary(holder, tv);
        handleTrailerButton(holder);
    }

    private void handleAddToLibrary(@NonNull TVShowInformationViewHolder holder, TVShowModel tvShowModel) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            holder.addBtn.setOnClickListener(v ->
                    Toast.makeText(fragmentActivity, "You must be logged in to save it!", Toast.LENGTH_SHORT).show());
            return;
        }

        String userId = currentUser.getUid();
        updateSaveButtonState(holder, userId, tvShowModel);

        holder.addBtn.setOnClickListener(v -> {
            int tvShowId = tvShowModel.getTvShowId();
            String title = tvShowModel.getName();
            String posterPath = tvShowModel.getPosterPath();

            if (isTVShowSavedByUser(userId, tvShowId)) {
                removeTVShowFromUserTVShow(userId, tvShowId);
                holder.addBtn.setImageResource(R.drawable.baseline_favorite_border_24);
                Toast.makeText(fragmentActivity, "TV Show removed from library", Toast.LENGTH_SHORT).show();
            } else {
                long result = saveTVShowToUserTVShows(userId, tvShowId, title, posterPath);
                if (result != -1) {
                    holder.addBtn.setImageResource(R.drawable.baseline_favorite_24);
                    Toast.makeText(fragmentActivity, "Added to library", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(fragmentActivity, "Failed to add TV Show", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateSaveButtonState(TVShowInformationViewHolder holder, String userId, TVShowModel tvShowModel) {
        if (isTVShowSavedByUser(userId, tvShowModel.getTvShowId())) {
            holder.addBtn.setImageResource(R.drawable.baseline_favorite_24);
        } else {
            holder.addBtn.setImageResource(R.drawable.baseline_favorite_border_24);
        }
    }

    private void handleTrailerButton(@NonNull TVShowInformationViewHolder holder) {
        holder.trailerBtn.setOnClickListener(v -> {
            if (!trailers.isEmpty()) {
                String videoKey = trailers.get(0).getKey();
                Intent intent = new Intent(holder.itemView.getContext(), TrailerActivity.class);
                intent.putExtra("MOVIE_ID", videoKey);
                holder.itemView.getContext().startActivity(intent);
            } else {
                Toast.makeText(fragmentActivity, "No trailer available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public long saveTVShowToUserTVShows(String userId, int tvId, String title, String posterPath) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_TVSHOW_ID, tvId);
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_POSTER_PATH, posterPath);
        return db.insert(TABLE_USER_TVSHOWS, null, values);
    }

    private boolean isTVShowSavedByUser(String userId, int tvShowId) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER_TVSHOWS + " WHERE " + COLUMN_USER_ID + " = ? AND " + COLUMN_TVSHOW_ID + " = ?",
                new String[]{userId, String.valueOf(tvShowId)});

        boolean isSaved = false;
        if (cursor.moveToFirst()) {
            isSaved = cursor.getInt(0) > 0;
        }
        cursor.close();
        return isSaved;
    }

    private void removeTVShowFromUserTVShow(String userId, int tvShowId) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.delete(TABLE_USER_TVSHOWS, COLUMN_USER_ID + " = ? AND " + COLUMN_TVSHOW_ID + " = ?", new String[]{userId, String.valueOf(tvShowId)});
    }


    public void setTeaserTrailers(List<TVShowsTrailerResponses.TrailerModel> adapterTrailers) {
        this.trailers = adapterTrailers != null ? adapterTrailers : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return tvInformationList.size();
    }

    public class TVShowInformationViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView overView;
        TextView language;
        TextView firstAirDate;
        TextView genre;
        TextView season;
        TextView episodes;
        TextView productCountry;
        ImageView poster;
        RatingBar ratingBar;
        ImageButton addBtn;
        Button trailerBtn;

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
            addBtn = itemView.findViewById(R.id.add_to_library_btn);
            trailerBtn = itemView.findViewById(R.id.trailer_btn);
        }
    }
}
