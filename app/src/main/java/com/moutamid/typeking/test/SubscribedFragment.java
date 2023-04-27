package com.moutamid.typeking.test;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.typeking.R;
import com.moutamid.typeking.databinding.FragmentSubscribedBinding;
import com.moutamid.typeking.models.SubscribeTaskModel;
import com.moutamid.typeking.utilis.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class SubscribedFragment extends Fragment {
    FragmentSubscribedBinding b;
    private static final String TAG = "SubscribeFragment";
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
    Handler handler = new Handler();
    boolean isTimerRunning = false;
    String currentVideoId = "";
    ArrayList<SubscribeTaskModel> subscribeTaskModelArrayList = new ArrayList<>();
    private ProgressDialog progressDialog;
    int currentCounter = 0;
    int currentPoints = 30;

    String totall = "30";
    boolean vipStatus;
    ArrayList<String> linkList = new ArrayList<>();
    int currentClick = 0;

    public SubscribedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        b = FragmentSubscribedBinding.inflate(getLayoutInflater(), container, false);

        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        mProgress = new ProgressDialog(requireContext());
        mProgress.setMessage("Calling Youtube Data API");

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(requireContext().getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        vipStatus = Stash.getBoolean(Constants.VIP_STATUS, false);


        Constants.databaseReference().child(Constants.SUBSCRIBE_TASKS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    subscribeTaskModelArrayList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        SubscribeTaskModel model = dataSnapshot.getValue(SubscribeTaskModel.class);

                        if (snapshot.child(model.getTaskKey()).child(Constants.SUBSCRIBER_PATH).child(Constants.auth().getCurrentUser().getUid()).exists()) {
//                        model.setSubscribed(true);
                        } else {
                            subscribeTaskModelArrayList.add(model);
                        }
                    }

                    if (subscribeTaskModelArrayList.size()>0){
                        int rlow = Integer.parseInt(subscribeTaskModelArrayList.get(currentCounter).getTotalViewTimeQuantity());
                        currentPoints = rlow / 10;
                        Stash.put(Constants.COIN, currentPoints);
                    }

                    progressDialog.dismiss();
                    setDataOnViews(0, false);
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "No data found", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(requireContext(), "No data found", Toast.LENGTH_SHORT).show();
            }
        });

        b.seeOther.setOnClickListener(view -> {
            currentCounter++;

            if (currentCounter >= subscribeTaskModelArrayList.size()) {
                Toast.makeText(requireContext(), "End of task", Toast.LENGTH_SHORT).show();

            } else{
                int rloww = Integer.parseInt(subscribeTaskModelArrayList.get(currentCounter).getTotalViewTimeQuantity());
                currentPoints = rloww / 10;
                Stash.put(Constants.COIN, currentPoints);
                setDataOnViews(currentCounter, false);
            }

        });

        //--------------------------------------------------------------------------------------------
        b.subscribeBtn.setOnClickListener(view -> subscribeUserToChannel());

        return b.getRoot();
    }

    private void subscribeUserToChannel() {

        if (subscribeTaskModelArrayList.size() == 0)
            return;

        currentVideoId = Constants.getVideoId(currentVideoLink);

        getResultsFromApi();
    }

}