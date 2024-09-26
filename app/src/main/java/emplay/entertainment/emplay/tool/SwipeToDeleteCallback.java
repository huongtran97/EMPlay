package emplay.entertainment.emplay.tool;

import android.graphics.Canvas;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import emplay.entertainment.emplay.adapter.MovieLikedAdapter;
import emplay.entertainment.emplay.adapter.TVLikedAdapter;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
    private final RecyclerView recyclerView;

    public SwipeToDeleteCallback(RecyclerView recyclerView) {
        super(0, ItemTouchHelper.UP | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.recyclerView = recyclerView;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;
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
    public void onChildDraw(@NonNull Canvas c,
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, ItemTouchHelper.UP | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
    }
}
