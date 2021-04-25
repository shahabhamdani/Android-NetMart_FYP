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
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.Shahab.netmart.activities.CartActivity;
import com.Shahab.netmart.activities.SettingsActivity;
import com.Shahab.netmart.activities.authentication.LoginActivity;
import com.Shahab.netmart.RiderMainActivity;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RiderMainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap gMap;
    private ImageView onlineBtn, profileIv;
    private double latitude, longitude;
    private TextView userName,userPhone,userEmail;
    String pImage;

    private String myLng, myLat;



    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;


    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_main);


        onlineBtn = findViewById(R.id.onlineIv);

        //drawer
        userName = findViewById(R.id.userNameTv);
        userPhone = findViewById(R.id.phoneTv);
        userEmail = findViewById(R.id.emailTv);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth = FirebaseAuth.getInstance();

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
                        Intent intent1 =  new Intent(RiderMainActivity.this, SettingsActivity.class);
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


        onlineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                navigateToStore(0,0);

            }
        });
    }

    public void navigateToStore(double lat, double lng){

        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lng);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);

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
        hashMap.put("online","false");

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
                        Toast.makeText(RiderMainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        gMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {

                LatLng riderLocation = new LatLng(Double.parseDouble(myLat),Double.parseDouble(myLng));
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(riderLocation);
                markerOptions.title(""+userName.getText());
                markerOptions.snippet(""+userPhone.getText());
                gMap.clear();
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(riderLocation, 17));
                gMap.addMarker((markerOptions)).showInfoWindow();
            }
        });
    }


    private void checkUser(){
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user == null){
            Intent intent = new Intent(RiderMainActivity.this, LoginActivity.class);
            startActivity(intent);;
            finish();
        }
        else{
            loadMyInfo();

        }
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())

                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            String name = ""+ds.child("name").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            String phone = ""+ds.child("phone").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();
                            String email = ""+ds.child("email").getValue();
                            String city = ""+ds.child("city").getValue();
                             myLat = ""+ds.child("latitude").getValue();
                             myLng = ""+ds.child("longitude").getValue();

                             pImage = profileImage;

                            userName = (TextView) findViewById(R.id.userNameTv);
                            userName.setText(name +" ("+accountType+")");

                            userPhone = (TextView) findViewById(R.id.phoneTv);
                            userPhone.setText(phone);

                            userEmail = (TextView) findViewById(R.id.emailTv);
                            userEmail.setText(email);

                            profileIv = findViewById(R.id.hProfileIv);

                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_black).into(profileIv);
                            }
                            catch (Exception e){
                                Toast.makeText(RiderMainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                            }

                            //
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

}
