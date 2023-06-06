package emplay.entertainment.emplay;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

public class MovieSlideAdapter extends PagerAdapter {

    private Context mContext;
    private List<MovieSlide> movieSlideList;

    public MovieSlideAdapter(Context mContext, List<MovieSlide> movieSlideList) {
        this.mContext = mContext;
        this.movieSlideList = movieSlideList;
    }


    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View movieSlideLayout = inflater.inflate(R.layout.movie_slide_items, null);

        ImageView movieSlideImg = movieSlideLayout.findViewById(R.id.movie_slide_image);
        TextView movieSlideTitle = movieSlideLayout.findViewById(R.id.movie_slide_title);
        TextView movieSileDes = movieSlideLayout.findViewById(R.id.movie_slide_description);

        movieSlideImg.setImageResource(movieSlideList.get(position).getImage());
        movieSlideTitle.setText(movieSlideList.get(position).getTitle());
        movieSileDes.setText(movieSlideList.get(position).getDescription());

        container.addView(movieSlideLayout);
        return movieSlideLayout;

    }

    @Override
    public int getCount() {
        return movieSlideList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}
