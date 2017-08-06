package com.wareproz.mac.gravydriver;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

public class SplashScreenActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; //3 second

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash_screen);

        Handler handler = new Handler();

        // run a thread after 3 seconds to start the home screen
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
            Intent mIntent = new Intent(SplashScreenActivity.this, LoginActivity.class);
            startActivity(mIntent);
            finish();
            }

        }, SPLASH_DURATION);
    }
}
