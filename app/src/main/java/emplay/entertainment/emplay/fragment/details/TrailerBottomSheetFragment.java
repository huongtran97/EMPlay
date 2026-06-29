package emplay.entertainment.emplay.fragment.details;

import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
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
    private static final String ARG_TITLE     = "title";

    // Samsung's WindowInsetsController has rotation bugs — legacy IMMERSIVE_STICKY
    // flags are what Samsung's own apps rely on for stable fullscreen behavior.
    @SuppressWarnings("deprecation")
    private static final int IMMERSIVE_FLAGS =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    private YouTubePlayerView youTubePlayerView;
    private FrameLayout       fullscreenContainer;
    private boolean           isFullscreen = false;

    // Construction
    public static TrailerBottomSheetFragment newInstance(String videoKey, String title) {
        TrailerBottomSheetFragment f = new TrailerBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_VIDEO_KEY, videoKey);
        args.putString(ARG_TITLE, title);
        f.setArguments(args);
        return f;
    }

    // DialogFragment lifecycle
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
        String title    = args.getString(ARG_TITLE, "");

        bindHeaderViews(view, title);
        initPlayer(view, videoKey);
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
        if (isFullscreen) {
            applyWindowState();
            addRotationBlackout();
        }
        View root = getView();
        if (root != null) {
            root.post(this::applyWindowState);
        }
    }

    @Override
    public void onDestroyView() {
        restoreActivityWindowState();
        releasePlayer();
        super.onDestroyView();
    }

    // View setup
    private void bindHeaderViews(View root, String title) {
        TextView tvTitle = root.findViewById(R.id.tv_trailer_title);
        tvTitle.setText(title);

        ImageButton btnClose = root.findViewById(R.id.btn_close_trailer);
        btnClose.setOnClickListener(v -> dismiss());
    }

    private void initPlayer(View root, String videoKey) {
        youTubePlayerView   = root.findViewById(R.id.youtube_player_view);
        fullscreenContainer = root.findViewById(R.id.fullscreen_container);

        getViewLifecycleOwner().getLifecycle().addObserver(youTubePlayerView);
        youTubePlayerView.addFullscreenListener(buildFullscreenListener());

        IFramePlayerOptions options = new IFramePlayerOptions.Builder(requireContext())
                .controls(1)
                .fullscreen(1)
                .build();

        youTubePlayerView.initialize(buildPlayerListener(videoKey), options);
    }

    // YouTube player listeners
    private FullscreenListener buildFullscreenListener() {
        return new FullscreenListener() {
            @Override
            public void onEnterFullscreen(@NonNull View fullscreenView,
                                          @NonNull Function0<Unit> exitFullscreen) {
                isFullscreen = true;
                fullscreenView.setBackgroundColor(Color.BLACK);
                fullscreenContainer.addView(fullscreenView, matchParentParams());
                fullscreenContainer.setVisibility(View.VISIBLE);
                requireActivity().setRequestedOrientation(
                        ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                applyWindowState();
            }

            @Override
            public void onExitFullscreen() {
                isFullscreen = false;
                fullscreenContainer.removeAllViews();
                fullscreenContainer.setVisibility(View.GONE);
                requireActivity().setRequestedOrientation(
                        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                applyWindowState();
            }
        };
    }

    private AbstractYouTubePlayerListener buildPlayerListener(String videoKey) {
        return new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer player) {
                if (videoKey != null && !videoKey.isEmpty()) {
                    player.loadVideo(videoKey, 0);
                } else {
                    dismiss();
                    showToast("No trailer available");
                }
            }

            @Override
            public void onError(@NonNull YouTubePlayer player,
                                @NonNull PlayerConstants.PlayerError error) {
                dismiss();
                showToast("This trailer can't be played in-app");
            }
        };
    }

    // Window state — top-level dispatcher
    private void applyWindowState() {
        Dialog dialog = getDialog();
        if (dialog == null) return;
        Window window = dialog.getWindow();
        if (window == null) return;

        window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        if (isFullscreen) {
            applyFullscreenState(window);
        } else {
            applyNormalState(window);
        }
    }

    // Window state — fullscreen
    @SuppressWarnings("deprecation")
    private void applyFullscreenState(Window dialogWindow) {
        Window activityWindow = requireActivity().getWindow();

        // Black out both windows so any gap during Samsung's rotation shows black, not cream.
        dialogWindow.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        activityWindow.setBackgroundDrawable(new ColorDrawable(Color.BLACK));

        dialogWindow.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        dialogWindow.addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        WindowCompat.setDecorFitsSystemWindows(dialogWindow, false);
        WindowCompat.setDecorFitsSystemWindows(activityWindow, false);

        // Use real pixel dimensions — MATCH_PARENT on a dialog resolves against its
        // parent frame, which can exclude system bar areas on Samsung devices.
        Rect screen = getScreenBounds();
        dialogWindow.setLayout(screen.width(), screen.height());
        dialogWindow.setGravity(Gravity.CENTER);
        centerDialogWindow(dialogWindow);

        dialogWindow.getDecorView().setSystemUiVisibility(IMMERSIVE_FLAGS);
        activityWindow.getDecorView().setSystemUiVisibility(IMMERSIVE_FLAGS);
    }

    private Rect getScreenBounds() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return requireActivity().getWindowManager()
                    .getCurrentWindowMetrics().getBounds();
        }
        DisplayMetrics dm = new DisplayMetrics();
        //noinspection deprecation
        requireActivity().getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        return new Rect(0, 0, dm.widthPixels, dm.heightPixels);
    }

    private void centerDialogWindow(Window window) {
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 0;
        window.setAttributes(params);
    }

    // Window state — normal (non-fullscreen)
    @SuppressWarnings("deprecation")
    private void applyNormalState(Window dialogWindow) {
        Window activityWindow = requireActivity().getWindow();
        WindowInsetsControllerCompat activityController =
                WindowCompat.getInsetsController(activityWindow, activityWindow.getDecorView());

        dialogWindow.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        activityWindow.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

        dialogWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        activityWindow.setBackgroundDrawableResource(R.color.bg_base);

        dialogWindow.clearFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        WindowCompat.setDecorFitsSystemWindows(dialogWindow, true);
        WindowCompat.setDecorFitsSystemWindows(activityWindow, true);
        activityController.show(WindowInsetsCompat.Type.systemBars());

        // Allow touches outside the trailer to reach the detail fragment beneath.
        dialogWindow.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

        dialogWindow.setLayout(WindowManager.LayoutParams.MATCH_PARENT, normalDialogHeight());
        dialogWindow.setGravity(Gravity.TOP);
        offsetDialogBelowStatusBar(dialogWindow);
    }

    private int normalDialogHeight() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int videoHeight    = (int) (dm.widthPixels * 9.0 / 16.0);
        int titleBarHeight = (int) (56 * dm.density);
        return videoHeight + titleBarHeight;
    }

    private void offsetDialogBelowStatusBar(Window window) {
        WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(
                requireActivity().getWindow().getDecorView());
        if (insets == null) return;
        int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
        WindowManager.LayoutParams params = window.getAttributes();
        params.y = statusBarHeight;
        window.setAttributes(params);
    }

    // Rotation blackout overlay
    /**
     * Covers the YouTube WebView with a solid black overlay during Samsung's rotation
     * animation so its intermediate re-render state is never visible to the user.
     * The overlay is removed once layout has settled (plus a short paint buffer).
     */
    private void addRotationBlackout() {
        View blackout = new View(requireContext());
        blackout.setBackgroundColor(Color.BLACK);
        fullscreenContainer.addView(blackout, matchParentParams());

        blackout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        blackout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        blackout.postDelayed(() -> {
                            if (fullscreenContainer != null) {
                                fullscreenContainer.removeView(blackout);
                            }
                        }, 800);
                    }
                });
    }

    // Cleanup helpers
    private void restoreActivityWindowState() {
        if (isFullscreen && fullscreenContainer != null) {
            fullscreenContainer.removeAllViews();
        }
        isFullscreen = false;

        if (getActivity() == null) return;
        Window activityWindow = getActivity().getWindow();
        //noinspection deprecation
        activityWindow.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        activityWindow.setBackgroundDrawableResource(R.color.bg_base);
        WindowCompat.setDecorFitsSystemWindows(activityWindow, true);
        WindowCompat.getInsetsController(activityWindow, activityWindow.getDecorView())
                .show(WindowInsetsCompat.Type.systemBars());
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    private void releasePlayer() {
        if (youTubePlayerView != null) {
            youTubePlayerView.release();
            youTubePlayerView = null;
        }
    }

    // Utility
    private static ViewGroup.LayoutParams matchParentParams() {
        return new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}