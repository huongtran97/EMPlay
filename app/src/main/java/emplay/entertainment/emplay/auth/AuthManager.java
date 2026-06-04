package emplay.entertainment.emplay.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Single source of truth for the current auth state.
 *
 * Priority on init: GOOGLE (Firebase) > TMDB (persisted session) > NONE.
 * Guest is ephemeral — it is never persisted and resets on the next launch.
 */
public class AuthManager {

    public enum AuthType { NONE, GUEST, GOOGLE, TMDB }

    private static final String TAG = "AuthManager";
    private static final String PREFS_NAME = "emplay_auth_prefs";
    private static final String KEY_TMDB_SESSION_ID = "tmdb_session_id";
    private static final String KEY_TMDB_ACCOUNT_ID = "tmdb_account_id";
    private static final String KEY_TMDB_USERNAME = "tmdb_username";

    private static AuthManager instance;
    private final SharedPreferences prefs;
    private AuthType authType;

    private AuthManager(Context context) {
        prefs = buildPrefs(context);
        loadState();
    }

    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context.getApplicationContext());
        }
        return instance;
    }

    private SharedPreferences buildPrefs(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            return EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "EncryptedSharedPreferences unavailable, using plain prefs", e);
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
    }

    private void loadState() {
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fbUser != null) {
            authType = AuthType.GOOGLE;
            return;
        }
        String sessionId = prefs.getString(KEY_TMDB_SESSION_ID, null);
        String accountId = prefs.getString(KEY_TMDB_ACCOUNT_ID, null);
        if (sessionId != null && accountId != null) {
            authType = AuthType.TMDB;
            return;
        }
        authType = AuthType.NONE;
    }

    public AuthType getAuthType() {
        return authType;
    }

    public boolean isLoggedIn() {
        return authType == AuthType.GOOGLE || authType == AuthType.TMDB;
    }

    public boolean isGuest() {
        return authType == AuthType.GUEST;
    }

    /** Firebase UID for Google users,
     *  TMDB account_id (as String) for TMDB users,
     *  null otherwise. */
    public String getUserId() {
        switch (authType) {
            case GOOGLE:
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                return user != null ? user.getUid() : null;
            case TMDB:
                return prefs.getString(KEY_TMDB_ACCOUNT_ID, null);
            default:
                return null;
        }
    }

    public String getTMDBSessionId() {
        return authType == AuthType.TMDB ? prefs.getString(KEY_TMDB_SESSION_ID, null) : null;
    }

    public String getTMDBAccountId() {
        return authType == AuthType.TMDB ? prefs.getString(KEY_TMDB_ACCOUNT_ID, null) : null;
    }

    public String getTMDBUsername() {
        return prefs.getString(KEY_TMDB_USERNAME, "");
    }

    public void setGuest() {
        authType = AuthType.GUEST;
    }

    public void setGoogle() {
        authType = AuthType.GOOGLE;
        clearTMDBSession();
    }

    public void setTMDB(String accountId, String sessionId, String username) {
        prefs.edit()
                .putString(KEY_TMDB_ACCOUNT_ID, accountId)
                .putString(KEY_TMDB_SESSION_ID, sessionId)
                .putString(KEY_TMDB_USERNAME, username != null ? username : "")
                .apply();
        authType = AuthType.TMDB;
    }

    public void signOut() {
        if (authType == AuthType.GOOGLE) {
            FirebaseAuth.getInstance().signOut();
        }
        clearTMDBSession();
        authType = AuthType.NONE;
    }

    private void clearTMDBSession() {
        prefs.edit()
                .remove(KEY_TMDB_SESSION_ID)
                .remove(KEY_TMDB_ACCOUNT_ID)
                .remove(KEY_TMDB_USERNAME)
                .apply();
    }
}