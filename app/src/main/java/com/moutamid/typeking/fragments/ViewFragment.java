package com.moutamid.typeking.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fxn.stash.Stash;
import com.moutamid.typeking.R;
import com.moutamid.typeking.constant.Constants;
import com.moutamid.typeking.databinding.FragmentViewBinding;
import com.moutamid.typeking.models.Taskk;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.ArrayList;

public class ViewFragment extends Fragment {
    private ArrayList<Taskk> taskArrayList = new ArrayList<>();
    String videoUrl;
    String url3 = "https://youtu.be/27m980F_obg";
    boolean isAutoPlayEnabled = false;
    FragmentViewBinding binding;
    int currentPoints = 0;
    int currentPosition = 0;
    int currentVideoLength = 0;

    public ViewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentViewBinding.inflate(getLayoutInflater(), container, false);

        currentPoints = Stash.getInt(Constants.CURRENT_COINS);

        return binding.getRoot();
    }
}