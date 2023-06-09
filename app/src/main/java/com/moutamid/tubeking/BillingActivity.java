package com.moutamid.tubeking;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.PurchaseInfo;
import com.fxn.stash.Stash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.tubeking.databinding.ActivityBillingBinding;
import com.moutamid.tubeking.models.UserDetails;
import com.moutamid.tubeking.utilis.Constants;

public class BillingActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {
    ActivityBillingBinding binding;
    BillingProcessor bp;

    int coin = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBillingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Constants.calledIniti(this);
        bp = BillingProcessor.newBillingProcessor(this, Constants.LICENSE_KEY, this);
        bp.initialize();

        binding.back.setOnClickListener(v -> {
            startActivity(new Intent(BillingActivity.this, MainActivity.class));
            finish();
        });

        if (!Stash.getBoolean(Constants.VIP_STATUS)) {
            Constants.loadIntersAD(BillingActivity.this, BillingActivity.this);
            Constants.showNativeAd(BillingActivity.this, binding.myTemplate);
            Constants.showBannerAd(binding.adView);
        } else {
            binding.myTemplate.setVisibility(View.GONE);
            binding.adView.setVisibility(View.GONE);
        }

        binding.coin4000.setOnClickListener(v -> {
            bp.purchase(BillingActivity.this, Constants.COIN_FOUR_THOUSAND);

        });
        binding.coin25000.setOnClickListener(v -> {
            bp.purchase(BillingActivity.this, Constants.COIN_TWENTY_FIVE_THOUSAND);

        });
        binding.coin60000.setOnClickListener(v -> {
            bp.purchase(BillingActivity.this, Constants.COIN_SIXTY_THOUSAND);

        });
        binding.coin400k.setOnClickListener(v -> {
            bp.purchase(BillingActivity.this, Constants.COIN_FOUR_HUNDRED_THOUSAND);

        });
        binding.coin100k.setOnClickListener(v -> {
            bp.purchase(BillingActivity.this, Constants.COIN_TEN_HUNDRED_THOUSAND);

        });

        Constants.databaseReference().child(Constants.USER).child(Constants.auth().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            UserDetails userDetails = snapshot.getValue(UserDetails.class);
                            binding.coin.setText(userDetails.getCoins() + "");
                            coin = userDetails.getCoins();
                            Stash.put(Constants.CURRENT_COINS, userDetails.getCoins());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(BillingActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }


    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable PurchaseInfo details) {
        if (productId.equals(Constants.COIN_FOUR_THOUSAND))
            buyCoin(4000);
        if (productId.equals(Constants.COIN_TWENTY_FIVE_THOUSAND))
            buyCoin(25000);
        if (productId.equals(Constants.COIN_SIXTY_THOUSAND))
            buyCoin(60000);
        if (productId.equals(Constants.COIN_FOUR_HUNDRED_THOUSAND))
            buyCoin(400000);
        if (productId.equals(Constants.COIN_TEN_HUNDRED_THOUSAND))
            buyCoin(1000000);
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {

    }

    @Override
    public void onBillingInitialized() {

    }

    private void buyCoin(int price) {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Please Wait...");
        dialog.show();
        Constants.databaseReference().child(Constants.USER).child(Constants.auth().getCurrentUser().getUid()).child(Constants.coins)
                .setValue(coin + price).addOnSuccessListener(v -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Thanks for buying this Package", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}