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

    private final List<CreditItem> credits;
    private final OnCreditClickListener listener;

    public interface OnCreditClickListener {
        void onCreditClick(CreditItem item);
    }

    public FilmographyAdapter(List<CreditItem> credits, OnCreditClickListener listener) {
        this.credits = credits;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_film_frame, parent, false); // Re-using item_filmography? Wait.
        // Actually, the plan says item_filmography.xml
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filmography, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CreditItem item = credits.get(position);

        String date = "movie".equals(item.getMediaType()) ? item.getReleaseDate() : item.getFirstAirDate();
        String year = (date != null && date.length() >= 4) ? date.substring(0, 4) : "—";
        
        holder.tvYear.setText(year);
        holder.tvTitle.setText("movie".equals(item.getMediaType()) ? item.getTitle() : item.getName());
        
        String role = item.getCharacter();
        String type = "movie".equals(item.getMediaType()) ? "Movie" : "TV";
        holder.tvRole.setText(String.format("as %s · %s", (role == null || role.isEmpty()) ? "TBA" : role, type));
        
        holder.tvRating.setText(String.format(Locale.getDefault(), "★ %.1f", item.getVoteAverage()));
        
        holder.itemView.setOnClickListener(v -> listener.onCreditClick(item));
    }

    @Override
    public int getItemCount() {
        return credits.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvYear, tvTitle, tvRole, tvRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvYear = itemView.findViewById(R.id.tvYear);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvRating = itemView.findViewById(R.id.tvRating);
        }
    }
}
