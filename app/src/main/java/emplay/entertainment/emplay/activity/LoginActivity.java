package emplay.entertainment.emplay.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.auth.TMDBAuthApiService;
import emplay.entertainment.emplay.api.auth.model.TMDBRequestTokenResponse;
import emplay.entertainment.emplay.api.common.ApiClient;
import emplay.entertainment.emplay.auth.AuthManager;
import emplay.entertainment.emplay.database.DatabaseHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private DatabaseHelper dbHelper;
    private CredentialManager credentialManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_login);

        mAuth = FirebaseAuth.getInstance();
        dbHelper = DatabaseHelper.getInstance(this);
        progressBar = findViewById(R.id.progress_bar);
        credentialManager = CredentialManager.create(this);

        findViewById(R.id.btn_google_sign_in).setOnClickListener(v -> startGoogleSignIn());
        findViewById(R.id.btn_tmdb_sign_in).setOnClickListener(v -> startTmdbSignIn());
        findViewById(R.id.btn_guest_continue).setOnClickListener(v -> {
            AuthManager.getInstance(this).setGuest();
            navigateToMainActivity();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (AuthManager.getInstance(this).isLoggedIn()) {
            navigateToMainActivity();
        }
    }

    private void startGoogleSignIn() {
        progressBar.setVisibility(View.VISIBLE);

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.google_web_client_id))
                .setAutoSelectEnabled(true)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        // Use the new CredentialManager to show the sign-in bottom sheet
        credentialManager.getCredentialAsync(this, request, null, ContextCompat.getMainExecutor(this),
                new androidx.credentials.CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(@NonNull GetCredentialResponse result) {
                        handleSignInResult(result.getCredential());
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        progressBar.setVisibility(View.GONE);
                        Log.e("LoginActivity", "Credential Manager Error: " + e.getMessage());
                        Toast.makeText(LoginActivity.this, "Sign-in failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleSignInResult(Credential credential) {
        if (credential instanceof CustomCredential
                && GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())) {
            CustomCredential customCredential = (CustomCredential) credential;
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

                            Toast.makeText(this, "Welcome, " + name + "!", Toast.LENGTH_SHORT).show();
                            navigateToMainActivity();
                        }
                    } else {
                        Toast.makeText(this, "Authentication failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startTmdbSignIn() {
        progressBar.setVisibility(View.VISIBLE);
        TMDBAuthApiService authService = ApiClient.getClient().create(TMDBAuthApiService.class);
        authService.getRequestToken()
                .enqueue(new Callback<TMDBRequestTokenResponse>() {
                    @Override
                    public void onResponse(Call<TMDBRequestTokenResponse> call,
                                           Response<TMDBRequestTokenResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().success) {
                            openTmdbAuthPage(response.body().requestToken);
                        } else {
                            Log.e("LoginActivity", "Request token failed: " + response.code());
                            Toast.makeText(LoginActivity.this,
                                    getString(R.string.tmdb_auth_failed), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<TMDBRequestTokenResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Log.e("LoginActivity", "Request token network error", t);
                        Toast.makeText(LoginActivity.this,
                                getString(R.string.tmdb_auth_failed), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openTmdbAuthPage(String requestToken) {
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
