package emplay.entertainment.emplay.tool;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import emplay.entertainment.emplay.R;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class UiUtils {

    /**
     * Sets a blurred backdrop as the background of a view.
     */
    public static void setupBlurredBackground(Fragment fragment, String backdropPath, String posterPath, View targetView) {
        if (!fragment.isAdded()) return;

        String path = (backdropPath != null && !backdropPath.isEmpty()) 
            ? "https://image.tmdb.org/t/p/w500/" + backdropPath 
            : (posterPath != null && !posterPath.isEmpty()) ? "https://image.tmdb.org/t/p/w500/" + posterPath : null;

        if (path == null) return;

        Glide.with(fragment)
                .load(path)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(new MultiTransformation<>(new CenterCrop(), new BlurTransformation(5)))
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        if (fragment.isAdded()) {
                            LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{
                                    resource,
                                    ContextCompat.getDrawable(fragment.requireContext(), R.drawable.gradient_bg)
                            });
                            targetView.setBackground(layerDrawable);
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) { }
                });
    }
}
