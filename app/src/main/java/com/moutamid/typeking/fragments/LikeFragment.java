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

import com.bumptech.glide.Glide;
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
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.typeking.R;
import com.moutamid.typeking.databinding.FragmentLikeBinding;
import com.moutamid.typeking.models.LikeTaskModel;
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


public class LikeFragment extends Fragment implements EasyPermissions.PermissionCallbacks{

    FragmentLikeBinding binding;
    private static final String TAG = "LikeFragment";
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {YouTubeScopes.YOUTUBE_READONLY, YouTubeScopes.YOUTUBE_FORCE_SSL};
    Video video;
    GoogleAccountCredential mCredential;
    String currentVideoLink;
    ProgressDialog mProgress;
    String currentVideoId = "";
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ArrayList<LikeTaskModel> likeTaskModelArrayList = new ArrayList<>();
    private ProgressDialog progressDialog;
    int currentCounter = 0;
    int currentPoints = 30;
    boolean vipStatus = false;
    boolean isAutoPlay = false;
    boolean isTimerRunning = false;
    int isError = 0;

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

        Random r = new Random();
        int low = 30;
        int high = 210;

        currentPoints = r.nextInt(high-low) + low;
        Stash.put(Constants.COIN, currentPoints);
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
                    setDataOnViews(0, false);
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
        mCredential = GoogleAccountCredential.usingOAuth2(
                        requireContext().getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        binding.otherBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentCounter++;
                Random r = new Random();
                int low = 30;
                int high = 210;
                currentPoints = r.nextInt(high-low) + low;
                Stash.put(Constants.COIN, currentPoints);
                if (currentCounter >= likeTaskModelArrayList.size()) {
                    Toast.makeText(requireContext(), "End Of Task!", Toast.LENGTH_SHORT).show();

                } else setDataOnViews(currentCounter, false);

            }
        });

        //--------------------------------------------------------------------------------------------
        binding.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                likeUserToVideo();
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
//                    mOutputText.setText(
//                            "This app requires Google Play Services. Please install " +
//                                    "Google Play Services on your device and relaunch this app.");
                    String msg = "This app requires Google Play Services. Please install " +
                            "Google Play Services on your device and relaunch this app.";
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
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
    Handler handler = new Handler();
    private void setDataOnViews(int counter, boolean isTaskCompleted) {

        if (likeTaskModelArrayList.size() == 0)
            return;
//        Log.e(TAG, "setDataOnViews: URLL: "+likeTaskModelArrayList.get(counter).getThumbnailUrl() );

        // IF FIRST TIME
        if (counter == 0 || !isTaskCompleted) {
            progressDialog.show();
            Log.e(TAG, "setDataOnViews: URLL: if (counter == 0 || !isTaskCompleted) {");

            binding.thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //Glide.with(requireContext()).load(likeTaskModelArrayList.get(counter).getThumbnailUrl()).into()
            with(requireContext())
                    .asBitmap()
                    .load(likeTaskModelArrayList.get(counter).getThumbnailUrl())
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

                                    isError++;

//                                    databaseReference.child(Constants.LIKE_TASKS)
//                                            .child(likeTaskModelArrayList.get(counter)
//                                                    .getTaskKey()).removeValue();

                                    currentCounter++;

                                    if (currentCounter >= likeTaskModelArrayList.size()) {
                                        Toast.makeText(requireContext(), "End of Task!", Toast.LENGTH_SHORT).show();

                                    } else setDataOnViews(currentCounter, false);

                                }
                            });
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            Log.e(TAG, "setDataOnViews: URLL: onResourceReady");

                            return false;
                        }
                    })
                    .diskCacheStrategy(DATA)
                    .into(binding.thumbnail);

            currentVideoLink = likeTaskModelArrayList.get(counter).getVideoUrl();

            progressDialog.dismiss();

            int i = Integer.parseInt(likeTaskModelArrayList.get(counter).getTotalViewTimeQuantity());
            Stash.put(Constants.TIME, i);
            Stash.put(Constants.COIN, currentPoints);

            if (isError > 0) {
                binding.thumbnail.setScaleType(ImageView.ScaleType.FIT_CENTER);
                binding.thumbnail.setImageResource(R.drawable.ic_baseline_access_time_filled_24);
                return;
            }

            if (isAutoPlay)
                likeUserToVideo();
            return;
        }

        // IF SECOND OR THIRD TIME
        binding.thumbnail.setScaleType(ImageView.ScaleType.FIT_CENTER);
        binding.thumbnail.setImageResource(R.drawable.ic_baseline_access_time_filled_24);
        new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                binding.thumbnail.setScaleType(ImageView.ScaleType.FIT_CENTER);
                binding.thumbnail.setImageResource(R.drawable.ic_baseline_access_time_filled_24);
                binding.thumbnail.animate().rotation(binding.thumbnail.getRotation() + 20)
                        .setDuration(100).start();
                //b.videoIdLike.setText("" + millisUntilFinished / 1000);
