package emplay.entertainment.emplay.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import emplay.entertainment.emplay.R;

public class SignUpActivity extends AppCompatActivity {
    private TextView user_login;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_sign_up);

        user_login = findViewById(R.id.re_login);
        String text = "Login";
        SpannableString spannableString = new SpannableString(text);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                user_login = findViewById(R.id.re_login);
                user_login.setOnClickListener(click -> {
                    Intent sign_up_page = new Intent(SignUpActivity.this, LoginActivity.class);
                    startActivity(sign_up_page);
                });

            }
        };
        spannableString.setSpan(clickableSpan,0,5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        user_login.setText(spannableString);
        user_login.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
