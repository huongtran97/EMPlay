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

import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.api.common.PersonCreditsResponse;

/**
 *  Horizontal filmography row on the cast detail screen.
 *  Tapping one opens the movie or TV show detail for that credit.
 */
public class CreditAdapter extends RecyclerView.Adapter<CreditAdapter.CreditViewHolder> {

    private List<PersonCreditsResponse.CreditItem> creditList;

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<PersonCreditsResponse.CreditItem> newList) {
        creditList = newList;
        notifyDataSetChanged();
    }
    private final Context context;
    private final OnCreditClickListener listener;

    public interface OnCreditClickListener {
        void onCreditClick(PersonCreditsResponse.CreditItem credit);
    }

    public CreditAdapter(List<PersonCreditsResponse.CreditItem> creditList,
                         Context context, OnCreditClickListener listener) {
        this.creditList = creditList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CreditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.credit_item, parent, false);
        return new CreditViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CreditViewHolder holder, int position) {
        PersonCreditsResponse.CreditItem credit = creditList.get(position);

        String imageUrl = null;
        if (credit.getPosterPath() != null && !credit.getPosterPath().isEmpty()) {
            imageUrl = ImageUrl.THUMBNAIL + credit.getPosterPath();
        } else if (credit.getBackdropPath() != null && !credit.getBackdropPath().isEmpty()) {
            imageUrl = ImageUrl.THUMBNAIL + credit.getBackdropPath();
        }

        Glide.with(context)
                .load(imageUrl != null ? imageUrl : R.drawable.placeholder_image)
                .placeholder(R.drawable.placeholder_image)
                .into(holder.posterImage);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCreditClick(credit);
        });
    }

    @Override
    public int getItemCount() {
        return creditList.size();
    }

    public static class CreditViewHolder extends RecyclerView.ViewHolder {
        ImageView posterImage;

        public CreditViewHolder(@NonNull View itemView) {
            super(itemView);
            posterImage = itemView.findViewById(R.id.credit_poster);
        }
    }
}
