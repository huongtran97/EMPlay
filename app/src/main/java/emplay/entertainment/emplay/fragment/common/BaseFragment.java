package emplay.entertainment.emplay.fragment.common;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.TextViewCompat;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import emplay.entertainment.emplay.activity.LoginActivity;

import com.google.android.flexbox.FlexboxLayout;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.databinding.WtwUnreleasedViewBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseFragment extends Fragment {

    protected CountDownTimer countDownTimer;

    // Current background color — tracked so argb animation starts from the right value.
    // Defaults to near-black. Subclasses that use animateBackground() share this state.
    @ColorInt
    private int currentBgColor = 0xFF0D0D0D;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Default: respect system bars padding (status bar + nav bar).
        // Fragments that want edge-to-edge (e.g. HomeFragment) should override
        // onViewCreated, call super, then clear the top padding for their hero area.
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), bars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
    }

    // Dynamic background color — used by fragments with a Palette banner
    protected void animateBackground(@ColorInt int toColor) {
        View root = getView();
        if (root == null || !isAdded()) return;

        int fromColor = currentBgColor;
        if (fromColor == toColor) return;

        ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), fromColor, toColor);
        animator.setDuration(500);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(anim -> {
            int color = (int) anim.getAnimatedValue();
            root.setBackgroundColor(color);
            currentBgColor = color;
        });
        animator.start();
    }

//    Resets the background to the default near-black without animation.
//    Call in onDestroyView or when leaving the banner screen.
    protected void resetBackground() {
        currentBgColor = 0xFF0D0D0D;
        View root = getView();
        if (root != null) root.setBackgroundColor(currentBgColor);
    }


    // Retrofit
    protected <T> void safeEnqueue(Call<T> call, Callback<T> callback) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
                if (isAdded() && getContext() != null) callback.onResponse(call, response);
            }

            @Override
            public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
                if (isAdded() && getContext() != null) callback.onFailure(call, t);
            }
        });
    }

    // UI thread
    protected void safeRunOnUiThread(Runnable runnable) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (isAdded()) runnable.run();
            });
        }
    }

    // Navigation
    protected void navigateTo(Fragment fragment) {
        if (!isAdded()) return;
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // Genre chips
    protected void buildGenreChips(FlexboxLayout container, List<String> genres) {
        if (container == null || genres == null) return;
        float density = getResources().getDisplayMetrics().density;
        int px9 = Math.round(9 * density);
        int px5 = Math.round(5 * density);
        int px6 = Math.round(6 * density);
        container.removeAllViews();
        for (String genre : genres) {
            TextView chip = new TextView(requireContext());
            chip.setText(genre);
            chip.setTextColor(0xFF777777);
            chip.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 13);
            chip.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_meta_chip));
            chip.setPadding(px9, px5, px9, px5);
            FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(px6);
            lp.bottomMargin = px6;
            chip.setLayoutParams(lp);
            container.addView(chip);
        }
    }


    // Release visibility
    protected void applyReleaseVisibility(String releaseDateStr, View releasedRoot,
                                          View unreleasedRoot, View comingSoonBadge,
                                          Runnable onUnreleased) {
        if (releaseDateStr == null || releaseDateStr.isEmpty()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate release    = LocalDate.parse(releaseDateStr);
            boolean isUnreleased = release.isAfter(LocalDate.now());
            releasedRoot.setVisibility(isUnreleased ? View.GONE  : View.VISIBLE);
            unreleasedRoot.setVisibility(isUnreleased ? View.VISIBLE : View.GONE);
            comingSoonBadge.setVisibility(isUnreleased ? View.VISIBLE : View.GONE);
            if (isUnreleased && onUnreleased != null) onUnreleased.run();
        }
    }

    // Countdown timer
    protected void startCountdown(LocalDate releaseDate,
                                  WtwUnreleasedViewBinding unreleasedBinding,
                                  Runnable onFinish) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        String formatted = releaseDate.format(
                DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault()));
        unreleasedBinding.tvExpectedLabel.setText(formatted);

        long releaseMillis = releaseDate.atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli();
        long diff = releaseMillis - System.currentTimeMillis();

        countDownTimer = new CountDownTimer(diff, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long days    = millisUntilFinished / 86400000;
                long hours   = (millisUntilFinished % 86400000) / 3600000;
                long minutes = (millisUntilFinished % 3600000)  / 60000;
                long seconds = (millisUntilFinished % 60000)    / 1000;
                safeRunOnUiThread(() -> {
                    unreleasedBinding.tvDays.setText(String.valueOf(days));
                    unreleasedBinding.tvHours.setText(String.format("%02d", hours));
                    unreleasedBinding.tvMinutes.setText(String.format("%02d", minutes));
                    unreleasedBinding.tvSeconds.setText(String.format("%02d", seconds));
                });
            }

            @Override
            public void onFinish() {
                if (onFinish != null) safeRunOnUiThread(onFinish);
            }
        }.start();
    }

    // Scales the drawableStart of a chip TextView to match its text height.
    protected static void sizeChipIcon(TextView tv) {
        Drawable[] drawables = TextViewCompat.getCompoundDrawablesRelative(tv);
        Drawable icon = drawables[0];
        if (icon == null) return;
        int size = Math.round(tv.getTextSize());
        icon.setBounds(0, 0, size, size);
        TextViewCompat.setCompoundDrawablesRelative(tv, icon, null, null, null);
    }

    protected void handleNoWatchProviders(View layoutEmpty, View rvProviders, View tabLayout) {
        if (!isAdded()) return;
        layoutEmpty.setVisibility(View.VISIBLE);
        rvProviders.setVisibility(View.GONE);
        tabLayout.setVisibility(View.GONE);
    }

    protected void cancelCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    protected void showLoginPromptDialog() {
        if (!isAdded() || getContext() == null) return;
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.login_prompt_title)
                .setMessage(R.string.login_prompt_message)
                .setPositiveButton(R.string.login_prompt_sign_in, (d, w) ->
                        startActivity(new Intent(requireContext(), LoginActivity.class)))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}