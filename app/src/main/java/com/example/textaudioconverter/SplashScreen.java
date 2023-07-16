package com.example.textaudioconverter;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Intent splash = new Intent(SplashScreen.this,LoginPage.class);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(splash);
                finish();
            }
        },2000);
    }
}