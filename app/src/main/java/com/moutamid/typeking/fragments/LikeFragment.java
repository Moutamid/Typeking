package com.moutamid.typeking.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moutamid.typeking.R;
import com.moutamid.typeking.databinding.FragmentLikeBinding;


public class LikeFragment extends Fragment {

    FragmentLikeBinding binding;

    public LikeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentLikeBinding.inflate(getLayoutInflater(), container, false);


        return binding.getRoot();
    }
}