package com.Shahab.netmart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class VerificationActivity extends AppCompatActivity {


    private EditText emialEt;
    private Button recoverBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);


        emialEt = findViewById(R.id.emailEt);
        recoverBtn = findViewById(R.id.recoverBtn);


    }

    public void onClickRecover(View v){
        startActivity( new Intent(VerificationActivity.this, ForgotPasswordActivity.class ));
    }
}