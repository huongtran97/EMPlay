package emplay.entertainment.emplay.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import emplay.entertainment.emplay.models.MovieModel;
import emplay.entertainment.emplay.models.TVShowModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Local SQLite database — used to cache the user's profile and their saved (liked) items.
 *
 * Table layout:
 *   movies / tvshows      — general catalog cache (id, title, poster_path)
 *   user_movies / user_tvshows — per-user saved items, keyed by Firebase UID
 *   profile               — cached display name + email for the current user
 *
 * All auth state comes from Firebase. The local DB just saves things so they
 * persist across sessions without needing a network call every time.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "emplay.db";
    private static final int DATABASE_VERSION = 10;
    public static final String TABLE_MOVIES = "movies";
    public static final String TABLE_SHOWS = "tvshows";
    public static final String TABLE_USER_PROFILE = "profile";
    public static final String TABLE_USER_MOVIES = "user_movies";
    public static final String TABLE_USER_SHOWS = "user_tvshows";
    public static final String TABLE_RECENT_SEARCHES = "recent_searches";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_POSTER_PATH = "poster_path";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_MOVIE_ID = "movie_id";
    public static final String COLUMN_SHOW_ID = "tvshow_id";
    public static final String COLUMN_NAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_GENRES = "genres";
    public static final String COLUMN_SEARCH_QUERY = "search_query";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    // Singleton — 1 DB connection shared across the app to avoid "database locked" issues.
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            // Use application context so the DB doesn't accidentally hold a reference to an Activity.
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String CREATE_TABLE_MOVIES =
            "CREATE TABLE " + TABLE_MOVIES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_POSTER_PATH + " TEXT)";

    private static final String CREATE_TABLE_SHOWS =
            "CREATE TABLE " + TABLE_SHOWS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_POSTER_PATH + " TEXT)";

    private static final String CREATE_TABLE_USER_PROFILE =
            "CREATE TABLE " + TABLE_USER_PROFILE + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_EMAIL + " TEXT UNIQUE)";

    private static final String CREATE_TABLE_USER_MOVIES =
            "CREATE TABLE " + TABLE_USER_MOVIES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_ID + " TEXT, " +
                    COLUMN_MOVIE_ID + " INTEGER, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_POSTER_PATH + " TEXT, " +
                    COLUMN_GENRES + " TEXT, " +
                    COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (" + COLUMN_MOVIE_ID + ") REFERENCES " + TABLE_MOVIES + "(" + COLUMN_ID + ") ON DELETE CASCADE)";

    private static final String CREATE_TABLE_USER_SHOWS =
            "CREATE TABLE " + TABLE_USER_SHOWS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_ID + " TEXT, " +
                    COLUMN_SHOW_ID + " INTEGER, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_POSTER_PATH + " TEXT, " +
                    COLUMN_GENRES + " TEXT, " +
                    COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (" + COLUMN_SHOW_ID + ") REFERENCES " + TABLE_SHOWS + "(" + COLUMN_ID + ") ON DELETE CASCADE)";

    private static final String CREATE_TABLE_RECENT_SEARCHES =
            "CREATE TABLE " + TABLE_RECENT_SEARCHES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_SEARCH_QUERY + " TEXT UNIQUE, " +
                    COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP)";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = ON;");
        db.execSQL(CREATE_TABLE_MOVIES);
        db.execSQL(CREATE_TABLE_SHOWS);
        db.execSQL(CREATE_TABLE_USER_PROFILE);
        db.execSQL(CREATE_TABLE_USER_MOVIES);
        db.execSQL(CREATE_TABLE_USER_SHOWS);
        db.execSQL(CREATE_TABLE_RECENT_SEARCHES);
        Log.d("DatabaseHelper", "All tables created successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECENT_SEARCHES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_SHOWS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_MOVIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_PROFILE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHOWS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOVIES);
        onCreate(db);
    }

    public void deleteMovie(int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USER_MOVIES, COLUMN_MOVIE_ID + " = ?", new String[]{"" + itemId});
        db.close();
    }

    public void deleteTV(int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USER_SHOWS, COLUMN_SHOW_ID + " = ?", new String[]{"" + itemId});
        db.close();
    }

    public void deleteUserProfile(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USER_PROFILE, COLUMN_EMAIL + " = ?", new String[]{email});
        db.close();
    }

    /**
     * Upsert: update the existing row if the email already exists, otherwise insert a new one.
     * Called after a successful Google sign-in so the profile is always up to date.
     */
    public void insertOrUpdateUser(String username, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, username);
        values.put(COLUMN_EMAIL, email);

        int rowsAffected = db.update(TABLE_USER_PROFILE, values, COLUMN_EMAIL + " = ?", new String[]{email});
        if (rowsAffected == 0) {
            db.insert(TABLE_USER_PROFILE, null, values);
        }
    }

    public void addRecentSearch(String query) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SEARCH_QUERY, query);
        // Using replace to update timestamp if query already exists due to UNIQUE constraint
        db.insertWithOnConflict(TABLE_RECENT_SEARCHES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public java.util.List<String> getRecentSearches() {
        java.util.List<String> searches = new java.util.ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        android.database.Cursor cursor = db.query(TABLE_RECENT_SEARCHES, new String[]{COLUMN_SEARCH_QUERY},
                null, null, null, null, COLUMN_TIMESTAMP + " DESC", "10");

        if (cursor.moveToFirst()) {
            do {
                searches.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return searches;
    }

    public void clearRecentSearches() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_RECENT_SEARCHES, null, null);
    }

    public List<MovieModel> getAllMoviesFromDatabase(String userId) {
        List<MovieModel> movies = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        android.database.Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USER_MOVIES +
                        " WHERE " + COLUMN_USER_ID + " = ?",
                new String[]{userId}
        );

        if (cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex(COLUMN_MOVIE_ID);
                int titleIndex = cursor.getColumnIndex(COLUMN_TITLE);
                int posterPathIndex = cursor.getColumnIndex(COLUMN_POSTER_PATH);
                int genreIndex = cursor.getColumnIndex(COLUMN_GENRES);
                int timestampIndex = cursor.getColumnIndex(COLUMN_TIMESTAMP);

                if (idIndex != -1 && titleIndex != -1 && posterPathIndex != -1) {
                    int id = cursor.getInt(idIndex);
                    String title = cursor.getString(titleIndex);
                    String posterPath = cursor.getString(posterPathIndex);
                    String genresStr = genreIndex != -1 ? cursor.getString(genreIndex) : null;
                    String timestamp = timestampIndex != -1 ? cursor.getString(timestampIndex) : null;

                    List<String> genres = new ArrayList<>();
                    if (genresStr != null && !genresStr.isEmpty()) {
                        Collections.addAll(genres, genresStr.split(","));
                    }

                    MovieModel movie = new MovieModel(id, title, posterPath);
                    movie.setGenres(genres);
                    movie.setSavedTimestamp(timestamp);
                    movies.add(movie);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return movies;
    }

    public List<TVShowModel> getSavedTVShows(String userId) {
        List<TVShowModel> tvShows = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        android.database.Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USER_SHOWS +
                        " WHERE " + COLUMN_USER_ID + " = ?",
                new String[]{userId}
        );

        if (cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex(COLUMN_SHOW_ID);
                int titleIndex = cursor.getColumnIndex(COLUMN_TITLE);
                int posterPathIndex = cursor.getColumnIndex(COLUMN_POSTER_PATH);
                int genreIndex = cursor.getColumnIndex(COLUMN_GENRES);
                int timestampIndex = cursor.getColumnIndex(COLUMN_TIMESTAMP);

                if (idIndex != -1 && titleIndex != -1 && posterPathIndex != -1) {
                    int id = cursor.getInt(idIndex);
                    String title = cursor.getString(titleIndex);
                    String posterPath = cursor.getString(posterPathIndex);
                    String genresStr = genreIndex != -1 ? cursor.getString(genreIndex) : null;
                    String timestamp = timestampIndex != -1 ? cursor.getString(timestampIndex) : null;

                    List<String> genres = new ArrayList<>();
                    if (genresStr != null && !genresStr.isEmpty()) {
                        Collections.addAll(genres, genresStr.split(","));
                    }

                    TVShowModel tv = new TVShowModel(id, title, posterPath);
                    tv.setGenres(genres);
                    tv.setSavedTimestamp(timestamp);
                    tvShows.add(tv);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tvShows;
    }
}
