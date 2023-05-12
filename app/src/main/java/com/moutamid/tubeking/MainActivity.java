package com.moutamid.tubeking;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fxn.stash.Stash;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.tubeking.utilis.Constants;
import com.moutamid.tubeking.databinding.ActivityMainBinding;
import com.moutamid.tubeking.fragments.MainFragment;
import com.moutamid.tubeking.models.UserDetails;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements OnUserEarnedRewardListener {
    ActivityMainBinding binding;
    int coin = 0;
    private RewardedInterstitialAd rewardedInterstitialAd;
    public static AdRequest adRequest = new AdRequest.Builder().build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Constants.checkApp(this);
        Constants.calledIniti(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                binding.drawLayout,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.navView.setNavigationItemSelectedListener(listener);
        binding.drawLayout.addDrawerListener(toggle);
        toggle.syncState();

        updateNavHead(binding.navView);

        binding.toogle.setOnClickListener(v -> {
            binding.drawLayout.openDrawer(GravityCompat.START);
        });

        binding.coin.setOnClickListener(v -> {
            startActivity(new Intent(this, BillingActivity.class));
        });

        Constants.databaseReference().child(Constants.USER).child(Constants.auth().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            UserDetails userDetails = snapshot.getValue(UserDetails.class);
                            binding.coin.setText(userDetails.getCoins()+"");
                            coin = userDetails.getCoins();
                            Stash.put(Constants.CURRENT_COINS, userDetails.getCoins());
                            if (!userDetails.isVipStatus()){
                                Constants.loadIntersAD(MainActivity.this, MainActivity.this);
                            }
                            Stash.put(Constants.VIP_STATUS, userDetails.isVipStatus());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        getSupportFragmentManager().beginTransaction().add(R.id.framelayout, new MainFragment()).commit();

    }

    NavigationView.OnNavigationItemSelectedListener listener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch(item.getItemId()){
                case R.id.nav_logout:
                    Constants.auth().signOut();
                    startActivity(new Intent(MainActivity.this, SplashScreenActivity.class));
                    finish();
                    break;
                case R.id.nav_vip:
                    startActivity(new Intent(MainActivity.this, VIPActivity.class));
                    finish();
                    break;
                case R.id.nav_buy:
                    startActivity(new Intent(MainActivity.this, BillingActivity.class));
                    finish();
                    break;
                case R.id.nav_faq:
                    startActivity(new Intent(MainActivity.this, FaqActivity.class));
                    finish();
                    break;
                case R.id.nav_privacy:
                    binding.drawLayout.closeDrawer(GravityCompat.START);
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://tubekingapp.blogspot.com/2023/05/privacy-policy-1.html"));
                    startActivity(browserIntent);
                    break;
                case R.id.nav_rate:
                    binding.drawLayout.closeDrawer(GravityCompat.START);
                    showDialog();
                    break;
                case R.id.nav_ads:
                    binding.drawLayout.closeDrawer(GravityCompat.START);
                    Toast.makeText(MainActivity.this, "Ad is Loading...", Toast.LENGTH_SHORT).show();
                    showRewardAd();
                    break;
                case R.id.nav_feedback:
                    binding.drawLayout.closeDrawer(GravityCompat.START);
                    try {
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:"));
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"tubekingapp@gmail.com"});
                        intent.putExtra(Intent.EXTRA_SUBJECT, "App feedback");
                        startActivity(intent);
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(MainActivity.this, "There are no email client installed on your device.", Toast.LENGTH_LONG).show();
                    }
                    break;
            }

            return true;
        }
    };

    private void showRewardAd() {

        String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.i(TAG, android_id);
        List<String> testDeviceIds = Arrays.asList(android_id);
        RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
        MobileAds.setRequestConfiguration(configuration);
        MobileAds.initialize(this, initializationStatus -> {
            loadAd();
        });
    }

    private void loadAd() {
        RewardedInterstitialAd.load(MainActivity.this, getString(R.string.Reward_ID),
                new AdRequest.Builder().build(),  new RewardedInterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(RewardedInterstitialAd ad) {
                        Log.d(TAG, "Ad was loaded.");
                        rewardedInterstitialAd = ad;
                        showAd();
                    }
                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        Log.d(TAG, loadAdError.toString());
                        Toast.makeText(MainActivity.this, "Ad Load Failed", Toast.LENGTH_SHORT).show();
                        rewardedInterstitialAd = null;
                    }
                });
    }

    private void showAd() {
        rewardedInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d(TAG, "Ad was clicked.");
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                // Set the ad reference to null so you don't show the ad a second time.
                Log.d(TAG, "Ad dismissed fullscreen content.");
                rewardedInterstitialAd = null;
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                // Called when ad fails to show.
                Log.e(TAG, "Ad failed to show fullscreen content.");
                rewardedInterstitialAd = null;
            }

            @Override
            public void onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d(TAG, "Ad recorded an impression.");
            }

            @Override
            public void onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Log.d(TAG, "Ad showed fullscreen content.");
            }
        });

        rewardedInterstitialAd.show(MainActivity.this, MainActivity.this);
    }

    private void showDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.rate_dialog);

        Button rate = dialog.findViewById(R.id.rate);
        Button close = dialog.findViewById(R.id.close);

        AtomicInteger starCount = new AtomicInteger();
        starCount.set(0);

        ImageView star1, star2, star3, star4, star5;

        star1 = dialog.findViewById(R.id.star1);
        star2 = dialog.findViewById(R.id.star2);
        star3 = dialog.findViewById(R.id.star3);
        star4 = dialog.findViewById(R.id.star4);
        star5 = dialog.findViewById(R.id.star5);

        close.setOnClickListener(v -> dialog.dismiss());

        star1.setOnClickListener(v -> {
            star1.setImageDrawable(getResources().getDrawable(R.drawable.star_rate));
            star2.setImageDrawable(getResources().getDrawable(R.drawable.star));
            star3.setImageDrawable(getResources().getDrawable(R.drawable.star));
            star4.setImageDrawable(getResources().getDrawable(R.drawable.star));
            star5.setImageDrawable(getResources().getDrawable(R.drawable.star));
            starCount.set(1);
        });
        star2.setOnClickListener(v -> {
            star1.setImageDrawable(getResources().getDrawable(R.drawable.star_rate));
            star2.setImageDrawable(getResources().getDrawable(R.drawable.star_rate));
            star3.setImageDrawable(getResources().getDrawable(R.drawable.star));
            star4.setImageDrawable(getResources().getDrawable(R.drawable.star));
            star5.setImageDrawable(getResources().getDrawable(R.drawable.star));
            starCount.set(2);
        });
        star3.setOnClickListener(v -> {
            star1.setImageDrawable(getResources().getDrawable(R.drawable.star_rate));
            star2.setImageDrawable(getResources().getDrawable(R.drawable.star_rate));
            star3.setImageDrawable(getResources().getDrawable(R.drawable.star_rate));
            star4.setImageDrawable(getResources().getDrawable(R.drawable.star));
            star5.setImageDrawable(getResources().getDrawable(R.drawable.star));
            starCount.set(3);
        });
        star4.setOnClickListener(v -> {
            star1.setImageDrawable(getResources().getDrawable(R.drawable.star_rate));
            star2.setImageDrawable(getResources().getDrawable(R.drawable.star_rate));
            star3.setImageDrawable(getResources().getDrawable(R.drawable.star_rate));
            star4.setImageDrawable(getResources().getDrawable(R.drawable.star_rate));
            star5.setImageDrawable(getResources().getDrawable(R.drawable.star));
            starCount.set(4);
        });
        star5.setOnClickListener(v -> {
            star1.setImageDrawable(getResources().getDrawable(R.drawable.star_rate));
            star2.setImageDrawable(getResources().getDrawable(R.drawable.star_rate));
            star3.setImageDrawable(getResources().getDrawable(R.drawable.star_rate));
            star4.setImageDrawable(getResources().getDrawable(R.drawable.star_rate));
            star5.setImageDrawable(getResources().getDrawable(R.drawable.star_rate));
            starCount.set(5);
        });

        rate.setOnClickListener(v -> {
            final String appPackageName = getPackageName();
            Constants.databaseReference().child(Constants.USER).child(Constants.auth().getCurrentUser().getUid()).child(Constants.coins)
                    .setValue(coin + 500).addOnSuccessListener(vv -> {
                        Toast.makeText(this, "Thanks for rating our app", Toast.LENGTH_SHORT).show();
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.Dialog;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void updateNavHead(NavigationView navView) {
        View Header = navView.getHeaderView(0);
        TextView headerName = Header.findViewById(R.id.name_header);
        TextView headerEmail = Header.findViewById(R.id.email_header);
        CircleImageView headerImage = Header.findViewById(R.id.img_nav_logo);

        FirebaseUser user = Constants.auth().getCurrentUser();
        headerName.setText(user.getDisplayName());
        headerEmail.setText(user.getEmail());
        Glide.with(this).load(user.getPhotoUrl()).placeholder(R.drawable.profile_icon).into(headerImage);

    }

    @Override
    public void onBackPressed() {
        if (binding.drawLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
        Log.i(TAG, "User earned reward.");
        Constants.databaseReference().child(Constants.USER).child(Constants.auth().getCurrentUser().getUid()).child(Constants.coins)
                .setValue(coin + 10).addOnSuccessListener(v -> {
                    Toast.makeText(this, "Thanks for rating our app", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}