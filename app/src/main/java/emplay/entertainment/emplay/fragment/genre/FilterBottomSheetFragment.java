package emplay.entertainment.emplay.fragment.genre;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.slider.RangeSlider;

import java.util.Calendar;
import java.util.List;

import emplay.entertainment.emplay.R;

public class FilterBottomSheetFragment extends BottomSheetDialogFragment {

    public interface FilterListener {
        void onFilterApplied(String sortToken, int yearFrom, int yearTo);
    }

    public static final String SORT_POPULAR = "popularity";
    public static final String SORT_RATING  = "rating";
    public static final String SORT_NEWEST  = "newest";

    private static final String ARG_SORT      = "sort";
    private static final String ARG_YEAR_FROM = "year_from";
    private static final String ARG_YEAR_TO   = "year_to";

    private FilterListener listener;

    public static FilterBottomSheetFragment newInstance(String sortToken, int yearFrom, int yearTo) {
        FilterBottomSheetFragment f = new FilterBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SORT, sortToken);
        args.putInt(ARG_YEAR_FROM, yearFrom);
        args.putInt(ARG_YEAR_TO, yearTo);
        f.setArguments(args);
        return f;
    }

    public void setFilterListener(FilterListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter_bottom_sheet, container, false);

        String currentSort = getArguments() != null
                ? getArguments().getString(ARG_SORT, SORT_POPULAR) : SORT_POPULAR;
        int maxYear = Calendar.getInstance().get(Calendar.YEAR);
        int yearFrom = getArguments() != null
                ? getArguments().getInt(ARG_YEAR_FROM, 1940) : 1940;
        int yearTo = getArguments() != null
                ? getArguments().getInt(ARG_YEAR_TO, maxYear) : maxYear;

        TextView chipPopular = view.findViewById(R.id.filterChipPopular);
        TextView chipRating  = view.findViewById(R.id.filterChipRating);
        TextView chipNewest  = view.findViewById(R.id.filterChipNewest);

        final String[] selectedSort = {currentSort};
        applySortSelection(chipPopular, chipRating, chipNewest, currentSort);

        chipPopular.setOnClickListener(v -> {
            selectedSort[0] = SORT_POPULAR;
            applySortSelection(chipPopular, chipRating, chipNewest, SORT_POPULAR);
        });
        chipRating.setOnClickListener(v -> {
            selectedSort[0] = SORT_RATING;
            applySortSelection(chipPopular, chipRating, chipNewest, SORT_RATING);
        });
        chipNewest.setOnClickListener(v -> {
            selectedSort[0] = SORT_NEWEST;
            applySortSelection(chipPopular, chipRating, chipNewest, SORT_NEWEST);
        });

        RangeSlider slider = view.findViewById(R.id.yearRangeSlider);
        TextView tvYearLabel = view.findViewById(R.id.tvYearRangeLabel);

        int clampedFrom = Math.max(1940, Math.min(yearFrom, maxYear));
        int clampedTo   = Math.max(clampedFrom, Math.min(yearTo, maxYear));
        slider.setValueFrom(1940f);
        slider.setValueTo((float) maxYear);
        slider.setStepSize(1f);
        slider.setValues((float) clampedFrom, (float) clampedTo);
        tvYearLabel.setText(clampedFrom + " – " + clampedTo);

        final int[] selectedYears = {clampedFrom, clampedTo};
        slider.addOnChangeListener((s, value, fromUser) -> {
            List<Float> vals = s.getValues();
            selectedYears[0] = vals.get(0).intValue();
            selectedYears[1] = vals.get(1).intValue();
            tvYearLabel.setText(selectedYears[0] + " – " + selectedYears[1]);
        });

        view.findViewById(R.id.btnApply).setOnClickListener(v -> {
            if (listener != null) {
                listener.onFilterApplied(selectedSort[0], selectedYears[0], selectedYears[1]);
            }
            dismiss();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!(getDialog() instanceof BottomSheetDialog)) return;
        FrameLayout bottomSheet = ((BottomSheetDialog) getDialog())
                .findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet == null) return;

        BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        bottomSheet.post(() -> {
            float startY = bottomSheet.getHeight() > 0 ? bottomSheet.getHeight() : 400f;
            bottomSheet.setTranslationY(startY);
            SpringAnimation spring = new SpringAnimation(bottomSheet, DynamicAnimation.TRANSLATION_Y, 0f);
            spring.getSpring()
                    .setStiffness(400f)
                    .setDampingRatio(0.3f);
            spring.start();
        });
    }

    private void applySortSelection(TextView popular, TextView rating, TextView newest, String sort) {
        popular.setSelected(SORT_POPULAR.equals(sort));
        rating.setSelected(SORT_RATING.equals(sort));
        newest.setSelected(SORT_NEWEST.equals(sort));
    }
}