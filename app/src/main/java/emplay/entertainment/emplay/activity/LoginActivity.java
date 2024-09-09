package emplay.entertainment.emplay.activity;

import android.content.Intent;
import android.os.Bundle;
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
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import emplay.entertainment.emplay.MainActivity;
import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.database.DatabaseHelper;

public class LoginActivity extends AppCompatActivity {

    private EditText inputUsername, inputPassword;
    private Button loginBtn;
    private ProgressBar progressBar;
    private TextView signUp;
    private FirebaseAuth mAuth;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_login);

        // Initialize Firebase Auth and DatabaseHelper
        mAuth = FirebaseAuth.getInstance();
        dbHelper = DatabaseHelper.getInstance(this);

        // Initialize views
        inputUsername = findViewById(R.id.username);
        inputPassword = findViewById(R.id.password);
        loginBtn = findViewById(R.id.login_btn);
        progressBar = findViewById(R.id.progress_bar);
        signUp = findViewById(R.id.sign_up);

        // Set login button click listener
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Set up sign up link
        setUpSignUpLink();
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

    private void loginUser() {
        String email = inputUsername.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        Log.d("LoginActivity", "Attempting to sign in with email: " + email);  // Add this line

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            Log.d("LoginActivity", "Sign in successful");  // Add this line
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String username = user.getDisplayName();
                                if (username == null) {
                                    username = "Unknown User";
                                }

                                dbHelper.insertOrUpdateUser(username, email);

                                Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                                navigateToMainActivity();
                            }
                        } else {
                            Log.e("LoginActivity", "Sign in failed", task.getException());  // Add this line
                            Toast.makeText(LoginActivity.this, "Authentication failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
