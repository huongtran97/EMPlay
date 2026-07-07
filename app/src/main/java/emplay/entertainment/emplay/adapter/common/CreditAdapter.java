package emplay.entertainment.emplay.adapter.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.api.common.PersonCreditsResponse.CreditItem;
import emplay.entertainment.emplay.tool.GlideImageLoader;

public class CreditAdapter extends RecyclerView.Adapter<CreditAdapter.CreditViewHolder> {

    private final List<CreditItem> creditList;
    private final Context context;
    private final OnCreditClickListener listener;

    public interface OnCreditClickListener {
        void onCreditClick(CreditItem item, View sharedElement);
    }

    public CreditAdapter(List<CreditItem> creditList, Context context, OnCreditClickListener listener) {
        this.creditList = new ArrayList<>(creditList);
        this.context = context;
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<CreditItem> newData) {
        creditList.clear();
        if (newData != null) creditList.addAll(newData);
        notifyDataSetChanged();
    }

    public void appendData(List<CreditItem> newItems) {
        int insertStart = creditList.size();
        creditList.addAll(newItems);
        notifyItemRangeInserted(insertStart, newItems.size());
    }

    @NonNull
    @Override
    public CreditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new CreditViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CreditViewHolder holder, int position) {
        CreditItem item = creditList.get(position);

        GlideImageLoader.load(context, ImageUrl.of(ImageUrl.THUMBNAIL, item.getPosterPath()),
                holder.posterImage, R.drawable.bg_poster_placeholder);

        holder.posterImage.setTransitionName("poster_" + item.getId());
        holder.itemView.setOnClickListener(v -> listener.onCreditClick(item, holder.posterImage));
    }

    @Override
    public int getItemCount() {
        return creditList.size();
    }

    public static class CreditViewHolder extends RecyclerView.ViewHolder {
        ImageView posterImage;

        public CreditViewHolder(@NonNull View view) {
            super(view);
            posterImage = view.findViewById(R.id.header);
        }
    }
}
