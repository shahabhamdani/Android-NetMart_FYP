package com.Shahab.netmart;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashScreen extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        //calling splash screen
        splashScreen();
    }
    ////////////////////////////////////////////////////////////////////////////////////////////
    //Splash Screen
    public void splashScreen(){

        //variables
        Animation topAnim, bottomAnim;
        ImageView image, logo;
        TextView slogan;

        //animations
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this,R.anim.bottom_animation);

        //hooks
        image = findViewById(R.id.SplashLogo);
        logo = findViewById(R.id.SplashSlogan);
        slogan = findViewById(R.id.slogan);

        //set animation
        image.setAnimation(topAnim);
        logo.setAnimation(bottomAnim);
        slogan.setAnimation(bottomAnim);
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////

}