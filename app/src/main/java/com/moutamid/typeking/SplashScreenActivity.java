package com.moutamid.typeking;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.moutamid.typeking.constant.Constants;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(() -> {
            if (Constants.auth().getCurrentUser() != null){
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                startActivity(new Intent(this, SignInActivity.class));
                finish();
            }
        }, 2000);

    }
}