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
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.models.common.ProviderModel;

public class ProviderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_STREAM = 0;
    public static final int TYPE_RENT_BUY = 1;
    public static final int TYPE_SIMPLE = 2;

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
        if (type == TYPE_SIMPLE) {
            return new SimpleViewHolder(
                    inflater.inflate(R.layout.item_provider, parent, false));
        } else if (type == TYPE_STREAM) {
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
        if (holder instanceof SimpleViewHolder) {
            ((SimpleViewHolder) holder).bind(provider);
        } else if (holder instanceof StreamViewHolder) {
            ((StreamViewHolder) holder).bind(provider);
        } else {
            ((RentBuyViewHolder) holder).bind(provider);
        }
    }

    @Override
    public int getItemCount() {
        return providers.size();
    }

    static class SimpleViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProviderLogo;
        TextView tvProviderName;

        SimpleViewHolder(View itemView) {
            super(itemView);
            imgProviderLogo = itemView.findViewById(R.id.imgProviderLogo);
            tvProviderName = itemView.findViewById(R.id.tvProviderName);
        }

        void bind(ProviderModel provider) {
            tvProviderName.setText(provider.getProviderName());
            String imageUrl = resolveLogoUrl(provider);
            if (imageUrl != null) {
                imgProviderLogo.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .transform(new RoundedCorners(4))
                        .into(imgProviderLogo);
            } else {
                imgProviderLogo.setVisibility(View.GONE);
            }
        }
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
                    .load(resolveLogoUrl(provider))
                    .placeholder(R.drawable.bg_provider_fallback)
                    .transform(new RoundedCorners(12))
                    .into(ivLogo);
        }
    }

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
                    .load(resolveLogoUrl(provider))
                    .placeholder(R.drawable.bg_provider_fallback)
                    .transform(new RoundedCorners(12))
                    .into(ivLogo);

            tvQuality.setVisibility(View.GONE);
        }
    }

    /**
     * Returns a Glide-loadable URL: full URL for MOTN providers, TMDB URL otherwise.
     */
    static String resolveLogoUrl(ProviderModel provider) {
        if (provider.getLogoUrl() != null && !provider.getLogoUrl().isEmpty()) {
            return provider.getLogoUrl();
        }
        return ImageUrl.of(ImageUrl.ORIGINAL, provider.getLogoPath());
    }
}