package emplay.entertainment.emplay.fragment.layout;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.fragment.common.WatchlistTabFragment;

public class WatchlistFragment extends Fragment {

    private static final String ARG_INITIAL_TAB = "initial_tab";
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TextView tvItemCount, tvSortLabel;
    private View llSortBtn;
    private int initialTab = 0;

    public static WatchlistFragment newInstance(int initialTab) {
        WatchlistFragment fragment = new WatchlistFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_INITIAL_TAB, initialTab);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            initialTab = getArguments().getInt(ARG_INITIAL_TAB, 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.watchlist_view, container, false);

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        tvItemCount = view.findViewById(R.id.tvItemCount);
        tvSortLabel = view.findViewById(R.id.tvSortLabel);
        llSortBtn = view.findViewById(R.id.llSortBtn);

        setupViewPager();
        viewPager.setCurrentItem(initialTab, false);

        return view;
    }

    private void setupViewPager() {
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0: return WatchlistTabFragment.newInstance("movie");
                    case 1: return WatchlistTabFragment.newInstance("tv");
                    default: return new Fragment();
                }
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Movies"); break;
                case 1: tab.setText("TV Shows"); break;
            }
        }).attach();

        // Listen for page changes to update global count or sorting label if needed
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // The TabFragment will update the parent's tvItemCount via callback or interface
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void updateItemCount(int count, String type) {
        if (tvItemCount != null) {
            tvItemCount.setText(count + " " + type + (count == 1 ? "" : "s"));
        }
    }
}
