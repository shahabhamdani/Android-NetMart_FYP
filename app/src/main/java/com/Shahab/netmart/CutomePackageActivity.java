package com.Shahab.netmart;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class CutomePackageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cutome_package);

        Spinner mySpinner1 = (Spinner) findViewById(R.id.spinnerCategory);
        ArrayAdapter<String> myAdapter1 = new ArrayAdapter<String>(CutomePackageActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.Category));
        myAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner1.setAdapter(myAdapter1);

        Spinner mySpinner2 = (Spinner) findViewById(R.id.spinnerBrand);
        ArrayAdapter<String> myAdapter2 = new ArrayAdapter<String>(CutomePackageActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.Brand));
        myAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner2.setAdapter(myAdapter2);

        Spinner mySpinner3 = (Spinner) findViewById(R.id.spinnerstoreName);
        ArrayAdapter<String> myAdapter3 = new ArrayAdapter<String>(CutomePackageActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.storeName));
        myAdapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner3.setAdapter(myAdapter3);



    }
}