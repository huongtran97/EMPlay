package emplay.entertainment.emplay.adapter.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.api.common.PersonCreditsResponse.CreditItem;

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

    @NonNull
    @Override
    public CreditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.credit_item, parent, false);
        return new CreditViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CreditViewHolder holder, int position) {
        CreditItem item = creditList.get(position);

        Glide.with(context)
                .load(ImageUrl.of(ImageUrl.THUMBNAIL, item.getPosterPath()))
                .placeholder(R.drawable.bg_poster_placeholder)
                .into(holder.posterImage);

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
            posterImage = view.findViewById(R.id.credit_poster);
        }
    }
}
