package com.Shahab.netmart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class RiderDeliverySuccess extends AppCompatActivity {

    String distance, time, fee, cost;

    TextView costTv, feeTv, timeTv, distanceTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_delivery_success);

        distance = getIntent().getStringExtra("distance");
        time = getIntent().getStringExtra("time");
        fee = getIntent().getStringExtra("fee");
        cost = getIntent().getStringExtra("cost");

        costTv = findViewById(R.id.fee);
        timeTv = findViewById(R.id.time);
        distanceTv = findViewById(R.id.distance);


        costTv.setText("Collect: Rs"+cost);
        distanceTv.setText(""+distance+"KM");
        timeTv.setText(""+time+" Minutes");





    }

    public void onOkClicked(View view) {

        startActivity(new Intent(RiderDeliverySuccess.this, RiderMainActivity.class));
        finish();
    }
}