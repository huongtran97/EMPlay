package emplay.entertainment.emplay.tool;

import android.app.AlertDialog;
import android.content.Context;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import emplay.entertainment.emplay.adapter.common.ProviderAdapter;
import emplay.entertainment.emplay.models.common.RegionProvidersModel;
import androidx.recyclerview.widget.RecyclerView;

public class WatchProviderHelper {

    public interface RegionSelectedListener {
        void onRegionSelected(String regionCode);
    }

    public static String defaultRegion() {
        return "CA";
    }

    public static void showRegionPicker(Context context, Map<String, RegionProvidersModel> providerResults, RegionSelectedListener listener) {
        if (providerResults == null || providerResults.isEmpty()) return;
        
        List<String> codes = new ArrayList<>(providerResults.keySet());
        if (!codes.contains("VN")) codes.add("VN");
        Collections.sort(codes);
        
        String[] displayNames = new String[codes.size()];
        for (int i = 0; i < codes.size(); i++) {
            String name = new Locale("", codes.get(i)).getDisplayCountry();
            displayNames[i] = name.isEmpty() ? codes.get(i) : name + " (" + codes.get(i) + ")";
        }
        
        new AlertDialog.Builder(context)
                .setTitle("Select region")
                .setItems(displayNames, (dialog, which) -> listener.onRegionSelected(codes.get(which)))
                .show();
    }

    public static void setupProviderTabs(Context context, TabLayout tabLayout, RecyclerView recyclerView, RegionProvidersModel providers) {
        ProviderAdapter streamAdapter = new ProviderAdapter(ProviderAdapter.TYPE_STREAM);
        ProviderAdapter rentAdapter   = new ProviderAdapter(ProviderAdapter.TYPE_RENT_BUY);
        ProviderAdapter buyAdapter    = new ProviderAdapter(ProviderAdapter.TYPE_RENT_BUY);

        FlexboxLayoutManager flexLm = new FlexboxLayoutManager(context);
        flexLm.setFlexWrap(FlexWrap.WRAP);
        recyclerView.setLayoutManager(flexLm);

        streamAdapter.submitList(providers.getFlatrate());
        recyclerView.setAdapter(streamAdapter);

        tabLayout.clearOnTabSelectedListeners();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        recyclerView.setAdapter(streamAdapter);
                        streamAdapter.submitList(providers.getFlatrate());
                        break;
                    case 1:
                        recyclerView.setAdapter(rentAdapter);
                        rentAdapter.submitList(providers.getRent());
                        break;
                    case 2:
                        recyclerView.setAdapter(buyAdapter);
                        buyAdapter.submitList(providers.getBuy());
                        break;
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
}
