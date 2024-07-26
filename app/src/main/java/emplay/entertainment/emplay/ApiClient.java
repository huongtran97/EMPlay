package emplay.entertainment.emplay;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 *  * @author Tran Ngoc Que Huong
 *  * @version 1.0
 *
 * Provides a singleton instance of the Retrofit client for making API requests.
 */
public class ApiClient {
    private static Retrofit retrofit = null;

    /**
     * Returns a singleton instance of the Retrofit client.
     * If the Retrofit instance does not already exist, it is created with the specified configuration.
     *
     * @return A {@link Retrofit} instance configured with the base URL and JSON converter.
     */
    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.themoviedb.org/3/") // Base URL for the API
                    .addConverterFactory(GsonConverterFactory.create()) // Converter for JSON to Java objects
                    .build();
        }
        return retrofit;
    }
}

