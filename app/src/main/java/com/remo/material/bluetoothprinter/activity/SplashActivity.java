package com.remo.material.bluetoothprinter.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

import com.remo.material.bluetoothprinter.R;
import com.remo.material.bluetoothprinter.interfaces.ListenerInterface;
import com.remo.material.bluetoothprinter.utils.AsyncTasker;

import static com.remo.material.bluetoothprinter.activity.MainActivity.MyPREFERENCES;

public class SplashActivity extends AppCompatActivity {

    SharedPreferences sharedpreferences;
    Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        AsyncTasker asyncTasker = new AsyncTasker(SplashActivity.this, new ListenerInterface() {
            @Override
            public void onCompleted() {
                sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                if (sharedpreferences.getString("TRIP", "").equals("")) {
                    intent = new Intent(SplashActivity.this, TripSelectActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                }
                /*Toast.makeText(SplashActivity.this, Settings.Secure.getString(getContentResolver(),
                        Settings.Secure.ANDROID_ID) , Toast.LENGTH_SHORT).show();*/
                startActivity(intent);
                finish();
            }
        });
        asyncTasker.execute();

    }
}
