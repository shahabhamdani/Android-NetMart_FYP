package com.Shahab.netmart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Shahab.netmart.activities.CartActivity;
import com.Shahab.netmart.activities.SettingsActivity;
import com.Shahab.netmart.activities.authentication.LoginActivity;
import com.Shahab.netmart.RiderMainActivity;
import com.Shahab.netmart.activities.authentication.StatusClass;
import com.Shahab.netmart.activities.user.MainUserActivity;
import com.Shahab.netmart.activities.user.ProfileEditUserActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RiderMainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap gMap;
    private ImageView onlineBtn, profileIv;
    private double latitude, longitude;
    private TextView userName, userPhone, userEmail;
    String pImage;

    private String myLng, myLat;
    private RelativeLayout gmapRl;
    private RelativeLayout bookingsRl;

    private TextView mapTv, bookingsTv;
    private ArrayList<String> bookingsArray, sellerArray, completedBookingsArray;
    private double monthlyEarn;
    private double totalEarn;


    private ListView bookingsLv;

    private StatusClass statusClass;

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;


    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_main);



        //drawer
        userName = findViewById(R.id.userNameTv);
        userPhone = findViewById(R.id.phoneTv);
        userEmail = findViewById(R.id.emailTv);

        gmapRl = findViewById(R.id.mapRl);
        bookingsRl = findViewById(R.id.bookingsRl);
        mapTv = findViewById(R.id.mapTv);
        bookingsTv = findViewById(R.id.bookingsTv);
        bookingsLv = findViewById(R.id.bookingsLv);

        bookingsArray = new ArrayList<String>();
        sellerArray = new ArrayList<String>();
        completedBookingsArray = new ArrayList<String>();


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        statusClass = new StatusClass(RiderMainActivity.this, firebaseAuth.getUid());
        statusClass.autoOnline();

        checkUser();
        setUpToolbar();

        navigationView = (NavigationView) findViewById(R.id.navigation_menu);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Intent intent = null;
                switch (menuItem.getItemId()) {

                    case R.id.navSignOut: {
                        makeMeOffline();
                    }
                    break;

                    case R.id.navSetting: {
                        Intent intent1 = new Intent(RiderMainActivity.this, SettingsActivity.class);
                        startActivity(intent1);
                    }
                    break;

                    case R.id.navEarning: {

                        Intent intent1 = new Intent(RiderMainActivity.this, RiderEarningActivity.class);
                        intent1.putExtra("bookings",completedBookingsArray);
                        intent1.putExtra("monthlyEarn",monthlyEarn);
                        intent1.putExtra("totalEarn",totalEarn);

                        startActivity(intent1);
                    }
                    break;

                    case R.id.navProfile: {
                        Intent intent1 = new Intent(RiderMainActivity.this, RiderEditProfileActivity.class);
                        startActivity(intent1);
                    }
                    break;

                }
                return false;
            }
        });


        SupportMapFragment supportMapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.gMap);
        supportMapFragment.getMapAsync(this);


        mapTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMapUi();
            }
        });

        bookingsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBookingsUi();
            }
        });

        bookingsLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String selectedBookingId = bookingsArray.get(position);
                String sellerId = sellerArray.get(position);


               Intent intent = new Intent(RiderMainActivity.this, RiderBookingDetailsActivity.class);
               intent.putExtra("bookingId", selectedBookingId);
               intent.putExtra("sellerId", sellerId);
               startActivity(intent);
            }
        });


    }



    public void setUpToolbar() {
        drawerLayout = findViewById(R.id.drawerLayout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

    }

    private void makeMeOffline() {

        //after logging in, make user online
        progressDialog.setMessage("Logging Out...");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onLine", "false");

        //update value to Database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Updated Successfullly
                        firebaseAuth.signOut();

                        //if logged in Go to Dashboard else go to Login screen
                        checkUser();
                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Failed Updating
                        progressDialog.dismiss();
                        Toast.makeText(RiderMainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        gMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {

                LatLng riderLocation = new LatLng(Double.parseDouble(myLat), Double.parseDouble(myLng));
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(riderLocation);
                markerOptions.title("" + userName.getText());
                markerOptions.snippet("" + userPhone.getText());
                gMap.clear();
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(riderLocation, 17));
                gMap.addMarker((markerOptions)).showInfoWindow();
            }
        });
    }


    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {
            Intent intent = new Intent(RiderMainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            loadMyInfo();
        }
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())

                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            String name = "" + ds.child("name").getValue();
                            String accountType = "" + ds.child("accountType").getValue();
                            String phone = "" + ds.child("phone").getValue();
                            String profileImage = "" + ds.child("profileImage").getValue();
                            String email = "" + ds.child("email").getValue();
                            String city = "" + ds.child("city").getValue();
                            myLat = "" + ds.child("latitude").getValue();
                            myLng = "" + ds.child("longitude").getValue();


                            pImage = profileImage;

                            userName = (TextView) findViewById(R.id.userNameTv);
                            userName.setText(name + " (" + accountType + ")");

                            userPhone = (TextView) findViewById(R.id.phoneTv);
                            userPhone.setText(phone);

                            userEmail = (TextView) findViewById(R.id.emailTv);
                            userEmail.setText(email);

                            profileIv = findViewById(R.id.hProfileIv);

                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_black).into(profileIv);
                            } catch (Exception e) {
                                //Toast.makeText(RiderMainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        loadBookings();

    }

    private void showBookingsUi() {
        gmapRl.setVisibility(View.GONE);
        bookingsRl.setVisibility(View.VISIBLE);

        mapTv.setTextColor(getResources().getColor(R.color.colourBlack));
        mapTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        bookingsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        bookingsTv.setBackgroundResource(R.drawable.shape_rect04);

        loadBookings();

    }


    private void showMapUi() {
        bookingsRl.setVisibility(View.GONE);
        gmapRl.setVisibility(View.VISIBLE);

        mapTv.setTextColor(getResources().getColor(R.color.colorWhite));
        mapTv.setBackgroundResource(R.drawable.shape_rect04);


        bookingsTv.setTextColor(getResources().getColor(R.color.colourBlack));
        bookingsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void loadBookings() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Bookings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //clear list before adding new data in it
                        bookingsArray.clear();
                        sellerArray.clear();
                        completedBookingsArray.clear();
                        monthlyEarn = 0;
                        totalEarn = 0;

                        for (DataSnapshot ds : dataSnapshot.getChildren()) {

                            String id = "" + ds.child("bookingId").getValue();
                            String sellerId = "" + ds.child("orderTo").getValue();
                            String status = ""+ds.child("status").getValue();
                            String cost = ""+ds.child("orderCost").getValue();
                            String fee = ""+ds.child("deliveryFee").getValue();
                           long  bookingTime = Long.parseLong(""+ds.child("bookingTime").getValue());



                            if(status.equals("Waiting")){
                                bookingsArray.add(id);
                                sellerArray.add(sellerId);
                            }

                           String date =  getDate(bookingTime,"dd-MM-yyyy");

                            String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());


                            if(status.equals("completed")){

                                completedBookingsArray.add("ID:"+id+" | Cost:"+cost+" | Earning:"+fee);


                                if(date.compareTo(currentDate) > -30  && date.compareTo(currentDate) < 1){
                                    monthlyEarn += Double.parseDouble(fee);
                                }

                                totalEarn+= Double.parseDouble(fee);

                            }


                        }
                        final ArrayAdapter<String> adapter = new ArrayAdapter<>(RiderMainActivity.this, android.R.layout.simple_list_item_1, bookingsArray);
                        bookingsLv.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public static String getDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

}
