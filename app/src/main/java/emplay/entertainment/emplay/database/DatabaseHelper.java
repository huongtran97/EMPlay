package emplay.entertainment.emplay.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import emplay.entertainment.emplay.models.movie.MovieModel;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;
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
    private static final int DATABASE_VERSION = 19;
    static final long CACHE_TTL_MS = 24 * 60 * 60 * 1000L;
    public static final String TABLE_MOVIES = "movies";
    public static final String TABLE_SHOWS = "tvshows";
    public static final String TABLE_USER_PROFILE = "profile";
    public static final String TABLE_USER_MOVIES = "user_movies";
    public static final String TABLE_USER_SHOWS = "user_tvshows";
    public static final String TABLE_RECENT_SEARCHES = "recent_searches";
    public static final String TABLE_RELEASE_ALERTS = "release_alerts";
    public static final String COLUMN_MEDIA_TYPE = "media_type";
    public static final String COLUMN_RELEASE_DATE = "release_date";
    public static final String COLUMN_DISMISSED = "dismissed";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_POSTER_PATH = "poster_path";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_MOVIE_ID = "movie_id";
    public static final String COLUMN_SHOW_ID = "tvshow_id";
    public static final String COLUMN_NAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_GENRES = "genres";
    public static final String COLUMN_VOTE_AVERAGE = "vote_average";
    public static final String COLUMN_SEARCH_QUERY = "search_query";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String TABLE_CATALOG_META = "catalog_meta";
    private static final String COLUMN_META_KEY = "meta_key";
    private static final String COLUMN_LAST_FETCHED_MS = "last_fetched_ms";
    private static final String TABLE_MOTN_CACHE = "motn_cache";
    private static final String COLUMN_MOTN_TMDB_ID = "tmdb_id";
    private static final String COLUMN_MOTN_SHOW_TYPE = "show_type";
    private static final String COLUMN_MOTN_JSON = "response_json";
    private static final String COLUMN_MOTN_FETCHED_AT = "fetched_at";

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
                    COLUMN_VOTE_AVERAGE + " REAL DEFAULT 0, " +
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
                    COLUMN_VOTE_AVERAGE + " REAL DEFAULT 0, " +
                    COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (" + COLUMN_SHOW_ID + ") REFERENCES " + TABLE_SHOWS + "(" + COLUMN_ID + ") ON DELETE CASCADE)";

    private static final String CREATE_TABLE_RECENT_SEARCHES =
            "CREATE TABLE " + TABLE_RECENT_SEARCHES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_SEARCH_QUERY + " TEXT UNIQUE, " +
                    COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP)";

    private static final String CREATE_TABLE_CATALOG_META =
            "CREATE TABLE " + TABLE_CATALOG_META + " (" +
                    COLUMN_META_KEY + " TEXT PRIMARY KEY, " +
                    COLUMN_LAST_FETCHED_MS + " INTEGER NOT NULL)";

    private static final String CREATE_TABLE_RELEASE_ALERTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_RELEASE_ALERTS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_ID + " TEXT NOT NULL, " +
                    "media_id INTEGER NOT NULL, " +
                    COLUMN_MEDIA_TYPE + " TEXT NOT NULL, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_POSTER_PATH + " TEXT, " +
                    COLUMN_RELEASE_DATE + " TEXT, " +
                    COLUMN_DISMISSED + " INTEGER NOT NULL DEFAULT 0, " +
                    "UNIQUE (" + COLUMN_USER_ID + ", media_id, " + COLUMN_MEDIA_TYPE + "))";

    private static final String CREATE_TABLE_MOTN_CACHE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_MOTN_CACHE + " (" +
                    COLUMN_MOTN_TMDB_ID + " INTEGER NOT NULL, " +
                    COLUMN_MOTN_SHOW_TYPE + " TEXT NOT NULL, " +
                    COLUMN_MOTN_JSON + " TEXT NOT NULL, " +
                    COLUMN_MOTN_FETCHED_AT + " INTEGER NOT NULL, " +
                    "PRIMARY KEY (" + COLUMN_MOTN_TMDB_ID + ", " + COLUMN_MOTN_SHOW_TYPE + "))";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = ON;");
        db.execSQL(CREATE_TABLE_MOVIES);
        db.execSQL(CREATE_TABLE_SHOWS);
        db.execSQL(CREATE_TABLE_USER_PROFILE);
        db.execSQL(CREATE_TABLE_USER_MOVIES);
        db.execSQL(CREATE_TABLE_USER_SHOWS);
        db.execSQL(CREATE_TABLE_RECENT_SEARCHES);
        db.execSQL(CREATE_TABLE_CATALOG_META);
        db.execSQL(CREATE_TABLE_MOTN_CACHE);
        db.execSQL(CREATE_TABLE_RELEASE_ALERTS);
        Log.d("DatabaseHelper", "All tables created successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 17) {
            // Drop motn_cache regardless of its previous schema — v15 used json_data/cached_at,
            // v16 may have left the old schema intact due to CREATE IF NOT EXISTS being a no-op.
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOTN_CACHE);
            db.execSQL(CREATE_TABLE_MOTN_CACHE);
        }
        if (oldVersion < 18) {
            // Repopulate motn_cache: cached JSON was written without addon field, so addon-gated
            // provider name matching would always fail on cache hits.
            db.execSQL("DELETE FROM " + TABLE_MOTN_CACHE);
        }
        if (oldVersion < 19) {
            db.execSQL(CREATE_TABLE_RELEASE_ALERTS);
        }
    }

    public void deleteMovie(int itemId, String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USER_MOVIES,
                COLUMN_MOVIE_ID + " = ? AND " + COLUMN_USER_ID + " = ?",
                new String[]{"" + itemId, userId});
    }

    public void deleteTV(int itemId, String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USER_SHOWS,
                COLUMN_SHOW_ID + " = ? AND " + COLUMN_USER_ID + " = ?",
                new String[]{"" + itemId, userId});
    }

    public void deleteUserProfile(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USER_PROFILE, COLUMN_EMAIL + " = ?", new String[]{email});
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
                int voteIndex = cursor.getColumnIndex(COLUMN_VOTE_AVERAGE);

                if (idIndex != -1 && titleIndex != -1 && posterPathIndex != -1) {
                    int id = cursor.getInt(idIndex);
                    String title = cursor.getString(titleIndex);
                    String posterPath = cursor.getString(posterPathIndex);
                    String genresStr = genreIndex != -1 ? cursor.getString(genreIndex) : null;
                    String timestamp = timestampIndex != -1 ? cursor.getString(timestampIndex) : null;
                    double voteAverage = voteIndex != -1 ? cursor.getDouble(voteIndex) : 0.0;

                    List<String> genres = new ArrayList<>();
                    if (genresStr != null && !genresStr.isEmpty()) {
                        Collections.addAll(genres, genresStr.split(","));
                    }

                    MovieModel movie = new MovieModel(id, title, posterPath);
                    movie.setGenres(genres);
                    movie.setSavedTimestamp(timestamp);
                    movie.setVoteAverage(voteAverage);
                    movies.add(movie);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return movies;
    }

    /** Records the current time as the last successful fetch for a named catalog key. */
    public void updateCatalogFetchTime(String key) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_META_KEY, key);
        values.put(COLUMN_LAST_FETCHED_MS, System.currentTimeMillis());
        db.insertWithOnConflict(TABLE_CATALOG_META, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Returns true if the catalog data for {@code key} has never been fetched or was last
     * fetched more than 24 hours ago. Callers should trigger a background refresh when true.
     */
    public boolean isCatalogStale(String key) {
        SQLiteDatabase db = this.getReadableDatabase();
        android.database.Cursor cursor = db.query(TABLE_CATALOG_META,
                new String[]{COLUMN_LAST_FETCHED_MS},
                COLUMN_META_KEY + " = ?", new String[]{key},
                null, null, null);
        try {
            if (!cursor.moveToFirst()) return true;
            long lastFetched = cursor.getLong(0);
            return System.currentTimeMillis() - lastFetched > CACHE_TTL_MS;
        } finally {
            cursor.close();
        }
    }

    /** Returns cached MOTN JSON for the given TMDB ID + show type, or null if expired/missing. */
    public String getCachedMotnJson(int tmdbId, String showType) {
        SQLiteDatabase db = this.getReadableDatabase();
        android.database.Cursor cursor = db.query(TABLE_MOTN_CACHE,
                new String[]{COLUMN_MOTN_JSON, COLUMN_MOTN_FETCHED_AT},
                COLUMN_MOTN_TMDB_ID + " = ? AND " + COLUMN_MOTN_SHOW_TYPE + " = ?",
                new String[]{String.valueOf(tmdbId), showType},
                null, null, null);
        try {
            if (!cursor.moveToFirst()) return null;
            long fetchedAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_MOTN_FETCHED_AT));
            if (System.currentTimeMillis() - fetchedAt > emplay.entertainment.emplay.tool.MotnHelper.CACHE_TTL_MS) return null;
            return cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOTN_JSON));
        } finally {
            cursor.close();
        }
    }

    /** Upsert MOTN JSON for the given TMDB ID + show type. */
    public void cacheMotnJson(int tmdbId, String showType, String json) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MOTN_TMDB_ID, tmdbId);
        values.put(COLUMN_MOTN_SHOW_TYPE, showType);
        values.put(COLUMN_MOTN_JSON, json);
        values.put(COLUMN_MOTN_FETCHED_AT, System.currentTimeMillis());
        db.insertWithOnConflict(TABLE_MOTN_CACHE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public boolean addReleaseAlert(String userId, int mediaId, String mediaType,
                                    String title, String posterPath, String releaseDate) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COLUMN_USER_ID, userId);
        v.put("media_id", mediaId);
        v.put(COLUMN_MEDIA_TYPE, mediaType);
        v.put(COLUMN_TITLE, title);
        v.put(COLUMN_POSTER_PATH, posterPath);
        v.put(COLUMN_RELEASE_DATE, releaseDate);
        v.put(COLUMN_DISMISSED, 0);
        return db.insertWithOnConflict(TABLE_RELEASE_ALERTS, null, v, SQLiteDatabase.CONFLICT_IGNORE) != -1;
    }

    public void removeReleaseAlert(String userId, int mediaId, String mediaType) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_RELEASE_ALERTS,
                COLUMN_USER_ID + "=? AND media_id=? AND " + COLUMN_MEDIA_TYPE + "=?",
                new String[]{userId, String.valueOf(mediaId), mediaType});
    }

    public boolean isReleaseAlertSet(String userId, int mediaId, String mediaType) {
        SQLiteDatabase db = getReadableDatabase();
        android.database.Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_RELEASE_ALERTS +
                " WHERE " + COLUMN_USER_ID + "=? AND media_id=? AND " + COLUMN_MEDIA_TYPE + "=? AND " + COLUMN_DISMISSED + "=0",
                new String[]{userId, String.valueOf(mediaId), mediaType});
        try { return c.moveToFirst() && c.getInt(0) > 0; } finally { c.close(); }
    }

    public int getUnreadReleasedCount(String userId) {
        SQLiteDatabase db = getReadableDatabase();
        android.database.Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_RELEASE_ALERTS +
                " WHERE " + COLUMN_USER_ID + "=? AND " + COLUMN_DISMISSED + "=0" +
                " AND " + COLUMN_RELEASE_DATE + " <= date('now')",
                new String[]{userId});
        try { return c.moveToFirst() ? c.getInt(0) : 0; } finally { c.close(); }
    }

    public List<emplay.entertainment.emplay.models.common.ReleaseAlertItem> getAllActiveAlerts(String userId) {
        SQLiteDatabase db = getReadableDatabase();
        android.database.Cursor c = db.rawQuery(
                "SELECT media_id, " + COLUMN_MEDIA_TYPE + ", " + COLUMN_TITLE + ", " +
                COLUMN_POSTER_PATH + ", " + COLUMN_RELEASE_DATE +
                " FROM " + TABLE_RELEASE_ALERTS +
                " WHERE " + COLUMN_USER_ID + "=? AND " + COLUMN_DISMISSED + "=0" +
                " ORDER BY CASE WHEN " + COLUMN_RELEASE_DATE + " <= date('now') THEN 0 ELSE 1 END, " +
                COLUMN_RELEASE_DATE + " ASC",
                new String[]{userId});
        List<emplay.entertainment.emplay.models.common.ReleaseAlertItem> result = new ArrayList<>();
        try {
            while (c.moveToNext()) {
                result.add(new emplay.entertainment.emplay.models.common.ReleaseAlertItem(
                        c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4)));
            }
        } finally { c.close(); }
        return result;
    }

    public void dismissReleasedAlerts(String userId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COLUMN_DISMISSED, 1);
        db.update(TABLE_RELEASE_ALERTS, v,
                COLUMN_USER_ID + "=? AND " + COLUMN_DISMISSED + "=0 AND " + COLUMN_RELEASE_DATE + " <= date('now')",
                new String[]{userId});
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
                int voteIndex = cursor.getColumnIndex(COLUMN_VOTE_AVERAGE);

                if (idIndex != -1 && titleIndex != -1 && posterPathIndex != -1) {
                    int id = cursor.getInt(idIndex);
                    String title = cursor.getString(titleIndex);
                    String posterPath = cursor.getString(posterPathIndex);
                    String genresStr = genreIndex != -1 ? cursor.getString(genreIndex) : null;
                    String timestamp = timestampIndex != -1 ? cursor.getString(timestampIndex) : null;
                    double voteAverage = voteIndex != -1 ? cursor.getDouble(voteIndex) : 0.0;

                    List<String> genres = new ArrayList<>();
                    if (genresStr != null && !genresStr.isEmpty()) {
                        Collections.addAll(genres, genresStr.split(","));
                    }

                    TVShowModel tv = new TVShowModel(id, title, posterPath);
                    tv.setGenres(genres);
                    tv.setSavedTimestamp(timestamp);
                    tv.setVoteAverage(voteAverage);
                    tvShows.add(tv);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tvShows;
    }
}
