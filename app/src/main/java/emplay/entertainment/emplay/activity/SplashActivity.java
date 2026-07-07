package emplay.entertainment.emplay.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import emplay.entertainment.emplay.R;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final long STEP_MS = 850L;
    private static final long INITIAL_DELAY_MS = 400L;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private FrameLayout[] blocks;
    private TextView[] numbers;
    private int accentColor;
    private int dimColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        accentColor = ContextCompat.getColor(this, R.color.accent);
        dimColor = ContextCompat.getColor(this, R.color.text_3);

        blocks = new FrameLayout[]{
                findViewById(R.id.block3),
                findViewById(R.id.block2),
                findViewById(R.id.block1)
        };
        numbers = new TextView[]{
                findViewById(R.id.tvNum3),
                findViewById(R.id.tvNum2),
                findViewById(R.id.tvNum1)
        };

        handler.postDelayed(() -> runStep(0), INITIAL_DELAY_MS);
    }

    private void runStep(int index) {
        if (index >= 3) {
            launchMain();
            return;
        }
        activateBlock(index);
        handler.postDelayed(() -> runStep(index + 1), STEP_MS);
    }

    private void activateBlock(int activeIndex) {
        for (int i = 0; i < 3; i++) {
            boolean active = (i == activeIndex);
            FrameLayout block = blocks[i];
            TextView num = numbers[i];

            if (active) {
                num.setTextColor(accentColor);
                // Bring block to full opacity with a bounce-in scale
                block.animate().cancel();
                block.setScaleX(0.88f);
                block.setScaleY(0.88f);
                block.animate()
                        .alpha(1f)
                        .scaleX(1.08f)
                        .scaleY(1.08f)
                        .setDuration(160)
                        .withEndAction(() ->
                                block.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(100)
                                        .start())
                        .start();
            } else {
                num.setTextColor(dimColor);
                block.animate().cancel();
                block.animate()
                        .alpha(0.35f)
                        .scaleX(0.9f)
                        .scaleY(0.9f)
                        .setDuration(200)
                        .start();
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void launchMain() {
        // Brief pause on "1" before transitioning
        handler.postDelayed(() -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out);
            } else {
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
            finish();
        }, 400);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}