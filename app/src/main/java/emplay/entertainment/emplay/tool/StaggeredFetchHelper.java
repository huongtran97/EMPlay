package emplay.entertainment.emplay.tool;

import android.os.Handler;
import android.os.Looper;

/**
 * Posts a set of Runnables at staggered delays on the main thread,
 * and cancels them all in onDestroyView to prevent stale callbacks.
 *
 * Usage:
 *   fetchHelper = new StaggeredFetchHelper()
 *       .add(150, this::fetchCast)
 *       .add(300, this::fetchSuggestions)
 *       .add(450, this::fetchTrailers);
 *
 *   // onDestroyView:
 *   if (fetchHelper != null) { fetchHelper.cancel(); fetchHelper = null; }
 */
public class StaggeredFetchHelper {

    private final Handler handler = new Handler(Looper.getMainLooper());

    public StaggeredFetchHelper add(long delayMs, Runnable task) {
        handler.postDelayed(task, delayMs);
        return this;
    }

    public void cancel() {
        handler.removeCallbacksAndMessages(null);
    }
}