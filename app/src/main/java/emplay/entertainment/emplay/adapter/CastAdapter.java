package emplay.entertainment.emplay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.models.CastModel;

public class CastAdapter extends RecyclerView.Adapter<CastAdapter.CastViewHolder> {

    private ArrayList<CastModel> castList;
    private Context context;

    public CastAdapter(List<CastModel> castList, Context context) {
        this.castList = (ArrayList<CastModel>) castList;
        this.context = context;
    }

    public void updateData(List<CastModel> newCastList) {
        castList.clear();
        castList.addAll(newCastList);
        notifyDataSetChanged();
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

            // Load cast profile image using Glide
            RequestOptions options = new RequestOptions().circleCrop();
            String profilePath = castModel.getProfilePath(); // Get the profile path

            if (profilePath != null && !profilePath.isEmpty()) {
                // If profile path is not null, load the image
                Glide.with(context)
                        .load("https://image.tmdb.org/t/p/w500" + profilePath)
                        .apply(options)
                        .placeholder(R.drawable.avatar) // Set a placeholder while loading
                        .into(holder.profileImageView);
            } else {
                // If profile path is null or empty, load a default image or placeholder
                Glide.with(context)
                        .load(R.drawable.avatar) // Default image when path is null
                        .apply(options)
                        .into(holder.profileImageView);
            }

        }
    }

    @Override
    public int getItemCount() {
        return castList.size();
    }

    public static class CastViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView characterTextView;
        ImageView profileImageView;

        public CastViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.cast_name_profile);
            characterTextView = itemView.findViewById(R.id.cast_character_profile);
            profileImageView = itemView.findViewById(R.id.cast_poster_profile);
        }
    }
}
