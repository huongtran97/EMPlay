package emplay.entertainment.emplay.api.common;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import java.util.concurrent.TimeUnit;

import emplay.entertainment.emplay.BuildConfig;
import okhttp3.Cache;
import okhttp3.ConnectionSpec;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    // Switch to "false" to bypass the proxy and hit TMDB directly (debug only).
    // When true, the API key never leaves the server — safer for release builds.
    public static boolean USE_PROXY = true;

    private static final String TMDB_API_KEY = BuildConfig.API_KEY;

    private static final String BASE_URL = "https://emplay-proxy-production.up.railway.app/";
    private static File cacheDir;
    private static String deviceId = "";

    public static void init(File appCacheDir,  String appDeviceId) {
        cacheDir = appCacheDir;
        deviceId = appDeviceId;
    }

    // Lazily created — one instance shared across the whole app.
    static Retrofit retrofit;

    public static synchronized Retrofit getClient() {
        if (retrofit == null) {
            ConnectionSpec tlsSpec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_3)
                    .build();

            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(35, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .callTimeout(50, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .protocols(Arrays.asList(Protocol.HTTP_1_1))
                    .connectionSpecs(Arrays.asList(tlsSpec, ConnectionSpec.CLEARTEXT))
                    .addInterceptor(chain -> {
                        // Retry once on timeout or PROTOCOL_ERROR
                        for (int attempt = 0; attempt < 2; attempt++) {
                            try {
                                return chain.proceed(chain.request());
                            } catch (IOException e) {
                                if (attempt == 0) {
                                    String msg = e.getMessage();
                                    if (e instanceof SocketTimeoutException
                                            || (msg != null && msg.contains("PROTOCOL_ERROR"))) {
                                        continue;
                                    }
                                }
                                throw e;
                            }
                        }
                        throw new IOException("unreachable");
                    })
                    .addInterceptor(chain -> {
                        Request original = chain.request();

                        // Only attach app headers to requests going to OUR proxy.
                        // Never leak the app token to third-party hosts (e.g. image.tmdb.org).
                        if (!"emplay-proxy-production.up.railway.app".equals(original.url().host())) {
                            return chain.proceed(original);
                        }

                        Request.Builder builder = original.newBuilder()
                                .header("X-App-Token", BuildConfig.APP_TOKEN);
                        if (!deviceId.isEmpty()) {
                            builder.header("X-Device-Id", deviceId);
                        }
                        return chain.proceed(builder.build());
                    })
                    .addInterceptor(new TMDBInterceptor());

            if (cacheDir != null) {
                clientBuilder.cache(new Cache(new File(cacheDir, "tmdb_http_cache"), 10 * 1024 * 1024));
            }

            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
                clientBuilder.addInterceptor(logging);
            }

            OkHttpClient okHttpClient = clientBuilder.build();
            glideClient = okHttpClient;

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    private static OkHttpClient glideClient;

    public static synchronized OkHttpClient getSharedClient() {
        if (glideClient == null) {
            glideClient = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .protocols(Arrays.asList(Protocol.HTTP_1_1))
                    .build();
        }
        return glideClient;
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
