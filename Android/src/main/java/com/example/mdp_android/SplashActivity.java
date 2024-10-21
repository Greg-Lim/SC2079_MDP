package com.example.mdp_android;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable(){
            public void run(){
                try {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                }catch(Exception e){
                    e.printStackTrace();
                }finally{
                    finish();
                }

            }

        }, 3000);
    }
}