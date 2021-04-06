package com.example.vuspeech;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class mainPage extends AppCompatActivity {


    private Button eRecord;
    private Button eStop;
    private Button eSave;

    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);





        eRecord = findViewById(R.id.btnRecord);
        eStop = findViewById(R.id.btnstopRecord);
        eSave = findViewById(R.id.btnSave);


        eRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Toast.makeText(mainPage.this,
                        "Recoding Started",
                        Toast.LENGTH_SHORT).show();
            }
        });


        eStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Toast.makeText(mainPage.this,
                        "Recording Stopped",
                        Toast.LENGTH_SHORT).show();
            }
        });

        eSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Toast.makeText(mainPage.this,
                        "Recording Saved",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}