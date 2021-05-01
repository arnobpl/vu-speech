package com.google.cloud.android.speech;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.HashMap;

public class WelcomeActivity extends AppCompatActivity {

    private TextView welcomeMessage;
    private TextView message1;
    private TextView message2;
    private TextView registerMessage;
    private Button welcomeLogin;

    String token = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        SessionManager sessionManager = new SessionManager(WelcomeActivity.this);
        HashMap<String, String> userDetails = sessionManager.getUserDetailsFromSession();

        if (userDetails.get(SessionManager.IS_LOGGEDIN) != null && userDetails.get(SessionManager.IS_LOGGEDIN).equals("true")) {
            Intent intent = new Intent(WelcomeActivity.this, TranscriptionActivity.class);
            token = userDetails.get(SessionManager.KEY_TOKEN);
            intent.putExtra("token", token);
            startActivity(intent);
        } else {

            welcomeMessage = findViewById(R.id.tvWelcome);
            message1 = findViewById(R.id.tvMessage1);
            message2 = findViewById(R.id.tvMessage2);
            registerMessage = findViewById(R.id.tvRegisterWelcome);
            welcomeLogin = findViewById(R.id.btnLoginWelocme);

            welcomeLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            });

            registerMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
                    startActivity(intent);
                }
            });
        }
    }


}