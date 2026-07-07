package emplay.entertainment.emplay.adapter.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.models.common.MediaItem;

/**
 * Extends BasePosterAdapter with a "More" sentinel item appended after all data items.
 * Eliminates the identical VIEW_TYPE_MORE block copy-pasted across
 * UpcomingMovieAdapter, UpComingTVAdapter, SuggestionMovieAdapter, SuggestionTVAdapter.
 */
public abstract class PaginatableAdapter<T extends MediaItem> extends BasePosterAdapter<T> {

    protected static final int VIEW_TYPE_ITEM = 0;
    protected static final int VIEW_TYPE_MORE = 1;

    private boolean mShowMoreItem = false;
    private Runnable mOnMoreClick;

    protected PaginatableAdapter(Context context, List<T> data, OnItemClickListener<T> listener) {
        super(context, data, listener);
    }

    public void setShowMoreItem(boolean show, Runnable onMoreClick) {
        boolean wasShowing = mShowMoreItem;
        mShowMoreItem = show;
        mOnMoreClick = onMoreClick;
        if (show && !wasShowing) {
            notifyItemInserted(mData.size());
        } else if (!show && wasShowing) {
            notifyItemRemoved(mData.size());
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (mShowMoreItem && position == mData.size()) ? VIEW_TYPE_MORE : VIEW_TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return mShowMoreItem ? mData.size() + 1 : mData.size();
    }

    @NonNull
    @Override
    public PosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_MORE) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.item_suggestion_more, parent, false);
            return new PosterViewHolder(v, R.id.header);
        }
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull PosterViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_MORE) {
            holder.itemView.setOnClickListener(v -> { if (mOnMoreClick != null) mOnMoreClick.run(); });
            return;
        }
        super.onBindViewHolder(holder, position);
    }
}