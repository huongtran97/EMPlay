package emplay.entertainment.emplay.tool;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.StringRes;

public final class ToastHelper {

    private ToastHelper() {}

    public static void show(Context ctx, String message) {
        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
    }

    public static void show(Context ctx, @StringRes int resId) {
        Toast.makeText(ctx, resId, Toast.LENGTH_SHORT).show();
    }

    public static void showLong(Context ctx, String message) {
        Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
    }

    public static void showLong(Context ctx, @StringRes int resId) {
        Toast.makeText(ctx, resId, Toast.LENGTH_LONG).show();
    }
}