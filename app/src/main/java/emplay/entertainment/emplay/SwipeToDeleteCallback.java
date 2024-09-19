package emplay.entertainment.emplay;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import emplay.entertainment.emplay.adapter.MovieLikedAdapter;
import emplay.entertainment.emplay.adapter.TVLikedAdapter;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
    private final Paint paint = new Paint();
    private final RecyclerView recyclerView;

    public SwipeToDeleteCallback(RecyclerView recyclerView) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.recyclerView = recyclerView;
        paint.setColor(Color.RED);
        paint.setTextSize(48);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        View itemView = viewHolder.itemView;
        if (dX > 0) {
            // Draw a red background with "DELETE" text for swipe right
            c.drawRect(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + dX, itemView.getBottom(), paint);
            paint.setColor(Color.WHITE);
            c.drawText("DELETE", itemView.getLeft() + 16, itemView.getTop() + 64, paint);
            paint.setColor(Color.RED);
        } else if (dX < 0) {
            // Draw a red background with "DELETE" text for swipe left
            c.drawRect(itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom(), paint);
            paint.setColor(Color.WHITE);
            c.drawText("DELETE", itemView.getRight() - 200, itemView.getTop() + 64, paint);
            paint.setColor(Color.RED);
        }
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        if (position != RecyclerView.NO_POSITION) {
            RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
            if (adapter instanceof MovieLikedAdapter) {
                ((MovieLikedAdapter) adapter).removeItem(position);
            } else if (adapter instanceof TVLikedAdapter) {
                ((TVLikedAdapter) adapter).removeItem(position);
            }
        }
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, ItemTouchHelper.UP); // Allow both swipe directions
    }
}
