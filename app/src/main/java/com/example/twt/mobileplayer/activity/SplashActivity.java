package com.example.twt.mobileplayer.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;

import com.example.twt.mobileplayer.R;

public class SplashActivity extends Activity {
    private static final String TAG = "SplashActivity";
    private Handler mHandler = new Handler();
    private boolean isFisrt = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startToMain();
            }
        }, 2000);
    }

    private void startToMain() {
        if (isFisrt) {
            isFisrt=false;
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        startToMain();

        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
