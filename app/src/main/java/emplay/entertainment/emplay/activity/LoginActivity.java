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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.database.DatabaseHelper;

public class LoginActivity extends AppCompatActivity {

    private EditText inputUsername;
    private EditText inputPassword;
    private Button loginBtn;
    private ProgressBar progressBar;
    private TextView signUp;
    private FirebaseAuth mAuth;
    private DatabaseHelper dbHelper;
    private AppCompatImageView passwordToggle;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_login);

        // Initialize Firebase Auth and DatabaseHelper
        mAuth = FirebaseAuth.getInstance();
        dbHelper = DatabaseHelper.getInstance(this);

        // Initialize views
        initViews();

        // Set login button click listener
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Set up sign up link
        setUpSignUpLink();

        // Set up password toggle click listener
        setUpPasswordToggle();

        TextView forgotPassword = findViewById(R.id.forgot_password);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgotPasswordDialog();
            }
        });

    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter your email to reset password");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = input.getText().toString();
                resetPassword(email);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void resetPassword(String email) {
        // Validate email format
        if (!isValidEmail(email)) {
            Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this,
                                    "Reset link sent to your email.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "Error sending reset email: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }


    private void setUpPasswordToggle() {
        passwordToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // Hide the password
                    inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    passwordToggle.setImageResource(R.drawable.baseline_visibility_off_24);
                } else {
                    // Show the password
                    inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    passwordToggle.setImageResource(R.drawable.baseline_visibility_24);
                }
                // Move the cursor to the end of the text
                inputPassword.setSelection(inputPassword.length());
                isPasswordVisible = !isPasswordVisible; // Toggle visibility state
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToMainActivity();
        }
    }

    private void initViews() {
        inputUsername = findViewById(R.id.username);
        inputPassword = findViewById(R.id.password);
        loginBtn = findViewById(R.id.login_btn);
        progressBar = findViewById(R.id.progress_bar);
        signUp = findViewById(R.id.sign_up);
        progressBar.setVisibility(View.GONE);
        passwordToggle = findViewById(R.id.password_toggle);
        progressBar.setVisibility(View.GONE);
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
        progressBar.setVisibility(View.VISIBLE);  // Show progress bar
        loginBtn.setEnabled(false);  // Disable login button during login
        Log.d("LoginActivity", "Attempting to sign in with email: " + email);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        loginBtn.setEnabled(true);

                        handleLoginResponse(task);
                    }
                });
    }

    private void handleLoginResponse(Task<AuthResult> task) {
        if (task.isSuccessful()) {
            Log.d("LoginActivity", "Sign in successful");
            FirebaseUser user = mAuth.getCurrentUser();
            String username = (user != null && user.getDisplayName() != null) ? user.getDisplayName() : "Unknown User";
            dbHelper.insertOrUpdateUser(username, user.getEmail());
            navigateToMainActivity();
        } else {
            Log.e("LoginActivity", "Sign in failed", task.getException());
            Toast.makeText(LoginActivity.this, "Login failed. Please check your email or password.", Toast.LENGTH_SHORT).show();
        }
    }


    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void setUpSignUpLink() {
        String text = "Sign Up";
        SpannableString spannableString = new SpannableString(text);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent signUpIntent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(signUpIntent);
            }
        };
        spannableString.setSpan(clickableSpan, 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        signUp.setText(spannableString);
        signUp.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
