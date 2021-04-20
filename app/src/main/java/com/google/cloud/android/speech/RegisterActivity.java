package com.google.cloud.android.speech;

import android.content.Intent;
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

public class RegisterActivity extends AppCompatActivity {

    private EditText eFirstname;
    private EditText eLastname;
    private EditText eEmail;
    private EditText ename;
    private EditText ePassword1;
    private EditText ePassword2;
    private Button eRegister;

    private String Username = "Admin";
    private String Password = "12345678";
    String resp = "";

    boolean isValid = false;
    private int counter = 5;

    String url_auth = "http://stark.cse.buffalo.edu:8000/api/v1/accounts/signup/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ename = findViewById(R.id.etnewUsername);
        eEmail = findViewById(R.id.etEmail);
        eFirstname = findViewById(R.id.etfirstName);
        eLastname = findViewById(R.id.etlastName);
        ePassword1 = findViewById(R.id.etPassword1);
        ePassword2 = findViewById(R.id.etPassword2);
        eRegister = findViewById(R.id.btnRegisterNew);

        VolleyLog.DEBUG = true;

        eRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userName = ename.getText().toString();
                String inputPassword1 = ePassword1.getText().toString();
                String inputPassword2 = ePassword2.getText().toString();
                String userEmail = eEmail.getText().toString();
                String firstName = eFirstname.getText().toString();
                String lastName = eLastname.getText().toString();


                if(userName.isEmpty() || userEmail.isEmpty() || inputPassword1.isEmpty() || inputPassword2.isEmpty() || firstName.isEmpty() || lastName.isEmpty())
                {
                    Toast.makeText(RegisterActivity.this,
                            "Please enter all the details",
                            Toast.LENGTH_SHORT).show();
                } else {

                    validate(userName, inputPassword1, userEmail, firstName, lastName);




                    //Add connection to Next Page

                }
            }


        });
    }

    private String validate(final String name, final String pass, final String email, final String first, final String last)
    {


        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest sr = new StringRequest(Request.Method.POST,url_auth, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("JSONPost", response.toString());
                resp = response.toString();
                update();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("JSONPost", "Error: " + error);
                Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("username",name.toString());
                params.put("password",pass.toString());
                params.put("first_name",first.toString());
                params.put("last_name",last.toString());
                params.put("email", email.toString());
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                // params.put("Authorization", "Token ");
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }



        };
        queue.add(sr);

       /* if(password1.equals(password2)){

            return true;
        }*/

        return resp;
    }


    private void update()
    {
        Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        //intent.putExtra("token", auth );
        startActivity(intent);
    }



}