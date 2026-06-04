package emplay.entertainment.emplay;

import android.app.Application;

import okhttp3.Request;
import emplay.entertainment.emplay.api.common.ApiClient;

public class EMPlayApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ApiClient.init(getCacheDir());
        warmUpProxy();
    }

    private void warmUpProxy() {
        // Fire-and-forget HEAD request to wake Railway container before user hits any screen.
        // If the container is already warm this completes in ~100ms and is discarded.
        new Thread(() -> {
            try {
                Request ping = new Request.Builder()
                        .url("https://emplay-proxy-production.up.railway.app/health")
                        .head()
                        .build();
                ApiClient.getSharedClient().newCall(ping).execute().close();
            } catch (Exception ignored) {
                // Warm-up is best-effort — failure here is fine,
                // real requests will still work (just slower on first load).
            }
        }, "proxy-warmup").start();
    }
}