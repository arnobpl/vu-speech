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

public class UpdateProfile extends AppCompatActivity {

    private EditText username;
    private EditText firstName;
    private EditText lastName;
    private EditText password;
    private EditText email;

    private Button updatebtn;
    private Button updatepass;

    private String newUsername;
    private String newFirstname;
    private String newLastname;
    private String newEmail;

    String token = "";

    String url_auth ="http://stark.cse.buffalo.edu:8000/api/v1/accounts/profile/update/";

    String resp = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        Intent intent = getIntent();
        token = getIntent().getStringExtra("token");

        VolleyLog.DEBUG = true;

        username = findViewById(R.id.etUsernameEdit);
        firstName = findViewById(R.id.etFirstNameEdit);
        lastName = findViewById(R.id.etLastNameEdit);
        email = findViewById(R.id.etEmailEdit);
        updatebtn = findViewById(R.id.btnUpdateBtn);
        updatepass = findViewById(R.id.btnUpdatePassword);

        updatebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                newUsername = username.getText().toString();
                newFirstname = firstName.getText().toString();
                newLastname = lastName.getText().toString();
                newEmail = email.getText().toString();


                if(newUsername.isEmpty() || newEmail.isEmpty() || newFirstname.isEmpty() || newLastname.isEmpty() )
                {
                    Toast.makeText(UpdateProfile.this,
                            "Please enter all the details",
                            Toast.LENGTH_SHORT).show();
                } else {





                    validate(token, newUsername, newFirstname, newLastname, newEmail);
                }
            }
        });

        updatepass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(UpdateProfile.this, UpdatePassword.class);
                intent.putExtra("token", token );
                startActivity(intent);
            }
        });





    }

    private String validate(String token, String username, String firstname, String lastname, String email)
    {
        // if(name.equals(Username) && password.equals(Password)){

        //    return true;
        //}

        //return false;

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest sr = new StringRequest(Request.Method.PUT,url_auth, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("JSONPost", response.toString());
                resp = response.toString();
                update(token);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("JSONPost", "Error: " + error);
                Toast.makeText(UpdateProfile.this,
                        "Couldnt update the details",
                        Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("username",username.toString());
                params.put("first_name",firstname.toString());
                params.put("last_name",lastname.toString());
                params.put("email",email.toString());
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Authorization", "Token "+token);
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }



        };
        queue.add(sr);

        return resp;
    }

    private void update(String tok)
    {
        Toast.makeText(UpdateProfile.this,
                "Details updated successfully",
                Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(UpdateProfile.this, User_profile.class);
        intent.putExtra("token", tok );
        startActivity(intent);
    }


}