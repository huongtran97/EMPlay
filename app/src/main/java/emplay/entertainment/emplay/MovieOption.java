package emplay.entertainment.emplay;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class MovieOption extends AppCompatActivity {
    private static final String KEY_NAME = "name";
    private static final String KEY_PASS = "password";
    SharedPreferences sharedPreferences;
    String stringURL;
    Bitmap img = null;
    TextView name;
    String mTitle = "";
    String mReleaseDate = "";
    String mVoteAverage = "";
    String mOverview = "";
    String mPoster = "";

    private List<MovieSlide> slideList;
    private ViewPager movieSlidePager;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_option);

        movieSlidePager = findViewById(R.id.slider_page);


        //list of slides
        slideList = new ArrayList<>();
        slideList.add(new MovieSlide(R.drawable.miaslide1,"Made In Abyss","A girl and her robot companion search for her mother, who's lost within a vast chasm."));
        slideList.add(new MovieSlide(R.drawable.dsslide2,"Demon Slayer: A guide to the Swordsmith Village arc ","The Swordsmith Village Arc is set to pick up where the last season left off, with Tanjiro travelling to the titular village to repair his damaged Nichirin sword. Along the way, he will reunite with two members of the Hashira, the highest-ranking swordsmen in the Demon Slayer Corps."));
        slideList.add(new MovieSlide(R.drawable.jkslide3,"Jujutsu Kaisen","Yuji Itadori, a kind-hearted teenager, joins his school's Occult Club for fun, but discovers that its members are actual sorcerers who can manipulate the energy between beings for their own use. He hears about a cursed talisman - the finger of Sukuna, a demon - and its being targeted by other cursed beings."));


        MovieSlideAdapter movieAdapter = new MovieSlideAdapter(this, slideList);
        movieSlidePager.setAdapter(movieAdapter);



    }

    public Bitmap getImg(String mTitle, String mPoster) {
        Bitmap img = null;
        try{
            Log.d("abc", mPoster);
            File file = new File(getFilesDir(),mPoster);
            if(file.exists()){
                img = BitmapFactory.decodeFile(getFilesDir() + mPoster);
            } else {
                URL imgUrl = new URL(mPoster );
                HttpURLConnection connection = (HttpURLConnection) imgUrl.openConnection();
                connection.connect();
                int responseCode = connection.getResponseCode();

                if (responseCode == 200) {
                    img = BitmapFactory.decodeStream(connection.getInputStream());
                    String filename = mTitle + mPoster.substring(mPoster.length()-4);
                    img.compress(Bitmap.CompressFormat.JPEG,100, openFileOutput(filename, Activity.MODE_PRIVATE));

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }
    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void runSearchBtn(String movieName) {

        Executor newThread = Executors.newSingleThreadExecutor();
        newThread.execute(() -> {
            try {
                stringURL = "https://api.themoviedb.org/3/movie/popular?api_key=ff3dce8592d15d036bf53cbedeca224b"
                        + URLEncoder.encode(mTitle, "UTF-8");
                URL url = new URL(stringURL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                String text = (new BufferedReader(
                        new InputStreamReader(in, StandardCharsets.UTF_8)))
                        .lines()
                        .collect(Collectors.joining("\n"));

                JSONObject theDocument = new JSONObject(text);
                JSONArray voteArray = theDocument.getJSONArray("vote_average");
                String position0 = String.valueOf(voteArray.getJSONObject(0));
                mTitle = position0 = theDocument.getString("title");
                mReleaseDate = position0 = theDocument.getString("release_date");
                mOverview = position0 = theDocument.getString("overview");
                mPoster = position0 = theDocument.getString("poster_path");

                img = getImg(mTitle, mPoster);

                runOnUiThread(() -> {
                    TextView title = findViewById(R.id.MTitle);
                    title.setText("mTitle");
                    TextView releaseDate = findViewById(R.id.MReleaseDate);
                    releaseDate.setText("Release Date: " + mReleaseDate);
                    TextView overview = findViewById(R.id.MOverview);
                    overview.setText("Overview: " + mOverview);
                    ImageView poster = findViewById(R.id.MPoster);
                    poster.setImageBitmap(img);
                });



            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
