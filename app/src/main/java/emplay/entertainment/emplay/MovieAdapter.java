package emplay.entertainment.emplay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public abstract class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MyViewHolder> {
    Context mContext;
    ArrayList<MovieDetails> mData;

    public int getItemVieType(int position) {
        MovieDetails thisRow = mData.get(position);
        return thisRow.getId();
    }

    public MovieAdapter(Context mContext, ArrayList<MovieDetails> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @Override
    public MovieAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        v = inflater.inflate(R.layout.movie_option,parent, false);
        return  new MovieAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MovieAdapter.MyViewHolder holder, @SuppressLint({"RecycleView", "RecyclerView"}) int position) {
        holder.mTitle.setText(mData.get(position).getmTitle());
        holder.mReleaseDate.setText(mData.get(position).getmTitle());
        holder.mVoteAverage.setText(mData.get(position).getmTitle());
        holder.mOverview.setText(mData.get(position).getmTitle());
        holder.mPoster.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                MovieInformation parentActivity = ( MovieInformation) view.getContext();
                parentActivity.userCLickedItem(mData.get(position),position);
            }
        });
        Glide.with(mContext).load(mData.get(position).getmPoster((holder.mPoster)));
    }

    @Override
    public int getItemCount(){
        return mData.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        List<MovieDetails> movieDetailsList = new ArrayList<>();
        TextView mTitle;
        TextView mReleaseDate;
        TextView mVoteAverage;
        TextView mOverview;
        TextView mPoster;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
