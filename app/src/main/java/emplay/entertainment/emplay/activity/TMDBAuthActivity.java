package emplay.entertainment.emplay.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.api.auth.TMDBAuthApiService;
import emplay.entertainment.emplay.api.auth.model.TMDBCreateSessionResponse;
import emplay.entertainment.emplay.api.auth.model.TMDBSessionRequest;
import emplay.entertainment.emplay.api.common.ApiClient;
import emplay.entertainment.emplay.auth.AuthManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Receives the deep-link redirect after the user approves the TMDB OAuth request.
 *
 * Deep link: emplay://auth/tmdb?request_token=XXX&approved=true
 *
 * The proxy's /api/tmdb/auth/create-session handles both the session exchange
 * and account fetch in one call, returning { session_id, account_id, username }.
 */
public class TMDBAuthActivity extends AppCompatActivity {

    private static final String TAG = "TmdbAuthActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tmdb_auth);

        Uri data = getIntent().getData();
        if (data != null) {
            String requestToken = data.getQueryParameter("request_token");
            String approved = data.getQueryParameter("approved");
            if ("true".equals(approved) && requestToken != null && !requestToken.isEmpty()) {
                createSession(requestToken);
                return;
            }
        }

        Toast.makeText(this, getString(R.string.tmdb_auth_cancelled), Toast.LENGTH_SHORT).show();
        navigateToLogin();
    }

    private void createSession(String requestToken) {
        ApiClient.getClient().create(TMDBAuthApiService.class)
                .createSession(new TMDBSessionRequest(requestToken))
                .enqueue(new Callback<TMDBCreateSessionResponse>() {
                    @Override
                    public void onResponse(Call<TMDBCreateSessionResponse> call,
                                           Response<TMDBCreateSessionResponse> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().success) {
                            TMDBCreateSessionResponse data = response.body();
                            AuthManager.getInstance(TMDBAuthActivity.this)
                                    .setTMDB(String.valueOf(data.accountId),
                                            data.sessionId, data.username);
                            String display = (data.username != null && !data.username.isEmpty())
                                    ? data.username : "TMDB User";
                            Toast.makeText(TMDBAuthActivity.this,
                                    getString(R.string.welcome_tmdb, display),
                                    Toast.LENGTH_SHORT).show();
                            navigateToMain();
                        } else {
                            Log.e(TAG, "Session creation failed: " + response.code());
                            onAuthFailed();
                        }
                    }

                    @Override
                    public void onFailure(Call<TMDBCreateSessionResponse> call, Throwable t) {
                        Log.e(TAG, "Session creation network error", t);
                        onAuthFailed();
                    }
                });
    }

    private void onAuthFailed() {
        runOnUiThread(() -> {
            Toast.makeText(this, getString(R.string.tmdb_auth_failed), Toast.LENGTH_SHORT).show();
            navigateToLogin();
        });
    }

    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    private void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        finish();
    }
}