package com.Shahab.netmart.activities.authentication;

import android.content.Context;
import android.net.ConnectivityManager;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class StatusClass {

    private Context context;
    private String uid;


    public StatusClass(Context context, String uid){

        this.context = context;
        this.uid = uid;
    }

    public void autoOnline(){
        DatabaseReference userStatus =
                FirebaseDatabase.getInstance().getReference("Users/"+uid+"/online");
        userStatus.onDisconnect().setValue("false");
        userStatus.setValue("true");
    }

    public  void isOnline(Boolean status) {

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", ""+status);

        //update value to Database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

}
