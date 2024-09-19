package emplay.entertainment.emplay.api;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 *  * @author Tran Ngoc Que Huong
 *  * @version 1.0
 *
 * Provides a singleton instance of the Retrofit client for making API requests.
 */
public class ApiClient {

    private static final String BASE_URL = "https://api.themoviedb.org/";
    static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}

