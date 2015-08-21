package com.example.ashu4642.mcommunicate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.example.ashu4642.mcommunicate.P2P.LoginActivity;

public class MainActivity extends Activity {
    static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getApplication();

            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
    }
