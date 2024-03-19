package emplay.entertainment.emplay;

import static emplay.entertainment.emplay.R.*;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;

import java.net.URL;
import java.net.HttpURLConnection;

import org.json.JSONObject;
import org.json.JSONArray;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;

public class MovieOption extends AppCompatActivity {

    //APIs JSON link on the Internet
    private static String JSON_URL = "https://api.themoviedb.org/3/movie/popular?api_key=ff3dce8592d15d036bf53cbedeca224b";
    List<MovieModel> movieList;
    RecyclerView movieRecyclerView;

    @Override
    protected void onCreate(android.os.Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(layout.movie_activity);

        movieList = new java.util.ArrayList<>();
        movieRecyclerView = findViewById(R.id.movie_recycler_view);

        GetMovieData getMovieData = new GetMovieData();
        getMovieData.execute();

    }

    public class GetMovieData extends AsyncTask<String, String, String> {


        @Override
        protected String doInBackground(String... strings) {

            String current = "";
            try {
                URL url;
                HttpURLConnection urlConnection = null;

                try {
                    url = new URL(JSON_URL);
                    urlConnection = (HttpURLConnection) url.openConnection();

                    java.io.InputStream is = urlConnection.getInputStream();
                    java.io.InputStreamReader isr = new java.io.InputStreamReader(is);


                    int movieData = isr.read();
                    while (movieData != -1) {
                        current += (char) movieData;
                        movieData = isr.read();
                    }
                    return current;

                } catch (java.net.MalformedURLException e) {
                    e.printStackTrace();
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return current;

        }

        @Override
        public void onPostExecute(String s) {

            try {
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = jsonObject.getJSONArray("results");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    MovieModel movieModel = new MovieModel();
                    movieModel.setId(jsonObject1.getString("id"));
                    movieModel.setName(jsonObject1.getString("title"));
                    movieModel.setVote(jsonObject1.getString("vote_average"));
                    movieModel.setImg(jsonObject1.getString("poster_path"));

                    movieList.add(movieModel);
                }
            } catch (org.json.JSONException e) {
                e.printStackTrace();
            }
            PutDataIntoRecyclerview(movieList);
        }
    }

    private void PutDataIntoRecyclerview(List<MovieModel> movieList) {

        MovieAdapter movieAdapter = new MovieAdapter(this, movieList);
        movieRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        movieRecyclerView.setAdapter(movieAdapter);
    }


}
