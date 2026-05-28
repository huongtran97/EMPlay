package emplay.entertainment.emplay.tool;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import emplay.entertainment.emplay.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BadgeHelper {

    /**
     * Applies "NEW" (released within last 30 days) or "SOON" (not yet released / older)
     * styling to a badge TextView. dateStr must be in "yyyy-MM-dd" format.
     */
    @SuppressLint("SetTextI18n")
    public static void applyWhatsNewBadge(TextView badge, String dateStr) {
        boolean isNew = isIsNew(dateStr);

        if (isNew) {
            badge.setText("NEW");
            badge.setTextColor(Color.parseColor("#5BA3D9"));
            badge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0D2A4A")));
        } else {
            badge.setText("SOON");
            badge.setTextColor(Color.parseColor("#C98A1A"));
            badge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2A1A00")));
        }
    }

    /**
     * Applies "MOVIE" or "TV SHOW" type badge styling.
     */
    public static void applyTypeBadge(Context context, TextView badge, boolean isMovie) {
        badge.setText(isMovie ? "MOVIE" : "TV SHOW");
        badge.setBackgroundTintList(ContextCompat.getColorStateList(context,
                isMovie ? R.color.red_primary : android.R.color.holo_blue_dark));
        badge.setTextColor(Color.WHITE);
    }

    private static boolean isIsNew(String dateStr) {
        boolean isNew = false;
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date releaseDate = sdf.parse(dateStr);
                if (releaseDate != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DAY_OF_YEAR, -30);
                    isNew = releaseDate.after(cal.getTime()) && releaseDate.before(new Date());
                }
            } catch (ParseException ignored) {}
        }
        return isNew;
    }

    public static int getGenreColorByName(Context context, String genreName) {
        if (genreName == null) return ContextCompat.getColor(context, R.color.genre_default);
        int colorRes;
        switch (genreName.toLowerCase(Locale.ROOT)) {
            case "action":
            case "action & adventure":  colorRes = R.color.genre_action;      break;
            case "adventure":           colorRes = R.color.genre_adventure;   break;
            case "animation":           colorRes = R.color.genre_animation;   break;
            case "comedy":              colorRes = R.color.genre_comedy;      break;
            case "crime":               colorRes = R.color.genre_crime;       break;
            case "documentary":         colorRes = R.color.genre_documentary; break;
            case "drama":               colorRes = R.color.genre_drama;       break;
            case "family":              colorRes = R.color.genre_family;      break;
            case "fantasy":             colorRes = R.color.genre_fantasy;     break;
            case "history":             colorRes = R.color.genre_history;     break;
            case "horror":              colorRes = R.color.genre_horror;      break;
            case "kids":                colorRes = R.color.genre_kids;        break;
            case "music":               colorRes = R.color.genre_music;       break;
            case "mystery":             colorRes = R.color.genre_mystery;     break;
            case "news":                colorRes = R.color.genre_news;        break;
            case "reality":             colorRes = R.color.genre_reality;     break;
            case "romance":             colorRes = R.color.genre_romance;     break;
            case "science fiction":
            case "sci-fi & fantasy":    colorRes = R.color.genre_scifi;       break;
            case "soap":                colorRes = R.color.genre_soap;        break;
            case "talk":                colorRes = R.color.genre_talk;        break;
            case "thriller":            colorRes = R.color.genre_thriller;    break;
            case "tv movie":            colorRes = R.color.genre_tv_movie;    break;
            case "war":
            case "war & politics":      colorRes = R.color.genre_war;         break;
            case "western":             colorRes = R.color.genre_western;     break;
            default:                    colorRes = R.color.genre_default;     break;
        }
        return ContextCompat.getColor(context, colorRes);
    }

    public static int getGenreColor(Context context, int genreId) {
        int colorRes;
        switch (genreId) {
            case 28: case 10759: colorRes = R.color.genre_action;      break;
            case 12:             colorRes = R.color.genre_adventure;   break;
            case 16:             colorRes = R.color.genre_animation;   break;
            case 35:             colorRes = R.color.genre_comedy;      break;
            case 80:             colorRes = R.color.genre_crime;       break;
            case 99:             colorRes = R.color.genre_documentary; break;
            case 18:             colorRes = R.color.genre_drama;       break;
            case 10751:          colorRes = R.color.genre_family;      break;
            case 14:             colorRes = R.color.genre_fantasy;     break;
            case 36:             colorRes = R.color.genre_history;     break;
            case 27:             colorRes = R.color.genre_horror;      break;
            case 10762:          colorRes = R.color.genre_kids;        break;
            case 10402:          colorRes = R.color.genre_music;       break;
            case 9648:           colorRes = R.color.genre_mystery;     break;
            case 10763:          colorRes = R.color.genre_news;        break;
            case 10764:          colorRes = R.color.genre_reality;     break;
            case 10749:          colorRes = R.color.genre_romance;     break;
            case 878: case 10765:colorRes = R.color.genre_scifi;       break;
            case 10766:          colorRes = R.color.genre_soap;        break;
            case 10767:          colorRes = R.color.genre_talk;        break;
            case 53:             colorRes = R.color.genre_thriller;    break;
            case 10770:          colorRes = R.color.genre_tv_movie;    break;
            case 10752: case 10768: colorRes = R.color.genre_war;      break;
            case 37:             colorRes = R.color.genre_western;     break;
            default:             colorRes = R.color.genre_default;     break;
        }
        return ContextCompat.getColor(context, colorRes);
    }
}
