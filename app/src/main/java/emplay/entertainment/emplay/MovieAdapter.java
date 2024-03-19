package emplay.entertainment.emplay;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;

import java.util.List;

import androidx.annotation.NonNull;

import android.view.ViewGroup;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;


public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MyViewHolder> {

    private Context mContext;
    private List<MovieModel> mData;


    public MovieAdapter(android.content.Context mContext, java.util.List<MovieModel> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        v = inflater.inflate(R.layout.movie_item, parent, false);
        return new MyViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.id.setText(mData.get(position).getId());
        holder.name.setText(mData.get(position).getName());
        holder.vote.setText(mData.get(position).getVote());


        //Using Glide library to display the image
        Glide.with(mContext).load(mData.get(position).getImg()).into(holder.img);


    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView id;
        TextView name;
        TextView vote;
        ImageView img;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            id = itemView.findViewById(R.id.id_txt);
            name = itemView.findViewById(R.id.name_txt);
            vote = itemView.findViewById(R.id.vote_txt);
            img = itemView.findViewById(R.id.img_movie);


        }
    }


}
