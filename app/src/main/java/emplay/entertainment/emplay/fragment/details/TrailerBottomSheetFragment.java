package emplay.entertainment.emplay.fragment.details;

import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import emplay.entertainment.emplay.R;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class TrailerBottomSheetFragment extends DialogFragment {

    private static final String ARG_VIDEO_KEY = "video_key";
    private static final String ARG_TITLE = "title";

    private YouTubePlayerView youTubePlayerView;
    private FrameLayout fullscreenContainer;
    private boolean isMaximized = false;

    public static TrailerBottomSheetFragment newInstance(String videoKey, String title) {
        TrailerBottomSheetFragment f = new TrailerBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_VIDEO_KEY, videoKey);
        args.putString(ARG_TITLE, title);
        f.setArguments(args);
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext(), R.style.Theme_EMPlay);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trailer_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = requireArguments();
        String videoKey = args.getString(ARG_VIDEO_KEY, "");
        String title = args.getString(ARG_TITLE, "");

        android.util.Log.d("TrailerDebug", "videoKey='" + videoKey + "'");

        TextView tvTitle = view.findViewById(R.id.tv_trailer_title);
        tvTitle.setText(title);

        ImageButton btnClose = view.findViewById(R.id.btn_close_trailer);
        btnClose.setOnClickListener(v -> dismiss());

        youTubePlayerView = view.findViewById(R.id.youtube_player_view);
        fullscreenContainer = view.findViewById(R.id.fullscreen_container);
        getViewLifecycleOwner().getLifecycle().addObserver(youTubePlayerView);

        youTubePlayerView.addFullscreenListener(new FullscreenListener() {
            @Override
            public void onEnterFullscreen(@NonNull View fullscreenView,
                                          @NonNull Function0<Unit> exitFullscreen) {
                isMaximized = true;
                fullscreenContainer.addView(fullscreenView);
                fullscreenContainer.setVisibility(View.VISIBLE);
                requireActivity().setRequestedOrientation(
                        ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                applyWindowState();
            }

            @Override
            public void onExitFullscreen() {
                isMaximized = false;
                fullscreenContainer.removeAllViews();
                fullscreenContainer.setVisibility(View.GONE);
                requireActivity().setRequestedOrientation(
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                applyWindowState();
            }
        });

        IFramePlayerOptions options = new IFramePlayerOptions.Builder(requireContext())
                .controls(1)
                .fullscreen(1)
                .build();

        youTubePlayerView.initialize(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                if (videoKey != null && !videoKey.isEmpty()) {
                    youTubePlayer.loadVideo(videoKey, 0);
                } else {
                    dismiss();
                    Toast.makeText(requireContext(),
                            "No trailer available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(@NonNull YouTubePlayer youTubePlayer,
                                @NonNull PlayerConstants.PlayerError error) {
                dismiss();
                Toast.makeText(requireContext(),
                        "This trailer can't be played in-app", Toast.LENGTH_SHORT).show();
            }
        }, options);
    }

    private void applyWindowState() {
        Dialog dialog = getDialog();
        if (dialog == null) return;
        Window window = dialog.getWindow();
        if (window == null) return;

        window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        if (isMaximized) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            window.setGravity(Gravity.CENTER);
            WindowManager.LayoutParams params = window.getAttributes();
            params.y = 0;
            window.setAttributes(params);
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int videoHeight = (int) (screenWidth * 9.0 / 16.0);
            int titleBarHeight = (int) (56 * getResources().getDisplayMetrics().density);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    videoHeight + titleBarHeight);
            window.setGravity(Gravity.TOP);

            WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(
                    requireActivity().getWindow().getDecorView());
            if (insets != null) {
                int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                WindowManager.LayoutParams params = window.getAttributes();
                params.y = statusBarHeight;
                window.setAttributes(params);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = requireDialog().getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setWindowAnimations(R.style.TrailerTopSheetAnimation);
        }
        applyWindowState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        View root = getView();
        if (root != null) {
            root.post(this::applyWindowState);
        }
    }

    @Override
    public void onDestroyView() {
        if (getActivity() != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        if (youTubePlayerView != null) {
            youTubePlayerView.release();
            youTubePlayerView = null;
        }
        super.onDestroyView();
    }
}