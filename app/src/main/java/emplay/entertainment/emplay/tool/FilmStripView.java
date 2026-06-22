package emplay.entertainment.emplay.tool;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

public class FilmStripView extends View {

    private Paint holePaint;
    private RectF holeRect;  // reusable, no allocation in onDraw
    private float holeWidth;
    private float holeHeight;
    private float holeGap;
    private float paddingH;
    private float cornerRadius;

    public FilmStripView(Context context) {
        super(context);
        init();
    }

    public FilmStripView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FilmStripView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        holePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        holePaint.setColor(Color.BLACK);
        holePaint.setStyle(Paint.Style.FILL);

        holeWidth    = dpToPx(11f);
        holeHeight   = dpToPx(6f);
        holeGap      = dpToPx(11f);
        paddingH     = dpToPx(11f);
        cornerRadius = dpToPx(2f);

        holeRect = new RectF(); // allocated once
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float top    = (getHeight() - holeHeight) / 2f; // vertically centered
        float bottom = top + holeHeight;
        float x      = paddingH;

        while (x + holeWidth <= getWidth() - paddingH) {
            holeRect.set(x, top, x + holeWidth, bottom);
            canvas.drawRoundRect(holeRect, cornerRadius, cornerRadius, holePaint);
            x += holeWidth + holeGap;
        }
    }

    private float dpToPx(float dp) {
        return dp * getContext().getResources().getDisplayMetrics().density;
    }
}
