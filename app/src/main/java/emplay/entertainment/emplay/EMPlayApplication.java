package emplay.entertainment.emplay;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;

import emplay.entertainment.emplay.api.common.DeviceIdHelper;
import okhttp3.Request;
import emplay.entertainment.emplay.api.common.ApiClient;

public class EMPlayApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        ApiClient.init(getCacheDir(), DeviceIdHelper.getDeviceId(getApplicationContext()));
        clearGlideDiskCache();
        warmUpProxy();
    }

    private void clearGlideDiskCache() {
        new Thread(() -> Glide.get(this).clearDiskCache(), "glide-cache-clear").start();
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