package emplay.entertainment.emplay.adapter.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.models.common.CastModel;
import emplay.entertainment.emplay.tool.GlideImageLoader;

/**
 * Horizontal cast row shown on both movie and TV detail screens.
 * Tapping a cast member opens CastDetailFragment via the OnCastClickListener callback.
 */
public class CastAdapter extends BaseDiffUtilAdapter<CastModel, CastAdapter.CastViewHolder> {

    private final Context context;
    private OnCastClickListener onCastClickListener;

    public interface OnCastClickListener {
        void onCastClick(CastModel cast);
    }

    public CastAdapter(List<CastModel> castList, Context context) {
        this.context = context;
        if (castList != null) mData.addAll(castList);
    }

    public CastAdapter(List<CastModel> castList, Context context, OnCastClickListener listener) {
        this.context = context;
        this.onCastClickListener = listener;
        if (castList != null) mData.addAll(castList);
    }

    @Override
    protected boolean areItemsTheSame(@NonNull CastModel oldItem, @NonNull CastModel newItem) {
        return oldItem.getId() == newItem.getId();
    }

    @Override
    protected boolean areContentsTheSame(@NonNull CastModel oldItem, @NonNull CastModel newItem) {
        return Objects.equals(oldItem.getName(), newItem.getName())
                && Objects.equals(oldItem.getProfilePath(), newItem.getProfilePath());
    }

    @NonNull
    @Override
    public CastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_result_cast, parent, false);
        return new CastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CastViewHolder holder, int position) {
        CastModel castModel = mData.get(position);
        if (castModel != null) {
            holder.nameTextView.setText(castModel.getName());
            holder.characterTextView.setText(castModel.getCharacter());

            GlideImageLoader.loadCircle(context,
                    ImageUrl.of(ImageUrl.POSTER, castModel.getProfilePath()),
                    holder.profileImageView, R.drawable.avatar);

            holder.itemView.setOnClickListener(v -> {
                if (onCastClickListener != null) {
                    onCastClickListener.onCastClick(castModel);
                }
            });
        }
    }

    public static class CastViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, characterTextView;
        ImageView profileImageView;

        public CastViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.cast_name_profile);
            characterTextView = itemView.findViewById(R.id.cast_character_profile);
            profileImageView = itemView.findViewById(R.id.cast_poster_profile);
        }
    }
}