package emplay.entertainment.emplay.models.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReleaseAlertItem {
    public static final String TYPE_MOVIE = "movie";
    public static final String TYPE_TV = "tv";

    private final int mediaId;
    private final String mediaType;
    private final String title;
    private final String posterPath;
    private final String releaseDate;

    public ReleaseAlertItem(int mediaId, String mediaType, String title, String posterPath, String releaseDate) {
        this.mediaId = mediaId;
        this.mediaType = mediaType;
        this.title = title;
        this.posterPath = posterPath;
        this.releaseDate = releaseDate;
    }

    public int getMediaId() { return mediaId; }
    public String getMediaType() { return mediaType; }
    public String getTitle() { return title; }
    public String getPosterPath() { return posterPath; }
    public String getReleaseDate() { return releaseDate; }

    public boolean isReleased() {
        if (releaseDate == null || releaseDate.isEmpty()) return false;
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(releaseDate);
            return date != null && !date.after(new Date());
        } catch (ParseException e) {
            return false;
        }
    }
}