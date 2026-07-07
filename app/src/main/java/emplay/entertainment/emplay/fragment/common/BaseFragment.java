package emplay.entertainment.emplay.fragment.common;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.view.WindowInsetsControllerCompat;
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
import androidx.transition.ChangeBounds;
import androidx.transition.ChangeImageTransform;
import androidx.transition.TransitionSet;

import emplay.entertainment.emplay.activity.LoginActivity;
import emplay.entertainment.emplay.database.DatabaseHelper;

import com.google.android.flexbox.FlexboxLayout;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.databinding.ActivityWtwUnreleasedBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseFragment extends Fragment {

    protected CountDownTimer countDownTimer;
    private long lastNavTime = 0;
    private final List<Call<?>> pendingCalls = new ArrayList<>();
    private DatabaseHelper dbHelper;

    protected DatabaseHelper getDbHelper() {
        if (dbHelper == null) dbHelper = DatabaseHelper.getInstance(requireContext());
        return dbHelper;
    }

    @ColorInt
    private int currentBgColor = 0xFF0D0D0D;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), bars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
    }

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
            if (isAdded()) requireActivity().getWindow().setStatusBarColor(color);
            currentBgColor = color;
        });
        animator.start();
    }

    protected void resetBackground() {
        currentBgColor = 0xFF0A0A0A;
        View root = getView();
        if (root != null) root.setBackgroundColor(currentBgColor);
        if (isAdded()) requireActivity().getWindow().setStatusBarColor(currentBgColor);
    }


    protected <T> void safeEnqueue(Call<T> call, Callback<T> callback) {
        pendingCalls.add(call);
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
                pendingCalls.remove(call);
                if (isAdded() && getContext() != null) callback.onResponse(call, response);
            }

            @Override
            public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
                pendingCalls.remove(call);
                if (isAdded() && getContext() != null) callback.onFailure(call, t);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        for (Call<?> c : pendingCalls) c.cancel();
        pendingCalls.clear();
    }

    protected void safeRunOnUiThread(Runnable runnable) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (isAdded()) runnable.run();
            });
        }
    }

    protected void navigateTo(Fragment fragment) {
        navigateTo(fragment, null, null);
    }

    protected void navigateTo(Fragment fragment, @Nullable View sharedElement, @Nullable String transitionName) {
        if (!isAdded()) return;
        long now = SystemClock.elapsedRealtime();
        if (now - lastNavTime < 500) return;
        lastNavTime = now;

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

        if (sharedElement != null && transitionName != null) {
            TransitionSet transitionSet = new TransitionSet();
            transitionSet.addTransition(new ChangeBounds());
            transitionSet.addTransition(new ChangeImageTransform());
            transitionSet.setDuration(350);

            fragment.setSharedElementEnterTransition(transitionSet);
            fragment.setSharedElementReturnTransition(transitionSet);

            transaction.addSharedElement(sharedElement, transitionName);
        } else {
            transaction.setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
            );
        }

        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

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


    protected void applyReleaseVisibility(String releaseDateStr, View releasedRoot,
                                          View unreleasedRoot, View comingSoonBadge,
                                          Runnable onUnreleased) {
        if (releaseDateStr == null || releaseDateStr.isEmpty()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate release    = LocalDate.parse(releaseDateStr);
            boolean isUnreleased = !release.isBefore(LocalDate.now());
            releasedRoot.setVisibility(isUnreleased ? View.GONE  : View.VISIBLE);
            unreleasedRoot.setVisibility(isUnreleased ? View.VISIBLE : View.GONE);
            comingSoonBadge.setVisibility(isUnreleased ? View.VISIBLE : View.GONE);
            if (isUnreleased && onUnreleased != null) onUnreleased.run();
        }
    }

    protected void startCountdown(LocalDate releaseDate,
                                  ActivityWtwUnreleasedBinding unreleasedBinding,
                                  Runnable onFinish) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        String formatted = releaseDate.format(
                DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault()));
        unreleasedBinding.tvExpectedLabel.setText(formatted);

        long releaseMillis = releaseDate.atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli();
        long diff = releaseMillis - System.currentTimeMillis();

        if (diff <= 0) {
            if (onFinish != null) onFinish.run();
            return;
        }

        unreleasedBinding.tvDays.setText(String.valueOf(diff / 86400000));
        unreleasedBinding.tvHours.setText(String.format(Locale.ROOT, "%02d", (diff % 86400000) / 3600000));
        unreleasedBinding.tvMinutes.setText(String.format(Locale.ROOT, "%02d", (diff % 3600000) / 60000));
        unreleasedBinding.tvSeconds.setText(String.format(Locale.ROOT, "%02d", (diff % 60000) / 1000));

        countDownTimer = new CountDownTimer(diff, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long days    = millisUntilFinished / 86400000;
                long hours   = (millisUntilFinished % 86400000) / 3600000;
                long minutes = (millisUntilFinished % 3600000)  / 60000;
                long seconds = (millisUntilFinished % 60000)    / 1000;
                safeRunOnUiThread(() -> {
                    unreleasedBinding.tvDays.setText(String.valueOf(days));
                    unreleasedBinding.tvHours.setText(String.format(Locale.ROOT, "%02d", hours));
                    unreleasedBinding.tvMinutes.setText(String.format(Locale.ROOT, "%02d", minutes));
                    unreleasedBinding.tvSeconds.setText(String.format(Locale.ROOT, "%02d", seconds));
                });
            }

            @Override
            public void onFinish() {
                if (onFinish != null) safeRunOnUiThread(onFinish);
            }
        }.start();
    }

    protected static void sizeChipIcon(TextView tv) {
        Drawable[] drawables = tv.getCompoundDrawablesRelative();
        Drawable icon = drawables[0];
        if (icon == null) return;
        int size = Math.round(tv.getTextSize());
        icon.setBounds(0, 0, size, size);
        tv.setCompoundDrawablesRelative(icon, null, null, null);
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

    protected void setDarkStatusBar() {
        if (!isAdded()) return;
        android.view.Window window = requireActivity().getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        new WindowInsetsControllerCompat(window, window.getDecorView())
                .setAppearanceLightStatusBars(false);
    }

    protected int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    protected void hideKeyboard() {
        View focused = requireActivity().getCurrentFocus();
        if (focused != null) {
            InputMethodManager imm = (InputMethodManager) requireActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
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

    protected void performTicketTearAnimation(View button, TextView label, boolean isSaved) {
        if (button == null || label == null || !isAdded()) return;

        button.setBackground(ContextCompat.getDrawable(requireContext(),
                isSaved ? R.drawable.bg_button_ticket_stub_solid : R.drawable.bg_button_ticket_stub));

        label.setText(isSaved ? R.string.detail_saved : R.string.detail_btn_mylist);

        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f, -dpToPx(4), 0f);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(button, pvhY);
        animator.setDuration(400);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }
}
