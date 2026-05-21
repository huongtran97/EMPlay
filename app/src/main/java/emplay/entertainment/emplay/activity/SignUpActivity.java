package emplay.entertainment.emplay.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import emplay.entertainment.emplay.R;

public class SignUpActivity extends AppCompatActivity {
    private TextView user_login;
    private TextInputEditText inputUsername, inputEmail, inputPassword;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_sign_up);

        mAuth = FirebaseAuth.getInstance();

        inputUsername = findViewById(R.id.username);
        inputEmail = findViewById(R.id.user_email);
        inputPassword = findViewById(R.id.password);
        progressBar = findViewById(R.id.progress_bar);
        Button sigUpBtn = findViewById(R.id.sign_up_btn);

        sigUpBtn.setOnClickListener(v -> {
            String email = inputEmail.getText() != null ? inputEmail.getText().toString() : "";
            String password = inputPassword.getText() != null ? inputPassword.getText().toString() : "";
            String username = inputUsername.getText() != null ? inputUsername.getText().toString() : "";

            progressBar.setVisibility(View.VISIBLE);

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(SignUpActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(SignUpActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user != null) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(username)
                                        .build();

                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                Toast.makeText(SignUpActivity.this,
                                                        "Account created! Welcome, " + username,
                                                        Toast.LENGTH_SHORT).show();

                                                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                                intent.putExtra("username", username);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(SignUpActivity.this, "Sign up failed. Please use a valid email.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        user_login = findViewById(R.id.re_login);
        String text = "Login";
        SpannableString spannableString = new SpannableString(text);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent signUpPage = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(signUpPage);
            }
        };
        spannableString.setSpan(clickableSpan, 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        user_login.setText(spannableString);
        user_login.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
