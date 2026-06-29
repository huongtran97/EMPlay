package emplay.entertainment.emplay.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;

import java.util.List;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import androidx.credentials.exceptions.NoCredentialException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import emplay.entertainment.emplay.BuildConfig;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.auth.TMDBAuthApiService;
import emplay.entertainment.emplay.api.auth.model.TMDBRequestTokenResponse;
import emplay.entertainment.emplay.api.common.ApiClient;
import emplay.entertainment.emplay.api.common.MovieApiService;
import emplay.entertainment.emplay.api.common.TMDBpath;
import emplay.entertainment.emplay.api.movie.MovieResponse;
import emplay.entertainment.emplay.auth.AuthManager;
import emplay.entertainment.emplay.database.DatabaseHelper;
import emplay.entertainment.emplay.models.movie.MovieModel;
import jp.wasabeef.glide.transformations.BlurTransformation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseHelper dbHelper;
    private CredentialManager credentialManager;

    private static final int[] MOSAIC_VIEW_IDS = {
        R.id.poster_0, R.id.poster_1, R.id.poster_2, R.id.poster_3,
        R.id.poster_4, R.id.poster_5, R.id.poster_6, R.id.poster_7
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressBar = findViewById(R.id.progress_bar);
        mAuth = FirebaseAuth.getInstance();
        dbHelper = DatabaseHelper.getInstance(this);
        credentialManager = CredentialManager.create(this);

        loadPosterMosaic();

        findViewById(R.id.btn_google).setOnClickListener(v -> startGoogleSignIn());
        findViewById(R.id.btn_tmdb).setOnClickListener(v -> startTMDBSignIn());
        findViewById(R.id.tv_guest).setOnClickListener(v -> {
            AuthManager.getInstance(this).setGuest();
            navigateToMainActivity();
        });
        findViewById(R.id.tv_forgot_password).setOnClickListener(v ->
                new ForgotPasswordSheet().show(getSupportFragmentManager(), "forgot_password"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (AuthManager.getInstance(this).isLoggedIn()) {
            navigateToMainActivity();
        }
    }

    private void loadPosterMosaic() {
        MovieApiService apiService = ApiClient.getClient().create(MovieApiService.class);
        apiService.getTrendingMovies(TMDBpath.trendingMovies()).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                if (isFinishing() || !response.isSuccessful() || response.body() == null) return;
                List<MovieModel> results = response.body().getResults();
                if (results == null || results.isEmpty()) return;

                View mosaic = findViewById(R.id.poster_mosaic);
                if (mosaic != null) mosaic.setVisibility(View.VISIBLE);

                for (int i = 0; i < MOSAIC_VIEW_IDS.length && i < results.size(); i++) {
                    ImageView iv = findViewById(MOSAIC_VIEW_IDS[i]);
                    String path = results.get(i).getPosterPath();
                    if (iv == null || path == null) continue;
                    Glide.with(LoginActivity.this)
                        .load("https://image.tmdb.org/t/p/w342" + path)
                        .transform(new MultiTransformation<>(new CenterCrop(), new BlurTransformation(22, 3)))
                        .placeholder(android.R.color.transparent)
                        .into(iv);
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                // Static login_bg remains as fallback — no action needed
            }
        });
    }

    private void startGoogleSignIn() {
        progressBar.setVisibility(View.VISIBLE);

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .setAutoSelectEnabled(true)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(this, request, null, ContextCompat.getMainExecutor(this),
                new androidx.credentials.CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(@NonNull GetCredentialResponse result) {
                        handleSignInResult(result.getCredential());
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        if (e instanceof NoCredentialException) {
                            startGoogleSignInFallback();
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Log.e("LoginActivity", "Credential Manager Error: " + e.getMessage());
                            Toast.makeText(LoginActivity.this, "Sign-in failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void startGoogleSignInFallback() {
        GetSignInWithGoogleOption signInWithGoogleOption = new GetSignInWithGoogleOption
                .Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build();

        credentialManager.getCredentialAsync(this, request, null, ContextCompat.getMainExecutor(this),
                new androidx.credentials.CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(@NonNull GetCredentialResponse result) {
                        handleSignInResult(result.getCredential());
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        progressBar.setVisibility(View.GONE);
                        Log.e("LoginActivity", "Google Sign-In Error: " + e.getMessage());
                        Toast.makeText(LoginActivity.this, "Sign-in failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleSignInResult(Credential credential) {
        if (credential instanceof CustomCredential customCredential
                && GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())) {
            GoogleIdTokenCredential googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(customCredential.getData());
            firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
        } else {
            progressBar.setVisibility(View.GONE);
            Log.e("LoginActivity", "Unexpected credential type: " + credential.getType());
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String name = user.getDisplayName() != null ? user.getDisplayName() : "User";
                            String email = user.getEmail() != null ? user.getEmail() : "";
                            // Cache the profile locally
                            dbHelper.insertOrUpdateUser(name, email);

                            if (user.getMetadata() != null) {
                                long creationTime = user.getMetadata().getCreationTimestamp();
                                SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                                if (!prefs.contains("join_date")) {
                                    prefs.edit().putLong("join_date", creationTime).apply();
                                }
                            }

                            AuthManager.getInstance(this).setGoogle();
                            Toast.makeText(this, "Welcome, " + name + "!", Toast.LENGTH_SHORT).show();
                            navigateToMainActivity();
                        }
                    } else {
                        Toast.makeText(this, "Authentication failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void startTMDBSignIn() {
        progressBar.setVisibility(View.VISIBLE);
        TMDBAuthApiService authService = ApiClient.getClient().create(TMDBAuthApiService.class);
        authService.getRequestToken().enqueue(new Callback<TMDBRequestTokenResponse>() {
            @Override
            public void onResponse(@NonNull Call<TMDBRequestTokenResponse> call,
                                   @NonNull Response<TMDBRequestTokenResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    openTMDBAuthPage(response.body().requestToken);
                } else {
                    Log.e("LoginActivity", "Request token failed: " + response.code());
                    Toast.makeText(LoginActivity.this,
                            getString(R.string.tmdb_auth_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TMDBRequestTokenResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("LoginActivity", "Request token network error", t);
                Toast.makeText(LoginActivity.this,
                        getString(R.string.tmdb_auth_failed), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openTMDBAuthPage(String requestToken) {
        String redirectUri = "emplay://auth/tmdb";
        String authUrl = "https://www.themoviedb.org/authenticate/" + requestToken
                + "?redirect_to=" + Uri.encode(redirectUri);
        new CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
                .launchUrl(this, Uri.parse(authUrl));
    }

    private void navigateToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
