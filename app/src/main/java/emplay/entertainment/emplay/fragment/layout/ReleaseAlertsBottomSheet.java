package emplay.entertainment.emplay.fragment.layout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import emplay.entertainment.emplay.tool.RecyclerViewHelper;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.List;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.auth.AuthManager;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.fragment.details.MovieResultDetailsFragment;
import emplay.entertainment.emplay.fragment.details.TVShowResultDetailsFragment;
import emplay.entertainment.emplay.models.common.ReleaseAlertItem;

public class ReleaseAlertsBottomSheet extends BottomSheetDialogFragment {

    public interface OnAlertsChangedListener {
        void onAlertsChanged();
        void onNavigateTo(androidx.fragment.app.Fragment fragment);
    }

    private OnAlertsChangedListener listener;
    private DatabaseHelper db;
    private String userId;
    private List<ReleaseAlertItem> items;

    // Views
    private RecyclerView rvAlerts;
    private TextView tvEmpty;
    private TextView btnDismissAll;

    public void setOnAlertsChangedListener(OnAlertsChangedListener l) { this.listener = l; }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_release_alerts_sheet, container, false);
        db = DatabaseHelper.getInstance(requireContext());
        userId = AuthManager.getInstance(requireContext()).getUserId();

        rvAlerts = view.findViewById(R.id.rvAlerts);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        btnDismissAll = view.findViewById(R.id.btnDismissAll);

        RecyclerViewHelper.setupVertical(rvAlerts, requireContext());
        loadData(view);

        btnDismissAll.setOnClickListener(v -> {
            new Thread(() -> {
                db.dismissReleasedAlerts(userId);
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    loadData(view);
                    if (listener != null) listener.onAlertsChanged();
                });
            }).start();
        });

        return view;
    }

    private void loadData(View root) {
        new Thread(() -> {
            items = db.getAllActiveAlerts(userId);
            if (getActivity() != null) getActivity().runOnUiThread(() -> {
                boolean hasReleased = false;
                for (ReleaseAlertItem item : items) {
                    if (item.isReleased()) { hasReleased = true; break; }
                }
                btnDismissAll.setVisibility(hasReleased ? View.VISIBLE : View.GONE);
                if (items.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    rvAlerts.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    rvAlerts.setVisibility(View.VISIBLE);
                    rvAlerts.setAdapter(new AlertsAdapter());
                }
            });
        }).start();
    }

    private class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.VH> {
        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_release_alert, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            ReleaseAlertItem item = items.get(position);

            Glide.with(requireContext())
                    .load(ImageUrl.of(ImageUrl.POSTER, item.getPosterPath()))
                    .placeholder(R.drawable.bg_poster_placeholder)
                    .into(holder.poster);

            holder.title.setText(item.getTitle());

            if (item.getReleaseDate() != null && !item.getReleaseDate().isEmpty()) {
                holder.date.setText(formatDate(item.getReleaseDate()));
                holder.date.setVisibility(View.VISIBLE);
            } else {
                holder.date.setVisibility(View.GONE);
            }

            if (item.isReleased()) {
                holder.badge.setVisibility(View.VISIBLE);
                holder.badge.setText(R.string.release_alerts_badge_released);
            } else {
                holder.badge.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> {
                dismiss();
                if (listener != null) {
                    androidx.fragment.app.Fragment dest;
                    if (ReleaseAlertItem.TYPE_MOVIE.equals(item.getMediaType())) {
                        dest = MovieResultDetailsFragment.newInstance(item.getMediaId());
                    } else {
                        dest = TVShowResultDetailsFragment.newInstance(item.getMediaId());
                    }
                    listener.onNavigateTo(dest);
                }
            });

            holder.dismiss.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                new Thread(() -> {
                    db.removeReleaseAlert(userId, item.getMediaId(), item.getMediaType());
                    if (getActivity() != null) getActivity().runOnUiThread(() -> {
                        items.remove(pos);
                        notifyItemRemoved(pos);
                        boolean hasReleased = false;
                        for (ReleaseAlertItem i : items) {
                            if (i.isReleased()) { hasReleased = true; break; }
                        }
                        btnDismissAll.setVisibility(hasReleased ? View.VISIBLE : View.GONE);
                        if (items.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                            rvAlerts.setVisibility(View.GONE);
                        }
                        if (listener != null) listener.onAlertsChanged();
                    });
                }).start();
            });
        }

        @Override
        public int getItemCount() { return items.size(); }

        class VH extends RecyclerView.ViewHolder {
            ShapeableImageView poster;
            TextView title, date, badge;
            ImageView dismiss;
            VH(@NonNull View itemView) {
                super(itemView);
                poster  = itemView.findViewById(R.id.ivAlertPoster);
                title   = itemView.findViewById(R.id.tvAlertTitle);
                date    = itemView.findViewById(R.id.tvAlertDate);
                badge   = itemView.findViewById(R.id.tvAlertBadge);
                dismiss = itemView.findViewById(R.id.btnDismiss);
            }
        }
    }

    private String formatDate(String iso) {
        try {
            java.text.SimpleDateFormat in  = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
            java.text.SimpleDateFormat out = new java.text.SimpleDateFormat("MMM d, yyyy",  java.util.Locale.US);
            java.util.Date d = in.parse(iso);
            return d != null ? out.format(d) : iso;
        } catch (java.text.ParseException e) {
            return iso;
        }
    }
}