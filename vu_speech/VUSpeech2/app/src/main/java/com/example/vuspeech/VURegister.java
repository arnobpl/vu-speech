package com.example.vuspeech;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class VURegister extends AppCompatActivity {

    private EditText eFirstname;
    private EditText eLastname;
    private EditText eEmail;
    private EditText ename;
    private EditText ePassword1;
    private EditText ePassword2;
    private Button eRegister;

    private String Username = "Admin";
    private String Password = "12345678";

    boolean isValid = false;
    private int counter = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_v_u_register);

        ename = findViewById(R.id.etnewUsername);
        eEmail = findViewById(R.id.etEmail);
        eFirstname = findViewById(R.id.etfirstName);
        eLastname = findViewById(R.id.etlastName);
        ePassword1 = findViewById(R.id.etPassword1);
        ePassword2 = findViewById(R.id.etPassword2);
        eRegister = findViewById(R.id.btnRegister);

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
                    Toast.makeText(VURegister.this,
                            "Please enter all the details",
                            Toast.LENGTH_SHORT).show();
                } else {

                    isValid = validate(inputPassword1, inputPassword2);

                    if(!isValid){


                        Toast.makeText(VURegister.this, "Enter same password", Toast.LENGTH_SHORT).show();


                    }else{
                        Toast.makeText(VURegister.this, "Registered Successful !", Toast.LENGTH_SHORT).show();



                        //Add connection to Next Page

                    }
                }


            }
        });


    }

    private boolean validate(String password1, String password2)
    {
        if(password1.equals(password2)){

            return true;
        }

        return false;
    }




    }
