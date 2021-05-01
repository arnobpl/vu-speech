package com.google.cloud.android.speech;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText eName;
    private EditText ePassword;
    private Button eLogin;
    private Button eRegister;


    private String inputName = "";
    private String inputPassword = "";
    private String resp = "";

    SharedPreferences sp;



    public static final String filename = "login";
    public static final String spusername = "username";
    public static final  String sppassword = "password";

    String isValid = "";
    String auth = "";
    private int counter = 5;
    String url_auth ="http://ec2-3-16-29-185.us-east-2.compute.amazonaws.com:8000/api/v1/accounts/api-token-auth/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        eName = findViewById(R.id.etUsername);
        ePassword = findViewById(R.id.etPassword);
        eLogin = findViewById(R.id.btnLogin);

        eRegister = findViewById(R.id.btnRegister);


        //SessionManager sessionManager = new SessionManager(LoginActivity.this);



        eLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String inputName = eName.getText().toString().trim();
                String inputPassword = ePassword.getText().toString().trim();

                validate(inputName, inputPassword);
                //Log.d("JSONOUT", isValid.toString());




            }



        });

        eRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void validate(final String name, final String password)
    {
        // if(name.equals(Username) && password.equals(Password)){

        //    return true;
        //}

        //return false;

        RequestQueue queue = Volley.newRequestQueue(this);




        StringRequest sr = new StringRequest(Request.Method.POST,url_auth, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("JSONPost", response.toString());


                resp = response.toString();
                update(resp);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("JSONPost", "Error: " + error);
                Toast.makeText(LoginActivity.this, "Username or Password wrong", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("username",name.toString());
                params.put("password",password.toString());
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }

            @Override
            public String getBodyContentType()
            {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

        };
        queue.add(sr);

        //return resp;
    }

    private void update(String resp)
    {
        Log.d("JSONOUT", resp.toString());

        auth = resp.substring(10, (resp.length()-2));



        Log.d("token", auth.toString());

        if(resp.startsWith("{\"token\":")) {
            Toast.makeText(LoginActivity.this, "Login successfull", Toast.LENGTH_SHORT).show();

            SessionManager sessionManager = new SessionManager(LoginActivity.this);

            sessionManager.createLoginSession(inputName, inputPassword, auth);
            Intent intent = new Intent(LoginActivity.this, TranscriptionActivity.class);
            intent.putExtra("token", auth );
            startActivity(intent);
        }
        else
            Toast.makeText(LoginActivity.this, "Not registered", Toast.LENGTH_SHORT).show();
    }
}