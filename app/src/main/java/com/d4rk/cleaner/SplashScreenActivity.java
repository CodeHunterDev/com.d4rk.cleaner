package com.d4rk.cleaner;

import android.content.Intent;
import android.os.Handler;
import android.app.Activity;
import android.os.Bundle;

public class SplashScreenActivity extends Activity {
    private final Handler mWaitHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mWaitHandler.postDelayed(() -> {

            //The following code will execute after the 5 seconds.

            try {

                //Go to next page i.e, start the next activity.
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);

                //Let's Finish Splash Activity since we don't want to show this when user press back button.
                finish();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }, 2000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Remove all the callbacks otherwise navigation will execute even after activity is killed or closed.
        mWaitHandler.removeCallbacksAndMessages(null);
    }
}