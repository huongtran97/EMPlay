package emplay.entertainment.emplay.activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import emplay.entertainment.emplay.R;

public class ForgotPasswordSheet extends BottomSheetDialogFragment {

    private static final String TMDB_RESET_URL = "https://www.themoviedb.org/reset-password";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_forgot_password, container, false);

        view.findViewById(R.id.btn_reset_tmdb).setOnClickListener(v -> {
            new CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .setToolbarColor(ContextCompat.getColor(requireContext(), R.color.bg_surface))
                    .build()
                    .launchUrl(requireContext(), Uri.parse(TMDB_RESET_URL));
            dismiss();
        });

        return view;
    }
}