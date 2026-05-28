package emplay.entertainment.emplay.models;

public interface MediaItem {
    int getMediaId();
    String getTitle();
    String getPosterPath();
    String getBackdropPath();
    double getVoteAverage();
    String getSavedTimestamp();
}