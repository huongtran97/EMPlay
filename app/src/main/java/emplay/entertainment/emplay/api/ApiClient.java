package emplay.entertainment.emplay.api;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import emplay.entertainment.emplay.BuildConfig;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    // Switch to false to bypass the proxy and hit TMDB directly (debug only).
    // When true, the API key never leaves the server — safer for release builds.
    public static boolean USE_PROXY = true;

    // Only used when USE_PROXY = false (direct TMDB calls).
    private static final String TMDB_API_KEY = BuildConfig.API_KEY;

    private static final String BASE_URL = "https://emplay-proxy-production.up.railway.app/";

    // Lazily created — one instance shared across the whole app.
    static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .addInterceptor(chain -> {
                        // "Accept-Encoding: identity" disables gzip on every request.
                        // Without this, some proxy responses came back with malformed
                        // compressed data and OkHttp threw "gzip finished without exhausting source".
                        Request request = chain.request().newBuilder()
                                .header("Accept-Encoding", "identity")
                                .header("X-App-Token", BuildConfig.APP_TOKEN) // authenticates with our proxy
                                .build();
                        return chain.proceed(request);
                    })
                    .addInterceptor(new TMDBInterceptor());

            // Only log in debug so we don't accidentally print tokens in production.
            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
                clientBuilder.addInterceptor(loggingInterceptor);
            }

            OkHttpClient okHttpClient = clientBuilder.build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    /**
     * Rewrites proxy-style requests to direct TMDB calls when USE_PROXY = false.
     * All requests in MovieApiService look like:
     *   GET /api/tmdb?path=3/movie/123&...
     * This interceptor transforms that into:
     *   GET https://api.themoviedb.org/3/movie/123?api_key=...&...
     * When USE_PROXY = true it's a no-op — the proxy handles the rewrite on the server side.
     */
    public static class TMDBInterceptor implements Interceptor {
        @Override
        @NonNull
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request request = chain.request();
            if (!USE_PROXY) {
                HttpUrl url = request.url();

                if (url.encodedPath().contains("/api/tmdb")) {
                    String path = url.queryParameter("path");
                    if (path != null) {
                        HttpUrl directBase = HttpUrl.parse("https://api.themoviedb.org/");
                        if (directBase != null) {
                            HttpUrl.Builder newUrlBuilder = directBase.newBuilder();

                            // Reconstruct the path segments from the "path" query parameter.
                            for (String segment : path.split("/")) {
                                if (!segment.isEmpty()) {
                                    newUrlBuilder.addPathSegment(segment);
                                }
                            }

                            // Copy all other query params (skip "path" — it was just a routing key).
                            for (int i = 0; i < url.querySize(); i++) {
                                String name = url.queryParameterName(i);
                                if (!"path".equals(name)) {
                                    String value = url.queryParameterValue(i);
                                    if (value != null) {
                                        newUrlBuilder.setQueryParameter(name, value);
                                    }
                                }
                            }

                            newUrlBuilder.setQueryParameter("api_key", TMDB_API_KEY);
                            request = request.newBuilder()
                                    .url(newUrlBuilder.build())
                                    .build();
                        }
                    }
                }
            }
            return chain.proceed(request);
        }
    }
}
