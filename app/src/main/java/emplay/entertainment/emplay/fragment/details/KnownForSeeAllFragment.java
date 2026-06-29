package emplay.entertainment.emplay.fragment.details;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import emplay.entertainment.emplay.adapter.common.CreditAdapter;
import emplay.entertainment.emplay.api.common.ApiClient;
import emplay.entertainment.emplay.api.common.MovieApiService;
import emplay.entertainment.emplay.api.common.PersonCreditsResponse;
import emplay.entertainment.emplay.api.common.PersonCreditsResponse.CreditItem;
import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.databinding.ActivitySeeAllBinding;
import emplay.entertainment.emplay.fragment.common.BaseFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KnownForSeeAllFragment extends BaseFragment {

    private static final String ARG_PERSON_ID = "person_id";
    private static final String ARG_PERSON_NAME = "person_name";
    private static final int PAGE_SIZE = 18;

    private ActivitySeeAllBinding binding;
    private int personId;
    private String personName;
    private MovieApiService apiService;
    private final List<CreditItem> allCredits = new ArrayList<>();
    private CreditAdapter adapter;

    public static KnownForSeeAllFragment newInstance(int personId, String personName) {
        KnownForSeeAllFragment fragment = new KnownForSeeAllFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PERSON_ID, personId);
        args.putString(ARG_PERSON_NAME, personName != null ? personName : "");
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            personId = getArguments().getInt(ARG_PERSON_ID);
            personName = getArguments().getString(ARG_PERSON_NAME, "");
        }
        apiService = ApiClient.getClient().create(MovieApiService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ActivitySeeAllBinding.inflate(inflater, container, false);

//        binding.tvEyebrow.setText("KNOWN FOR");
        binding.tvSectionTitle.setText(personName);
        binding.tvSubtitle.setVisibility(View.GONE);
        binding.tvLoadingMore.setVisibility(View.VISIBLE);

        adapter = new CreditAdapter(new ArrayList<>(), requireContext(), this::onCreditClicked);
        binding.rvAllItems.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.rvAllItems.setAdapter(adapter);

        binding.rvAllItems.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && !recyclerView.canScrollVertically(1)) {
                    loadNextPage();
                }
            }
        });

        fetchCredits();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void fetchCredits() {
        safeEnqueue(apiService.getPersonCredits(TMDBpath.personCredits(personId)),
                new Callback<PersonCreditsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<PersonCreditsResponse> call,
                                           @NonNull Response<PersonCreditsResponse> response) {
                        if (binding == null) return;
                        binding.tvLoadingMore.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            List<CreditItem> cast = response.body().getCast();
                            if (cast != null) {
                                allCredits.addAll(cast);
                                Collections.sort(allCredits,
                                        (a, b) -> Double.compare(b.getPopularity(), a.getPopularity()));
                            }
                            binding.tvSubtitle.setText(allCredits.size() + " titles");
                            binding.tvSubtitle.setVisibility(View.VISIBLE);
                            loadNextPage();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<PersonCreditsResponse> call, @NonNull Throwable t) {
                        if (binding != null) binding.tvLoadingMore.setVisibility(View.GONE);
                        Log.e("KnownForSeeAll", "Failed to fetch credits", t);
                    }
                });
    }

    private void loadNextPage() {
        if (binding == null) return;
        int from = adapter.getItemCount();
        int to = Math.min(from + PAGE_SIZE, allCredits.size());
        if (from >= to) {
            binding.tvLoadingMore.setVisibility(View.GONE);
            return;
        }
        adapter.appendData(allCredits.subList(from, to));
        binding.tvLoadingMore.setVisibility(adapter.getItemCount() < allCredits.size() ? View.VISIBLE : View.GONE);
    }

    private void onCreditClicked(CreditItem item, View sharedElement) {
        Fragment fragment;
        if ("movie".equals(item.getMediaType())) {
            fragment = MovieResultDetailsFragment.newInstance(item.getId());
        } else if ("tv".equals(item.getMediaType())) {
            fragment = TVShowResultDetailsFragment.newInstance(item.getId());
        } else {
            return;
        }
        navigateTo(fragment, sharedElement, "poster_transition");
    }
}