package com.moutamid.typeking.test;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.moutamid.typeking.R;
import com.moutamid.typeking.databinding.FragmentLikedBinding;
import com.moutamid.typeking.utilis.Constants;
import com.moutamid.typeking.utilis.YouTubeAPI;

import java.io.IOException;

public class LikedFragment extends Fragment {
    FragmentLikedBinding binding;

    public LikedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLikedBinding.inflate(getLayoutInflater(), container, false);

        binding.likeBtn.setOnClickListener(v -> {
            GoogleSignInClient signInClient = GoogleSignIn.getClient(requireContext(), GoogleSignInOptions.DEFAULT_SIGN_IN);

            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());
            if (account != null) {
                // The user is already signed in, so you can use the account to authenticate with the YouTube API
                Log.d("Checking124", "Logged In : " + account.getId());
                String apiKey = getString(R.string.youtube_api_key);
                YouTubeAPI youtubeApi = new YouTubeAPI(account, apiKey);
                boolean t = youtubeApi.hasLikedVideo(Constants.getVideoId("https://youtu.be/QFAO-Kn-9Gg"));
                Log.d("Checking124", "Liked/Not : " + t);
            } else {
                // The user is not signed in, so you'll need to prompt them to sign in first
                Intent signInIntent = signInClient.getSignInIntent();
                startActivityForResult(signInIntent, Constants.RC_SIGN_IN);
            }
        });

        return binding.getRoot();
    }
}