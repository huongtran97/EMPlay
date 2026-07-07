package emplay.entertainment.emplay.adapter.common;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Eliminates the updateData/DiffUtil boilerplate repeated across CastAdapter,
 * SeasonsTVAdapter, EpisodeAdapter, etc. Subclasses only implement the two
 * comparison methods; everything else is handled here.
 */
public abstract class BaseDiffUtilAdapter<T, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    protected final List<T> mData = new ArrayList<>();

    /** True when oldItem and newItem represent the same logical record (e.g. same ID). */
    protected abstract boolean areItemsTheSame(@NonNull T oldItem, @NonNull T newItem);

    /** True when the visible content of two same-identity items has not changed. */
    protected abstract boolean areContentsTheSame(@NonNull T oldItem, @NonNull T newItem);

    public void updateData(List<T> newData) {
        List<T> old = new ArrayList<>(mData);
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return old.size(); }
            @Override public int getNewListSize() { return newData != null ? newData.size() : 0; }
            @Override public boolean areItemsTheSame(int o, int n) {
                return BaseDiffUtilAdapter.this.areItemsTheSame(old.get(o), newData.get(n));
            }
            @Override public boolean areContentsTheSame(int o, int n) {
                return BaseDiffUtilAdapter.this.areContentsTheSame(old.get(o), newData.get(n));
            }
        });
        mData.clear();
        if (newData != null) mData.addAll(newData);
        diff.dispatchUpdatesTo(this);
    }

    @Override
    public int getItemCount() { return mData.size(); }
}