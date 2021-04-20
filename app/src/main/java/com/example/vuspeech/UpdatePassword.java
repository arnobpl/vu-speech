package com.example.vuspeech;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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


public class UpdatePassword extends AppCompatActivity {


    private EditText password;
    private EditText password2;
    private Button updatePassword;

    private String password11;
    private String password22;

    String token = "";
    String resp = "";

    String url_auth ="http://stark.cse.buffalo.edu:8000/api/v1/accounts/password/update/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);

        password = findViewById(R.id.etUpdatePassword);
        password2 = findViewById(R.id.etUpdatePassword2);
        updatePassword = findViewById(R.id.btnPasswordUpdate);

        VolleyLog.DEBUG = true;

        Intent intent = getIntent();
        token = getIntent().getStringExtra("token");

        updatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                password11 = password.getText().toString();
                password22 = password2.getText().toString();

                if(password11.isEmpty() || password22.isEmpty())
                {
                    Toast.makeText(UpdatePassword.this,
                            "Please enter all the details",
                            Toast.LENGTH_SHORT).show();
                } else {


                            validate(password11, password22, token);

                }

            }
        });

    }


    private void validate(String pass1, String pass2, String tok)
    {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest sr = new StringRequest(Request.Method.PUT,url_auth, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("JSONPost", response.toString());
                resp = response.toString();
                update(tok);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("JSONPost", "Error: " + error);
                Toast.makeText(UpdatePassword.this,
                        "Wrong details provided",
                        Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders()
            {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization","Token "+tok);
                headers.put("Content-Type","application/x-www-form-urlencoded");
                //or try with this:
                //headers.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
                return headers;
            }
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("old_password", pass1.toString());
                params.put("new_password", pass2.toString());
                //params.put("token", "blah");
                return params;
            }




        };
        queue.add(sr);

        //return resp;
    }

    private void update(String token)
    {
        Toast.makeText(UpdatePassword.this,
                "Password updated successfully",
                Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(UpdatePassword.this, User_profile.class);
        intent.putExtra("token", token );
        startActivity(intent);
    }
}