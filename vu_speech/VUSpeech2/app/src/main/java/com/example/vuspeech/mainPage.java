package com.example.vuspeech;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class mainPage extends AppCompatActivity {


    private Button eRecord;
    private Button eStop;
    private Button eSave;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);


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