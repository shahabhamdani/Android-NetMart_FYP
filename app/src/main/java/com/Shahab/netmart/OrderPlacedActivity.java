package com.Shahab.netmart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.Shahab.netmart.activities.user.OrderDetailsUsersActivity;
import com.Shahab.netmart.activities.user.ShopDetailsActivity;

public class OrderPlacedActivity extends AppCompatActivity {

    String orderTo, orderId = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placed_order);

        orderTo = getIntent().getStringExtra("orderTo");
        orderId = getIntent().getStringExtra("orderId");



    }

    public void onOrderDetailsClick(View view) {

        if (! (orderTo.equals("") && orderId.equals("") ) ){

            Intent intent = new Intent(OrderPlacedActivity.this, OrderDetailsUsersActivity.class);
            intent.putExtra("orderTo", orderTo);
            intent.putExtra("orderId", orderId);
            startActivity(intent);

            finish();

        }

    }
}