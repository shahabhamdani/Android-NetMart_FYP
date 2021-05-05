package com.Shahab.netmart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RiderEarningActivity extends AppCompatActivity {

    ArrayList<String> completedBookingsList;
    ListView completedBookingsLv;

    TextView monthlyEarningsTv, totalEarningsTv;

    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_earning);

        completedBookingsLv = findViewById(R.id.completedBookingsLv);
        monthlyEarningsTv = findViewById(R.id.lastMonthEarningTv);
        totalEarningsTv = findViewById(R.id.totalEarningTv);

        firebaseAuth = FirebaseAuth.getInstance();

        Bundle b = new Bundle(getIntent().getExtras());
        completedBookingsList = b.getStringArrayList("bookings");

        monthlyEarningsTv.setText("Last month:"+b.getDouble("monthlyEarn"));
        totalEarningsTv.setText("Total earnings: "+b.getDouble("totalEarn"));


        final ArrayAdapter<String> adapter = new ArrayAdapter<>(RiderEarningActivity.this, android.R.layout.simple_list_item_1, completedBookingsList);
        completedBookingsLv.setAdapter(adapter);

    }

    public void onBackClick(View view) {
        onBackPressed();
    }
}