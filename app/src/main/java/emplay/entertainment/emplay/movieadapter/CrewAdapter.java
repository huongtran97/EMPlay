package emplay.entertainment.emplay.movieadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.models.CrewModel;

public class CrewAdapter extends RecyclerView.Adapter<CrewAdapter.CrewViewHolder> {

    private final List<CrewModel> crewList;
    private final Context context;

    public CrewAdapter(List<CrewModel> crewList, Context context) {
        this.crewList = crewList != null ? crewList : new ArrayList<>();
        this.context = context;
    }

    public void updateData(List<CrewModel> newCrewList) {
        if (newCrewList != null) {
            crewList.clear();
            crewList.addAll(newCrewList);
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public CrewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.tvshow_cast_item, parent, false);
        return new CrewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CrewViewHolder holder, int position) {
        CrewModel crewModel = crewList.get(position);
        if (crewModel != null) {
            holder.nameTextView.setText(crewModel.getName());

            // Load crew profile image using Glide
            Glide.with(context)
                    .load("https://image.tmdb.org/t/p/w500" + crewModel.getProfile_path())
                    .into(holder.profileImageView);
        }
    }

    @Override
    public int getItemCount() {
        return crewList.size();
    }

    public static class CrewViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        ImageView profileImageView;

        public CrewViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.crew_name_profile);
            profileImageView = itemView.findViewById(R.id.crew_poster_profile);
        }
    }
}
