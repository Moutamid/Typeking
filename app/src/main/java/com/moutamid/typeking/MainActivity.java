package com.moutamid.typeking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fxn.stash.Stash;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.typeking.utilis.Constants;
import com.moutamid.typeking.databinding.ActivityMainBinding;
import com.moutamid.typeking.fragments.MainFragment;
import com.moutamid.typeking.models.UserDetails;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
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
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
                    startActivity(browserIntent);
                    break;
                case R.id.nav_rate:
                    final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                    break;
                case R.id.nav_feedback:
                    try {
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:"));
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"example.yahoo.com"});
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
}