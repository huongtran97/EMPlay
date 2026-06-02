package emplay.entertainment.emplay.database;

import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_GENRES;
import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_MOVIE_ID;
import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_POSTER_PATH;
import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_SHOW_ID;
import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_TITLE;
import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_USER_ID;
import static emplay.entertainment.emplay.database.DatabaseHelper.COLUMN_VOTE_AVERAGE;
import static emplay.entertainment.emplay.database.DatabaseHelper.TABLE_USER_MOVIES;
import static emplay.entertainment.emplay.database.DatabaseHelper.TABLE_USER_SHOWS;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class WatchlistHelper {

    public static boolean isMovieSaved(DatabaseHelper dbHelper, String userId, int movieId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_USER_MOVIES +
                        " WHERE " + COLUMN_USER_ID + " = ? AND " + COLUMN_MOVIE_ID + " = ?",
                new String[]{userId, String.valueOf(movieId)});
        boolean isSaved = false;
        if (cursor.moveToFirst()) {
            isSaved = cursor.getInt(0) > 0;
        }
        cursor.close();
        return isSaved;
    }

    public static long saveMovie(DatabaseHelper dbHelper, String userId, int movieId, String title, String posterPath, String genres, double voteAverage) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_MOVIE_ID, movieId);
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_POSTER_PATH, posterPath);
        values.put(COLUMN_GENRES, genres);
        values.put(COLUMN_VOTE_AVERAGE, voteAverage);
        return db.insert(TABLE_USER_MOVIES, null, values);
    }

    public static void removeMovie(DatabaseHelper dbHelper, String userId, int movieId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE_USER_MOVIES, COLUMN_USER_ID + " = ? AND " + COLUMN_MOVIE_ID + " = ?", new String[]{userId, String.valueOf(movieId)});
    }

    public static boolean isTVShowSaved(DatabaseHelper dbHelper, String userId, int tvShowId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_USER_SHOWS +
                        " WHERE " + COLUMN_USER_ID + " = ? AND " + COLUMN_SHOW_ID + " = ?",
                new String[]{userId, String.valueOf(tvShowId)});
        boolean isSaved = false;
        if (cursor.moveToFirst()) {
            isSaved = cursor.getInt(0) > 0;
        }
        cursor.close();
        return isSaved;
    }

    public static long saveTVShow(DatabaseHelper dbHelper, String userId, int tvShowId, String title, String posterPath, String genres, double voteAverage) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_SHOW_ID, tvShowId);
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_POSTER_PATH, posterPath);
        values.put(COLUMN_GENRES, genres);
        values.put(COLUMN_VOTE_AVERAGE, voteAverage);
        return db.insert(TABLE_USER_SHOWS, null, values);
    }

    public static void removeTVShow(DatabaseHelper dbHelper, String userId, int tvShowId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE_USER_SHOWS, COLUMN_USER_ID + " = ? AND " + COLUMN_SHOW_ID + " = ?", new String[]{userId, String.valueOf(tvShowId)});
    }
}
