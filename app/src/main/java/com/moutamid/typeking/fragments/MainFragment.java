package com.moutamid.typeking.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.fxn.stash.Stash;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.typeking.BillingActivity;
import com.moutamid.typeking.R;
import com.moutamid.typeking.VIPActivity;
import com.moutamid.typeking.test.LikedFragment;
import com.moutamid.typeking.test.SubscribedFragment;
import com.moutamid.typeking.utilis.Constants;
import com.moutamid.typeking.databinding.FragmentMainBinding;
import com.moutamid.typeking.models.UserDetails;

public class MainFragment extends Fragment {
    FragmentMainBinding binding;
    ProgressDialog progressDialog;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(getLayoutInflater(), container, false);

        getChildFragmentManager().beginTransaction().replace(R.id.framelayout, new ViewFragment()).commit();
        binding.bottomNav.setSelectedItemId(R.id.nav_view);
        binding.bottomNav.setOnNavigationItemSelectedListener(listener);

        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        Constants.databaseReference().child("user").child(Constants.auth().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            progressDialog.dismiss();
                            UserDetails userDetails = snapshot.getValue(UserDetails.class);
                            if (userDetails.isVipStatus()){
                                binding.promotion.setVisibility(View.GONE);
                                binding.adView.setVisibility(View.GONE);
                            } else {
                                Constants.showBannerAd(binding.adView);
                            }
                            Stash.put(Constants.VIP_STATUS, userDetails.isVipStatus());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        binding.close.setOnClickListener(v -> {
            binding.promotion.setVisibility(View.GONE);
        });

        binding.promotion.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), VIPActivity.class));
        });

        return binding.getRoot();
    }

    BottomNavigationView.OnNavigationItemSelectedListener listener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()){
                case R.id.nav_view:
                    getChildFragmentManager().beginTransaction().replace(R.id.framelayout, new ViewFragment()).commit();
                    break;
                case R.id.nav_subscribe:
                    getChildFragmentManager().beginTransaction().replace(R.id.framelayout, new SubscribedFragment()).commit();
                    break;
                case R.id.nav_like:
                    getChildFragmentManager().beginTransaction().replace(R.id.framelayout, new LikedFragment()).commit();
                    break;
                case R.id.nav_campaign:
                    getChildFragmentManager().beginTransaction().replace(R.id.framelayout, new CampaignFragment()).commit();
                    break;
                default:
                    break;
            }

            return true;
        }
    };

}