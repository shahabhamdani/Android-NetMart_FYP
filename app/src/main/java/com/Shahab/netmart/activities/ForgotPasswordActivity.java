package com.Shahab.netmart.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.Shahab.netmart.R;

public class ForgotPasswordActivity extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
    }


    public void onClickBack(View v){
        startActivity( new Intent(ForgotPasswordActivity.this, LoginActivity.class ));
    }
}