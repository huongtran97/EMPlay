package emplay.entertainment.emplay.fragment;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.CreditAdapter;
import emplay.entertainment.emplay.api.ApiClient;
import emplay.entertainment.emplay.api.MovieApiService;
import emplay.entertainment.emplay.api.PersonCreditsResponse;
import emplay.entertainment.emplay.api.PersonDetailsResponse;
import emplay.entertainment.emplay.api.TMDBpath;
import emplay.entertainment.emplay.tool.ReadHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *  Cast member detail screen: photo, biography (with Read More/Less), and a paginated
 *  grid of movies/TV shows the person has appeared in.
 */
public class CastDetailFragment extends BaseFragment {

    private static final String ARG_PERSON_ID = "PERSON_ID";
    private static final int PAGE_SIZE = 9; // 3-column grid × 3 rows per page
    private int personId;
    private MovieApiService apiService;
    private ImageView profileImage;
    private TextView nameText, departmentText, birthdayText, placeOfBirthText, biographyText, creditsPageIndicator, readMoreText;
    private RecyclerView creditsRecyclerView;
    private LinearLayout creditsPaginationBar;
    private ImageButton creditsBtnPrev, creditsBtnNext;
    private CreditAdapter creditAdapter;
    private final List<PersonCreditsResponse.CreditItem> allCredits = new ArrayList<>();
    private int creditsPage = 1;
    private boolean isExpanded = false;

    public static CastDetailFragment newInstance(int personId) {
        CastDetailFragment fragment = new CastDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PERSON_ID, personId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cast_details_fragment, container, false);

        profileImage = view.findViewById(R.id.cast_detail_image);
        nameText = view.findViewById(R.id.cast_detail_name);
        departmentText = view.findViewById(R.id.cast_detail_department);
        birthdayText = view.findViewById(R.id.cast_detail_birthday);
        placeOfBirthText = view.findViewById(R.id.cast_detail_place);
        biographyText = view.findViewById(R.id.cast_detail_biography);
        readMoreText = view.findViewById(R.id.read_more_text);
        creditsRecyclerView = view.findViewById(R.id.cast_detail_credits_recyclerview);
        creditsPaginationBar = view.findViewById(R.id.credits_pagination_bar);
        creditsPageIndicator = view.findViewById(R.id.credits_page_indicator);
        creditsBtnPrev = view.findViewById(R.id.credits_btn_prev);
        creditsBtnNext = view.findViewById(R.id.credits_btn_next);

        // Read More / Less
        ReadHelper.setup(biographyText, readMoreText, isExpanded, expanded -> isExpanded = expanded);

        creditAdapter = new CreditAdapter(new ArrayList<>(), getContext(), this::onCreditClicked);
        creditsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        creditsRecyclerView.setAdapter(creditAdapter);
        creditsRecyclerView.setNestedScrollingEnabled(false);

        creditsBtnPrev.setOnClickListener(v -> {
            creditsPage--;
            showCreditsPage();
            creditsRecyclerView.scrollToPosition(0);
        });
        creditsBtnNext.setOnClickListener(v -> {
            creditsPage++;
            showCreditsPage();
            creditsRecyclerView.scrollToPosition(0);
        });

        apiService = ApiClient.getClient().create(MovieApiService.class);

        if (getArguments() != null) {
            personId = getArguments().getInt(ARG_PERSON_ID, -1);
            if (personId != -1) {
                fetchPersonDetails();
                fetchPersonCredits();
            }
        }

        return view;
    }

    private void fetchPersonDetails() {
        safeEnqueue(apiService.getPersonDetails(TMDBpath.personDetails(personId)), new Callback<PersonDetailsResponse>() {
            @Override
            public void onResponse(@NonNull Call<PersonDetailsResponse> call, @NonNull Response<PersonDetailsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PersonDetailsResponse person = response.body();

                    nameText.setText(person.getName());
                    departmentText.setText(person.getKnownForDepartment() != null
                            ? person.getKnownForDepartment() : "");
                    birthdayText.setText(person.getBirthday() != null
                            ? "Born: " + person.getBirthday() : "");
                    placeOfBirthText.setText(person.getPlaceOfBirth() != null
                            ? person.getPlaceOfBirth() : "");
                    String bio = person.getBiography() != null && !person.getBiography().isEmpty()
                            ? person.getBiography() : "No biography available.";
                    biographyText.setText(bio);

                    ReadHelper.bind(biographyText, readMoreText, isExpanded);

                    if (person.getProfilePath() != null) {
                        Glide.with(CastDetailFragment.this)
                                .load("https://image.tmdb.org/t/p/w500" + person.getProfilePath())
                                .apply(new RequestOptions().circleCrop())
                                .placeholder(R.drawable.avatar)
                                .into(profileImage);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PersonDetailsResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Failed to load person details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPersonCredits() {
        safeEnqueue(apiService.getPersonCredits(TMDBpath.personCredits(personId)), new Callback<PersonCreditsResponse>() {
            @Override
            public void onResponse(@NonNull Call<PersonCreditsResponse> call, @NonNull Response<PersonCreditsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PersonCreditsResponse.CreditItem> cast = response.body().getCast();
                    List<PersonCreditsResponse.CreditItem> crew = response.body().getCrew();

                    allCredits.clear();
                    java.util.Set<Integer> seenIds = new java.util.HashSet<>();

                    if (cast != null) {
                        for (PersonCreditsResponse.CreditItem item : cast) {
                            boolean hasImage = (item.getPosterPath() != null && !item.getPosterPath().isEmpty())
                                    || (item.getBackdropPath() != null && !item.getBackdropPath().isEmpty());
                            if (hasImage && !seenIds.contains(item.getId())) {
                                allCredits.add(item);
                                seenIds.add(item.getId());
                            }
                        }
                    }

                    if (crew != null) {
                        for (PersonCreditsResponse.CreditItem item : crew) {
                            boolean hasImage = (item.getPosterPath() != null && !item.getPosterPath().isEmpty())
                                    || (item.getBackdropPath() != null && !item.getBackdropPath().isEmpty());
                            if (hasImage && !seenIds.contains(item.getId())) {
                                allCredits.add(item);
                                seenIds.add(item.getId());
                            }
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        allCredits.sort((a, b) -> Double.compare(b.getPopularity(), a.getPopularity()));
                    }
                    creditsPage = 1;
                    showCreditsPage();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PersonCreditsResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Failed to load credits", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void showCreditsPage() {
        int total = allCredits.size();
        int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);
        int fromIndex = (creditsPage - 1) * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, total);

        creditAdapter.updateData(new ArrayList<>(allCredits.subList(fromIndex, toIndex)));

        if (totalPages > 1) {
            creditsPaginationBar.setVisibility(View.VISIBLE);
            creditsPageIndicator.setText("Page " + creditsPage + " of " + totalPages);
            creditsBtnPrev.setEnabled(creditsPage > 1);
            creditsBtnNext.setEnabled(creditsPage < totalPages);
        } else {
            creditsPaginationBar.setVisibility(View.GONE);
        }
    }

    private void onCreditClicked(PersonCreditsResponse.CreditItem credit) {
        Fragment fragment;
        if ("tv".equals(credit.getMediaType())) {
            fragment = ShowResultTVShowDetailsFragment.newInstance(credit.getId());
        } else {
            fragment = ShowResultDetailsFragment.newInstance(credit.getId());
        }
        navigateTo(fragment);
    }
}