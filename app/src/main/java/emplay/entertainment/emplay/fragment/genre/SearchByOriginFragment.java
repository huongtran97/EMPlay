package emplay.entertainment.emplay.fragment.genre;

import android.os.Bundle;
import android.util.Log;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.OriginGridAdapter;
import emplay.entertainment.emplay.api.common.ApiClient;
import emplay.entertainment.emplay.api.common.MovieApiService;
import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.fragment.common.BaseFragment;
import emplay.entertainment.emplay.models.common.CountryModel;
import emplay.entertainment.emplay.models.common.OriginModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchByOriginFragment extends BaseFragment {

    private static final String ARG_DEFAULT_TV = "DEFAULT_TV";

    private static final Map<String, String> ORIGIN_GLYPHS = new HashMap<String, String>() {{
        put("KR", "한"); put("JP", "日"); put("CN", "中");
        put("IN", "भ"); put("TH", "ไ"); put("DE", "De"); put("MX", "Mx");
    }};

    private static final List<OriginModel> FALLBACK_ORIGINS = Arrays.asList(
        new OriginModel("KR", "South Korea",   "한"),
        new OriginModel("JP", "Japan",          "日"),
        new OriginModel("JP", "Anime",          "ア"),
        new OriginModel("CN", "China",          "中"),
        new OriginModel("US", "United States",  "Us"),
        new OriginModel("GB", "United Kingdom", "Gb"),
        new OriginModel("FR", "France",         "Fr"),
        new OriginModel("IN", "India",          "भ"),
        new OriginModel("TH", "Thailand",       "ไ"),
        new OriginModel("TR", "Turkey",         "Tr"),
        new OriginModel("ES", "Spain",          "Es"),
        new OriginModel("IT", "Italy",          "It"),
        new OriginModel("DE", "Germany",        "De"),
        new OriginModel("MX", "Mexico",         "Mx")
    );

    private OriginGridAdapter adapter;
    private final List<OriginModel> allOrigins = new ArrayList<>();
    private boolean defaultTV;
    private ShimmerFrameLayout shimmer;
    private RecyclerView rv;

    public static SearchByOriginFragment newInstance(boolean defaultTV) {
        SearchByOriginFragment f = new SearchByOriginFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_DEFAULT_TV, defaultTV);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_by_origin, container, false);

        defaultTV = getArguments() != null && getArguments().getBoolean(ARG_DEFAULT_TV, false);

        shimmer = view.findViewById(R.id.shimmerOrigin);
        rv = view.findViewById(R.id.rvOriginGrid);
        TextInputEditText etSearch = view.findViewById(R.id.etOriginSearch);
        ImageView btnClear = view.findViewById(R.id.btnClearSearch);
        TextView tvNoResults = view.findViewById(R.id.tvNoResults);

        shimmer.startShimmer();

        adapter = new OriginGridAdapter(allOrigins, origin -> {
            boolean isAnime = "Anime".equals(origin.getName());
            String code = isAnime ? "JP" : origin.getCountryCode();
            navigateTo(OriginResultsFragment.newInstance(
                    code, origin.getName(), origin.getGlyph(), isAnime, isAnime || defaultTV));
        });

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                String q = s.toString();
                btnClear.setVisibility(q.isEmpty() ? View.GONE : View.VISIBLE);
                adapter.filter(q);
                tvNoResults.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
        });

        btnClear.setOnClickListener(v -> etSearch.setText(""));

        fetchOriginCountries();
        return view;
    }

    private void fetchOriginCountries() {
        MovieApiService api = ApiClient.getClient().create(MovieApiService.class);
        safeEnqueue(api.getCountries(TMDBpath.countries()), new Callback<List<CountryModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<CountryModel>> call,
                                   @NonNull Response<List<CountryModel>> response) {
                List<OriginModel> resolved = null;
                if (response.isSuccessful() && response.body() != null) {
                    resolved = new ArrayList<>();
                    for (CountryModel c : response.body()) {
                        if (c.getIso31661() == null || c.getEnglishName() == null) continue;
                        String code = c.getIso31661();
                        String glyph = ORIGIN_GLYPHS.containsKey(code)
                                ? ORIGIN_GLYPHS.get(code)
                                : code.substring(0, 1).toUpperCase();
                        resolved.add(new OriginModel(code, c.getEnglishName(), glyph));
                        if ("JP".equals(code)) resolved.add(new OriginModel("JP", "Anime", "ア"));
                    }
                }
                showOrigins(resolved);
            }

            @Override
            public void onFailure(@NonNull Call<List<CountryModel>> call, @NonNull Throwable t) {
                Log.e("SearchByOriginFragment", "Failed to fetch country list", t);
                showOrigins(null);
            }
        });
    }

    private void showOrigins(List<OriginModel> origins) {
        allOrigins.clear();
        allOrigins.addAll(origins != null && !origins.isEmpty() ? origins : FALLBACK_ORIGINS);
        if (adapter != null) adapter.updateAll(allOrigins);
        if (shimmer != null) {
            shimmer.stopShimmer();
            shimmer.setVisibility(View.GONE);
        }
        if (rv != null) rv.setVisibility(View.VISIBLE);
    }
}