package com.Shahab.netmart;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.Shahab.netmart.activities.AdapterProductsListings;
import com.Shahab.netmart.activities.seller.ShopReviewsActivity;
import com.Shahab.netmart.activities.user.MainUserActivity;
import com.Shahab.netmart.activities.user.OrderDetailsUsersActivity;
import com.Shahab.netmart.activities.user.ShopDetailsActivity;
import com.Shahab.netmart.adapters.AdapterCartItem;
import com.Shahab.netmart.adapters.AdapterProductUser;
import com.Shahab.netmart.models.ModelCartItem;
import com.Shahab.netmart.models.ModelProduct;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class CustomPackageActivity extends AppCompatActivity {

    //declare ui views

    public String deliveryFee = "0";
    private TextView filteredProductsTv, cartCountTv;
    private ImageButton cartBtn, backBtn, filterProductBtn;
    private EditText searchProductEt;
    private RecyclerView productsRv;

    private ArrayList<String> storesList;

    private String myLatitude, myLongitude, myPhone;

    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    private ArrayList<ModelProduct> productsList;
    private AdapterProductsListings adapterProductsListings;

    //cart
    private ArrayList<ModelCartItem> cartItemList;
    private AdapterCartItemProductsListings adapterCartItemProductsListings;


    private EasyDB easyDB;

    ArrayList<LatLng> latLngs;
    AlertDialog.Builder builder;
    private ModelProduct modelProduct;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cutom_package);


        //init ui views

        filteredProductsTv = findViewById(R.id.filteredProductsTv);
        cartBtn = findViewById(R.id.cartBtn);
        backBtn = findViewById(R.id.backBtn);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        searchProductEt = findViewById(R.id.searchProductEt);
        productsRv = findViewById(R.id.productsRv);
        cartCountTv = findViewById(R.id.cartCountTv);

        storesList = new ArrayList<String>();
        latLngs = new ArrayList<LatLng>();

        //init progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //get uid of the shop from intent
        firebaseAuth = FirebaseAuth.getInstance();

        modelProduct = new ModelProduct();

        loadMyInfo();
        loadShopProducts();

        //declare it to class level and init in onCreate
        easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();


        // delete cart data whenever user open this activity
        deleteCartData();
        cartCount();

        //search
        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductsListings.getFilter().filter(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //go previous activity
                onBackPressed();
            }
        });

        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show cart dialog



                showCartDialog();
            }
        });


        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CustomPackageActivity.this);
                builder.setTitle("Filter Products:")
                        .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //get selected item
                                String selected = Constants.productCategories1[which];
                                filteredProductsTv.setText(selected);
                                if (selected.equals("All")) {
                                    //load all
                                    loadShopProducts();
                                } else {
                                    //load filtered


                                    adapterProductsListings.getFilter().filter(selected);
                                }
                            }
                        })
                        .show();
            }
        });


    }

    public double allTotalPrice = 0.00;
    //need to access these views in adapter so making public
    public TextView sTotalTv, dFeeTv, allTotalPriceTv;

    RecyclerView cartItemsRv;

    private void showCartDialog() {
        //init list
        cartItemList = new ArrayList<>();

        //inflate cart layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart, null);
        //init views
         cartItemsRv = view.findViewById(R.id.cartItemsRv);
        sTotalTv = view.findViewById(R.id.sTotalTv);
        dFeeTv = view.findViewById(R.id.dFeeTv);
        allTotalPriceTv = view.findViewById(R.id.totalTv);
        Button checkoutBtn = view.findViewById(R.id.checkoutBtn);

        //dialog
        builder = new AlertDialog.Builder(this);
        //set view to dialog
        builder.setView(view);

        EasyDB easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        //get all records from db
        Cursor res = easyDB.getAllData();
        while (res.moveToNext()) {
            String id = res.getString(1);
            String pId = res.getString(2);
            String name = res.getString(3);
            String price = res.getString(4);
            String cost = res.getString(5);
            String quantity = res.getString(6);

            allTotalPrice = allTotalPrice + Double.parseDouble(cost);

            ModelCartItem modelCartItem = new ModelCartItem(
                    "" + id,
                    "" + pId,
                    "" + name,
                    "" + price,
                    "" + cost,
                    "" + quantity
            );

            cartItemList.add(modelCartItem);
        }

        //setup adapter
        adapterCartItemProductsListings = new AdapterCartItemProductsListings(this, cartItemList);
        //set to recyclerview

        getSelectedStoreLocations(cartItemList);

        progressDialog.setMessage("Estimating Delivery Fees");
        progressDialog.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {


                estimateDeliveryFee(latLngs);

                cartItemsRv.setAdapter(adapterCartItemProductsListings);

                dFeeTv.setText("Rs" + deliveryFee);
                sTotalTv.setText("Rs" + String.format("%.2f", allTotalPrice));
                allTotalPriceTv.setText("Rs" + (allTotalPrice + Double.parseDouble(deliveryFee.replace("Rs", ""))));

                //show dialog
                AlertDialog dialog = builder.create();
                dialog.show();

                //reset total price on dialog dismiss
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        allTotalPrice = 0.00;
                    }
                });


            }
        }, 4000);



        //place order
        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //first validate delivery address
                if (myLatitude.equals("") || myLatitude.equals("null") || myLongitude.equals("") || myLongitude.equals("null")) {
                    //user didn't enter address in profile
                    Toast.makeText(CustomPackageActivity.this, "Please enter your address in you profile before placing order...", Toast.LENGTH_SHORT).show();
                    return; //don't proceed further
                }
                if (myPhone.equals("") || myPhone.equals("null")) {
                    //user didn't enter phone number in profile
                    Toast.makeText(CustomPackageActivity.this, "Please enter your phone number in you profile before placing order...", Toast.LENGTH_SHORT).show();
                    return; //don't proceed further
                }
                if (cartItemList.size() == 0) {
                    //cart list is empty
                    Toast.makeText(CustomPackageActivity.this, "No item in cart", Toast.LENGTH_SHORT).show();
                    return; //don't proceed further
                }


                for (int i=0; i<storesList.size(); i++){

                    submitOrder(storesList.get(i), i);

                }
            }
        });

    }

    private void submitOrder(final String shopUid, int i) {
        //show progress dialog
        progressDialog.setMessage("Placing orders...");
        progressDialog.show();

        //for order id and order time
        final String timestamp = ""+System.currentTimeMillis();

        String cost = caolculateOrderCost(shopUid);

        ArrayList<LatLng> currentUserLatLngs = new ArrayList<>();
        currentUserLatLngs.add(latLngs.get(i));
        deliveryFee = "";
        estimateDeliveryFee(currentUserLatLngs);

        //setup oder data
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("orderId", ""+timestamp);
        hashMap.put("orderTime", ""+timestamp);
        hashMap.put("orderStatus", "In Progress"); //In Progress/Completed/Cancelled
        hashMap.put("orderCost", ""+cost);
        hashMap.put("orderBy", ""+firebaseAuth.getUid());
        hashMap.put("orderTo", ""+shopUid);
        hashMap.put("latitude", ""+myLatitude);
        hashMap.put("longitude", ""+myLongitude);
        hashMap.put("deliveryFee", ""+deliveryFee);
        hashMap.put("riderStatus", ""+"NotAssigned");


        //add to db
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Orders");
        ref.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //order info added now add order items
                        for ( int i=0; i<cartItemList.size(); i++){

                            for (int j=0; j<productsList.size(); j++){

                                if (productsList.get(j).getProductId().equals(cartItemList.get(i).getpId())){

                                    if (productsList.get(j).getUid().equals(shopUid)){

                                        String pId = cartItemList.get(i).getpId();
                                        String id = cartItemList.get(i).getId();
                                        String cost = cartItemList.get(i).getCost();
                                        String name = cartItemList.get(i).getName();
                                        String price = cartItemList.get(i).getPrice();
                                        String quantity = cartItemList.get(i).getQuantity();

                                        HashMap<String, String> hashMap1 = new HashMap<>();
                                        hashMap1.put("pId", pId);
                                        hashMap1.put("name", name);
                                        hashMap1.put("cost", cost);
                                        hashMap1.put("price", price);
                                        hashMap1.put("quantity", quantity);

                                        ref.child(timestamp).child("Items").child(pId).setValue(hashMap1);

                                    }

                                }

                            }
                        }

                        progressDialog.dismiss();
                        Toast.makeText(CustomPackageActivity.this, "Order Placed Successfully...", Toast.LENGTH_SHORT).show();
                        prepareNotificationMessage(timestamp, shopUid);


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed placing order
                        progressDialog.dismiss();
                        Toast.makeText(CustomPackageActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String caolculateOrderCost(String shopUid) {

        double cost=0;

        for ( int i=0; i<cartItemList.size(); i++){

            for (int j=0; j<productsList.size(); j++){

                if (productsList.get(j).getProductId().equals(cartItemList.get(i).getpId())){

                    if (productsList.get(j).getUid().equals(shopUid)){

                         cost += Double.parseDouble(cartItemList.get(i).getCost());

                    }
                }
            }
        }

        return ""+cost;
    }

    private void toast(String s) {

        Toast.makeText(CustomPackageActivity.this, ""+s, Toast.LENGTH_SHORT).show();

    }

    public static <String> ArrayList<String> removeDuplicates(ArrayList<String> list) {
        Set<String> set = new LinkedHashSet<>(list);
        return new ArrayList<String>(set);
    }

    private void getSelectedStoreLocations(ArrayList<ModelCartItem> cartItemList) {

        if (cartItemList.size() == 0) {
            //cart list is empty
            Toast.makeText(CustomPackageActivity.this, "No item in cart", Toast.LENGTH_SHORT).show();
            return; //don't proceed further
        }

        try {

            for (int i = 0; i < cartItemList.size(); i++) {

                for (int j = 0; j < productsList.size(); j++) {

                    if (productsList.get(j).getProductId().equals(cartItemList.get(i).getpId())) {

                        storesList.add(productsList.get(j).getUid());

                    }

                }

            }
        } catch (Exception e) {
            Toast.makeText(CustomPackageActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        storesList = removeDuplicates(storesList);


        progressDialog.show();


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("accountType").equalTo("Seller")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds : dataSnapshot.getChildren()) {

                            for (int i = 0; i < storesList.size(); i++) {

                                if ((storesList.get(i).equals("" + ds.child("uid").getValue()))) {

                                    String lat = "" + ds.child("latitude").getValue();
                                    String lng = "" + ds.child("longitude").getValue();

                                    LatLng latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                                    latLngs.add(latLng);
                                }

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }


    private void deleteCartData() {
        easyDB.deleteAllDataFromTable();//delete all records from cart
    }

    public void cartCount() {
        //keep it public so we can access in adapter
        //get cart count
        int count = easyDB.getAllData().getCount();
        if (count <= 0) {
            //no item in cart, hide cart count textview
            cartCountTv.setVisibility(View.GONE);
        } else {
            //have items in cart, show cart count textview and set count
            cartCountTv.setVisibility(View.VISIBLE);
            cartCountTv.setText("" + count);//concatenate with string, because we cant set integer in textview
        }
    }

    private void loadShopProducts() {
        //init list
        productsList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("accountType").equalTo("Seller")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //clear list before adding items
                        productsList.clear();
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {

                            if(ds.child("shopOpen").getValue().equals("true")) {

                                for (DataSnapshot ds2 : ds.child("Products").getChildren()) {

                                    ///shops in 10km range
                                    double dist = calculateDistanceInKilometer(Double.parseDouble(myLatitude),Double.parseDouble(myLongitude),Double.parseDouble(""+ds.child("latitude").getValue()),Double.parseDouble(""+ds.child("longitude").getValue()));
                                    if(dist < 20){
                                        modelProduct = ds2.getValue(ModelProduct.class);
                                        productsList.add(modelProduct);
                                    }
                                }
                            }
                        }
                        //setup adapter
                        adapterProductsListings = new AdapterProductsListings(CustomPackageActivity.this, productsList);
                        //set adapter
                        productsRv.setAdapter(adapterProductsListings);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            //get user data
                            String name = "" + ds.child("name").getValue();
                            String email = "" + ds.child("email").getValue();
                            myPhone = "" + ds.child("phone").getValue();
                            String profileImage = "" + ds.child("profileImage").getValue();
                            String accountType = "" + ds.child("accountType").getValue();
                            String city = "" + ds.child("city").getValue();
                            myLatitude = "" + ds.child("latitude").getValue();
                            myLongitude = "" + ds.child("longitude").getValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public final static double AVERAGE_RADIUS_OF_EARTH_KM = 6371;

    public double calculateDistanceInKilometer(double userLat, double userLng,
                                            double venueLat, double venueLng) {

        double latDistance = Math.toRadians(userLat - venueLat);
        double lngDistance = Math.toRadians(userLng - venueLng);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(venueLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (double) (Math.round(AVERAGE_RADIUS_OF_EARTH_KM * c));
    }

    public void  estimateDeliveryFee(ArrayList<LatLng> latLngs) {

        if (myLatitude.equals("") || myLatitude.equals("null") || myLongitude.equals("") || myLongitude.equals("null")) {
            //user didn't enter address in profile
            Toast.makeText(CustomPackageActivity.this, "Please enter your address in you profile...", Toast.LENGTH_SHORT).show();
            return; //don't proceed further
        }


        //double maxDist = 0;
        double dist = 0;
        //calculating cost per minute
        double cpk = 10;
        double totalFee=0;

        //comparing latlngs
        for(int i=0; i<latLngs.size(); i++){

            //getting distance in kilometers
            dist = calculateDistanceInKilometer(Double.parseDouble(myLatitude),Double.parseDouble(myLongitude),latLngs.get(i).latitude, latLngs.get(i).longitude);

            //calculating max distance
           // if (dist > maxDist){
            //   maxDist = dist;
            //}


            //minimum fee is 30
            if (dist*cpk < 30){

                totalFee += 30;
            }
            else {
                totalFee += dist*cpk;
            }
        }

        deliveryFee = ""+totalFee;
        //deliveryFee = ""+maxDist*cpk;

        progressDialog.dismiss();
    }

    private void prepareNotificationMessage(String orderId, String shopUid){
        //When user places order, send notification to seller

        //prepare data for notification
        String NOTIFICATION_TOPIC = "/topics/" +Constants.FCM_TOPIC; //must be same as subscribed by user
        String NOTIFICATION_TITLE = "New Order "+ orderId;
        String NOTIFICATION_MESSAGE = "Congratulations...! You have new order.";
        String NOTIFICATION_TYPE = "NewOrder";

        //prepare json (what to send and where to send)
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            //what to send
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid", firebaseAuth.getUid()); //since we are logged in as buyer to place order so current user uid is buyer uid
            notificationBodyJo.put("sellerUid", shopUid);
            notificationBodyJo.put("orderId", orderId);
            notificationBodyJo.put("notificationTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage", NOTIFICATION_MESSAGE);
            //where to send
            notificationJo.put("to", NOTIFICATION_TOPIC); //to all who subscribed to this topic
            notificationJo.put("data", notificationBodyJo);
        }
        catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        sendFcmNotification(notificationJo, orderId, shopUid);
    }

    private void sendFcmNotification(JSONObject notificationJo, final String orderId,final String shopUid) {
        //send volley request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                onBackPressed();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //if failed sending fcm, still start order details activity
                onBackPressed();
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
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

}