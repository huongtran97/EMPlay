package emplay.entertainment.emplay.adapter.common;

import android.content.Context;
import java.util.List;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.models.common.MediaItem;

/**
 * Adapter for the "Recently Saved" horizontal list in the Profile screen.
 * Extends BasePosterAdapter to reuse Glide loading and click handling logic.
 */
public class RecentlySavedAdapter extends BasePosterAdapter<MediaItem> {
    
    public RecentlySavedAdapter(Context context, List<MediaItem> data, OnItemClickListener<MediaItem> listener) {
        super(context, data, listener);
    }

    @Override 
    protected int getLayoutRes() { 
        return R.layout.item_recently_saved_poster; 
    }

    @Override 
    protected int getImageViewId() { 
        return R.id.ivPoster;
    }

    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull PosterViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        MediaItem item = mData.get(position);
        android.widget.TextView titleView = holder.itemView.findViewById(R.id.trendNow);
        if (titleView != null) {
            titleView.setText(item.getTitle());
        }
    }
}
