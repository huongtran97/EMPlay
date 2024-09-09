package emplay.entertainment.emplay.movieadapter;

import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_ID;
import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_POSTER_PATH;
import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_TITLE;
import static emplay.entertainment.emplay.database.DatabaseHelper.TABLE_MOVIES;
import static emplay.entertainment.emplay.database.DatabaseHelper.TABLE_TVSHOWS;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import emplay.entertainment.emplay.LanguageMapper;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.models.TVShowModel;

public class TVShowInformationAdapter extends RecyclerView.Adapter<TVShowInformationAdapter.TVShowInformationViewHolder> {
    private List<TVShowModel> tvInformationList;
    private FragmentActivity fragmentActivity;
    private DatabaseHelper databaseHelper;
    private Context context;

    public TVShowInformationAdapter(List<TVShowModel> tvInformationList, FragmentActivity fragmentActivity) {
        this.tvInformationList = tvInformationList;
        this.fragmentActivity = fragmentActivity;
        this.context = fragmentActivity;
        this.databaseHelper = new DatabaseHelper(context);
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
        holder.overView.setText(tv.getOverview());

        if (VERSION.SDK_INT >= VERSION_CODES.N) {
            holder.language.setText("Language: " + LanguageMapper.getLanguageName(tv.getOriginalLanguage()));
        }

        holder.firstAirDate.setText("Date: " + (tv.getFirstAirDate() != null ? tv.getFirstAirDate() : "N/A"));
        holder.genre.setText(tv.getGenres() != null && !tv.getGenres().isEmpty() ? String.join(" | ", tv.getGenres()) : "N/A");
        holder.season.setText(tv.getSeasons() != null && !tv.getSeasons().isEmpty() ? "Seasons: " + String.join(" \u2022 ", tv.getSeasons()) : "Seasons: N/A");
        holder.episodes.setText("Episodes: " + tv.getNumberOfEpisodes());
        holder.productCountry.setText(tv.getProductionCountries() != null && !tv.getProductionCountries().isEmpty() ? "Product from: " + String.join(", ", tv.getProductionCountries()) : "Product Country: N/A");

        Glide.with(holder.itemView.getContext())
                .load("https://image.tmdb.org/t/p/w500" + tv.getPosterPath())
                .into(holder.poster);

        if (isTVShowSaved(tv.getId())) {
            holder.addBtn.setImageResource(R.drawable.baseline_favorite_24); // Your filled heart icon drawable
        } else {
            holder.addBtn.setImageResource(R.drawable.baseline_favorite_border_24); // Your border heart icon drawable
        }

        holder.addBtn.setOnClickListener(v -> {
            if (isTVShowSaved(tv.getId())) {
                removeTVShowFromDatabase(tv.getId());
                holder.addBtn.setImageResource(R.drawable.baseline_favorite_border_24);
                Toast.makeText(context, "TV Show removed from library", Toast.LENGTH_SHORT).show();
            } else {
                long result = saveTVShowToDatabase(tv);
                if (result != -1) {
                    holder.addBtn.setImageResource(R.drawable.baseline_favorite_24);
                    Toast.makeText(context, "TV Show added to library", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to add TV Show", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.itemView.setOnClickListener(v -> {
            // Handle item click trailer event
        });
    }

    private long saveTVShowToDatabase(TVShowModel tv) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, tv.getId());
        values.put(COLUMN_TITLE, tv.getName());
        values.put(COLUMN_POSTER_PATH, tv.getPosterPath());

        return db.insertWithOnConflict(TABLE_TVSHOWS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    private boolean isTVShowSaved(long tvShowId) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_TVSHOWS + " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(tvShowId)});
        boolean isSaved = false;
        if (cursor.moveToFirst()) {
            isSaved = cursor.getInt(0) > 0;
        }
        cursor.close();
        return isSaved;
    }

    private void removeTVShowFromDatabase(long tvShowId) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.delete(TABLE_TVSHOWS, COLUMN_ID + " = ?", new String[]{String.valueOf(tvShowId)});
    }

    @Override
    public int getItemCount() {
        return tvInformationList.size();
    }

    public void updateData(List<TVShowModel> newTVShow) {
        if (newTVShow != null) {
            tvInformationList.clear();
            tvInformationList.addAll(newTVShow);
            notifyDataSetChanged();
        }
    }

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
        ImageButton addBtn;

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
        }
    }
}
