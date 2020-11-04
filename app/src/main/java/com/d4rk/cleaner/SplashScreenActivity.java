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
            try {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }, 2000);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mWaitHandler.removeCallbacksAndMessages(null);
    }
}