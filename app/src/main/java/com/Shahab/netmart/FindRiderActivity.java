package com.Shahab.netmart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.Shahab.netmart.activities.Global;
import com.Shahab.netmart.activities.user.MainUserActivity;
import com.Shahab.netmart.adapters.AdapterRider;
import com.Shahab.netmart.adapters.AdapterShop;
import com.Shahab.netmart.models.ModelRider;
import com.Shahab.netmart.models.ModelShop;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FindRiderActivity extends AppCompatActivity {

    private ArrayList<ModelRider> riderList;
    private AdapterRider adapterRider;
    private RecyclerView riderRv;
    String sourceLat, sourceLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_rider);

        riderRv = findViewById(R.id.riderRv);

        //get data from intent
        Global.orderId = getIntent().getStringExtra("orderId");
        Global.buyerId = getIntent().getStringExtra("orderBy");
        sourceLat = getIntent().getStringExtra("sourceLat");
        sourceLng = getIntent().getStringExtra("sourceLng");

        loadRiders("Mountain View");

    }


    private void loadRiders(final String myCity) {
        //init list
        riderList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("accountType").equalTo("Rider")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //clear list before adding
                        riderList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()){

                            ModelRider modelRider = ds.getValue(ModelRider.class);
                            String riderCity = ""+ds.child("city").getValue();
                            String riderLat = ""+ds.child("latitude").getValue();
                            String riderLng = ""+ds.child("longitude").getValue();
                            String status = ""+ds.child("onLine").getValue();



                            int dist = calculateDistanceInKilometer(Double.parseDouble(sourceLat),Double.parseDouble(sourceLng),
                                    Double.parseDouble(riderLat),Double.parseDouble(riderLng));

                            if (dist <= 1 && status.equals("true")){
                                riderList.add(modelRider);
                            }

                        }
                        //setup adapter
                        adapterRider = new AdapterRider(FindRiderActivity.this, riderList);
                        //set adapter to recyclerview
                        riderRv.setAdapter(adapterRider);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }


    public final static double AVERAGE_RADIUS_OF_EARTH_KM = 6371;
    public int calculateDistanceInKilometer(double userLat, double userLng,
                                            double venueLat, double venueLng) {

        double latDistance = Math.toRadians(userLat - venueLat);
        double lngDistance = Math.toRadians(userLng - venueLng);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(venueLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (int) (Math.round(AVERAGE_RADIUS_OF_EARTH_KM * c));
    }

}