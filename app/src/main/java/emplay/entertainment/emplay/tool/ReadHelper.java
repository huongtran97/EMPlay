package emplay.entertainment.emplay.tool;

import android.graphics.text.LineBreaker;
import android.os.Build;
import android.text.Layout;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Set;

public class ReadHelper {

    private static final int MAX_LINES = 4;

    /**
     * Wire up a collapsible text block. Call this every time the view is bound
     */
    public static void bind(TextView content, TextView button, boolean isExpanded) {
        content.setMaxLines(isExpanded ? Integer.MAX_VALUE : MAX_LINES);
        button.setText(isExpanded ? "Show Less" : "Read More");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            content.setJustificationMode(isExpanded ? LineBreaker.JUSTIFICATION_MODE_NONE : LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }

        // post() defers the check until after the layout pass — getLayout() is null
        // before the view has been measured, so we can't call it synchronously here.
        content.post(() -> {
            Layout layout = content.getLayout();

            if (layout == null) return;

            boolean needs = isExpanded
                    ? content.getLineCount() > MAX_LINES       // expanded: real line count is available
                    : layout.getEllipsisCount(layout.getLineCount() - 1) > 0; // collapsed: check if text was cut off

            button.setVisibility(needs ? View.VISIBLE : View.GONE);
        });
    }

    /**
     * Fully configures a collapsible text block including its click listener.
     * Useful for Fragments or simple Views that don't need adapter position management.
     *
     * @param content        The overview/content TextView.
     * @param button         The "Read More/Less" button.
     * @param initialExpanded The initial expanded state.
     * @param onToggle       The callback to run when toggled.
     */
    public static void setup(TextView content, TextView button, boolean initialExpanded, OnToggleListener onToggle) {
        bind(content, button, initialExpanded);
        button.setOnClickListener(new View.OnClickListener() {
            private boolean isExpanded = initialExpanded;

            @Override
            public void onClick(View v) {
                isExpanded = !isExpanded;
                bind(content, button, isExpanded);
                if (onToggle != null) {
                    onToggle.onToggle(isExpanded);
                }
            }
        });
    }

    public interface OnToggleListener {
        void onToggle(boolean isExpanded);
    }

    /**
     * Fully configures a collapsible text block including its click listener and state management for Adapters.
     *
     * @param content        The overview/content TextView.
     * @param button         The "Read More/Less" button.
     * @param itemId         The unique ID of the item (e.g., movieId, tvShowId, or episodeNumber).
     * @param expandedItems  The Set tracking expanded state in the adapter.
     * @param holder         The RecyclerView.ViewHolder.
     * @param adapter        The RecyclerView.Adapter.
     */
    public static void setup(TextView content, TextView button, int itemId, Set<Integer> expandedItems, RecyclerView.ViewHolder holder, RecyclerView.Adapter<?> adapter) {
        final boolean isAlreadyExpanded = expandedItems.contains(itemId);
        bind(content, button, isAlreadyExpanded);

        button.setOnClickListener(v -> {
            if (expandedItems.contains(itemId)) {
                expandedItems.remove(itemId);
            } else {
                expandedItems.add(itemId);
            }

            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                adapter.notifyItemChanged(adapterPosition);
            }
        });
    }
}
