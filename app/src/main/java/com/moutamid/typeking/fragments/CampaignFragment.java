package com.moutamid.typeking.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import com.moutamid.typeking.AddVideoActivity;
import com.moutamid.typeking.R;
import com.moutamid.typeking.databinding.FragmentCampaignBinding;

public class CampaignFragment extends Fragment {
    FragmentCampaignBinding binding;
    public CampaignFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCampaignBinding.inflate(getLayoutInflater(), container, false);

        binding.addBtn.setOnClickListener(v -> {
            showDialog();
        });

        return binding.getRoot();
    }

    private void showDialog() {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.new_campaign_dialog);

        Button all = dialog.findViewById(R.id.all);
        Button view = dialog.findViewById(R.id.view);
        Button like = dialog.findViewById(R.id.like);
        Button cancel = dialog.findViewById(R.id.cancel);

        all.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AddVideoActivity.class));
            dialog.dismiss();
        });

        view.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AddVideoActivity.class));
            dialog.dismiss();
        });

        like.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AddVideoActivity.class));
            dialog.dismiss();
        });

        cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);

    }
}