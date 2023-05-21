package emplay.entertainment.emplay;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    VideoView splashView;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.splash_screen);

        splashView = (VideoView) findViewById(R.id.splashScreen);
        String videoScreen = new StringBuilder("android.resource://")
                .append(getPackageName())
                .append("/raw/splash_video")
                .toString();
        splashView.setVideoPath(videoScreen);

        splashView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(SplashScreen.this, MainActivity.class));
                        finish();
                    }
                }, 500);
            }
        });
        splashView.start();
    }


}
