package emplay.entertainment.emplay.fragment.details;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.CreditAdapter;
import emplay.entertainment.emplay.adapter.common.FilmographyAdapter;
import emplay.entertainment.emplay.tool.ReadHelper;
import emplay.entertainment.emplay.api.common.ApiClient;
import emplay.entertainment.emplay.api.common.ImageUrl;
import emplay.entertainment.emplay.api.common.MovieApiService;
import emplay.entertainment.emplay.api.common.PersonCreditsResponse;
import emplay.entertainment.emplay.api.common.PersonCreditsResponse.CreditItem;
import emplay.entertainment.emplay.api.common.PersonDetailsResponse;
import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.fragment.common.BaseFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CastDetailFragment extends BaseFragment {

    private static final String ARG_PERSON_ID = "PERSON_ID";
    private int personId;
    private String personName;
    private MovieApiService apiService;
    private ImageView profileImage;
    private TextView nameText, metaText, biographyText, creditsPageIndicator, readMoreText, ghostGlyphText;
    private TextView btnKnownForSeeAll, tvBackToTop;
    private RecyclerView creditsRecyclerView, filmographyRecyclerView;
    private CreditAdapter creditAdapter;
    private FilmographyAdapter filmographyAdapter;
    private List<CreditItem> allCredits = new ArrayList<>();

    public static CastDetailFragment newInstance(int personId) {
        CastDetailFragment fragment = new CastDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PERSON_ID, personId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            personId = getArguments().getInt(ARG_PERSON_ID);
        }
        apiService = ApiClient.getClient().create(MovieApiService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_cast, container, false);

        profileImage = view.findViewById(R.id.cast_detail_image);
        nameText = view.findViewById(R.id.cast_detail_name);
        metaText = view.findViewById(R.id.cast_detail_meta);
        biographyText = view.findViewById(R.id.cast_detail_biography);
        readMoreText = view.findViewById(R.id.read_more_text);
        ghostGlyphText = view.findViewById(R.id.tvGhostGlyph);
        creditsPageIndicator = view.findViewById(R.id.credits_page_indicator);

        creditsRecyclerView = view.findViewById(R.id.cast_detail_credits_recyclerview);
        filmographyRecyclerView = view.findViewById(R.id.rvFilmography);
        btnKnownForSeeAll = view.findViewById(R.id.btn_known_for_see_all);
        tvBackToTop = view.findViewById(R.id.tvBackToTop);
        tvBackToTop.setOnClickListener(v -> {
            androidx.core.widget.NestedScrollView sv = view.findViewById(R.id.castScrollView);
            sv.smoothScrollTo(0, 0);
        });

        btnKnownForSeeAll.setOnClickListener(v ->
                navigateTo(KnownForSeeAllFragment.newInstance(personId, personName)));

        fetchPersonDetails();
        fetchPersonCredits();

        return view;
    }

    private void fetchPersonDetails() {
        safeEnqueue(apiService.getPersonDetails(TMDBpath.personDetails(personId)), new Callback<PersonDetailsResponse>() {
            @Override
            public void onResponse(@NonNull Call<PersonDetailsResponse> call, @NonNull Response<PersonDetailsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PersonDetailsResponse person = response.body();
                    personName = person.getName();
                    nameText.setText(personName);
                    
                    if (person.getName() != null && !person.getName().isEmpty()) {
                        ghostGlyphText.setText(person.getName().substring(0, 1).toUpperCase());
                    }

                    String bio = person.getBiography();
                    if (bio != null && !bio.isEmpty()) {
                        biographyText.setText(bio);
                        ReadHelper.setup(biographyText, readMoreText, false, null);
                    }

                    String dept = person.getKnownForDepartment();
                    String birth = person.getBirthday();
                    String place = person.getPlaceOfBirth();
                    StringBuilder meta = new StringBuilder();
                    if (dept != null) meta.append(dept);
                    if (birth != null) meta.append(" · Born ").append(birth);
                    if (place != null) meta.append("\n").append(place);
                    metaText.setText(meta.toString());

                    Glide.with(requireContext())
                            .load(ImageUrl.of(ImageUrl.PROFILE, person.getProfilePath()))
                            .placeholder(R.drawable.bg_poster_placeholder)
                            .into(profileImage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PersonDetailsResponse> call, @NonNull Throwable t) {
                Log.e("CastDetailFragment", "Failed to fetch person details", t);
            }
        });
    }

    private void fetchPersonCredits() {
        safeEnqueue(apiService.getPersonCredits(TMDBpath.personCredits(personId)), new Callback<PersonCreditsResponse>() {
            @Override
            public void onResponse(@NonNull Call<PersonCreditsResponse> call, @NonNull Response<PersonCreditsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allCredits = response.body().getCast();
                    if (allCredits == null) allCredits = new ArrayList<>();

                    List<CreditItem> filmographyList = new ArrayList<>(allCredits);
                    Collections.sort(filmographyList, (a, b) -> {
                        String dateA = a.getDisplayDate();
                        String dateB = b.getDisplayDate();
                        if (dateA == null) return 1;
                        if (dateB == null) return -1;
                        return dateB.compareTo(dateA);
                    });

                    List<CreditItem> knownFor = new ArrayList<>(allCredits);
                    Collections.sort(knownFor, (a, b) -> Double.compare(b.getPopularity(), a.getPopularity()));
                    List<CreditItem> top10 = knownFor.subList(0, Math.min(5, knownFor.size()));

                    creditAdapter = new CreditAdapter(top10, requireContext(), (item, view) -> onCreditClicked(item, view));
                    creditsRecyclerView.setAdapter(creditAdapter);
                    creditsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

                    filmographyAdapter = new FilmographyAdapter(filmographyList, (item, view) -> onCreditClicked(item, view));
                    filmographyRecyclerView.setAdapter(filmographyAdapter);

                    btnKnownForSeeAll.setVisibility(View.VISIBLE);

                    int totalCount = allCredits.size();
                    creditsPageIndicator.setText(String.format(java.util.Locale.getDefault(), "%d titles ›", totalCount));
                    if (filmographyAdapter.hasMore()) {
                        creditsPageIndicator.setOnClickListener(v -> {
                            if (filmographyAdapter.isExpanded()) {
                                filmographyAdapter.collapse();
                                creditsPageIndicator.setText(String.format(java.util.Locale.getDefault(), "%d titles ›", totalCount));
                                tvBackToTop.setVisibility(View.GONE);
                            } else {
                                filmographyAdapter.showAll();
                                creditsPageIndicator.setText(R.string.see_less);
                                tvBackToTop.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PersonCreditsResponse> call, @NonNull Throwable t) {
                Log.e("CastDetailFragment", "Failed to fetch person credits", t);
            }
        });
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
