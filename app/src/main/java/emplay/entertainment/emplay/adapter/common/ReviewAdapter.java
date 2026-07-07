package emplay.entertainment.emplay.adapter.common;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.models.common.ReviewModel;
import emplay.entertainment.emplay.tool.GlideImageLoader;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private static final int MAX_LINES = 2;

    private final List<ReviewModel> reviews;
    private final Set<Integer> expandedPositions = new HashSet<>();
    private final Context context;

    public ReviewAdapter(List<ReviewModel> reviews, Context context) {
        this.reviews = reviews;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReviewModel review = reviews.get(position);
        ReviewModel.AuthorDetails details = review.getAuthorDetails();

        String username = (details != null && details.getUsername() != null && !details.getUsername().isEmpty())
                ? details.getUsername()
                : review.getAuthor();
        holder.tvUsername.setText(username);

        if (details != null && details.getRating() != null) {
            holder.tvRating.setText(String.format(Locale.getDefault(), "★ %.1f", details.getRating()));
            holder.tvRating.setVisibility(View.VISIBLE);
        } else {
            holder.tvRating.setVisibility(View.GONE);
        }

        String avatarUrl = null;
        if (details != null && details.getAvatarPath() != null) {
            String path = details.getAvatarPath();
            if (path.startsWith("/https://") || path.startsWith("/http://")) {
                avatarUrl = path.substring(1);
            } else if (!path.isEmpty()) {
                avatarUrl = ImageUrl.of(ImageUrl.THUMBNAIL, path);
            }
        }
        GlideImageLoader.loadCircle(context, avatarUrl, holder.ivAvatar, R.drawable.ic_user);

        String content = review.getContent() != null ? review.getContent() : "";
        holder.tvContent.setText(content);

        boolean isExpanded = expandedPositions.contains(position);
        holder.tvContent.setMaxLines(isExpanded ? Integer.MAX_VALUE : MAX_LINES);
        holder.tvContent.setEllipsize(isExpanded ? null : android.text.TextUtils.TruncateAt.END);
        holder.tvReadMore.setText(isExpanded ? R.string.read_less : R.string.read_more);

        // Defer the visibility check until after the text has been laid out so
        // getLayout() is non-null. When collapsed, use ellipsis count (not
        // getLineCount(), which is capped at MAX_LINES even for long text).
        holder.tvContent.post(() -> {
            Layout layout = holder.tvContent.getLayout();
            if (layout == null) return;
            boolean needsToggle = isExpanded
                    ? holder.tvContent.getLineCount() > MAX_LINES
                    : layout.getEllipsisCount(layout.getLineCount() - 1) > 0;
            holder.tvReadMore.setVisibility(needsToggle ? View.VISIBLE : View.GONE);
        });

        holder.tvReadMore.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            if (expandedPositions.contains(pos)) {
                expandedPositions.remove(pos);
            } else {
                expandedPositions.add(pos);
            }
            notifyItemChanged(pos);
        });
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivAvatar;
        final TextView tvUsername;
        final TextView tvRating;
        final TextView tvContent;
        final TextView tvReadMore;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvContent = itemView.findViewById(R.id.tv_review_content);
            tvReadMore = itemView.findViewById(R.id.tv_read_more);
        }
    }
}
