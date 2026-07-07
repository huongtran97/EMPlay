package emplay.entertainment.emplay.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.util.Linkify;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import emplay.entertainment.emplay.BuildConfig;
import emplay.entertainment.emplay.R;

public class AboutActivity extends AppCompatActivity {

    private Dialog privacyPolicyDialog;
    private Dialog librariesDialog;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView versionText = findViewById(R.id.app_version);
        versionText.setText("Version " + BuildConfig.VERSION_NAME);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            findViewById(R.id.privacy_policy_row).setOnClickListener(v -> showPrivacyPolicy());
        }

        findViewById(R.id.libraries_row).setOnClickListener(v -> showLibraries());

        ImageView tmdbLogo = findViewById(R.id.tmdb_logo);
        tmdbLogo.setOnClickListener(v -> startActivity(
                new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.themoviedb.org"))));

        findViewById(R.id.motn_link).setOnClickListener(v -> startActivity(
                new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.movieofthenight.com"))));

    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void showPrivacyPolicy() {
        privacyPolicyDialog = new Dialog(this);
        privacyPolicyDialog.setContentView(R.layout.dialog_privacy_policy);

        Window window = privacyPolicyDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            // Force the dialog to be wider for better readability and justification
            window.setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.90),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView messageText = privacyPolicyDialog.findViewById(R.id.dialog_message);
        if (messageText != null) {
            CharSequence rawText = getText(R.string.privacy_policy_text);
            SpannableString spannable = new SpannableString(rawText);

            // Find all bold spans and make them larger
            StyleSpan[] styleSpans = spannable.getSpans(0, spannable.length(), StyleSpan.class);
            for (StyleSpan span : styleSpans) {
                if (span.getStyle() == Typeface.BOLD) {
                    int start = spannable.getSpanStart(span);
                    int end = spannable.getSpanEnd(span);
                    // 1.2f = 120% size
                    spannable.setSpan(new RelativeSizeSpan(1.2f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            messageText.setText(spannable);

            messageText.setAutoLinkMask(Linkify.WEB_URLS);
            messageText.setMovementMethod(LinkMovementMethod.getInstance());
            messageText.setLinkTextColor(Color.parseColor("#E53935"));

            // Force justification programmatically (Android 8.0+)
            // Use literal value 1 for JUSTIFICATION_MODE_INTER_WORD to satisfy linter across API levels
            messageText.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }

        privacyPolicyDialog.findViewById(R.id.btn_close).setOnClickListener(v -> privacyPolicyDialog.dismiss());
        privacyPolicyDialog.show();
    }

    private void showLibraries() {
        librariesDialog = new Dialog(this);
        librariesDialog.setContentView(R.layout.dialog_libraries);

        Window window = librariesDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.90),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        librariesDialog.findViewById(R.id.btn_close).setOnClickListener(v -> librariesDialog.dismiss());
        librariesDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (privacyPolicyDialog != null && privacyPolicyDialog.isShowing()) {
            privacyPolicyDialog.dismiss();
        }
        if (librariesDialog != null && librariesDialog.isShowing()) {
            librariesDialog.dismiss();
        }
    }
}
