package emplay.entertainment.emplay.tool;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class PaginationHelper<T> {

    private final int pageSize;
    private final List<T> allItems;
    private int currentPage = 1;
    private final PaginationCallback<T> callback;

    public interface PaginationCallback<T> {
        void onPageUpdated(List<T> pageItems);
        void onUiUpdate(int currentPage, int totalPages, boolean hasPrev, boolean hasNext);
    }

    public PaginationHelper(int pageSize, List<T> allItems, PaginationCallback<T> callback) {
        this.pageSize = pageSize;
        this.allItems = allItems;
        this.callback = callback;
    }

    public void updateData(List<T> newData) {
        this.allItems.clear();
        this.allItems.addAll(newData);
        this.currentPage = 1;
        showPage();
    }

    public void nextPage() {
        if (hasNext()) {
            currentPage++;
            showPage();
        }
    }

    public void prevPage() {
        if (currentPage > 1) {
            currentPage--;
            showPage();
        }
    }

    public int getCurrentPage() {
        return currentPage;
    }

    private boolean hasNext() {
        int totalPages = (int) Math.ceil((double) allItems.size() / pageSize);
        return currentPage < totalPages;
    }

    public void showPage() {
        int total = allItems.size();
        int totalPages = (int) Math.ceil((double) total / pageSize);
        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        if (fromIndex < total) {
            callback.onPageUpdated(new ArrayList<>(allItems.subList(fromIndex, toIndex)));
        } else {
            callback.onPageUpdated(new ArrayList<>());
        }

        callback.onUiUpdate(currentPage, totalPages, currentPage > 1, currentPage < totalPages);
    }

    @SuppressLint("SetTextI18n")
    public static void updatePaginationBar(View bar, TextView indicator, ImageButton prev, ImageButton next, 
                                          int current, int total, boolean hasPrev, boolean hasNext) {
        if (total > 1) {
            bar.setVisibility(View.VISIBLE);
            indicator.setText("Page " + current + " of " + total);
            prev.setEnabled(hasPrev);
            next.setEnabled(hasNext);
        } else {
            bar.setVisibility(View.GONE);
        }
    }
}
