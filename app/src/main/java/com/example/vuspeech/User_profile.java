package com.example.vuspeech;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class User_profile extends AppCompatActivity {

    private TextView eFirstname;
    private TextView eLastname;
    private TextView eUsername;
    private TextView eEmail;
    private Button updatebtn;
    private Button recordbtn;
    private String resp = "";
    String token = "";
    private String url_auth = "http://stark.cse.buffalo.edu:8000/api/v1/accounts/profile/retrieve/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);


        Intent intent = getIntent();
        token = getIntent().getStringExtra("token");


        // params.put("password","whatsmyname123");


        eFirstname = findViewById(R.id.etFirstName);
        eLastname = findViewById(R.id.etSecondName);
        eEmail = findViewById(R.id.etEmailid);
        eUsername = findViewById(R.id.etDisplayname);
        updatebtn = findViewById(R.id.btnUpdate);
        recordbtn = findViewById(R.id.btnRecordbtn);

        validate(token);

        updatebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(User_profile.this, UpdateProfile.class);
                intent.putExtra("token", token);
                startActivity(intent);
            }
        });


        recordbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(User_profile.this, Transcription.class);
                //intent.putExtra("token", token );
                startActivity(intent);
            }
        });

    }


    private void validate(String token)
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
                Toast.makeText(User_profile.this, "Username or Password wrong", Toast.LENGTH_SHORT).show();
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
        Log.d("JSONUTUser", user.firstName);
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








