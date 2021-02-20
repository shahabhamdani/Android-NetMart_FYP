package com.Shahab.netmart;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

public class RegisterUserActivity extends AppCompatActivity {

    private ImageButton backBtn, gpsBtn;
    private ImageView profileIv;
    private EditText nameEt, phoneEt, countryEt, stateEt, cityEt, addressEt,
            emailEt, passwordEt, cPasswordEt;
    private Button registerBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        //init Values
        backBtn = findViewById(R.id.backBtn);
        gpsBtn = findViewById(R.id.gpsBtn);
        profileIv = findViewById(R.id.profileIv);
        phoneEt = findViewById(R.id.phoneEt);
        nameEt = findViewById(R.id.nameEt);
        countryEt = findViewById(R.id.countryEt);
        stateEt = findViewById(R.id.passwordEt);
        cityEt = findViewById(R.id.cityEt);
        addressEt = findViewById(R.id.addressEt);
        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        cPasswordEt = findViewById(R.id.cPasswordEt);
        registerBtn = findViewById(R.id.registerBtn);



        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onBackPressed();
            }
        });

        gpsBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                //detect current location
            }
        });

        profileIv.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                //pick image
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                //register user
            }
        });



    }
}