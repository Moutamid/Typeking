package com.moutamid.typeking.fragments;

import static android.app.Activity.RESULT_OK;
import static com.bumptech.glide.Glide.with;
import static com.bumptech.glide.load.engine.DiskCacheStrategy.DATA;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.fxn.stash.Stash;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionSnippet;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.typeking.R;
import com.moutamid.typeking.databinding.FragmentSubscribeBinding;
import com.moutamid.typeking.models.SubscribeTaskModel;
import com.moutamid.typeking.services.ForegroundService;
import com.moutamid.typeking.utilis.Constants;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class SubscribeFragment extends Fragment implements EasyPermissions.PermissionCallbacks {
    FragmentSubscribeBinding b;
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

    public SubscribeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        b = FragmentSubscribeBinding.inflate(getLayoutInflater(), container, false);
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

        Random r = new Random();
        int low = 30;
        int high = 210;
        currentPoints = r.nextInt(high-low) + low;
        Stash.put(Constants.COIN, currentPoints);
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
            Random rr = new Random();
            int rlow = 30;
            int rhigh = 210;
            currentPoints = rr.nextInt(rhigh-rlow) + rlow;
            Stash.put(Constants.COIN, currentPoints);
            if (currentCounter >= subscribeTaskModelArrayList.size()) {
                Toast.makeText(requireContext(), "End of task", Toast.LENGTH_SHORT).show();

            } else setDataOnViews(currentCounter, false);

        });

        //--------------------------------------------------------------------------------------------
        b.subscribeBtn.setOnClickListener(view -> subscribeUserToChannel());


        return b.getRoot();
    }

    private void subscribeUserToChannel() {

//        if (vipStatus) {
//            if (remainingDailyLimitInt == 80) {
//                Utils.toast(getString(R.string.yourdailylimitreached));
//                return;
//            }
//        } else {
//            if (remainingDailyLimitInt == 30) {
//                Utils.toast(getString(R.string.yourdailylimitreached));
//                return;
//            }
//        }

        if (subscribeTaskModelArrayList.size() == 0)
            return;

        currentVideoId = Constants.getVideoId(currentVideoLink);

        getResultsFromApi();
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }

    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.youtube.YouTube mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.youtube.YouTube.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("YouTube Data API Android Quickstart")
                    .build();
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private List<String> getDataFromApi() throws IOException {
            // Get a list of up to 10 files.
            List<String> channelInfo = new ArrayList<String>();

            YouTube.Videos.List request = mService.videos()
                    .list("snippet,contentDetails,statistics,status");
            VideoListResponse response = request.setId(currentVideoId).execute();

            Log.d("clima", response.getItems().get(0).getSnippet().getChannelId());


            video = response.getItems().get(0);

            channelInfo.add("This video's title is " + video.getSnippet().getTitle() + ". " +
                    "Channel title is '" + video.getSnippet().getChannelTitle() + ", " +
                    "and it has " + video.getStatistics().getLikeCount() + " likes."

            );

            status = video.getSnippet().getTitle();

            return channelInfo;
        }

        String status = "nothing";

        @Override
        protected void onPreExecute() {
//            mOutputText.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {

            //Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show();

            if (output == null || output.size() == 0) {
//                mOutputText.setText("No results returned.");
                Toast.makeText(requireContext(), "No results returned.", Toast.LENGTH_SHORT).show();
            } else {
                output.add(0, "Data retrieved using the YouTube Data API:");
//                mOutputText.setText(TextUtils.join("\n", output));
                String text = TextUtils.join("\n", output);

               // Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show();
            }

            try {
                subscribeToYoutubeChannel();
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            SubscribeFragment.REQUEST_AUTHORIZATION);
                } else {
//                    mOutputText.setText("The following error occurred:\n"
//                            + mLastError.getMessage());

                    Toast.makeText(requireContext(), "The following error occurred:\n"
                            + mLastError.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("TAGSUB", "onCancelled: " + "The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
//                mOutputText.setText("Request cancelled.");
                Toast.makeText(requireContext(), "Request cancelled.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            Toast.makeText(requireContext(), "No Network Connection", Toast.LENGTH_LONG).show();
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                requireContext(), Manifest.permission.GET_ACCOUNTS)) {
            String accountName = requireContext().getSharedPreferences("com.moutamid.typeking", Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    @Override
    public void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
//                    mOutputText.setText(
//                            "This app requires Google Play Services. Please install " +
//                                    "Google Play Services on your device and relaunch this app.");
                    Toast.makeText(requireContext(), "This app requires Google Play Services. Please install " +
                            "Google Play Services on your device and relaunch this app.", Toast.LENGTH_SHORT).show();
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                requireContext().getSharedPreferences("com.moutamid.typeking", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(requireContext());
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(requireContext());
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                requireActivity(),
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    private void subscribeToYoutubeChannel() throws GeneralSecurityException, IOException {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        YouTube youtubeService = new com.google.api.services.youtube.YouTube.Builder(
                transport, jsonFactory, mCredential)
                .setApplicationName("YouTube Data API Android Quickstart")
                .build();

        Subscription subscription = new Subscription();

        // Add the snippet object property to the Subscription object.
        SubscriptionSnippet snippet = new SubscriptionSnippet();
        ResourceId resourceId = new ResourceId();
        resourceId.setChannelId(video.getSnippet().getChannelId());
        resourceId.setKind("youtube#channel");
        snippet.setResourceId(resourceId);
        subscription.setSnippet(snippet);

        // Define and execute the API request
        YouTube.Subscriptions.Insert request = youtubeService.subscriptions()
                .insert("snippet", subscription);


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Subscription response = request.execute();
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (checkOverlayPermission()){
                                startService();
                                String url = currentVideoLink;
                                Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + Constants.getVideoId(url)));
                                Intent webIntent = new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("http://www.youtube.com/watch?v=" + Constants.getVideoId(url)));

                                try {
                                    requireContext().startActivity(appIntent);
                                } catch (ActivityNotFoundException ex) {
                                    requireContext().startActivity(webIntent);
                                }
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void uploadAddedSubscribers() {
//        mProgress.hide();
//        progressDialog.show();

        /*databaseReference.child("tasks")
                .child(taskArrayList.get(currentPosition).getTaskKey())*/
        //.child("currentViewsQuantity")
        Constants.databaseReference()
                .child(Constants.SUBSCRIBE_TASKS)
                .child(subscribeTaskModelArrayList.get(currentCounter).getTaskKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        SubscribeTaskModel taskk = snapshot.getValue(SubscribeTaskModel.class);

                        String currentViews = String.valueOf(taskk.getCurrentSubscribesQuantity());

                        if (currentViews.equals(taskk.getTotalSubscribesQuantity())) {

                            Constants.databaseReference()
                                    .child(Constants.SUBSCRIBE_TASKS)
                                    .child(subscribeTaskModelArrayList.get(currentCounter).getTaskKey())
                                    .child("completedDate")
                                    .setValue(Constants.getDate())
//                                    .setValue(new Utils().getDate())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            uploadAddedCoins();
                                        }
                                    });

                        } else {

                            Constants.databaseReference()
                                    .child(Constants.SUBSCRIBE_TASKS)
                                    .child(subscribeTaskModelArrayList.get(currentCounter).getTaskKey())
                                    .child("currentSubscribesQuantity")
                                    .setValue(taskk.getCurrentSubscribesQuantity() + 1)
                                    .addOnSuccessListener(
                                            new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    uploadAddedCoins();
                                                }
                                            }
                                    );

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "onCancelled: " + error.getMessage());
                        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        mProgress.hide();
                    }
                });
    }

    private void uploadAddedCoins() {
        Constants.databaseReference().child(Constants.USER).child(Constants.auth().getCurrentUser().getUid())
                .child("coins").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        int value = snapshot.getValue(Integer.class);
                        Constants.databaseReference().child(Constants.USER).child(Constants.auth().getCurrentUser().getUid())
                                .child("coins")
                                .setValue(value + currentPoints)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("user", Constants.auth().getCurrentUser().getUid());
                                        map.put("date", Constants.getDate());
                                        Constants.databaseReference()
                                                .child(Constants.SUBSCRIBE_TASKS)
                                                .child(subscribeTaskModelArrayList.get(currentCounter).getTaskKey())
                                                .child(Constants.SUBSCRIBER_PATH)
                                                .push()
                                                .setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        mProgress.hide();
                                                        Toast.makeText(requireContext(), "Subscribed Done", Toast.LENGTH_SHORT).show();
                                                        Stash.put(Constants.CHECK, false);
                                                        currentCounter++;
                                                        Random rr = new Random();
                                                        int rlow = 30;
                                                        int rhigh = 210;
                                                        currentPoints = rr.nextInt(rhigh-rlow) + rlow;
                                                        Stash.put(Constants.COIN, currentPoints);
                                                        if (currentCounter >= subscribeTaskModelArrayList.size()) {
                                                            Toast.makeText(requireContext(), "End of task", Toast.LENGTH_SHORT).show();
                                                            b.videoImageSubscribe.setBackgroundResource(0);
                                                        } else setDataOnViews(currentCounter, true);

                                                    }
                                                });

                                    }
                                });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "onCancelled: " + error.getMessage());
                        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        mProgress.hide();
                    }
                });
    }

    public boolean checkOverlayPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(requireContext())) {
                // send user to the device settings
                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(myIntent);
                return false;
            }
        }
        return true;
    }

    public void startService(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(Settings.canDrawOverlays(requireContext())) {
                // start the service based on the android version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Intent i = new Intent(requireContext(), ForegroundService.class);
                    requireContext().startForegroundService(i);
                } else {
                    Intent i = new Intent(requireContext(), ForegroundService.class);
                    requireContext().startService(i);
                }
            }
        }else{
            Intent i = new Intent(requireContext(), ForegroundService.class);
            requireContext().startService(i);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean check = Stash.getBoolean(Constants.CHECK, false);
        if (check) {
            uploadAddedSubscribers();
        }
    }

    private void setDataOnViews(int counter, boolean isTaskCompleted) {

        if (subscribeTaskModelArrayList.size() == 0)
            return;

        // IF FIRST TIME
        if (counter == 0 || !isTaskCompleted) {
            progressDialog.show();

            b.videoImageSubscribe.setScaleType(ImageView.ScaleType.CENTER_CROP);
            with(requireActivity())
                    .asBitmap()
                    .load(subscribeTaskModelArrayList.get(counter).getThumbnailUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.color.light_grey)
                            .error(R.color.light_grey)
                    )
                    .diskCacheStrategy(DATA)
                    .addListener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    b.videoImageSubscribe.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                    b.videoImageSubscribe.setImageResource(R.drawable.ic_baseline_access_time_filled_24);

                                    currentCounter++;

                                    if (currentCounter >= subscribeTaskModelArrayList.size()) {
                                        Toast.makeText(requireContext(), "End of task", Toast.LENGTH_SHORT).show();
                                        b.videoImageSubscribe.setBackgroundResource(0);
                                    } else setDataOnViews(currentCounter, false);

                                }
                            });

                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(b.videoImageSubscribe);

            currentVideoLink = subscribeTaskModelArrayList.get(counter).getVideoUrl();
            int i = Integer.parseInt(subscribeTaskModelArrayList.get(counter).getTotalViewTimeQuantity());
            Stash.put(Constants.TIME, i);
            Stash.put(Constants.COIN, currentPoints);
            progressDialog.dismiss();

            return;
        }

        // IF SECOND OR THIRD TIME
        b.videoImageSubscribe.setScaleType(ImageView.ScaleType.FIT_CENTER);
        b.videoImageSubscribe.setImageResource(R.drawable.ic_baseline_access_time_filled_24);
        new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                b.videoImageSubscribe.setScaleType(ImageView.ScaleType.FIT_CENTER);
                b.videoImageSubscribe.setImageResource(R.drawable.ic_baseline_access_time_filled_24);
                b.videoImageSubscribe.animate().rotation(b.videoImageSubscribe.getRotation() + 20)
                        .setDuration(100).start();

                isTimerRunning = true;
                b.subscribeBtn.setEnabled(false);
                b.seeOther.setEnabled(false);
            }

            public void onFinish() {
                isTimerRunning = false;
//                b.autoPlaySwitchSubscribe.setEnabled(true);
                b.subscribeBtn.setEnabled(true);
                b.seeOther.setEnabled(true);

                b.videoImageSubscribe.setRotation(0);

                progressDialog.show();

                b.videoImageSubscribe.setScaleType(ImageView.ScaleType.CENTER_CROP);
                with(requireContext())
                        .asBitmap()
                        .load(subscribeTaskModelArrayList.get(counter).getThumbnailUrl())
                        .apply(new RequestOptions()
                                .placeholder(R.color.light_grey)
                                .error(R.color.light_grey)
                        )
                        .addListener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        b.videoImageSubscribe.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                        b.videoImageSubscribe.setImageResource(R.drawable.ic_baseline_access_time_filled_24);

                                        currentCounter++;
                                        Random rr = new Random();
                                        int rlow = 30;
                                        int rhigh = 210;
                                        currentPoints = rr.nextInt(rhigh-rlow) + rlow;
                                        Stash.put(Constants.COIN, currentPoints);
                                        if (currentCounter >= subscribeTaskModelArrayList.size()) {
                                            Toast.makeText(requireContext(), "End of task", Toast.LENGTH_SHORT).show();
                                            b.videoImageSubscribe.setBackgroundResource(0);
                                        } else setDataOnViews(currentCounter, false);
                                    }
                                });
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .diskCacheStrategy(DATA)
                        .into(b.videoImageSubscribe);

                currentVideoLink = subscribeTaskModelArrayList.get(counter).getVideoUrl();

                progressDialog.dismiss();

            }
        }.start();

    }
}