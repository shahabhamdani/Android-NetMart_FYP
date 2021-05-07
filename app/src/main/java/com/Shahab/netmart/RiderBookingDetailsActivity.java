package com.Shahab.netmart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.Shahab.netmart.activities.authentication.StatusClass;
import com.Shahab.netmart.activities.seller.MainSellerActivity;
import com.Shahab.netmart.adapters.AdapterOrderShop;
import com.Shahab.netmart.models.ModelBooking;
import com.Shahab.netmart.models.ModelOrderShop;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.OnPausedListener;

import java.util.ArrayList;
import java.util.HashMap;


public class RiderBookingDetailsActivity extends AppCompatActivity implements OnMapReadyCallback  {

    private TextView catTv, distanceTv, timeTv, storeNameTv, storeAddressTv, custNameTv, custAddressTv, deliveryFeesTv, totalPriceTv;
    private Button acceptBtn, rejectBtn, startRideBtn, deliveredBtn, pickedBtn;
    private String orderId;
    private GoogleMap gMapBooking;
    private FirebaseAuth firebaseAuth;
    private LatLng shopLatLng, customerLatLng;
    private ProgressDialog progressDialog;
    private int dist, time;
    private ArrayList<ModelOrderShop> orderShopArrayList;

    private String bookingId, sellerId;
    public   String fee, cost, to, by;

    ModelBooking modelBooking = new ModelBooking();

