package com.Shahab.netmart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class VerificationActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private EditText emialEt;
    private Button recoverBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        backBtn = findViewById(R.id.backBtn);
        emialEt = findViewById(R.id.emailEt);
        recoverBtn = findViewById(R.id.recoverBtn);

        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }



    public void onClickRecover(View v){
        startActivity( new Intent(VerificationActivity.this, ForgotPasswordActivity.class ));
    }
}