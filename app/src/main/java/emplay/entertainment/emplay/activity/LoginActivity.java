package emplay.entertainment.emplay.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import emplay.entertainment.emplay.database.DatabaseHelper;

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
        findViewById(R.id.btn_guest_continue).setOnClickListener(v -> navigateToMainActivity());
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Skip the login screen if the user is already signed in.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
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

    private void navigateToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
