package emplay.entertainment.emplay.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.database.DatabaseHelper;

public class LoginActivity extends AppCompatActivity {

    private EditText inputUsername;
    private EditText inputPassword;
    private Button loginBtn;
    private ProgressBar progressBar;
    private TextView signUp;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private DatabaseHelper dbHelper;
    private AppCompatImageView passwordToggle;
    private boolean isPasswordVisible = false;

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account.getIdToken());
                } catch (ApiException e) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Sign-in cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_login);

        mAuth = FirebaseAuth.getInstance();
        dbHelper = DatabaseHelper.getInstance(this);

        // Initialize views
        initViews();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Set up sign up link
        setUpSignUpLink();

        // Set up password toggle click listener
        setUpPasswordToggle();

        TextView forgotPassword = findViewById(R.id.forgot_password);
        forgotPassword.setOnClickListener(v -> showForgotPasswordDialog());

        findViewById(R.id.btn_google_sign_in).setOnClickListener(v -> startGoogleSignIn());
        findViewById(R.id.btn_guest_continue).setOnClickListener(v -> navigateToMainActivity());
        
        loginBtn.setOnClickListener(v -> loginUser());
    }

    private void initViews() {
        inputUsername = findViewById(R.id.username);
        inputPassword = findViewById(R.id.password);
        loginBtn = findViewById(R.id.login_btn);
        progressBar = findViewById(R.id.progress_bar);
        signUp = findViewById(R.id.sign_up);
        passwordToggle = findViewById(R.id.password_toggle);
        
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setUpSignUpLink() {
        if (signUp == null) return;
        
        String text = "Don't have an account? Sign Up";
        SpannableString spannableString = new SpannableString(text);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        };
        
        int start = text.indexOf("Sign Up");
        if (start != -1) {
            spannableString.setSpan(clickableSpan, start, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        signUp.setText(spannableString);
        signUp.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void loginUser() {
        String email = inputUsername.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (validateInput(email, password)) {
            performLogin(email, password);
        }
    }

    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void performLogin(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);
        loginBtn.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    loginBtn.setEnabled(true);
                    handleLoginResponse(task);
                });
    }

    private void handleLoginResponse(Task<AuthResult> task) {
        if (task.isSuccessful()) {
            FirebaseUser user = mAuth.getCurrentUser();
            String username = (user != null && user.getDisplayName() != null) ? user.getDisplayName() : "Unknown User";
            dbHelper.insertOrUpdateUser(username, user != null ? user.getEmail() : "");
            navigateToMainActivity();
        } else {
            Toast.makeText(LoginActivity.this, "Login failed. Please check your email or password.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter your email to reset password");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String email = input.getText().toString();
            resetPassword(email);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void resetPassword(String email) {
        if (!isValidEmail(email)) {
            Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Reset link sent to your email.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void setUpPasswordToggle() {
        if (passwordToggle == null) return;
        
        passwordToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordToggle.setImageResource(R.drawable.baseline_visibility_off_24);
            } else {
                inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passwordToggle.setImageResource(R.drawable.baseline_visibility_24);
            }
            inputPassword.setSelection(inputPassword.length());
            isPasswordVisible = !isPasswordVisible;
        });
    }

    private void startGoogleSignIn() {
        progressBar.setVisibility(View.VISIBLE);
        signInLauncher.launch(googleSignInClient.getSignInIntent());
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
                            dbHelper.insertOrUpdateUser(name, email);
                            Toast.makeText(this, "Welcome, " + name + "!", Toast.LENGTH_SHORT).show();
                            navigateToMainActivity();
                        }
                    } else {
                        Toast.makeText(this, "Authentication failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToMainActivity();
        }
    }

    private void navigateToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
