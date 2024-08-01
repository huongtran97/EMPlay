package emplay.entertainment.emplay.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import emplay.entertainment.emplay.R;

public class LoginActivity extends AppCompatActivity {
    private EditText username;
    private EditText password;
    private Button loginBtn;
    private TextView signUp;

//Login button still not done yet.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_login);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginBtn = findViewById(R.id.login_btn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (username.getText().toString().equals("emily") && password.getText().toString().equals("1997")) {
                    Toast.makeText(LoginActivity.this, "Successful!", Toast.LENGTH_SHORT).show();
                    Intent logIn_yes = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(logIn_yes);

                } else {
                    Toast.makeText(LoginActivity.this, "Wrong password or username. Try again!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        signUp = findViewById(R.id.sign_up);
        String text = "Sign Up";
        SpannableString spannableString = new SpannableString(text);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                signUp = findViewById(R.id.sign_up);
                signUp.setOnClickListener(click -> {
                    Intent sign_up_page = new Intent(LoginActivity.this, SignUpActivity.class);
                    startActivity(sign_up_page);
                });

            }
        };
        spannableString.setSpan(clickableSpan,0,7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        signUp.setText(spannableString);
        signUp.setMovementMethod(LinkMovementMethod.getInstance());


    }

}
