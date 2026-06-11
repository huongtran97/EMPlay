package emplay.entertainment.emplay.tool;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
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
     * @param newThresholdDays past days that still count as "NEW" (14 for movies, 7 for TV).
     * Rules:
     *  - Released within newThresholdDays → "NEW" (blue)
     *  - Future, ≤ 30 days away           → "in X day(s)" (amber)
     *  - Future, > 30 days away           → "in X month(s)" (amber)
     *  - Older than newThresholdDays      → badge hidden
     */
    @SuppressLint("SetTextI18n")
    public static void applyWhatsNewBadge(TextView badge, String dateStr, int newThresholdDays) {
        if (dateStr == null || dateStr.isEmpty()) {
            badge.setVisibility(View.GONE);
            return;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = sdf.parse(dateStr);
            if (date == null) { badge.setVisibility(View.GONE); return; }

            long diffMs = new Date().getTime() - date.getTime();

            if (diffMs < 0) {
                // Future release — show countdown
                long daysUntil = (long) Math.ceil((double) (-diffMs) / (1000L * 60 * 60 * 24));
                badge.setVisibility(View.VISIBLE);
                if (daysUntil <= 30) {
                    badge.setText("In " + daysUntil + (daysUntil == 1 ? " day" : " days"));
                } else {
                    long months = Math.max(1, daysUntil / 30);
                    badge.setText("In " + months + (months == 1 ? " month" : " months"));
                }
                badge.setTextColor(Color.parseColor("#C98A1A"));
                badge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2A1A00")));
            } else if (diffMs <= (long) newThresholdDays * 24 * 60 * 60 * 1000) {
                // Released within threshold — "NEW"
                badge.setVisibility(View.VISIBLE);
                badge.setText("NEW");
                badge.setTextColor(Color.parseColor("#5BA3D9"));
                badge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0D2A4A")));
            } else {
                // Past newThresholdDays but still within filter window — "Released"
                badge.setVisibility(View.VISIBLE);
                badge.setText("RELEASED");
                badge.setTextColor(Color.parseColor("#AAAAAA"));
                badge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1E1E1E")));
            }
        } catch (ParseException ignored) {
            badge.setVisibility(View.GONE);
        }
    }

    /**
     * Movie status badge.
     *  release_date ≤ today → "NOW SHOWING" (green)
     *  release_date > today → "COMING SOON"  (amber)
     */
    public static void applyMovieBadge(TextView badge, String releaseDateStr) {
        if (releaseDateStr == null || releaseDateStr.isEmpty()) {
            badge.setVisibility(View.GONE);
            return;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date releaseDate = sdf.parse(releaseDateStr);
            if (releaseDate == null) { badge.setVisibility(View.GONE); return; }
            badge.setVisibility(View.VISIBLE);
            if (releaseDate.after(new Date())) {
                badge.setText("COMING SOON");
                badge.setTextColor(Color.parseColor("#C98A1A"));
                badge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2A1A00")));
            } else {
                badge.setText("NOW SHOWING");
                badge.setTextColor(Color.parseColor("#4DB87A"));
                badge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0D2A1A")));
            }
        } catch (ParseException ignored) {
            badge.setVisibility(View.GONE);
        }
    }

    /**
     * TV show status badge.
     *  first_air_date > today                          → "COMING SOON"  (amber)
     *  first_air_date ≤ today AND nextEpisodeExists    → "NOW SHOWING"  (green)
     *  first_air_date ≤ today AND !nextEpisodeExists   → "FULL SERIES"  (muted blue)
     *  nextEpisodeExists == null (unknown)             → "NOW SHOWING"  as provisional
     */
    public static void applyTVStatusBadge(TextView badge, String firstAirDate, Boolean nextEpisodeExists) {
        badge.setVisibility(View.VISIBLE);
        boolean isFuture = false;
        if (firstAirDate != null && !firstAirDate.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date aired = sdf.parse(firstAirDate);
                if (aired != null && aired.after(new Date())) isFuture = true;
            } catch (ParseException ignored) {}
        }
        if (isFuture) {
            badge.setText("COMING SOON");
            badge.setTextColor(Color.parseColor("#C98A1A"));
            badge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2A1A00")));
        } else if (Boolean.FALSE.equals(nextEpisodeExists)) {
            badge.setText("FULL SERIES");
            badge.setTextColor(Color.parseColor("#9BB5CC"));
            badge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0D1A2A")));
        } else {
            badge.setText("NOW SHOWING");
            badge.setTextColor(Color.parseColor("#4DB87A"));
            badge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0D2A1A")));
        }
    }

    /** True if dateStr is a future date. */
    public static boolean isFutureDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = sdf.parse(dateStr);
            return date != null && date.after(new Date());
        } catch (ParseException ignored) {
            return false;
        }
    }

    /**
     * TV badge for the "What's New – see all" list.
     *  future              → badge hidden (filtered before reaching here)
     *  nextEpisodeExists   → "NEW EP"     (green)
     *  FULL SERIES         → "FULL SERIES" (muted blue)
     */
    public static void applyWhatsNewTVBadge(TextView badge, String firstAirDate, Boolean nextEpisodeExists) {
        badge.setVisibility(View.VISIBLE);
        boolean isFuture = isFutureDate(firstAirDate);
        if (isFuture) {
            badge.setVisibility(View.GONE);
        } else if (Boolean.FALSE.equals(nextEpisodeExists)) {
            badge.setText("FULL SERIES");
            badge.setTextColor(Color.parseColor("#9BB5CC"));
            badge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0D1A2A")));
        } else {
            badge.setText("NEW EP");
            badge.setTextColor(Color.parseColor("#4DB87A"));
            badge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0D2A1A")));
        }
    }

    /**
     * TV show badge — always visible.
     *  < 30 days   → "New"         (blue)
     *  30–365 days → "New season"  (green)
     *  > 365 days  → "New episode" (amber)
     */
    @SuppressLint("SetTextI18n")
    public static void applyTVShowBadge(TextView badge, String firstAirDate) {
        badge.setVisibility(View.VISIBLE);
        String type = getTVShowType(firstAirDate);
        badge.setText(type.equals("New TV show") ? "New" : type);
        switch (type) {
            case "New TV show":
                badge.setTextColor(Color.parseColor("#5BA3D9"));
                badge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0D2A4A")));
                break;
            case "SEASON":
                badge.setTextColor(Color.parseColor("#4DB87A"));
                badge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0D2A1A")));
                break;
            default:
                badge.setTextColor(Color.parseColor("#C98A1A"));
                badge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2A1A00")));
                break;
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

    public static String formatRelativeDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = sdf.parse(dateStr);
            if (date == null) return "";
            long diffMs = new Date().getTime() - date.getTime();
            if (diffMs < 0) return "";
            long days = diffMs / (1000L * 60 * 60 * 24);
            if (days == 0) return "Today";
            if (days == 1) return "Yesterday";
            return days + " days ago";
        } catch (ParseException ignored) {
            return "";
        }
    }

    /**
     * Returns "New TV show" (< 30 days), "New season" (30–365 days), or "New episode" (> 365 days).
     */
    public static String getTVShowType(String firstAirDate) {
        if (isIsNew(firstAirDate)) return "New TV show";
        if (isWithinDays(firstAirDate, 365)) return "SEASON";
        return "EP";
    }

    /** True if dateStr is not older than {@code days} days — includes future dates. */
    public static boolean isNotOlderThan(String dateStr, int days) {
        if (dateStr == null || dateStr.isEmpty()) return false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = sdf.parse(dateStr);
            if (date == null) return false;
            Calendar cutoff = Calendar.getInstance();
            cutoff.add(Calendar.DAY_OF_YEAR, -days);
            return date.after(cutoff.getTime());
        } catch (ParseException ignored) {
            return false;
        }
    }

    /** True if dateStr falls within the last {@code days} days (past only, not future). */
    public static boolean isWithinDays(String dateStr, int days) {
        if (dateStr == null || dateStr.isEmpty()) return false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = sdf.parse(dateStr);
            if (date == null) return false;
            Calendar cutoff = Calendar.getInstance();
            cutoff.add(Calendar.DAY_OF_YEAR, -days);
            Date now = new Date();
            return date.after(cutoff.getTime()) && date.before(now);
        } catch (ParseException ignored) {
            return false;
        }
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
