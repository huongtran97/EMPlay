package emplay.entertainment.emplay.api;

public class ImageUrl {
    private static final String BASE = "https://image.tmdb.org/t/p/";

    public static final String THUMBNAIL       = BASE + "w185";  // small grid / horizontal rows
    public static final String CARD            = BASE + "w342";  // medium cards (profile, watchlist)
    public static final String POSTER          = BASE + "w500";  // detail screen poster
    public static final String BACKDROP        = BASE + "w780";  // hero banner
}
