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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText eName;
    private EditText ePassword;
    private Button eLogin;
    private Button eRegister;


    private String inputName = "";
    private String inputPassword = "";
    private String resp = "";

    String isValid = "";
    String auth = "";
    private int counter = 5;
    String url_auth ="http://stark.cse.buffalo.edu:8000/api/v1/accounts/api-token-auth/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eName = findViewById(R.id.etUsername);
        ePassword = findViewById(R.id.etPassword);
        eLogin = findViewById(R.id.btnLogin);

        eRegister = findViewById(R.id.btnRegister);







        //RequestQueue queue = Volley.newRequestQueue(this);


        //textView =  findViewById(R.id.View);


        //JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url_auth, null,
         //       new Response.Listener<JSONObject>() {
         //           @Override
         //           public void onResponse(JSONObject response) {
         //               // Display the first 500 characters of the response string.
        //                try {
         //                   JSONArray jsonArray = response.getJSONArray("elements");
//
         //                   for (int i =0; i< jsonArray.length(); i ++){
         //                       JSONObject transcript = jsonArray.getJSONObject(i);

         //                       String type = transcript.getString("type");
         //                       String value = transcript.getString("value");


         //                   }
          //              } catch (JSONException e) {
         //                   e.printStackTrace();
          //              }
         //               //textView.setText("Response is: "+ response);
         //               Log.i("REST Response", response.toString());
         //           }
        //        }, new Response.ErrorListener() {
        //    @Override
         //   public void onErrorResponse(VolleyError error) {
         //       Log.i("REST Response", "Error");
                //textView.setText("That didn't work!");
         //   }
        //});


      //  queue.add(request);




       /* eLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String inputName = eName.getText().toString();
                String inputPassword = ePassword.getText().toString();

                validate(inputName, inputPassword);
            }
        }); */



        eLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String inputName = eName.getText().toString();
                String inputPassword = ePassword.getText().toString();

                validate(inputName, inputPassword);
                    //Log.d("JSONOUT", isValid.toString());











                }



        });

        eRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, VURegister.class);
                startActivity(intent);
            }
        });
    }

    private void validate(String name, String password)
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
                Toast.makeText(MainActivity.this, "Username or Password wrong", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(MainActivity.this, "Login successfull", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, User_profile.class);
            intent.putExtra("token", auth );
            startActivity(intent);
        }
        else
            Toast.makeText(MainActivity.this, "Not registered", Toast.LENGTH_SHORT).show();
    }
}
