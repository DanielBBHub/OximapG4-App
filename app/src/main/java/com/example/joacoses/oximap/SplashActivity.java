package com.example.joacoses.oximap;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    ImageView logo, appName, splashImg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introductory);

        logo = findViewById(R.id.logo);
        //  appName = findViewById(R.id.app_name);
        splashImg = findViewById(R.id.image);
        //    lottieAnimationView = findViewById(R.id.lottie);

        //desplazare hacia arriba
        splashImg.animate().translationY(-1600).setDuration(1000).setStartDelay(2000);
        logo.animate().translationY(1400).setDuration(1000).setStartDelay(2000);
        // appName.animate().translationY(1400).setDuration(1000).setStartDelay(2000);
        //lottieAnimationView.animate().translationY(1400).setDuration(1000).setStartDelay(4000);
        //desplazarse hacia abajo
        /*startActivity(new Intent(this,login.class));*/

        getSupportActionBar().hide();




        //Code to start timer and take action after the timer ends
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do any action here. Now we are moving to next page
                Intent mySuperIntent = new Intent(SplashActivity.this, Login.class);
                startActivity(mySuperIntent);

                //This 'finish()' is for exiting the app when back button pressed from Home page which is ActivityHome
                finish();

            }
        }, 2800);


    }

}
