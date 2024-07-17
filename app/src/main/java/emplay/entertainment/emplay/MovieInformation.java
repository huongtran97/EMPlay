package emplay.entertainment.emplay;

public class MovieInformation extends androidx.appcompat.app.AppCompatActivity {

    android.widget.TextView name;
    android.widget.TextView overview;
    android.widget.ImageView img;

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(emplay.entertainment.emplay.R.layout.movie_information);

        name = (android.widget.TextView)findViewById(emplay.entertainment.emplay.R.id.item_movie_name);
        overview = (android.widget.TextView)findViewById(emplay.entertainment.emplay.R.id.item_movie_overview);
        img = (android.widget.ImageView)findViewById(emplay.entertainment.emplay.R.id.item_information);

        String name_movie = getIntent().getStringExtra("name");
        String overview_movie = getIntent().getStringExtra("overview");
        String img_movie = getIntent().getStringExtra("poster_path");

        name.setText(name_movie);
        overview.setText(overview_movie);

        com.bumptech.glide.Glide.with(this)
                .load(img_movie)
                .into(new com.bumptech.glide.request.target.CustomTarget<android.graphics.drawable.Drawable>() {
                    @Override
                    public void onResourceReady(@androidx.annotation.NonNull android.graphics.drawable.Drawable resource, @androidx.annotation.Nullable com.bumptech.glide.request.transition.Transition<? super android.graphics.drawable.Drawable> transition) {
                        img.setImageDrawable(resource);
                    }

                    @Override
                    public void onLoadCleared(@androidx.annotation.Nullable android.graphics.drawable.Drawable placeholder) {
                        img.setImageDrawable(placeholder);
                    }
                });//Working on it
    }
}
