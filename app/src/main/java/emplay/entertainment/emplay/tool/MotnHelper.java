package emplay.entertainment.emplay.tool;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import emplay.entertainment.emplay.api.motn.MotnShowResponse;
import emplay.entertainment.emplay.api.motn.MotnStreamingOption;
import emplay.entertainment.emplay.models.common.ProviderModel;
import emplay.entertainment.emplay.models.common.RegionProvidersModel;

public class MotnHelper {

    private static final Gson GSON = new Gson();

    public static final String SHOW_TYPE_MOVIE  = "movie";
    public static final String SHOW_TYPE_SERIES = "series";

    /** 7 days in milliseconds */
    public static final long CACHE_TTL_MS = 7L * 24 * 60 * 60 * 1000;

    /**
     * Convert a MotnShowResponse into the same Map<region, RegionProvidersModel> shape
     * that TMDB watch/providers returns so the existing WTW UI works unchanged.
     *
     * MOTN uses lowercase country codes ("us", "ca") — we uppercase them for consistency
     * with WatchProviderHelper.defaultRegion().
     */
    public static Map<String, RegionProvidersModel> toProviderMap(MotnShowResponse response) {
        Map<String, RegionProvidersModel> result = new HashMap<>();
        if (response == null || response.getStreamingInfo() == null) return result;

        for (Map.Entry<String, List<MotnStreamingOption>> entry : response.getStreamingInfo().entrySet()) {
            String region = entry.getKey().toUpperCase(Locale.ROOT);
            List<MotnStreamingOption> options = entry.getValue();

            List<ProviderModel> flatrate = new ArrayList<>();
            List<ProviderModel> rent     = new ArrayList<>();
            List<ProviderModel> buy      = new ArrayList<>();

            for (MotnStreamingOption opt : options) {
                ProviderModel pm = toProviderModel(opt);
                if (pm == null) continue;
                switch (opt.getType()) {
                    case "subscription":
                    case "free":
                    case "addon":
                        flatrate.add(pm);
                        break;
                    case "rent":
                        rent.add(pm);
                        break;
                    case "buy":
                        buy.add(pm);
                        break;
                }
            }

            RegionProvidersModel rpm = new RegionProvidersModel();
            rpm.setFlatrate(flatrate);
            rpm.setRent(rent);
            rpm.setBuy(buy);
            result.put(region, rpm);
        }
        return result;
    }

    private static ProviderModel toProviderModel(MotnStreamingOption opt) {
        if (opt.getService() == null) return null;
        String name = opt.getService().getName();
        String logoUrl = null;
        if (opt.getService().getImageSet() != null) {
            logoUrl = opt.getService().getImageSet().getDarkThemeImage();
            if (logoUrl == null || logoUrl.isEmpty()) {
                logoUrl = opt.getService().getImageSet().getLightThemeImage();
            }
        }
        return ProviderModel.fromMotn(name, logoUrl);
    }

    public static String toJson(MotnShowResponse response) {
        return GSON.toJson(response);
    }

    public static MotnShowResponse fromJson(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            return GSON.fromJson(json, MotnShowResponse.class);
        } catch (Exception e) {
            return null;
        }
    }
}