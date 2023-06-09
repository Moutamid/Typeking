package com.moutamid.tubeking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.fxn.stash.Stash;
import com.moutamid.tubeking.utilis.Constants;
import com.moutamid.tubeking.databinding.ActivityAddVideoBinding;

public class AddVideoActivity extends AppCompatActivity {
    ActivityAddVideoBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddVideoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Constants.calledIniti(this);
        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (!Stash.getBoolean(Constants.VIP_STATUS)){
            Constants.loadIntersAD(AddVideoActivity.this, AddVideoActivity.this);
            Constants.showNativeAd(AddVideoActivity.this, binding.myTemplate);
            Constants.showBannerAd(binding.adView);
        } else {
            binding.myTemplate.setVisibility(View.GONE);
            binding.adView.setVisibility(View.GONE);
        }

        binding.button.setOnClickListener(v -> {
            if (valid()){
                Stash.put(Constants.RECENT_LINK, binding.link.getText().toString());
                startActivity(new Intent(this, CreateCampaignActivity.class));
            }
        });

        if (!Stash.getString(Constants.RECENT_IMAGE, "").isEmpty()){
            binding.recentVideo.setVisibility(View.VISIBLE);
            Glide.with(this).load(Stash.getString(Constants.RECENT_IMAGE)).into(binding.recentVideo);
        } else {
            binding.recentVideo.setVisibility(View.GONE);
        }

        binding.recentVideo.setOnClickListener(v -> {
            startActivity(new Intent(this, CreateCampaignActivity.class));
        });

    }

    private boolean valid() {
        if (binding.link.getText().toString().isEmpty()){
            binding.link.setError("Please add a url");
            return false;
        } if (Constants.getVideoId(binding.link.getText().toString()).isEmpty()){
            binding.link.setError("Url is not valid");
            return false;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}