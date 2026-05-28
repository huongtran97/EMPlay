package emplay.entertainment.emplay.tool;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

/**
 *  Draws a filled black semicircle — used as a decorative divider between sections
 *  in some layouts. The arc spans the bottom half of the view's bounding box.
 */
public class ArcView extends View {
    private Paint paint;
    private Path path;

    public ArcView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF000000);
        path = new Path();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        path.reset();
        path.addArc(0, 0, width, height, 180, 180);
        canvas.drawPath(path, paint);
    }
}

