package emplay.entertainment.emplay.api;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.List;

import emplay.entertainment.emplay.models.MovieModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MovieApiHelper {
    private static final String API_KEY = "ff3dce8592d15d036bf53cbedeca224b"; // Consider securing the API key
    private static final String BASE_URL = "https://api.themoviedb.org/3/";
    private MovieApiService apiService;
    private Context context;

    public MovieApiHelper(Context context) {
        this.context = context.getApplicationContext(); // Use application context to avoid memory leaks
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(MovieApiService.class);
    }

    public void fetchMovieDetails(int movieId, final MovieDetailsCallback callback) {
        Call<MovieDetailsResponse> call = apiService.getMovieDetails(movieId, API_KEY);
        call.enqueue(new Callback<MovieDetailsResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieDetailsResponse> call, @NonNull Response<MovieDetailsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieModel movieDetails = response.body().getMovieDetails();
                    if (movieDetails != null) {
                        callback.onSuccess(movieDetails);
                    } else {
                        showToast("Movie details not found");
                        callback.onFailure("Movie details not found");
                    }
                } else {
                    handleApiError(response);
                    callback.onFailure("API call unsuccessful: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovieDetailsResponse> call, @NonNull Throwable t) {
                showToast("Error: " + t.getMessage());
                callback.onFailure(t.getMessage());
            }
        });
    }

    public void fetchCastList(int movieId, final CastListCallback callback) {
        Call<MovieCreditsResponse> call = apiService.getMovieCredits(movieId, API_KEY);
        call.enqueue(new Callback<MovieCreditsResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieCreditsResponse> call, @NonNull Response<MovieCreditsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MovieCreditsResponse.Cast> castList = response.body().getCast();
                    if (castList != null) {
                        callback.onSuccess(castList);
                    } else {
                        showToast("Cast list not found");
                        callback.onFailure("Cast list not found");
                    }
                } else {
                    handleApiError(response);
                    callback.onFailure("API call unsuccessful: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovieCreditsResponse> call, @NonNull Throwable t) {
                showToast("Error: " + t.getMessage());
                callback.onFailure(t.getMessage());
            }
        });
    }

    private void handleApiError(Response<?> response) {
        // Log error or handle specific API errors
        String errorBody = response.errorBody() != null ? response.errorBody().toString() : "No error body";
        showToast("API Error: " + errorBody);
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public interface MovieDetailsCallback {
        void onSuccess(MovieModel movieDetails);
        void onFailure(String errorMessage);
    }

    public interface CastListCallback {
        void onSuccess(List<MovieCreditsResponse.Cast> castList);
        void onFailure(String errorMessage);
    }
}