    StatusClass statusClass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_booking_details);



        //get data from intent
        bookingId = getIntent().getStringExtra("bookingId");
        sellerId = getIntent().getStringExtra("sellerId");

        totalPriceTv = findViewById(R.id.totalPriceTv);
        deliveryFeesTv = findViewById(R.id.deliveryFeesTv);

        distanceTv = findViewById(R.id.distanceTv);
        timeTv = findViewById(R.id.timeTv);
        storeNameTv = findViewById(R.id.storeNameTv);
        storeAddressTv = findViewById(R.id.storeAddressTv);
        custNameTv = findViewById(R.id.custNameTv);
        custAddressTv = findViewById(R.id.custAddressTv);
        acceptBtn = findViewById(R.id.acceptBtn);
        startRideBtn = findViewById(R.id.startBtn);
        deliveredBtn = findViewById(R.id.deliveredBtn);
        pickedBtn = findViewById(R.id.pickedBtn);

        rejectBtn = findViewById(R.id.rejectBtn);

        firebaseAuth = FirebaseAuth.getInstance();


        SupportMapFragment supportMapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.gMapBooking);
        supportMapFragment.getMapAsync(this);

        loadBooking();


        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                rejectBtn.setVisibility(View.GONE);
                acceptBtn.setVisibility(View.GONE);
                startRideBtn.setVisibility(View.VISIBLE);

            }
        });

        startRideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                navigate(Double.parseDouble(modelBooking.getsLat()), Double.parseDouble(modelBooking.getsLng()));
                startRideBtn.setVisibility(View.GONE);
                setBooking("accepted");

                pickedBtn.setVisibility(View.VISIBLE);
            }
        });

        rejectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBooking("rejected");
            }
        });

        pickedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                navigate(Double.parseDouble(modelBooking.getcLat()), Double.parseDouble(modelBooking.getcLng()));
                pickedBtn.setVisibility(View.GONE);
                deliveredBtn.setVisibility(View.VISIBLE);

            }
        });

        deliveredBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setBooking("completed");
                Intent intent = new Intent(RiderBookingDetailsActivity.this, RiderDeliverySuccess.class);
                intent.putExtra("distance", ""+dist);
                intent.putExtra("time",""+time);
                intent.putExtra("fee",""+fee);
                intent.putExtra("cost",""+cost);
                startActivity(intent);
                finish();

            }
        });

    }


    @Override
    public void onResume() {
        super.onResume ();
        //isOnline(true);
    }


    @Override
    public void onStop() {
        super.onStop();
    }


    private void setBooking(final String value) {

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("riderStatus", ""+value);

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                reference.child(sellerId).child("Orders").child(bookingId).updateChildren(hashMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                setBookingStatus(value);

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //failed adding to db
                                Toast.makeText(RiderBookingDetailsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
     }



    public void navigate(double lat, double lng) {

        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lng);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);

    }

    private void setBookingStatus(final String value) {

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", ""+value);


            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Bookings").child(bookingId).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            if(value.equals("rejected")){
                                startActivity(new Intent(RiderBookingDetailsActivity.this, RiderMainActivity.class));
                                finish();
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed adding to db
                            Toast.makeText(RiderBookingDetailsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

    }


    private void setBookingDetailsOnScreen() {
        dist = calculateDistanceInKilometer(Double.parseDouble(modelBooking.getsLat()), Double.parseDouble(modelBooking.getsLng()),
                Double.parseDouble(modelBooking.getcLat()), Double.parseDouble(modelBooking.getcLng()));

       time = calculateTime(dist);

        distanceTv.setText(""+dist+"KM");
        storeNameTv.setText(modelBooking.getShopeName());
        storeAddressTv.setText(modelBooking.getShopAddress());
        custNameTv.setText(modelBooking.getCustomerName());
        custAddressTv.setText(modelBooking.getCustomerAddress());
        timeTv.setText("("+time+"min)");

    }

    private int calculateTime(int dist) {

        int time = dist/25 * 60;
        return time;
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

    private void loadBooking() {


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Bookings").child(bookingId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                           by  = "" + dataSnapshot.child("orderBy").getValue().toString();
                            cost  = "" + dataSnapshot.child("orderCost").getValue().toString();
                            to  = "" + dataSnapshot.child("orderTo").getValue().toString();
                            fee  = "" + dataSnapshot.child("deliveryFee").getValue().toString();

                            deliveryFeesTv.setText("Delivery:Rs"+fee);
                            totalPriceTv.setText("Rs:"+cost);

                            //seller
                            LoadInfo(to);

                            //huyer
                            LoadInfo(by);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }


    public void LoadInfo(String id){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                        String accountType = "" + dataSnapshot.child("accountType").getValue();
                        String latitude = "" + dataSnapshot.child("latitude").getValue();
                        String longitude = "" + dataSnapshot.child("longitude").getValue();
                        String address = "" + dataSnapshot.child("address").getValue();
                        String phone = "" + dataSnapshot.child("phone").getValue();

                        String name;
                        if(accountType.equals("Seller")){
                              name =  "" + dataSnapshot.child("shopName").getValue();

                              modelBooking.setShopeName(name);
                              modelBooking.setShopAddress(address);
                              modelBooking.setsLat(latitude);
                              modelBooking.setsLng(longitude);
                              modelBooking.setShopPhone(phone);

                        }
                        else
                            {
                            name =  "" + dataSnapshot.child("name").getValue();
                            modelBooking.setCustomerName(name);
                            modelBooking.setCustomerAddress(address);
                            modelBooking.setcLat(latitude);
                            modelBooking.setcLng(longitude);
                            modelBooking.setCustomerPhone(phone);

                            setBookingDetailsOnScreen();

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMapBooking = googleMap;

        gMapBooking.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {

                LatLng customerLoc = new LatLng(Double.parseDouble(modelBooking.getcLat()), Double.parseDouble(modelBooking.getcLng()));

                gMapBooking.clear();

                Marker m1 = gMapBooking.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.parseDouble(modelBooking.getcLat()), Double.parseDouble(modelBooking.getcLng())))
                        .anchor(0.5f, 0.5f));


                Marker m2 = gMapBooking.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.parseDouble(modelBooking.getsLat()),Double.parseDouble(modelBooking.getsLng())))
                        .anchor(0.5f, 0.5f));

                gMapBooking.animateCamera(CameraUpdateFactory.newLatLngZoom(customerLoc, 10));


            }
        });
    }

}