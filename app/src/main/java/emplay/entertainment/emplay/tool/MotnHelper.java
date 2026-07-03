package emplay.entertainment.emplay.tool;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import emplay.entertainment.emplay.api.motn.MotnServiceInfo;
import emplay.entertainment.emplay.api.motn.MotnShowResponse;
import emplay.entertainment.emplay.api.motn.MotnStreamingOption;
import emplay.entertainment.emplay.models.common.ProviderModel;

public class MotnHelper {

    private static final Gson GSON = new Gson();

    public static final String SHOW_TYPE_MOVIE = "movie";
    public static final String SHOW_TYPE_TV    = "tv";

    /** 7 days — stays well within the 500 req/month limit. */
    public static final long CACHE_TTL_MS = 7L * 24 * 60 * 60 * 1000;

    /**
     * Find the direct deep-link for a specific provider in the given region.
     *
     * When tmdbProviderId is known, we first try a precise match against MOTN's service "id"
     * slug (e.g. "netflix", "prime") before falling back to fuzzy name matching.
     * MOTN uses lowercase country codes ("us", "ca") while the app uses uppercase ("US", "CA"),
     * so we lowercase the region before lookup.
     */
    public static String findLink(MotnShowResponse response, String region, String providerName, int tmdbProviderId) {
        if (response == null || response.getStreamingOptions() == null) return null;
        String target = normalize(providerName);
        String motnSlug = TMDB_TO_MOTN_SLUG.get(tmdbProviderId);

        Map<String, List<MotnStreamingOption>> all = response.getStreamingOptions();
        String primaryKey = region.toLowerCase(Locale.ROOT);

        String link = findLinkInList(all.get(primaryKey), target, motnSlug);
        if (link != null) return link;

        for (Map.Entry<String, List<MotnStreamingOption>> entry : all.entrySet()) {
            if (entry.getKey().equals(primaryKey)) continue;
            link = findLinkInList(entry.getValue(), target, motnSlug);
            if (link != null) return link;
        }
        return null;
    }

    /** Overload for callers that don't have a TMDB provider ID. */
    public static String findLink(MotnShowResponse response, String region, String providerName) {
        return findLink(response, region, providerName, -1);
    }

