package com.Shahab.netmart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class RegisterMenuActivity extends AppCompatActivity {


    private TextView txtBuyer;
    private TextView txtSeller;
    private TextView txtRider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_menu);

        txtBuyer = findViewById(R.id.txtBuyer);
        txtSeller = findViewById(R.id.txtSeller);
        txtRider =  findViewById((R.id.txtRider));

        txtBuyer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity( new Intent(RegisterMenuActivity.this, RegisterUserActivity.class ));
            }
        });

        txtSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity( new Intent(RegisterMenuActivity.this, RegisterSellerActivity.class ));
            }
        });

        txtRider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity( new Intent(RegisterMenuActivity.this, RegisterRider.class ));
            }
        });
    }
}