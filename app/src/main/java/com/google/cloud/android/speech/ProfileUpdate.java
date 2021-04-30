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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProfileUpdate extends AppCompatActivity {

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

    String url_auth ="http://ec2-3-16-29-185.us-east-2.compute.amazonaws.com:8000/api/v1/accounts/profile/update/";

    String url_get = "http://stark.cse.buffalo.edu:8000/api/v1/accounts/profile/retrieve/";

    String resp = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_update);


        Intent intent = getIntent();
        token = getIntent().getStringExtra("token");

        VolleyLog.DEBUG = true;

        username = findViewById(R.id.etUsernameEdit);
        firstName = findViewById(R.id.etFirstNameEdit);
        lastName = findViewById(R.id.etLastNameEdit);
        email = findViewById(R.id.etEmailEdit);
        updatebtn = findViewById(R.id.btnUpdateBtn);
        //updatepass = findViewById(R.id.btnUpdatePass);

        validate_details(token);

        updatebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                newUsername = username.getText().toString();
                newFirstname = firstName.getText().toString();
                newLastname = lastName.getText().toString();
                newEmail = email.getText().toString();


                if(newUsername.isEmpty() || newEmail.isEmpty() || newFirstname.isEmpty() || newLastname.isEmpty() )
                {
                    Toast.makeText(ProfileUpdate.this,
                            "Please enter all the details",
                            Toast.LENGTH_SHORT).show();
                } else {





                    validate(token, newUsername, newFirstname, newLastname, newEmail);
                }
            }
        });
    }

    private String validate(final String token, final String username, final String firstname, final String lastname, final String email)
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
                Toast.makeText(ProfileUpdate.this,
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
        Toast.makeText(ProfileUpdate.this,
                "Details updated successfully",
                Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(ProfileUpdate.this, UserProfile.class);
        intent.putExtra("token", tok );
        startActivity(intent);
    }



    private void validate_details(final String token)
    {
        // if(name.equals(Username) && password.equals(Password)){

        //    return true;
        //}

        //return false;

        RequestQueue queue = Volley.newRequestQueue(this);




        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url_auth, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("JSONPost", response.toString());
                        resp = response.toString();
                        update_details(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d("JSONPost", "Error: " + error);
                        Toast.makeText(ProfileUpdate.this, "Username or Password wrong", Toast.LENGTH_SHORT).show();
                    }
                }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                //params.put("username",name.toString());
                //params.put("password",password.toString());
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Authorization", "Token "+token);
                params.put("Content-Type","multipart/form-data");
                return params;
            }

           /* @Override
            public String getBodyContentType()
            {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            } */

        };
        queue.add(jsonObjectRequest);

        //return resp;
    }

    private void update_details(JSONObject resp) {
        Log.d("JSONOUTUser", resp.toString());
        ProfileUpdate.User user = new Gson().fromJson(resp.toString(), ProfileUpdate.User.class);
        username.setText(user.userName);
        //firstName.setText(user.firstName+" "+user.lastName);
        //username = user.userName;
        firstName.setText(user.firstName);
        lastName.setText(user.lastName);
        email.setText(user.email);
        /* JSONObject jusername = null;
        try {
            jusername = resp.getJSONObject("username");
            JSONObject jfirstname = resp.getJSONObject("first_name");
            JSONObject jlastname = resp.getJSONObject("last_name");
            JSONObject jemail = resp.getJSONObject("email");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        eUsername.setText(jusername.toString());
        eFirstname.setText(); */

        //auth = resp.substring(10, (resp.length()-2));

        //Log.d("token", auth.toString());


    }
    private class User{
        @SerializedName("username")
        String userName;
        @SerializedName("first_name")
        String firstName;
        @SerializedName("last_name")
        String lastName;
        String email;
    }
}