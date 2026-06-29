package emplay.entertainment.emplay;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;

import emplay.entertainment.emplay.api.common.DeviceIdHelper;
import okhttp3.Request;
import emplay.entertainment.emplay.api.common.ApiClient;

public class EMPlayApplication extends Application {

    public static final String PREF_NIGHT_MODE = "pref_night_mode";

    @Override
    public void onCreate() {
        super.onCreate();
        int savedMode = PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(PREF_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_YES);
        AppCompatDelegate.setDefaultNightMode(savedMode);
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