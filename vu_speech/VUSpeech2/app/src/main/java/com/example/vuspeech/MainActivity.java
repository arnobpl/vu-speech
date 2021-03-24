package com.example.vuspeech;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText eName;
    private EditText ePassword;
    private Button eLogin;
    private Button eRegister;

    private String Username = "Admin";
    private String Password = "12345678";

    boolean isValid = false;
    private int counter = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eName = findViewById(R.id.etUsername);
        ePassword = findViewById(R.id.etPassword);
        eLogin = findViewById(R.id.btnLogin);
        eRegister = findViewById(R.id.btnRegister);

        eLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String inputName = eName.getText().toString();
                String inputPassword = ePassword.getText().toString();

                if(inputName.isEmpty() || inputPassword.isEmpty())
                {
                    Toast.makeText(MainActivity.this,
                            "Please enter all the details",
                            Toast.LENGTH_SHORT).show();
                } else {

                    isValid = validate(inputName, inputPassword);

                    if(!isValid){
                        counter --;

                        Toast.makeText(MainActivity.this, "Incorrect credentials!!", Toast.LENGTH_SHORT).show();

                        if(counter == 0)
                        {
                            eLogin.setEnabled(false);
                        }
                    }else{
                        Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                        //Add connection to Next Page

                    }
                }


            }
        });

        eRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                
            }
        });
    }

    private boolean validate(String name, String password)
    {
        if(name.equals(Username) && password.equals(Password)){

            return true;
        }

        return false;
    }
}