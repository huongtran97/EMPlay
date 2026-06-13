package emplay.entertainment.emplay.adapter.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.models.common.OriginModel;

public class OriginAdapter extends RecyclerView.Adapter<OriginAdapter.ViewHolder> {
    private final List<OriginModel> origins;
    private final OnOriginClickListener listener;

    public interface OnOriginClickListener {
        void onOriginClick(OriginModel origin);
    }

    public OriginAdapter(List<OriginModel> origins, OnOriginClickListener listener) {
        this.origins = origins;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_origin_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OriginModel origin = origins.get(position);
        holder.tvName.setText(origin.getName());
        holder.tvGlyph.setText(origin.getGlyph());
        holder.itemView.setOnClickListener(v -> listener.onOriginClick(origin));
    }

    @Override
    public int getItemCount() {
        return origins.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvGlyph;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvOriginName);
            tvGlyph = itemView.findViewById(R.id.tvGhostGlyph);
        }
    }
}
