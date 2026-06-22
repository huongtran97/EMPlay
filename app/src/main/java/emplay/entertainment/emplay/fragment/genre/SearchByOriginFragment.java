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

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
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

    private static final String[] ORIGIN_CODES = {
        "KR", "JP", "CN", "FR", "GB", "US", "IN", "ES", "TH", "TR", "IT", "DE", "MX"
    };

    private static final Map<String, String> ORIGIN_GLYPHS = new HashMap<String, String>() {{
        put("KR", "한"); put("JP", "日"); put("CN", "中");
        put("IN", "भ"); put("TH", "ไ"); put("DE", "De"); put("MX", "Mx");
    }};

    private OriginGridAdapter adapter;
    private final List<OriginModel> allOrigins = new ArrayList<>();
    private boolean defaultTV;

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

        RecyclerView rv = view.findViewById(R.id.rvOriginGrid);
        TextInputEditText etSearch = view.findViewById(R.id.etOriginSearch);
        ImageView btnClear = view.findViewById(R.id.btnClearSearch);
        TextView tvNoResults = view.findViewById(R.id.tvNoResults);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        List<OriginModel> fallback = buildOriginList();
        allOrigins.addAll(fallback);

        adapter = new OriginGridAdapter(new ArrayList<>(fallback), origin -> {
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
                if (!response.isSuccessful() || response.body() == null) return;
                List<OriginModel> resolved = new ArrayList<>();
                for (CountryModel c : response.body()) {
                    if (c.getIso31661() == null || c.getEnglishName() == null) continue;
                    String code = c.getIso31661();
                    String glyph = ORIGIN_GLYPHS.containsKey(code) ? ORIGIN_GLYPHS.get(code) : code.substring(0, 1);
                    resolved.add(new OriginModel(code, c.getEnglishName(), glyph));
                    if ("JP".equals(code)) resolved.add(new OriginModel("JP", "Anime", "ア"));
                }
                allOrigins.clear();
                allOrigins.addAll(resolved);
                if (adapter != null) adapter.updateAll(resolved);
            }

            @Override
            public void onFailure(@NonNull Call<List<CountryModel>> call, @NonNull Throwable t) {
                Log.e("SearchByOriginFragment", "Failed to fetch country list", t);
            }
        });
    }

    private List<OriginModel> buildOriginList() {
        List<OriginModel> list = new ArrayList<>();
        for (String code : ORIGIN_CODES) {
            String glyph = ORIGIN_GLYPHS.containsKey(code) ? ORIGIN_GLYPHS.get(code) : code.substring(0, 1);
            list.add(new OriginModel(code, code, glyph));
            if ("JP".equals(code)) list.add(new OriginModel("JP", "Anime", "ア"));
        }
        return list;
    }
}