//                b.autoPlaySwitchLike.setEnabled(false);
                isTimerRunning = true;
                binding.likeBtn.setEnabled(false);
                binding.otherBtn.setEnabled(false);
            }

            public void onFinish() {
//                b.autoPlaySwitchLike.setEnabled(true);
                isTimerRunning = false;
                binding.likeBtn.setEnabled(true);
                binding.otherBtn.setEnabled(true);

                binding.thumbnail.setRotation(0);

                progressDialog.show();

                binding.thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
                with(requireContext())
                        .asBitmap()
                        .load(likeTaskModelArrayList.get(counter).getThumbnailUrl())
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
                                        isError++;
//                                        databaseReference.child(Constants.LIKE_TASKS)
//                                                .child(likeTaskModelArrayList.get(counter)
//                                                        .getTaskKey()).removeValue();
                                        currentCounter++;

                                        if (currentCounter >= likeTaskModelArrayList.size()) {
                                            Toast.makeText(requireContext(), "End of Task!", Toast.LENGTH_SHORT).show();

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
                        .into(binding.thumbnail);

                currentVideoLink = likeTaskModelArrayList.get(counter).getVideoUrl();

                progressDialog.dismiss();

                if (isError > 0) {
                    binding.thumbnail.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    binding.thumbnail.setImageResource(R.drawable.ic_baseline_access_time_filled_24);
                    return;
                }

                if (isAutoPlay)
                    likeUserToVideo();
            }
        }.start();

    }

    private void likeUserToVideo() {

//        if (vipStatus) {
////            if (remainingDailyLimitInt == 80) {
////                Utils.toast(getString(R.string.yourdailylimitreached));
////                return;
////            }
//        } else {
//            if (remainingDailyLimitInt == 30) {
//                //Utils.toast(getString(R.string.yourdailylimitreached));
//                return;
//            }
//        }

        if (likeTaskModelArrayList.size() == 0)
            return;

        currentVideoId = Constants.getVideoId(currentVideoLink);

        getResultsFromApi();

    }

    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {

            Toast.makeText(requireContext(), "No network connection", Toast.LENGTH_SHORT).show();
        } else {
            new MakeRequestTask(mCredential).execute();
        }
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

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
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

        /**
         * Background task to call YouTube Data API.
         *
         * @param params no parameters needed for this task.
         */
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

        /**
         * Fetch information about the "GoogleDevelopers" YouTube channel.
         *
         * @return List of Strings containing information about the channel.
         * @throws IOException
         */
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

            return channelInfo;
        }

        @Override
        protected void onPreExecute() {
//            mOutputText.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {

            if (output == null || output.size() == 0) {
//                mOutputText.setText("No results returned.");
                Log.d(TAG, "No results returned.");
                Toast.makeText(requireContext(), "No results returned.", Toast.LENGTH_SHORT).show();
            } else {
                output.add(0, "Data retrieved using the YouTube Data API:");
//                mOutputText.setText(TextUtils.join("\n", output));
                String text = TextUtils.join("\n", output);
                Log.d(TAG, text);
//                Utils.toast(text);
            }

            try {
                likeVideo();
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
                            REQUEST_AUTHORIZATION);//TODO: MainActivity is removed
                } else {
//                    mOutputText.setText("The following error occurred:\n"
//                            + mLastError.getMessage());
                    Log.d(TAG, "The following error occurred:\n"
                            + mLastError.getMessage());
                    Toast.makeText(requireContext(), "The following error occurred:\n"
                            + mLastError.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("TAGSUB", "onCancelled: " + "The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
//                mOutputText.setText("Request cancelled.");
                Log.d(TAG, "Request cancelled.");
                Toast.makeText(requireContext(), "Request cancelled.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void likeVideo() throws GeneralSecurityException, IOException {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        YouTube youtubeService = new com.google.api.services.youtube.YouTube.Builder(
                transport, jsonFactory, mCredential)
                .setApplicationName("YouTube Data API Android Quickstart")
                .build();

        YouTube.Videos.Rate request = youtubeService.videos()
                .rate(currentVideoId, "like");


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    request.execute();
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            Log.d(TAG, response.toString());
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
//                            Toast.makeText(MainActivity.this, "Liked", Toast.LENGTH_SHORT).show();

                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void uploadAddedLikers() {
        Constants.databaseReference()
                .child(Constants.LIKE_TASKS)
                .child(likeTaskModelArrayList.get(currentCounter).getTaskKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        LikeTaskModel taskk = snapshot.getValue(LikeTaskModel.class);

                        String currentViews = String.valueOf(taskk.getCurrentLikesQuantity());

                        if (currentViews.equals(taskk.getTotalLikesQuantity())) {

                            Constants.databaseReference()
                                    .child(Constants.LIKE_TASKS)
                                    .child(likeTaskModelArrayList.get(currentCounter).getTaskKey())
                                    .child("completedDate")
                                    .setValue(Constants.getDate())
//                                    .setValue(new Utils().getDate())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

//                                            likeTaskModelArrayList.remove(currentCounter);

                                            uploadAddedCoins();
                                            // UPLOAD COINS AND THEN RESTART VIDEO PLAYER

                                        }
                                    });

                        } else {

                            Constants.databaseReference()
                                    .child(Constants.LIKE_TASKS)
                                    .child(likeTaskModelArrayList.get(currentCounter).getTaskKey())
                                    .child("currentLikesQuantity")
                                    .setValue(taskk.getCurrentLikesQuantity() + 1)
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
        Constants.databaseReference().child(Constants.USER).child(mAuth.getCurrentUser().getUid())
                .child("coins").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        int value = snapshot.getValue(Integer.class);

                        Constants.databaseReference().child(Constants.USER).child(mAuth.getCurrentUser().getUid())
                                .child("coins")
                                .setValue(value + currentPoints)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("user", Constants.auth().getCurrentUser().getUid());
                                        map.put("date", Constants.getDate());
                                        Constants.databaseReference()
                                                .child(Constants.LIKE_TASKS)
                                                .child(likeTaskModelArrayList.get(currentCounter).getTaskKey())
                                                .child(Constants.LIKERS_PATH)
                                                .push()
                                                .setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        mProgress.hide();
                                                        Toast.makeText(requireContext(), "Liked!", Toast.LENGTH_SHORT).show();
                                                        currentCounter++;
                                                        Stash.put(Constants.CHECK, false);
                                                        Random rr = new Random();
                                                        int rlow = 30;
                                                        int rhigh = 210;
                                                        currentPoints = rr.nextInt(rhigh-rlow) + rlow;
                                                        Stash.put(Constants.COIN, currentPoints);
                                                        if (currentCounter >= likeTaskModelArrayList.size()) {
                                                            Toast.makeText(requireContext(), "End of Task!", Toast.LENGTH_SHORT).show();
                                                            binding.thumbnail.setBackgroundResource(0);
                                                           // b.videoIdLike.setText("Empty");
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
            uploadAddedLikers();
        }
    }

}