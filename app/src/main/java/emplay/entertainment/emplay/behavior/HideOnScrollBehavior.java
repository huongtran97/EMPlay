package emplay.entertainment.emplay.behavior;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

public class HideOnScrollBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

    private static final int SHOW_DURATION_MS = 225;
    private static final int HIDE_DURATION_MS = 175;

    private int childHeight;
    private boolean shown = true;

    public HideOnScrollBehavior() {}

    public HideOnScrollBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onLayoutChild(@NonNull CoordinatorLayout parent, V child, int layoutDirection) {
        childHeight = child.getMeasuredHeight();
        return super.onLayoutChild(parent, child, layoutDirection);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V child,
                                       @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        return (axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull View target,
                               int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed,
                               int type, @NonNull int[] consumed) {
        if (dyConsumed > 0 && shown) {
            slideDown(child);
        } else if (dyConsumed < 0 && !shown) {
            slideUp(child);
        }
    }

    private void slideDown(V child) {
        shown = false;
        int bottomMargin = 0;
        ViewGroup.LayoutParams lp = child.getLayoutParams();
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            bottomMargin = ((ViewGroup.MarginLayoutParams) lp).bottomMargin;
        }
        child.animate()
                .translationY(childHeight + bottomMargin)
                .setDuration(HIDE_DURATION_MS)
                .start();
    }

    private void slideUp(V child) {
        shown = true;
        child.animate()
                .translationY(0)
                .setDuration(SHOW_DURATION_MS)
                .start();
    }
}