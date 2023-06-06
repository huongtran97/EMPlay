package emplay.entertainment.emplay;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;



import java.util.ArrayList;
import java.util.List;

public class MovieOption extends AppCompatActivity {

    private List<MovieSlide> slideList;
    private ViewPager movieSlidePager;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_option);

        movieSlidePager = findViewById(R.id.slider_page);


        //list of slides
        slideList = new ArrayList<>();
        slideList.add(new MovieSlide(R.drawable.miaslide1,"Made In Abyss","A girl and her robot companion search for her mother, who's lost within a vast chasm."));
        slideList.add(new MovieSlide(R.drawable.dsslide2,"Demon Slayer: A guide to the Swordsmith Village arc ","The Swordsmith Village Arc is set to pick up where the last season left off, with Tanjiro travelling to the titular village to repair his damaged Nichirin sword. Along the way, he will reunite with two members of the Hashira, the highest-ranking swordsmen in the Demon Slayer Corps."));
        slideList.add(new MovieSlide(R.drawable.jkslide3,"Jujutsu Kaisen","Yuji Itadori, a kind-hearted teenager, joins his school's Occult Club for fun, but discovers that its members are actual sorcerers who can manipulate the energy between beings for their own use. He hears about a cursed talisman - the finger of Sukuna, a demon - and its being targeted by other cursed beings."));


        MovieSlideAdapter movieAdapter = new MovieSlideAdapter(this, slideList);
        movieSlidePager.setAdapter(movieAdapter);

    }
}
