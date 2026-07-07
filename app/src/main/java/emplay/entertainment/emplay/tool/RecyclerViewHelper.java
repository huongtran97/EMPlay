package emplay.entertainment.emplay.tool;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public final class RecyclerViewHelper {

    private RecyclerViewHelper() {}

    public static LinearLayoutManager setupVertical(RecyclerView rv, Context ctx) {
        LinearLayoutManager lm = new LinearLayoutManager(ctx);
        rv.setLayoutManager(lm);
        return lm;
    }

    public static LinearLayoutManager setupVertical(RecyclerView rv, Context ctx, RecyclerView.Adapter<?> adapter) {
        LinearLayoutManager lm = setupVertical(rv, ctx);
        rv.setAdapter(adapter);
        return lm;
    }

    public static LinearLayoutManager setupHorizontal(RecyclerView rv, Context ctx) {
        LinearLayoutManager lm = new LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false);
        rv.setLayoutManager(lm);
        return lm;
    }

    public static LinearLayoutManager setupHorizontal(RecyclerView rv, Context ctx, RecyclerView.Adapter<?> adapter) {
        LinearLayoutManager lm = setupHorizontal(rv, ctx);
        rv.setAdapter(adapter);
        return lm;
    }

    public static GridLayoutManager setupGrid(RecyclerView rv, Context ctx, int spanCount) {
        GridLayoutManager lm = new GridLayoutManager(ctx, spanCount);
        rv.setLayoutManager(lm);
        rv.setHasFixedSize(true);
        return lm;
    }

    public static GridLayoutManager setupGrid(RecyclerView rv, Context ctx, int spanCount, RecyclerView.Adapter<?> adapter) {
        GridLayoutManager lm = setupGrid(rv, ctx, spanCount);
        rv.setAdapter(adapter);
        return lm;
    }

    public static RecyclerView.OnScrollListener endlessScrollListener(LinearLayoutManager lm, int threshold, Runnable onLoadMore) {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (dy <= 0) return;
                if (lm.findLastVisibleItemPosition() >= lm.getItemCount() - threshold) {
                    onLoadMore.run();
                }
            }
        };
    }
}