package emplay.entertainment.emplay.adapter.common;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.models.common.OriginModel;

public class OriginGridAdapter extends RecyclerView.Adapter<OriginGridAdapter.VH> {

    public interface OnOriginClickListener {
        void onOriginClick(OriginModel origin);
    }

    private final List<OriginModel> all = new ArrayList<>();
    private List<OriginModel> filtered = new ArrayList<>();
    private final OnOriginClickListener listener;

    public OriginGridAdapter(List<OriginModel> origins, OnOriginClickListener listener) {
        this.all.addAll(origins);
        this.filtered = new ArrayList<>(origins);
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateAll(List<OriginModel> newOrigins) {
        all.clear();
        all.addAll(newOrigins);
        filtered = new ArrayList<>(all);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            filtered = new ArrayList<>(all);
        } else {
            String q = query.trim().toLowerCase(Locale.getDefault());
            filtered = new ArrayList<>();
            for (OriginModel o : all) {
                if (o.getName().toLowerCase(Locale.getDefault()).contains(q)) {
                    filtered.add(o);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.genres_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        OriginModel origin = filtered.get(position);
        holder.tvName.setText(origin.getName());
        holder.tvGlyph.setText(origin.getGlyph());
        holder.itemView.setOnClickListener(v -> listener.onOriginClick(origin));
    }

    @Override
    public int getItemCount() {
        return filtered.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvGlyph;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName  = itemView.findViewById(R.id.genre_name);
            tvGlyph = itemView.findViewById(R.id.tvGhostGlyph);
        }
    }
}