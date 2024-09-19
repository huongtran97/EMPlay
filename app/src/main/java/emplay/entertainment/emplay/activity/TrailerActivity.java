package emplay.entertainment.emplay.activity;


import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import emplay.entertainment.emplay.R;

public class TrailerActivity extends AppCompatActivity {

    private static final String ARG_MOVIE_ID = "MOVIE_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trailer);

        WebView webView = findViewById(R.id.webview_trailer);

        if (webView != null) {
            // Enable JavaScript
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true); // Enable DOM storage
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);

            // Enable hardware acceleration for video playback
            webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null);

            // Set WebViewClient to handle navigation within WebView
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    // This ensures that all URL loads happen in the WebView itself
                    view.loadUrl(url);
                    return true;
                }

                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    Toast.makeText(TrailerActivity.this, "Error loading video", Toast.LENGTH_SHORT).show();
                }
            });


            // Get the video ID from the Intent
            String videoId = getIntent().getStringExtra(ARG_MOVIE_ID);
            if (videoId != null && !videoId.isEmpty()) {
                // Build the YouTube embed URL for in-app playback
                String embedUrl = "https://www.youtube.com/embed/" + videoId + "?autoplay=1&fullscreen=1";
                // Load the URL in WebView
                webView.loadUrl(embedUrl);
            } else {
                Toast.makeText(this, "Video ID not provided", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "WebView is not available", Toast.LENGTH_SHORT).show();
        }
    }


}