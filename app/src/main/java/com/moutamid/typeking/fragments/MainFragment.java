package com.moutamid.typeking.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.moutamid.typeking.R;
import com.moutamid.typeking.databinding.FragmentMainBinding;

public class MainFragment extends Fragment {
    FragmentMainBinding binding;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(getLayoutInflater(), container, false);

        getChildFragmentManager().beginTransaction().replace(R.id.framelayout, new ViewFragment()).commit();
        binding.bottomNav.setSelectedItemId(R.id.nav_view);
        binding.bottomNav.setOnNavigationItemSelectedListener(listener);

        binding.close.setOnClickListener(v -> {
            binding.promotion.setVisibility(View.GONE);
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
                    getChildFragmentManager().beginTransaction().replace(R.id.framelayout, new SubscribeFragment()).commit();
                    break;
                case R.id.nav_like:
                    getChildFragmentManager().beginTransaction().replace(R.id.framelayout, new LikeFragment()).commit();
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