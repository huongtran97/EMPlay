package emplay.entertainment.emplay.adapter.tvshow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.BasePosterAdapter;
import emplay.entertainment.emplay.models.tvshow.TVShowModel;

public class SuggestionTVAdapter extends BasePosterAdapter<TVShowModel> {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_MORE = 1;

    private boolean mShowMoreItem = false;
    private Runnable mOnMoreClick;

    public interface OnItemClickListener {
        void onItemClick(TVShowModel tvShow, View sharedElement);
    }

    public SuggestionTVAdapter(List<TVShowModel> data, Context context, OnItemClickListener listener) {
        super(context, data, listener::onItemClick);
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

    @Override
    protected int getLayoutRes() { return R.layout.item_tvshow_popular; }

    @Override
    protected int getImageViewId() { return R.id.header; }
}
