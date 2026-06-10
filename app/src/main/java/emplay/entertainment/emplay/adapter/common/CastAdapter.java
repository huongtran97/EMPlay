package emplay.entertainment.emplay.adapter.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.models.common.CastModel;

/**
 *  Horizontal cast row shown on both movie and TV detail screens.
 *  Tapping a cast member opens CastDetailFragment via the OnCastClickListener callback.
 */
public class CastAdapter extends RecyclerView.Adapter<CastAdapter.CastViewHolder> {

    private final ArrayList<CastModel> castList;
    private final Context context;
    private OnCastClickListener onCastClickListener;

    public interface OnCastClickListener {
        void onCastClick(CastModel cast);
    }

    public CastAdapter(List<CastModel> castList, Context context) {
        this.castList = (ArrayList<CastModel>) castList;
        this.context = context;
    }

    public CastAdapter(List<CastModel> castList, Context context, OnCastClickListener listener) {
        this.castList = (ArrayList<CastModel>) castList;
        this.context = context;
        this.onCastClickListener = listener;
    }

    public void updateData(List<CastModel> newCastList) {
        List<CastModel> oldList = new ArrayList<>(castList);
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return oldList.size(); }
            @Override public int getNewListSize() { return newCastList != null ? newCastList.size() : 0; }
            @Override public boolean areItemsTheSame(int o, int n) {
                return oldList.get(o).getId() == newCastList.get(n).getId();
            }
            @Override public boolean areContentsTheSame(int o, int n) {
                CastModel a = oldList.get(o), b = newCastList.get(n);
                return Objects.equals(a.getName(), b.getName())
                        && Objects.equals(a.getProfilePath(), b.getProfilePath());
            }
        });
        castList.clear();
        if (newCastList != null) castList.addAll(newCastList);
        diff.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public CastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_result_cast_item, parent, false);
        return new CastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CastViewHolder holder, int position) {
        CastModel castModel = castList.get(position);
        if (castModel != null) {
            holder.nameTextView.setText(castModel.getName());
            holder.characterTextView.setText(castModel.getCharacter());

            RequestOptions options = new RequestOptions().circleCrop();
            String profilePath = castModel.getProfilePath();

            if (profilePath != null && !profilePath.isEmpty()) {
                Glide.with(context)
                        .load(ImageUrl.POSTER + profilePath)
                        .apply(options)
                        .placeholder(R.drawable.avatar)
                        .into(holder.profileImageView);
            } else {
                Glide.with(context)
                        .load(R.drawable.avatar)
                        .apply(options)
                        .into(holder.profileImageView);
            }

            holder.itemView.setOnClickListener(v -> {
                if (onCastClickListener != null) {
                    onCastClickListener.onCastClick(castModel);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return castList.size();
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