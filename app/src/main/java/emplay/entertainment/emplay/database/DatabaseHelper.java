package emplay.entertainment.emplay.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "emplay.db";
    private static final int DATABASE_VERSION = 8;

    public static final String TABLE_MOVIES = "movies";
    public static final String TABLE_TVSHOWS = "tvshows";
    public static final String TABLE_USER_PROFILE = "profile";
    public static final String TABLE_USER_MOVIES = "user_movies";
    public static final String TABLE_USER_TVSHOWS = "user_tvshows";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_POSTER_PATH = "poster_path";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_MOVIE_ID = "movie_id";
    public static final String COLUMN_TVSHOW_ID = "tvshow_id";
    public static final String COLUMN_NAME = "username";
    public static final String COLUMN_EMAIL = "email";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
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

    private static final String CREATE_TABLE_TVSHOWS =
            "CREATE TABLE " + TABLE_TVSHOWS + " (" +
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
                    "FOREIGN KEY (" + COLUMN_MOVIE_ID + ") REFERENCES " + TABLE_MOVIES + "(" + COLUMN_ID + ") ON DELETE CASCADE)";

    private static final String CREATE_TABLE_USER_TVSHOWS =
            "CREATE TABLE " + TABLE_USER_TVSHOWS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_ID + " TEXT, " +
                    COLUMN_TVSHOW_ID + " INTEGER, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_POSTER_PATH + " TEXT, " +
                    "FOREIGN KEY (" + COLUMN_TVSHOW_ID + ") REFERENCES " + TABLE_TVSHOWS + "(" + COLUMN_ID + ") ON DELETE CASCADE)";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = ON;");
        db.execSQL(CREATE_TABLE_MOVIES);
        db.execSQL(CREATE_TABLE_TVSHOWS);
        db.execSQL(CREATE_TABLE_USER_PROFILE);
        db.execSQL(CREATE_TABLE_USER_MOVIES);
        db.execSQL(CREATE_TABLE_USER_TVSHOWS);
        Log.d("DatabaseHelper", "All tables created successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_TVSHOWS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_MOVIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_PROFILE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TVSHOWS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOVIES);
        onCreate(db);
    }

    public void deleteMovie(int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USER_MOVIES, COLUMN_MOVIE_ID + " = ?", new String[]{String.valueOf(itemId)});
        db.close();
    }

    public void deleteTV(int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USER_TVSHOWS, COLUMN_TVSHOW_ID + " = ?", new String[]{String.valueOf(itemId)});
        db.close();
    }

    public void deleteUserProfile(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USER_PROFILE, COLUMN_EMAIL + " = ?", new String[]{email});
        db.close();
    }

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
}
