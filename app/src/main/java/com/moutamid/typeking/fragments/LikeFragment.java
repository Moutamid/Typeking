package com.moutamid.typeking.fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.fxn.stash.Stash;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.Video;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.typeking.R;
import com.moutamid.typeking.databinding.FragmentLikeBinding;
import com.moutamid.typeking.models.LikeTaskModel;
import com.moutamid.typeking.utilis.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class LikeFragment extends Fragment implements EasyPermissions.PermissionCallbacks{

    FragmentLikeBinding binding;
    private static final String TAG = "LikeFragment";

  //  int remainingDailyLimitInt = Utils.getInt(Utils.getDate() + "Like", 0);

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String BUTTON_TEXT = "Call YouTube Data API";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {YouTubeScopes.YOUTUBE_READONLY, YouTubeScopes.YOUTUBE_FORCE_SSL};
    private static final String CLIENT_SECRETS = "client_secret.json";
    private static final Collection<String> SCOPE =
            Arrays.asList("https://www.googleapis.com/auth/youtube.force-ssl", "https://www.googleapis.com/auth/youtube");

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    Video video;

    GoogleAccountCredential mCredential;
    String currentVideoLink;
    ProgressDialog mProgress;
    String currentVideoId = "";
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ArrayList<LikeTaskModel> likeTaskModelArrayList = new ArrayList<>();
    private ProgressDialog progressDialog;
    int currentCounter = 0;
    int currentPoints = 120;
    boolean vipStatus = false;
    public LikeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentLikeBinding.inflate(getLayoutInflater(), container, false);
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        vipStatus = Stash.getBoolean(Constants.VIP_STATUS, false);

        Constants.databaseReference().child(Constants.LIKE_TASKS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    likeTaskModelArrayList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                        LikeTaskModel model = dataSnapshot.getValue(LikeTaskModel.class);

                        if (snapshot.child(model.getTaskKey()).child(Constants.LIKERS_PATH).child(mAuth.getUid()).exists()) {
//                        model.setSubscribed(true);
                        } else {
                            likeTaskModelArrayList.add(model);
                        }


                    }
                    progressDialog.dismiss();
                    //setDataOnViews(0, false);
                } else {
                    Toast.makeText(requireContext(), "No data found", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "No data found", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });

        mProgress = new ProgressDialog(requireContext());
        mProgress.setMessage("Calling Youtube Data API");

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(//TODO: requireContext() is added
                        requireContext().getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        return binding.getRoot();
    }


    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }
}