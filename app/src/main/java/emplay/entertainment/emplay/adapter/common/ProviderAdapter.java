package emplay.entertainment.emplay.adapter.common;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.models.common.ProviderModel;

public class ProviderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_STREAM = 0;
    public static final int TYPE_RENT_BUY = 1;

    private List<ProviderModel> providers = new ArrayList<>();
    private final int viewType;

    public ProviderAdapter(int viewType) {
        this.viewType = viewType;
    }

    /**
     * Replace the current list and refresh the RecyclerView
     */
    @SuppressLint("NotifyDataSetChanged")
    public void submitList(List<ProviderModel> list) {
        this.providers = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return viewType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (type == TYPE_STREAM) {
            return new StreamViewHolder(
                    inflater.inflate(R.layout.item_provider_fallback, parent, false));
        } else {
            return new RentBuyViewHolder(
                    inflater.inflate(R.layout.item_provider_rent_buy, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ProviderModel provider = providers.get(position);
        if (holder instanceof StreamViewHolder) {
            ((StreamViewHolder) holder).bind(provider);
        } else {
            ((RentBuyViewHolder) holder).bind(provider);
        }
    }

    @Override
    public int getItemCount() {
        return providers.size();
    }

    /**
     * Stream ViewHolder
     */
    static class StreamViewHolder extends RecyclerView.ViewHolder {
        ImageView ivLogo;
        TextView tvName;

        StreamViewHolder(View itemView) {
            super(itemView);
            ivLogo = itemView.findViewById(R.id.iv_provider_logo);
            tvName = itemView.findViewById(R.id.tv_provider_name);
        }

        void bind(ProviderModel provider) {
            tvName.setText(provider.getProviderName());
            Glide.with(itemView.getContext())
                    .load("https://image.tmdb.org/t/p/original" + provider.getLogoPath())
                    .placeholder(R.drawable.bg_provider_fallback)
                    .transform(new RoundedCorners(12))
                    .into(ivLogo);
        }
    }

    /**
     * Rent/Buy ViewHolder
     */
    static class RentBuyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivLogo;
        TextView tvName, tvQuality;

        RentBuyViewHolder(View itemView) {
            super(itemView);
            ivLogo    = itemView.findViewById(R.id.iv_provider_logo);
            tvName    = itemView.findViewById(R.id.tv_provider_name);
            tvQuality = itemView.findViewById(R.id.tv_quality);
        }

        void bind(ProviderModel provider) {
            tvName.setText(provider.getProviderName());
            Glide.with(itemView.getContext())
                    .load("https://image.tmdb.org/t/p/original" + provider.getLogoPath())
                    .placeholder(R.drawable.bg_provider_fallback)
                    .transform(new RoundedCorners(12))
                    .into(ivLogo);

            tvQuality.setVisibility(View.GONE);
        }
    }
}