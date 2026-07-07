package emplay.entertainment.emplay.tool;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

public final class GlideImageLoader {

    private GlideImageLoader() {}

    /** Load a URL with a placeholder. A null/empty url shows the placeholder immediately. */
    public static void load(@NonNull Context ctx, @Nullable String url,
                            @NonNull ImageView iv, @DrawableRes int placeholder) {
        Glide.with(ctx).load(url).placeholder(placeholder).into(iv);
    }

    /** Circle-crop variant. Used for avatars and cast profile photos. */
    public static void loadCircle(@NonNull Context ctx, @Nullable String url,
                                  @NonNull ImageView iv, @DrawableRes int placeholder) {
        Glide.with(ctx).load(url).circleCrop().placeholder(placeholder).into(iv);
    }

    /**
     * Rounded-corners variant. Pass {@code placeholder = 0} to skip the placeholder.
     * Used for provider logos.
     */
    public static void loadRounded(@NonNull Context ctx, @Nullable String url,
                                   @NonNull ImageView iv, int radiusPx, @DrawableRes int placeholder) {
        RequestBuilder<Drawable> req = Glide.with(ctx).load(url).transform(new RoundedCorners(radiusPx));
        if (placeholder != 0) req = req.placeholder(placeholder);
        req.into(iv);
    }

    public static void clear(@NonNull Context ctx, @NonNull ImageView iv) {
        Glide.with(ctx).clear(iv);
    }
}