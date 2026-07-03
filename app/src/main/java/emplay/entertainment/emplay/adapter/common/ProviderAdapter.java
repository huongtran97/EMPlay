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

    public interface OnProviderClick {
        void onClick(ProviderModel provider);
    }

    private List<ProviderModel> providers = new ArrayList<>();
    private final int viewType;
    private OnProviderClick clickListener;

    public ProviderAdapter(int viewType) {
        this.viewType = viewType;
    }

    public void setOnProviderClick(OnProviderClick listener) {
        this.clickListener = listener;
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
                    inflater.inflate(R.layout.item_provider, parent, false));
        } else {
            return new RentBuyViewHolder(
                    inflater.inflate(R.layout.item_provider, parent, false));
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
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onClick(provider);
        });
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
            imgProviderLogo = itemView.findViewById(R.id.imageProviderLogo);
            tvProviderName = itemView.findViewById(R.id.textProviderName);
            itemView.findViewById(R.id.iv_chevron).setVisibility(View.GONE);
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
        ImageView ivLogo, ivChevron;
        TextView tvName, tvLinkUnavailable;

        StreamViewHolder(View itemView) {
            super(itemView);
            ivLogo            = itemView.findViewById(R.id.imageProviderLogo);
            tvName            = itemView.findViewById(R.id.textProviderName);
            ivChevron         = itemView.findViewById(R.id.iv_chevron);
            tvLinkUnavailable = itemView.findViewById(R.id.tv_link_unavailable);
        }

        void bind(ProviderModel provider) {
            tvName.setText(provider.getProviderName());
            Glide.with(itemView.getContext())
                    .load(resolveLogoUrl(provider))
                    .placeholder(R.drawable.bg_provider_fallback)
                    .transform(new RoundedCorners(12))
                    .into(ivLogo);

            if (provider.hasDeepLink()) {
                ivChevron.setVisibility(View.VISIBLE);
                tvLinkUnavailable.setVisibility(View.GONE);
            } else {
                ivChevron.setVisibility(View.GONE);
                tvLinkUnavailable.setVisibility(View.VISIBLE);
            }
        }
    }

    static class RentBuyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivLogo, ivChevron;
        TextView tvName, tvLinkUnavailable;

        RentBuyViewHolder(View itemView) {
            super(itemView);
            ivLogo            = itemView.findViewById(R.id.imageProviderLogo);
            tvName            = itemView.findViewById(R.id.textProviderName);
            ivChevron         = itemView.findViewById(R.id.iv_chevron);
            tvLinkUnavailable = itemView.findViewById(R.id.tv_link_unavailable);
        }

        void bind(ProviderModel provider) {
            tvName.setText(provider.getProviderName());
            Glide.with(itemView.getContext())
                    .load(resolveLogoUrl(provider))
                    .placeholder(R.drawable.bg_provider_fallback)
                    .transform(new RoundedCorners(12))
                    .into(ivLogo);

            if (provider.hasDeepLink()) {
                ivChevron.setVisibility(View.VISIBLE);
                tvLinkUnavailable.setVisibility(View.GONE);
            } else {
                ivChevron.setVisibility(View.GONE);
                tvLinkUnavailable.setVisibility(View.VISIBLE);
            }
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