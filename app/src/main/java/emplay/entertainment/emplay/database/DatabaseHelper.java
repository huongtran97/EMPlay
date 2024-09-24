package emplay.entertainment.emplay.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "emplay.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_MOVIES = "movies";
    public static final String TABLE_TVSHOWS = "tvshows";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_POSTER_PATH = "poster_path";

    private static DatabaseHelper instance;
    public static final String TABLE_USER_PROFILE = "profile";
    public static final String COLUMN_NAME = "username";
    public static final String COLUMN_EMAIL = "email";


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
                    COLUMN_ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_EMAIL + " TEXT)";


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = ON;");
        db.execSQL(CREATE_TABLE_MOVIES);
        db.execSQL(CREATE_TABLE_TVSHOWS);
        db.execSQL(CREATE_TABLE_USER_PROFILE);
        Log.d("DatabaseHelper", "Tables created: " + CREATE_TABLE_MOVIES + ", " + CREATE_TABLE_TVSHOWS + ", " + CREATE_TABLE_USER_PROFILE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOVIES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TVSHOWS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_PROFILE);
            onCreate(db);
        }
    }

    public void deleteMovie(int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = "id = ?";
        String[] whereArgs = { String.valueOf(itemId) };
        db.delete("movies", whereClause, whereArgs);
        db.close();
    }

    public void deleteTV(int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = "id = ?";
        String[] whereArgs = { String.valueOf(itemId) };
        db.delete("tvshows", whereClause, whereArgs);
        db.close();
    }


    public void insertOrUpdateUser(String username, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("email", email);

        // Assuming user table has a unique constraint on email
        int rowsAffected = db.update("users", values, "email = ?", new String[]{email});
        if (rowsAffected == 0) {
            db.insert("users", null, values);
        }
    }
}
