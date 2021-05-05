package com.Shahab.netmart.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.Shahab.netmart.Constants;
import com.Shahab.netmart.FindRiderActivity;
import com.Shahab.netmart.R;
import com.Shahab.netmart.RiderBookingDetailsActivity;
import com.Shahab.netmart.activities.Global;
import com.Shahab.netmart.activities.seller.MainSellerActivity;
import com.Shahab.netmart.activities.seller.OrderDetailsSellerActivity;
import com.Shahab.netmart.activities.user.OrderDetailsUsersActivity;
import com.Shahab.netmart.activities.user.ShopDetailsActivity;
import com.Shahab.netmart.models.ModelRider;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AdapterRider extends RecyclerView.Adapter<AdapterRider.HolderRider> {

    private Context context;
    private FirebaseAuth firebaseAuth;
    public ArrayList<ModelRider> riderList;
    private ProgressDialog progressDialog;

    private String orderBy, orderCost, orderId, orderTo, deliveryFee, latitude, longitude, orderTime;

    public AdapterRider(Context context, ArrayList<ModelRider> riderList) {
        this.context = context;
        this.riderList = riderList;
    }

    @NonNull
    @Override
    public HolderRider onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_shop.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_rider, parent, false);
        return new HolderRider(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderRider holder, int position) {
        //get data
        ModelRider modelRider = riderList.get(position);

        String accountType = modelRider.getAccountType();
        String address = modelRider.getAddress();
        String city =  modelRider.getCity();
        String country = modelRider.getCountry();
        String email = modelRider.getEmail();
        String latitude = modelRider.getLatitude();
        String longitude = modelRider.getLongitude();
        String name = modelRider.getName();
        String phone = modelRider.getPhone();
        final String uid = modelRider.getUid();
        String timestamp = modelRider.getTimestamp();
        String state = modelRider.getState();
        String profileImage = modelRider.getProfileImage();

        holder.phoneTv.setText(phone);
        holder.addressTv.setText(address);
        holder.riderName.setText(name);

        try {
            Picasso.get().load(profileImage).placeholder(R.drawable.ic_riderlogo).into(holder.riderIv);
        }
        catch (Exception e){
        }

        //handle click listener, show shop details
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Book Rider")
                        .setMessage("Do you want to book This Rider?")

                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                Global.riderId = uid;
                                loadBookings(uid);

                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_menu_info_details)
                        .show();
            }
        });

    }

    private void loadBookings(String uid) {

        updateRiderStatus("Waiting");

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
                        String riderStatus = ""+dataSnapshot.child("riderStatus").getValue();

                        //convert timestamp
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String dateFormated = DateFormat.format("dd/MM/yyyy", calendar).toString();

                        if(!(riderStatus.equals("rejected"))){
                            saveOrderToRider();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void saveOrderToRider() {

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
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(Global.riderId).child("Bookings");
        ref.child(orderId).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        prepareNotificationMessage(orderId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed placing order
                        Toast.makeText(context, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

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
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed updating status, show reason
                            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

    }


    private void prepareNotificationMessage(String bookingId){
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
            notificationBodyJo.put("sellerUid", Global.myId );//since we are logged in as buyer to place order so current user uid is buyer uid
            notificationBodyJo.put("riderUid", Global.riderId);
            notificationBodyJo.put("bookingId", bookingId);
            notificationBodyJo.put("notificationTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage", NOTIFICATION_MESSAGE);
            //where to send
            notificationJo.put("to", NOTIFICATION_TOPIC); //to all who subscribed to this topic
            notificationJo.put("data", notificationBodyJo);
        }
        catch (Exception e){
            Toast.makeText(context, "Fail1 "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        sendFcmNotification(notificationJo, orderId);
    }

    private void sendFcmNotification(JSONObject notificationJo, final String orderId) {
        //send volley request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //after sending fcm start booking activity
                Intent intent = new Intent(context, MainSellerActivity.class);
                context.startActivity(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //if failed sending fcm, still start  booking activity
                Toast.makeText(context, "Fail2 "+ error.getMessage(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, OrderDetailsSellerActivity.class);
                context.startActivity(intent);

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
        Volley.newRequestQueue(context).add(jsonObjectRequest);
    }




    @Override
    public int getItemCount() {
        return riderList.size(); //return number of records
    }

    //view holder
    class HolderRider extends RecyclerView.ViewHolder{

        //ui views of row_shop.xml
        private ImageView riderIv, onlineIv;
        private TextView riderName, phoneTv, addressTv;

        public HolderRider(@NonNull View itemView) {
            super(itemView);

            //init uid views
            riderIv = itemView.findViewById(R.id.riderIv);
            onlineIv = itemView.findViewById(R.id.onlineIv);
            phoneTv = itemView.findViewById(R.id.phoneTv);
            addressTv = itemView.findViewById(R.id.addressTv);
            riderName = itemView.findViewById(R.id.riderNameTv);
        }
    }
}