    /**
     * When no movie-specific deep link exists, return the matching service's homepage
     * (e.g. "https://www.netflix.com") so the user lands on the right app/site.
     */
    public static String findServiceHomePage(MotnShowResponse response, String providerName, int tmdbProviderId) {
        if (response == null || response.getStreamingOptions() == null) return null;
        String target = normalize(providerName);
        String motnSlug = TMDB_TO_MOTN_SLUG.get(tmdbProviderId);

        // Pass 1: precise ID match across all regions
        if (motnSlug != null) {
            for (List<MotnStreamingOption> options : response.getStreamingOptions().values()) {
                if (options == null) continue;
                for (MotnStreamingOption opt : options) {
                    if (matchesById(opt.getService(), motnSlug) && opt.getService().getHomePage() != null)
                        return opt.getService().getHomePage();
                    if (matchesById(opt.getAddon(), motnSlug) && opt.getAddon().getHomePage() != null)
                        return opt.getAddon().getHomePage();
                }
            }
        }
        // Pass 2: fuzzy name match
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

    /** Overload for callers that don't have a TMDB provider ID. */
    public static String findServiceHomePage(MotnShowResponse response, String providerName) {
        return findServiceHomePage(response, providerName, -1);
    }

    private static String findLinkInList(List<MotnStreamingOption> options, String target, String motnSlug) {
        if (options == null) return null;
        // Pass 1: precise MOTN service ID match
        if (motnSlug != null) {
            for (MotnStreamingOption opt : options) {
                if (opt.getLink() == null) continue;
                if (matchesById(opt.getService(), motnSlug) || matchesById(opt.getAddon(), motnSlug))
                    return opt.getLink();
            }
        }
        // Pass 2: fuzzy name match
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

    /**
     * Marks each provider's hasDeepLink flag AND filters the list in a single pass.
     *
     * Three cases:
     *  - motn == null (not yet cached): return all providers with hasDeepLink=true — we don't
     *    know their status yet, so we stay optimistic and show the chevron.
     *  - motn present, some providers match: return only the matched providers, each with
     *    hasDeepLink=true. Unmatched providers are removed from the list.
     *  - motn present, nothing matches at all (title not indexed by MOTN): reset all to
     *    hasDeepLink=true and return the full list — "no MOTN data" is not the same as
     *    "no link", so we must not show "Link unavailable" here.
     */
    public static List<ProviderModel> filterByMotn(List<ProviderModel> providers, MotnShowResponse motn, String region) {
        if (motn == null || providers == null || providers.isEmpty()) return providers;
        List<ProviderModel> filtered = new ArrayList<>();
        for (ProviderModel p : providers) {
            boolean hasLink = findLink(motn, region, p.getProviderName(), p.getProviderId()) != null
                    || findServiceHomePage(motn, p.getProviderName(), p.getProviderId()) != null;
            p.setHasDeepLink(hasLink);
            if (hasLink) filtered.add(p);
        }
        if (filtered.isEmpty()) {
            for (ProviderModel p : providers) p.setHasDeepLink(true);
            return providers;
        }
        return filtered;
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

    /**
     * TMDB provider ID → MOTN service slug (the "id" field in MotnServiceInfo).
     * Enables precise ID-based matching instead of fuzzy name matching.
     * Add entries here as new streaming services are confirmed.
     */
    private static final Map<Integer, String> TMDB_TO_MOTN_SLUG = new HashMap<>();
    static {
        TMDB_TO_MOTN_SLUG.put(8,    "netflix");
        TMDB_TO_MOTN_SLUG.put(9,    "prime");       // Amazon Prime Video (subscription)
        TMDB_TO_MOTN_SLUG.put(10,   "prime");        // Amazon Video (rent/buy)
        TMDB_TO_MOTN_SLUG.put(337,  "disney");
        TMDB_TO_MOTN_SLUG.put(15,   "hulu");
        TMDB_TO_MOTN_SLUG.put(350,  "apple");        // Apple TV+
        TMDB_TO_MOTN_SLUG.put(2,    "apple");        // Apple TV / iTunes
        TMDB_TO_MOTN_SLUG.put(1899, "max");          // Max (formerly HBO Max)
        TMDB_TO_MOTN_SLUG.put(386,  "peacock");
        TMDB_TO_MOTN_SLUG.put(531,  "paramount");
        TMDB_TO_MOTN_SLUG.put(283,  "crunchyroll");
        TMDB_TO_MOTN_SLUG.put(1715, "mubi");
        TMDB_TO_MOTN_SLUG.put(73,   "tubi");
        TMDB_TO_MOTN_SLUG.put(300,  "pluto");
        TMDB_TO_MOTN_SLUG.put(43,   "starz");
        TMDB_TO_MOTN_SLUG.put(123,  "showtime");
        TMDB_TO_MOTN_SLUG.put(151,  "britbox");
        TMDB_TO_MOTN_SLUG.put(526,  "amc");
        TMDB_TO_MOTN_SLUG.put(100,  "shudder");
        TMDB_TO_MOTN_SLUG.put(584,  "curiosity");
        TMDB_TO_MOTN_SLUG.put(268,  "mgm");          // MGM+ (formerly Epix)
        TMDB_TO_MOTN_SLUG.put(510,  "discovery");    // discovery+
        TMDB_TO_MOTN_SLUG.put(7,    "fandangonow");  // Vudu (rebranded to Fandango at Home)
        TMDB_TO_MOTN_SLUG.put(37,   "fandangonow");  // Fandango at Home
        TMDB_TO_MOTN_SLUG.put(188,  "youtube");      // YouTube
        TMDB_TO_MOTN_SLUG.put(192,  "youtube");      // YouTube Free
        TMDB_TO_MOTN_SLUG.put(257,  "fubo");         // FuboTV
        TMDB_TO_MOTN_SLUG.put(426,  "philo");
        TMDB_TO_MOTN_SLUG.put(538,  "plex");
        // Google Play Movies (3), YouTube TV (~227), and Spectrum On Demand (486) are not
        // tracked by MOTN: Google Play is purchase-only, the other two are cable/live-TV services.
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

    private static boolean matchesById(MotnServiceInfo info, String slug) {
        return info != null && slug != null && slug.equals(info.getId());
    }
}