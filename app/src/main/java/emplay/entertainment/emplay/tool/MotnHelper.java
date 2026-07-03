package emplay.entertainment.emplay.tool;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import emplay.entertainment.emplay.api.motn.MotnServiceInfo;
import emplay.entertainment.emplay.api.motn.MotnShowResponse;
import emplay.entertainment.emplay.api.motn.MotnStreamingOption;

public class MotnHelper {

    private static final Gson GSON = new Gson();

    public static final String SHOW_TYPE_MOVIE = "movie";
    public static final String SHOW_TYPE_TV    = "tv";

    /** 7 days — stays well within the 500 req/month limit. */
    public static final long CACHE_TTL_MS = 7L * 24 * 60 * 60 * 1000;

    /**
     * Find the direct deep-link for a specific provider in the given region.
     *
     * MOTN uses lowercase country codes ("us", "ca") while the app uses uppercase ("US", "CA"),
     * so we lowercase the region before lookup. Name matching is fuzzy — we normalize both
     * sides to lowercase alphanumeric so "Disney Plus" matches "Disney+" and vice versa.
     */
    public static String findLink(MotnShowResponse response, String region, String providerName) {
        if (response == null || response.getStreamingOptions() == null) return null;
        String target = normalize(providerName);

        // Try the user's region first, then fall back to any region that has a matching provider.
        // Streaming deep-links (Netflix, Prime Video, etc.) are usable regardless of which
        // country entry they came from.
        Map<String, List<MotnStreamingOption>> all = response.getStreamingOptions();
        String primaryKey = region.toLowerCase(Locale.ROOT);

        String link = findLinkInList(all.get(primaryKey), target);
        if (link != null) return link;

        for (Map.Entry<String, List<MotnStreamingOption>> entry : all.entrySet()) {
            if (entry.getKey().equals(primaryKey)) continue;
            link = findLinkInList(entry.getValue(), target);
            if (link != null) return link;
        }
        return null;
    }

    /**
     * When no movie-specific deep link exists, return the matching service's homepage
     * (e.g. "https://www.netflix.com") so the user lands on the right app/site.
     */
    public static String findServiceHomePage(MotnShowResponse response, String providerName) {
        if (response == null || response.getStreamingOptions() == null) return null;
        String target = normalize(providerName);
        for (List<MotnStreamingOption> options : response.getStreamingOptions().values()) {
            if (options == null) continue;
            for (MotnStreamingOption opt : options) {
                if (matches(opt.getService(), target) && opt.getService().getHomePage() != null)
                    return opt.getService().getHomePage();
                if (matches(opt.getAddon(), target) && opt.getAddon().getHomePage() != null)
                    return opt.getAddon().getHomePage();
            }
        }
        return null;
    }

    private static String findLinkInList(List<MotnStreamingOption> options, String target) {
        if (options == null) return null;
        for (MotnStreamingOption opt : options) {
            if (opt.getLink() == null) continue;
            if (matches(opt.getService(), target)) return opt.getLink();
            if (matches(opt.getAddon(), target))   return opt.getLink();
        }
        return null;
    }

    private static boolean matches(MotnServiceInfo info, String target) {
        if (info == null || info.getName() == null) return false;
        String candidate = normalize(info.getName());
        return candidate.contains(target) || target.contains(candidate);
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

    // TMDB and MOTN use different names for the same service.
    // Key = normalized TMDB provider name, value = normalized MOTN service name.
    private static final Map<String, String> ALIASES = new HashMap<>();
    static {
        // Amazon: TMDB uses "Amazon Video" (rent/buy, ID 10) or "Amazon Prime Video" (sub, ID 9)
        // MOTN always uses "Prime Video"
        ALIASES.put("amazonvideo",      "primevideo");
        ALIASES.put("amazonprimevideo", "primevideo");

        // Apple: TMDB renamed provider ID 2 from "Apple iTunes" to "Apple TV Store";
        // MOTN calls the same service "Apple TV"
        ALIASES.put("appleitunes",      "appletv");
        ALIASES.put("appletvstore",     "appletv");
    }

    private static String normalize(String name) {
        if (name == null) return "";
        String n = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        return ALIASES.getOrDefault(n, n);
    }
}