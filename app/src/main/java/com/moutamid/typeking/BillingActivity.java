package com.moutamid.typeking;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.PurchaseInfo;
import com.fxn.stash.Stash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.typeking.databinding.ActivityBillingBinding;
import com.moutamid.typeking.models.UserDetails;
import com.moutamid.typeking.utilis.Constants;

public class BillingActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler  {
    ActivityBillingBinding binding;
    BillingProcessor bp;

    int coin = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBillingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bp = BillingProcessor.newBillingProcessor(this, Constants.LICENSE_KEY, this);
        bp.initialize();

        binding.back.setOnClickListener(v -> {
            startActivity(new Intent(BillingActivity.this, MainActivity.class));
            finish();
        });

        binding.coin4000.setOnClickListener(v -> {
            buyCoin();
        });

        Constants.databaseReference().child(Constants.USER).child(Constants.auth().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            UserDetails userDetails = snapshot.getValue(UserDetails.class);
                            binding.coin.setText(userDetails.getCoins()+"");
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
        Toast.makeText(this, "Purchased", Toast.LENGTH_SHORT).show();
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

    private void buyCoin() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Please Wait...");
        dialog.show();
        Constants.databaseReference().child(Constants.USER).child(Constants.USERID).child(Constants.coins)
                .setValue(coin + 4000).addOnSuccessListener(v -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Thanks for buying this Package", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}