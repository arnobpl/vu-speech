package com.google.cloud.android.speech;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.google.cloud.android.speech.SessionManager.IS_LOGGEDIN;

public class UserProfile extends AppCompatActivity {

    private TextView eFirstname;
    private TextView eLastname;
    private TextView eUsername;
    private TextView eEmail;
    private FloatingActionButton updatebtn;
    private FloatingActionButton updatepass;
    private FloatingActionButton recordbtn;
    private TextView details;
    private TextView etdisplay;
    private String resp = "";
    private FloatingActionButton logout;
    private TextView eFullname;
    String token = "";
    String username = "";
    private String url_auth = "http://ec2-3-16-29-185.us-east-2.compute.amazonaws.com:8000/api/v1/accounts/profile/retrieve/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Intent intent = getIntent();
        token = getIntent().getStringExtra("token");


        // params.put("password","whatsmyname123");


        eFirstname = findViewById(R.id.etFirstname);
        eLastname = findViewById(R.id.etLastname);
        eEmail = findViewById(R.id.etemail);
        //eUsername = findViewById(R.id.etDisplayname);
        updatebtn = (FloatingActionButton) findViewById(R.id.btnUpdate);
        updatepass = (FloatingActionButton) findViewById(R.id.btnUpdatePass);
        //recordbtn = findViewById(R.id.btnRecordbtn);
        //etdisplay = findViewById(R.id.etDisplayname);
        logout = (FloatingActionButton) findViewById(R.id.btnlogout);
        eFullname = findViewById(R.id.etDisplayname1);

        validate(token);

        updatebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfile.this, ProfileUpdate.class);
                intent.putExtra("token", token);
                startActivity(intent);
            }
        });


        updatepass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfile.this, UpdatePassword.class);
                intent.putExtra("token", token);
                intent.putExtra("username", username );
                startActivity(intent);
            }
        });




        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfile.this, WelcomeActivity.class);
                //intent.putExtra("username", username );
                startActivity(intent);

                SessionManager sessionManager = new SessionManager(UserProfile.this);
                HashMap<String, String> userDetails = sessionManager.getUserDetailsFromSession();
                sessionManager.logout();
                sessionManager.editor.putBoolean(IS_LOGGEDIN, false);

            }
        });

    }

    private void validate(final String token)
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
                        update(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d("JSONPost", "Error: " + error);
                        Toast.makeText(UserProfile.this, "Username or Password wrong", Toast.LENGTH_SHORT).show();
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

    private void update(JSONObject resp) {
        Log.d("JSONOUTUser", resp.toString());
        User user = new Gson().fromJson(resp.toString(), User.class);
        //eUsername.setText(user.userName);
        eFullname.setText(user.firstName+" "+user.lastName);
        username = user.userName;
        eFirstname.setText("First Name:    "+user.firstName);
        eLastname.setText("Last Name:    "+user.lastName);
        eEmail.setText("Email:    "+user.email);
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