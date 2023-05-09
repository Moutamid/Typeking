package com.moutamid.typeking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.fxn.stash.Stash;
import com.moutamid.typeking.databinding.ActivityFaqBinding;
import com.moutamid.typeking.utilis.Constants;

public class FaqActivity extends AppCompatActivity {
    ActivityFaqBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFaqBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Constants.calledIniti(this);
        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (!Stash.getBoolean(Constants.VIP_STATUS)){
            Constants.loadIntersAD(FaqActivity.this, FaqActivity.this);
           // Constants.showBannerAd(binding.adView);
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                startActivity(new Intent(FaqActivity.this, MainActivity.class));
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}