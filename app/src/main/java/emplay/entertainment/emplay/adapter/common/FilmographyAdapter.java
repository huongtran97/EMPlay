package emplay.entertainment.emplay.adapter.common;

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
import emplay.entertainment.emplay.api.common.PersonCreditsResponse.CreditItem;

public class FilmographyAdapter extends RecyclerView.Adapter<FilmographyAdapter.ViewHolder> {

    private static final int COLLAPSE_LIMIT = 5;

    private final List<CreditItem> fullList;
    private final List<CreditItem> credits;
    private final OnCreditClickListener listener;
    private boolean expanded = false;

    public interface OnCreditClickListener {
        void onCreditClick(CreditItem item, View sharedElement);
    }

    public FilmographyAdapter(List<CreditItem> credits, OnCreditClickListener listener) {
        this.fullList = new ArrayList<>(credits);
        this.credits = new ArrayList<>(credits.subList(0, Math.min(COLLAPSE_LIMIT, credits.size())));
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filmography_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CreditItem item = credits.get(position);

        String date = item.getDisplayDate();
        String year = (date != null && date.length() >= 4) ? date.substring(0, 4) : "—";
        
        holder.tvYear.setText(year);
        holder.tvTitle.setText(item.getDisplayTitle());
        
        String role = item.getCharacter();
        String type = "movie".equals(item.getMediaType()) ? "Movie" : "TV";
        holder.tvRole.setText(String.format("as %s · %s", (role == null || role.isEmpty()) ? "TBA" : role, type));
        
        holder.tvRating.setText(String.format(Locale.getDefault(), "★ %.1f", item.getVoteAverage()));
        holder.itemView.setOnClickListener(v -> listener.onCreditClick(item, null));
    }

    @Override
    public int getItemCount() {
        return credits.size();
    }

    @android.annotation.SuppressLint("NotifyDataSetChanged")
    public void showAll() {
        expanded = true;
        credits.clear();
        credits.addAll(fullList);
        notifyDataSetChanged();
    }

    @android.annotation.SuppressLint("NotifyDataSetChanged")
    public void collapse() {
        expanded = false;
        credits.clear();
        credits.addAll(fullList.subList(0, Math.min(COLLAPSE_LIMIT, fullList.size())));
        notifyDataSetChanged();
    }

    public boolean isExpanded() {
        return expanded;
    }

    public boolean hasMore() {
        return fullList.size() > COLLAPSE_LIMIT;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvYear, tvTitle, tvRole, tvRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvYear = itemView.findViewById(R.id.tv_year);
            tvTitle = itemView.findViewById(R.id.tv_film_title);
            tvRole = itemView.findViewById(R.id.tv_film_role);
            tvRating = itemView.findViewById(R.id.tv_film_rating);
        }
    }
}
