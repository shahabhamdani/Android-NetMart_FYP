package com.Shahab.netmart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.Shahab.netmart.activities.Global;
import com.Shahab.netmart.activities.seller.MainSellerActivity;
import com.Shahab.netmart.activities.seller.OrderDetailsSellerActivity;
import com.Shahab.netmart.activities.user.MainUserActivity;
//import com.Shahab.netmart.adapters.AdapterRider;
import com.Shahab.netmart.adapters.AdapterShop;
import com.Shahab.netmart.models.ModelRider;
import com.Shahab.netmart.models.ModelShop;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class FindRiderActivity extends AppCompatActivity {

    ProgressBar progressBar;
    TextView statusTv;

    private ArrayList<ModelRider> riderList;
   // private AdapterRider adapterRider;
    private RecyclerView riderRv;
    String sourceLat, sourceLng;

    Boolean found;
    private String riderStatus,orderBy, orderCost, orderId, orderTo, deliveryFee, latitude, longitude, orderTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_rider);

        //riderRv = findViewById(R.id.riderRv);

        //get data from intent
        Global.orderId = getIntent().getStringExtra("orderId");
        Global.buyerId = getIntent().getStringExtra("orderBy");
        sourceLat = getIntent().getStringExtra("sourceLat");
        sourceLng = getIntent().getStringExtra("sourceLng");
        progressBar = findViewById(R.id.progressPb);
        statusTv = findViewById(R.id.statusTv);

        riderList = new ArrayList<>();

        progressBar.setVisibility(View.VISIBLE);

        loadRiders();

    }


    private void loadRiders() {
        //init list

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("accountType").equalTo("Rider")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //clear list before adding

                            riderList.clear();
                            for (DataSnapshot ds: dataSnapshot.getChildren() ) {

                                ModelRider modelRider = ds.getValue(ModelRider.class);
                                String riderCity = "" + ds.child("city").getValue();
                                String riderLat = "" + ds.child("latitude").getValue();
                                String riderLng = "" + ds.child("longitude").getValue();
                                String online = "" + ds.child("online").getValue();


                                int dist = calculateDistanceInKilometer(Double.parseDouble(sourceLat), Double.parseDouble(sourceLng),
                                        Double.parseDouble(riderLat), Double.parseDouble(riderLng));

                                if (dist <= 40 && online.equals("true")) {

                                    loadOrderDetails(modelRider.getUid());
                                }

                            }

                            ref.removeEventListener(this);



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


    private void loadOrderDetails(final String riderId) {


        //load detailed info of this order, based on order id
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(Global.myId).child("Orders").child(Global.orderId)

                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //get order info
                        orderBy = ""+dataSnapshot.child("orderBy").getValue();
                        orderCost = ""+dataSnapshot.child("orderCost").getValue();
                        orderId = ""+dataSnapshot.child("orderId").getValue();
                        orderTime = ""+dataSnapshot.child("orderTime").getValue();
                        orderTo = ""+dataSnapshot.child("orderTo").getValue();
                        deliveryFee = ""+dataSnapshot.child("deliveryFee").getValue();
                        latitude = ""+dataSnapshot.child("latitude").getValue();
                        longitude = ""+dataSnapshot.child("longitude").getValue();
                        riderStatus = ""+dataSnapshot.child("riderStatus").getValue();

                        //convert timestamp
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String dateFormated = DateFormat.format("dd/MM/yyyy", calendar).toString();


                       saveOrderToRider(riderId);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }



    private void saveOrderToRider(final String riderId) {

        //for order id and order time
        final String timestamp = ""+System.currentTimeMillis();

        //setup oder data
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("bookingId", ""+orderId);
        hashMap.put("bookingTime", ""+timestamp);
        hashMap.put("orderCost", ""+orderCost);
        hashMap.put("orderBy", ""+orderBy);
        hashMap.put("orderTo", ""+orderTo);
        hashMap.put("deliveryFee", ""+deliveryFee);
        hashMap.put("status", ""+"Waiting");


        //add to db
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(riderId).child("Bookings");
        ref.child(orderId).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        prepareNotificationMessage(orderId,riderId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed placing order
                        Toast.makeText(FindRiderActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }



    private void prepareNotificationMessage(String bookingId, String riderId){
        //When user places order, send notification to seller

        //prepare data for notification
        String NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC; //must be same as subscribed by reciever
        String NOTIFICATION_TITLE = "New Booking "+ bookingId;
        String NOTIFICATION_MESSAGE = "Congratulations...! You have new Booking.";
        String NOTIFICATION_TYPE = "NewBooking";

        //prepare json (what to send and where to send)
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            //what to send
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("sellerUid", Global.myId );
            notificationBodyJo.put("riderUid", riderId);
            notificationBodyJo.put("bookingId", bookingId);
            notificationBodyJo.put("notificationTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage", NOTIFICATION_MESSAGE);
            //where to send
            notificationJo.put("to", NOTIFICATION_TOPIC); //to all who subscribed to this topic
            notificationJo.put("data", notificationBodyJo);
        }
        catch (Exception e){
            Toast.makeText(FindRiderActivity.this, "Fail1 "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        sendFcmNotification(notificationJo, orderId);
    }

    private void sendFcmNotification(JSONObject notificationJo, final String orderId) {
        //send volley request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //after sending fcm start booking activity

                progressBar.setVisibility(View.GONE);
                statusTv.setText("Rider Found");
                updateRiderStatus("waiting");
                onBackPressed();
                finish();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //if failed sending fcm, still start  booking activity
                Toast.makeText(FindRiderActivity.this, "Fail2 "+ error.getMessage(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(FindRiderActivity.this, OrderDetailsSellerActivity.class);
                startActivity(intent);

            }
        }){
            @Override
            public Map<String, String> getHeaders() {

                //put required headers
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=" + Constants.FCM_KEY);

                return headers;
            }
        };

        //enque the volley request
        Volley.newRequestQueue(FindRiderActivity.this).add(jsonObjectRequest);
    }

    private void updateRiderStatus(final String selectedOption) {

        //setup data to put in firebase db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("riderStatus", ""+selectedOption);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(Global.myId).child("Orders").child(Global.orderId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        String message = "Rider Status : " + selectedOption;
                        Toast.makeText(FindRiderActivity.this, message, Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed updating status, show reason
                        Toast.makeText(FindRiderActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }



}