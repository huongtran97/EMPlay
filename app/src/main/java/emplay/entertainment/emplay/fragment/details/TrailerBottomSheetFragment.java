package emplay.entertainment.emplay.fragment.details;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import emplay.entertainment.emplay.R;

public class TrailerBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_VIDEO_KEY = "video_key";
    private static final String ARG_TITLE = "title";

    private WebView webView;

    public static TrailerBottomSheetFragment newInstance(String videoKey, String title) {
        TrailerBottomSheetFragment f = new TrailerBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_VIDEO_KEY, videoKey);
        args.putString(ARG_TITLE, title);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trailer_bottom_sheet, container, false);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = requireArguments();
        String videoKey = args.getString(ARG_VIDEO_KEY, "");
        String title = args.getString(ARG_TITLE, "");

        TextView tvTitle = view.findViewById(R.id.tv_trailer_title);
        tvTitle.setText(title);

        ImageButton btnClose = view.findViewById(R.id.btn_close_trailer);
        btnClose.setOnClickListener(v -> dismiss());

        webView = view.findViewById(R.id.webview_trailer);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setMediaPlaybackRequiresUserGesture(false);
        // YouTube's embed player checks the UA and refuses to play on the stock WebView UA
        settings.setUserAgentString(
                "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 "
                + "(KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36");
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        webView.setWebChromeClient(new WebChromeClient());

        // youtube-nocookie.com/embed/ is the privacy-enhanced endpoint; it also bypasses
        // the WebView detection that triggers Error 152 on the standard YouTube.com embed.
        String html = "<!DOCTYPE html><html><body style='margin:0;padding:0;background:#000;'>"
                + "<iframe width='100%' height='100%' style='border:0;'"
                + " src='https://www.youtube-nocookie.com/embed/" + videoKey
                + "?autoplay=1&rel=0&playsinline=1'"
                + " allow='autoplay;encrypted-media' allowfullscreen></iframe>"
                + "</body></html>";
        webView.loadDataWithBaseURL(
                "https://www.youtube-nocookie.com", html, "text/html", "utf-8", null);
    }

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) requireDialog();
        BottomSheetBehavior<?> behavior = dialog.getBehavior();
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setSkipCollapsed(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (webView != null) webView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (webView != null) webView.onResume();
    }

    @Override
    public void onDestroyView() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }
        super.onDestroyView();
    }
}