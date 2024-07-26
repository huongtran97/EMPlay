package emplay.entertainment.emplay;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *  * @author Tran Ngoc Que Huong
 *  * @version 1.0
 *
 * Fragment that displays a list of popular movies and movie details.
 */
public class HomeFragment extends Fragment {
    private static final String INITIAL_JSON_URL = "https://api.themoviedb.org/3/movie/popular?api_key=ff3dce8592d15d036bf53cbedeca224b";
    private List<MovieModel> movieList;
    private RecyclerView movieRecyclerView, movieRecyclerView1;
    private MovieAdapter adapter;
    private MovieInformationAdapter adapter1;

    /**
     * Called to create the view hierarchy associated with this fragment.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return The view for the fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.movie_activity, container, false);

        // Initialize RecyclerViews
        movieRecyclerView = view.findViewById(R.id.movie_card_view);
        movieRecyclerView1 = view.findViewById(R.id.movie_information_details);

        // Initialize movie list and adapters
        movieList = new ArrayList<>();
        adapter = new MovieAdapter(getContext(), movieList, this::onItemClicked);
        movieRecyclerView.setAdapter(adapter);
        movieRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));  // Use GridLayoutManager for grid view

        adapter1 = new MovieInformationAdapter(new ArrayList<>());
        movieRecyclerView1.setAdapter(adapter1);
        movieRecyclerView1.setLayoutManager(new LinearLayoutManager(getContext()));  // Use LinearLayoutManager for list view

        // Fetch initial movie data
        new FetchInitialData().execute(INITIAL_JSON_URL);

        return view;
    }

    /**
     * Handles click events on items in the movie list.
     *
     * @param itemView The clicked MovieModel item.
     */
    public void onItemClicked(MovieModel itemView) {
        new FetchNewData().execute(itemView.getId());
    }

    /**
     * AsyncTask to fetch initial data (list of popular movies) from the API.
     */
    private class FetchInitialData extends AsyncTask<String, Void, List<MovieModel>> {
        @Override
        protected List<MovieModel> doInBackground(String... urls) {
            String current = "";
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try (java.io.InputStream is = urlConnection.getInputStream();
                     java.io.InputStreamReader isr = new java.io.InputStreamReader(is)) {
                    int movieData = isr.read();
                    while (movieData != -1) {
                        current += (char) movieData;
                        movieData = isr.read();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return parseMovieData(current);
        }

        @Override
        protected void onPostExecute(List<MovieModel> movieList) {
            PutDataIntoRecyclerview(movieList);  // Update both RecyclerViews
        }
    }

    /**
     * AsyncTask to fetch detailed data for a single movie based on its ID.
     */
    private class FetchNewData extends AsyncTask<String, Void, MovieModel> {
        @Override
        protected MovieModel doInBackground(String... itemIds) {
            String itemId = itemIds[0];
            String url = "https://api.themoviedb.org/3/movie/" + itemId + "?api_key=ff3dce8592d15d036bf53cbedeca224b";
            String current = "";
            try {
                URL apiUrl = new URL(url);
                HttpURLConnection urlConnection = (HttpURLConnection) apiUrl.openConnection();
                try (java.io.InputStream is = urlConnection.getInputStream();
                     java.io.InputStreamReader isr = new java.io.InputStreamReader(is)) {
                    int movieData = isr.read();
                    while (movieData != -1) {
                        current += (char) movieData;
                        movieData = isr.read();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return parseSingleMovieData(current);
        }

        @Override
        protected void onPostExecute(MovieModel movieModel) {
            if (movieModel != null) {
                List<MovieModel> detailList = new ArrayList<>();
                detailList.add(movieModel);
                adapter1.updateData(detailList);  // Update adapter1 with a single movie detail in list format
            }
        }
    }

    /**
     * Parses JSON data to extract a list of movies.
     *
     * @param jsonData The JSON data as a string.
     * @return A list of MovieModel objects.
     */
    private List<MovieModel> parseMovieData(String jsonData) {
        List<MovieModel> movieList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray jsonArray = jsonObject.getJSONArray("results");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                MovieModel movieModel = new MovieModel(
                        jsonObject1.getString("id"),
                        jsonObject1.getString("title"),
                        "Vote: " + jsonObject1.getString("vote_average"),
                        jsonObject1.getString("poster_path"),
                        "Overview: " + jsonObject1.getString("overview"),
                        "Language: " + jsonObject1.getString("original_language"),
                        "Release Date: " + jsonObject1.getString("release_date")
                );
                movieList.add(movieModel);
            }
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
        return movieList;
    }

    /**
     * Parses JSON data to extract a single movie's details.
     *
     * @param jsonData The JSON data as a string.
     * @return A MovieModel object containing the movie's details.
     */
    private static MovieModel parseSingleMovieData(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            return new MovieModel(
                    jsonObject.getString("id"),
                    jsonObject.getString("title"),
                    "Vote: " + jsonObject.getString("vote_average"),
                    jsonObject.getString("poster_path"),
                    "Overview: " + jsonObject.getString("overview"),
                    "Language: " + jsonObject.getString("original_language"),
                    "Release Date: " + jsonObject.getString("release_date")
            );
        } catch (org.json.JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Updates the data displayed in the RecyclerViews.
     *
     * @param movieList The list of movies to display.
     */
    private void PutDataIntoRecyclerview(List<MovieModel> movieList) {
        // Update the main RecyclerView
        adapter.updateData(movieList);

        // Update the detail RecyclerView with the same list or a new list if needed
        movieRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));  // Use GridLayoutManager for grid view
        movieRecyclerView.setAdapter(adapter);

        movieRecyclerView1.setLayoutManager(new LinearLayoutManager(getContext()));  // Use LinearLayoutManager for list view
        movieRecyclerView1.setAdapter(adapter1);
    }
}


