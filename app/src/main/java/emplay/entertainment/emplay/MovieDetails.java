package emplay.entertainment.emplay;

import android.widget.TextView;

public class MovieDetails {
    int id;
    String mTitle;
    String mReleaseDate;
    String mVoteAverage;
    String mOverview;
    String mPoster;

    public MovieDetails(int id, String mTitle, String mReleaseDate, String mVoteAverage, String mOverview, String mPoster) {

        this.id = id;
        this.mTitle = mTitle;
        this.mReleaseDate = mReleaseDate;
        this.mVoteAverage = mVoteAverage;
        this.mOverview = mOverview;
        this.mPoster = mPoster;
    }
    public void setId(int l){ id = l;}
    public int getId(){ return id;}
    public MovieDetails(String s) {
        s = s;
    }

    public MovieDetails() {
        this.mTitle = mTitle;
        this.mReleaseDate = mReleaseDate;
        this.mVoteAverage = mVoteAverage;
        this.mOverview = mOverview;
        this.mPoster = mPoster;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getmReleaseDate() {
        return mReleaseDate;
    }

    public void setmReleaseDate(String mReleaseDate) {
        this.mReleaseDate = mReleaseDate;
    }

    public String getmVoteAverage() {
        return mVoteAverage;
    }

    public void setmVoteAverage(String mVoteAverage) {
        this.mVoteAverage = mVoteAverage;
    }

    public String getmOverview() {
        return mOverview;
    }

    public void setmOverview(String mOverview) {
        this.mOverview = mOverview;
    }

    public String getmPoster(TextView mPoster) {
        return this.mPoster;
    }

    public void setmPoster(String mPoster) {
        this.mPoster = mPoster;
    }

}
