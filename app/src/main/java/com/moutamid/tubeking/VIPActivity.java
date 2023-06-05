package com.moutamid.tubeking;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.PurchaseInfo;
import com.fxn.stash.Stash;
import com.google.common.collect.ImmutableList;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.tubeking.databinding.ActivityVipactivityBinding;
import com.moutamid.tubeking.models.UserDetails;
import com.moutamid.tubeking.utilis.Constants;

import java.util.ArrayList;
import java.util.List;

public class VIPActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {
    ActivityVipactivityBinding binding;
    BillingProcessor bp;

    static final String TAG = "InAppPurchaseTag";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVipactivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Constants.calledIniti(this);

        bp = BillingProcessor.newBillingProcessor(this, Constants.LICENSE_KEY, this);
        bp.initialize();


        if (!Stash.getBoolean(Constants.VIP_STATUS)){
            Constants.loadIntersAD(VIPActivity.this, VIPActivity.this);
            Constants.showNativeAd(VIPActivity.this, binding.myTemplate);
            Constants.showBannerAd(binding.adView);
        } else {
            binding.myTemplate.setVisibility(View.GONE);
            binding.adView.setVisibility(View.GONE);
        }

        binding.monthSubscription.setOnClickListener(v -> {
            bp.subscribe(VIPActivity.this, Constants.VIP_MONTH);
        });
        binding.ThreeMonthSubscription.setOnClickListener(v -> {
            bp.subscribe(VIPActivity.this, Constants.VIP_YEAR);
        });


        binding.googlePlay.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/store/account/subscriptions"));
            startActivity(intent);
        });

        binding.back.setOnClickListener(v -> {
            startActivity(new Intent(VIPActivity.this, MainActivity.class));
            finish();
        });

        binding.coin.setOnClickListener(v -> {
            startActivity(new Intent(this, BillingActivity.class));
        });

        Constants.databaseReference().child(Constants.USER).child(Constants.auth().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            UserDetails userDetails = snapshot.getValue(UserDetails.class);
                            binding.coin.setText(userDetails.getCoins()+"");
                            Stash.put(Constants.CURRENT_COINS, userDetails.getCoins());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(VIPActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void VipUpdate() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Please Wait...");
        dialog.show();
        Constants.databaseReference().child(Constants.USER).child(Constants.auth().getCurrentUser().getUid()).child(Constants.vipStatus)
                .setValue(true).addOnSuccessListener(v -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Thanks for buying this Package", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show(); 
                });
    }

    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable PurchaseInfo details) {
        VipUpdate();
        Toast.makeText(this, "Purchased", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {
        Toast.makeText(VIPActivity.this, "onBillingError: code: " + errorCode + " \n" + error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBillingInitialized() {

    }

    @Override
    protected void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }

}