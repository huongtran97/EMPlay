package emplay.entertainment.emplay.fragment.common;

import android.os.Build;
import android.os.CountDownTimer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.flexbox.FlexboxLayout;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.databinding.WtwUnreleasedViewBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseFragment extends Fragment {

    protected CountDownTimer countDownTimer;

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

    // UI Thread
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

    // Genre Chips
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
            FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(px6);
            lp.bottomMargin = px6;
            chip.setLayoutParams(lp);
            container.addView(chip);
        }
    }

    // Coming Soon / Released Visibility
    protected void applyReleaseVisibility(String releaseDateStr, View releasedRoot, View unreleasedRoot, View comingSoonBadge, Runnable onUnreleased) {
        if (releaseDateStr == null || releaseDateStr.isEmpty()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate release = LocalDate.parse(releaseDateStr);
            boolean isUnreleased = release.isAfter(LocalDate.now());
            releasedRoot.setVisibility(isUnreleased ? View.GONE : View.VISIBLE);
            unreleasedRoot.setVisibility(isUnreleased ? View.VISIBLE : View.GONE);
            comingSoonBadge.setVisibility(isUnreleased ? View.VISIBLE : View.GONE);
            if (isUnreleased && onUnreleased != null) onUnreleased.run();
        }
    }

    // Countdown Timer
    protected void startCountdown(LocalDate releaseDate, WtwUnreleasedViewBinding unreleasedBinding, Runnable onFinish) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

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

    // Watch Providers
    protected void handleNoWatchProviders(View layoutEmpty, View rvProviders, View tabLayout) {
        if (!isAdded()) return;
        layoutEmpty.setVisibility(View.VISIBLE);
        rvProviders.setVisibility(View.GONE);
        tabLayout.setVisibility(View.GONE);
    }

    // Countdown Cleanup
    protected void cancelCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
}