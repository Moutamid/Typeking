package com.moutamid.typeking;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.fxn.stash.Stash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.typeking.utilis.Constants;
import com.moutamid.typeking.databinding.ActivityCreateCampaignBinding;
import com.moutamid.typeking.models.LikeTaskModel;
import com.moutamid.typeking.models.SubscribeTaskModel;
import com.moutamid.typeking.models.UserDetails;
import com.moutamid.typeking.models.ViewTaskModel;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import org.json.JSONException;
import org.json.JSONObject;

public class CreateCampaignActivity extends AppCompatActivity {
    ActivityCreateCampaignBinding binding;
    int CAMPAIGN_SELECTION = Stash.getInt(Constants.CAMPAIGN_SELECTION);
    int CurrentCoins = 0;
    boolean VIP_STATUS = false;
    String url;
    double totalCost = 0;
    double subDefault = 2100;
    double likeDefault = 1700;
    int pickedSub = 0;
    int pickedView = 1;
    int pickedLike = 0;
    int pickedSubTime = 1;
    double holderSub = 0;
    int pickedViewTime = 1;
    int pickedLikeTime = 1;
    String thumbnailUrl;
    double discount = 0.10; // 10% discount
    GetVideoTitle getVideoTitle = new GetVideoTitle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateCampaignBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        url = Stash.getString(Constants.RECENT_LINK);

        binding.back.setOnClickListener(v -> onBackPressed());

        Constants.databaseReference().child("user").child(Constants.auth().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            UserDetails userDetails = snapshot.getValue(UserDetails.class);
                            CurrentCoins = userDetails.getCoins();
                            VIP_STATUS = userDetails.isVipStatus();
                            binding.coin.setText(userDetails.getCoins()+"");
                            Stash.put(Constants.CURRENT_COINS, userDetails.getCoins());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CreateCampaignActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        binding.coin.setText(CurrentCoins+"");

        binding.coin.setOnClickListener(v -> {
            startActivity(new Intent(this, BillingActivity.class));
        });

        initYoutubePlayer();

        if (CAMPAIGN_SELECTION == 0){
            binding.allLayout.setVisibility(View.VISIBLE);
            binding.likesLayout.setVisibility(View.GONE);
            binding.viewsLayout.setVisibility(View.GONE);

            binding.pickerSubAll.setText(Constants.subsQuantityArray[pickedSub]);
            binding.likeAll.setText(Constants.subsQuantityArray[pickedSub]);
            binding.viewsAll.setText(Constants.subsQuantityArray[pickedSub]);

            totalCost = ((300 * pickedSubTime) + subDefault );
            holderSub = totalCost;

            if (VIP_STATUS){
                totalCost = totalCost - (totalCost * discount);
            }

            binding.totalCoins.setText(totalCost+"");

        } else if (CAMPAIGN_SELECTION == 1){
            binding.allLayout.setVisibility(View.GONE);
            binding.likesLayout.setVisibility(View.GONE);
            binding.viewsLayout.setVisibility(View.VISIBLE);

            totalCost = Integer.valueOf(Constants.viewQuantityArray[pickedView]) * Integer.valueOf(Constants.viewTimeArray[pickedViewTime]);

            if (VIP_STATUS){
                totalCost = totalCost - (totalCost * discount);
            }

            binding.totalCoins.setText(totalCost+"");
            binding.pickerViews.setText("50");
        } else if (CAMPAIGN_SELECTION == 2){
            binding.allLayout.setVisibility(View.GONE);
            binding.likesLayout.setVisibility(View.VISIBLE);
            binding.viewsLayout.setVisibility(View.GONE);

            totalCost = ((300 * pickedLikeTime) + likeDefault );
            holderSub = totalCost;

            if (VIP_STATUS){
                totalCost = totalCost - (totalCost * discount);
            }

            binding.totalCoins.setText(totalCost+"");
        }

        binding.pickerSubAll.setOnClickListener(v -> {
            pickerAllDialog();
        });

        binding.pickerLikes.setOnClickListener(v -> {
            pickerLikesDialog();
        });

        binding.pickerTime.setOnClickListener(v -> {
            pickerTimeDialog();
        });

        binding.pickerViews.setOnClickListener(v -> {
           pickerViewDialog();
        });

        binding.done.setOnClickListener(v -> {
            if (totalCost <= CurrentCoins){
                if (CAMPAIGN_SELECTION == 0){
                    uploadSubscribeCampaign();
                }
                if (CAMPAIGN_SELECTION == 1){
                    uploadViewTask();
                }
                if (CAMPAIGN_SELECTION == 2){
                    uploadLikeTask();
                }
            } else {
                Toast.makeText(this, "Not Enough Coin", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, BillingActivity.class));
                finish();
            }
        });

    }

    private void uploadViewTask() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Creating Your Campaign..");
        progressDialog.show();

        Constants.databaseReference().child("user").child(Constants.auth().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            UserDetails userDetails = snapshot.getValue(UserDetails.class);
                            Constants.databaseReference().child("user").child(Constants.auth().getCurrentUser().getUid())
                                    .child("coins")
                                    .setValue(userDetails.getCoins() - totalCost)
                                    .addOnCompleteListener(task -> {

                                        if (task.isSuccessful()){
                                            String key = Constants.databaseReference().child(Constants.VIEW_TASKS).push().getKey();

                                            ViewTaskModel task1 = new ViewTaskModel();

                                            task1.setVideoUrl(url);
                                            task1.setThumbnailUrl(thumbnailUrl);
                                            task1.setTotalViewsQuantity(Constants.viewQuantityArray[pickedView]);
                                            task1.setTotalViewTimeQuantity(Constants.viewTimeArray[pickedViewTime]);
                                            task1.setCompletedDate("error");
                                            task1.setCurrentViewsQuantity(0);
                                            task1.setPosterUid(Constants.auth().getCurrentUser().getUid());
                                            task1.setTaskKey(key);
                                            task1.setCreatedTime(Constants.getDate());

                                            Constants.databaseReference().child(Constants.VIEW_TASKS).child(key).setValue(task1)
                                                    .addOnCompleteListener(task2 -> {

                                                        if (task2.isSuccessful()) {
                                                            progressDialog.dismiss();
                                                            Toast.makeText(CreateCampaignActivity.this, "Campaign Started Succesfully", Toast.LENGTH_SHORT).show();
                                                            finish();

                                                        } else {
                                                            progressDialog.dismiss();
                                                            Toast.makeText(CreateCampaignActivity.this, task2.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }

                                                    });

                                        } else {
                                            progressDialog.dismiss();
                                            Toast.makeText(CreateCampaignActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(CreateCampaignActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(CreateCampaignActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
    private void uploadLikeTask() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Creating Your Campaign..");
        progressDialog.show();

        Constants.databaseReference().child("user").child(Constants.auth().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            UserDetails userDetails = snapshot.getValue(UserDetails.class);
                            Constants.databaseReference().child("user").child(Constants.auth().getCurrentUser().getUid())
                                    .child("coins")
                                    .setValue(userDetails.getCoins() - totalCost)
                                    .addOnCompleteListener(task -> {

                                        if (task.isSuccessful()){
                                            String key = Constants.databaseReference().child(Constants.LIKE_TASKS).push().getKey();

                                            LikeTaskModel task1 = new LikeTaskModel();
                                            task1.setVideoUrl(url);
                                            task1.setThumbnailUrl(thumbnailUrl);
                                            task1.setTotalLikesQuantity(Constants.subsQuantityArray[pickedLike]);
                                            task1.setCompletedDate("error");
                                            task1.setTotalViewTimeQuantity(Constants.likeTimeArray[pickedLikeTime]);
                                            task1.setCurrentLikesQuantity(0);
                                            task1.setPosterUid(Constants.auth().getCurrentUser().getUid());
                                            task1.setTaskKey(key);
                                            task1.setCreatedTime(Constants.getDate());

                                            Constants.databaseReference().child(Constants.LIKE_TASKS).child(key).setValue(task1)
                                                    .addOnCompleteListener(task2 -> {

                                                        if (task2.isSuccessful()) {
                                                            progressDialog.dismiss();
                                                            Toast.makeText(CreateCampaignActivity.this, "Campaign Started Succesfully", Toast.LENGTH_SHORT).show();
                                                            finish();

                                                        } else {
                                                            progressDialog.dismiss();
                                                            Toast.makeText(CreateCampaignActivity.this, task2.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }

                                                    });

                                        } else {
                                            progressDialog.dismiss();
                                            Toast.makeText(CreateCampaignActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(CreateCampaignActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(CreateCampaignActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
    private void uploadSubscribeCampaign() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Creating Your Campaign..");
        progressDialog.show();

        Constants.databaseReference().child("user").child(Constants.auth().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            UserDetails userDetails = snapshot.getValue(UserDetails.class);
                            Constants.databaseReference().child("user").child(Constants.auth().getCurrentUser().getUid())
                                    .child("coins")
                                    .setValue(userDetails.getCoins() - totalCost)
                                    .addOnCompleteListener(task -> {

                                        if (task.isSuccessful()){
                                            String key = Constants.databaseReference().child(Constants.SUBSCRIBE_TASKS).push().getKey();

                                            SubscribeTaskModel task1 = new SubscribeTaskModel();
                                            task1.setVideoUrl(url);
                                            task1.setThumbnailUrl(thumbnailUrl);
                                            task1.setTotalSubscribesQuantity(Constants.subsQuantityArray[pickedSub]);
                                            task1.setCompletedDate("error");
                                            task1.setTotalViewTimeQuantity(Constants.subTimeArray[pickedSubTime]);
                                            task1.setCurrentSubscribesQuantity(0);
                                            task1.setPosterUid(Constants.auth().getCurrentUser().getUid());
                                            task1.setTaskKey(key);
                                            task1.setCreatedTime(Constants.getDate());

                                            Constants.databaseReference().child(Constants.SUBSCRIBE_TASKS).child(key).setValue(task1)
                                                    .addOnCompleteListener(task2 -> {

                                                        if (task2.isSuccessful()) {
                                                            progressDialog.dismiss();
                                                            Toast.makeText(CreateCampaignActivity.this, "Campaign Started Succesfully", Toast.LENGTH_SHORT).show();
                                                            finish();

                                                        } else {
                                                            progressDialog.dismiss();
                                                            Toast.makeText(CreateCampaignActivity.this, task2.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }

                                                    });

                                        } else {
                                            progressDialog.dismiss();
                                            Toast.makeText(CreateCampaignActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(CreateCampaignActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(CreateCampaignActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void pickerViewDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.picker_dialog);

        TextView title = dialog.findViewById(R.id.title);
        TextView subTitle = dialog.findViewById(R.id.subTitle);
        Button cancel = dialog.findViewById(R.id.cancel);
        Button select = dialog.findViewById(R.id.select);
        NumberPicker picker = dialog.findViewById(R.id.picker);

        picker.setMinValue(0);
        picker.setMaxValue(Constants.viewQuantityArray.length-1);
        picker.setDisplayedValues(Constants.viewQuantityArray);
        picker.setValue(pickedView);

        title.setText("EXPECTED VIEWS");
        subTitle.setText("Select number of views that you want to have");
        cancel.setOnClickListener(v -> dialog.dismiss());

        select.setOnClickListener(v -> {
            pickedView = picker.getValue();
            String picked = picker.getDisplayedValues()[picker.getValue()];
            dialog.dismiss();
            binding.pickerViews.setText(picked);

            totalCost = Integer.parseInt(picked) * Integer.parseInt(Constants.viewTimeArray[pickedViewTime]);
            if (VIP_STATUS){
                totalCost = totalCost - (totalCost * discount);
            }
            binding.totalCoins.setText(totalCost+"");
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);
    }
    private void pickerTimeDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.picker_dialog);

        TextView title = dialog.findViewById(R.id.title);
        TextView subTitle = dialog.findViewById(R.id.subTitle);
        Button cancel = dialog.findViewById(R.id.cancel);
        Button select = dialog.findViewById(R.id.select);
        NumberPicker picker = dialog.findViewById(R.id.picker);

        if (CAMPAIGN_SELECTION == 0){
            picker.setMinValue(0);
            picker.setMaxValue(Constants.subTimeArray.length-1);
            picker.setDisplayedValues(Constants.subTimeArray);
            picker.setValue(pickedSubTime);
        } if (CAMPAIGN_SELECTION == 1) {
            picker.setMinValue(0);
            picker.setMaxValue(Constants.viewTimeArray.length-1);
            picker.setDisplayedValues(Constants.viewTimeArray);
            picker.setValue(pickedViewTime);
        } if (CAMPAIGN_SELECTION == 2) {
            picker.setMinValue(0);
            picker.setMaxValue(Constants.likeTimeArray.length-1);
            picker.setDisplayedValues(Constants.likeTimeArray);
            picker.setValue(pickedLikeTime);
        }



        title.setText("TIME REQUIRED (SECONDS):");
        subTitle.setText("");
        cancel.setOnClickListener(v -> dialog.dismiss());

        select.setOnClickListener(v -> {
            String picked = picker.getDisplayedValues()[picker.getValue()];

            if (CAMPAIGN_SELECTION == 0){
                pickedSubTime = picker.getValue();
                if (pickedSub == 0){
                    totalCost = ((300 * pickedSubTime) + subDefault);
                } else {
                    totalCost = ((300 * pickedSubTime) + subDefault) * (pickedSub+1);
                }
                holderSub = totalCost;
            } if (CAMPAIGN_SELECTION == 1) {
                pickedViewTime = picker.getValue();
                totalCost = Integer.valueOf(picked) * Integer.valueOf(Constants.viewQuantityArray[pickedView]);
            } if (CAMPAIGN_SELECTION == 2) {
                pickedLikeTime = picker.getValue();
                if (pickedLike == 0){
                    totalCost = ((300 * pickedLikeTime) + likeDefault);
                } else {
                    totalCost = ((300 * pickedLikeTime) + likeDefault) * (pickedLike+1);
                }
                holderSub = totalCost;
            }

            dialog.dismiss();
            binding.pickerTime.setText(picked+"");

            if (VIP_STATUS){
                totalCost = totalCost - (totalCost * discount);
            }

            binding.totalCoins.setText(totalCost+"");
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);
    }
    private void pickerLikesDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.picker_dialog);

        TextView title = dialog.findViewById(R.id.title);
        TextView subTitle = dialog.findViewById(R.id.subTitle);
        Button cancel = dialog.findViewById(R.id.cancel);
        Button select = dialog.findViewById(R.id.select);
        NumberPicker picker = dialog.findViewById(R.id.picker);

        picker.setMinValue(0);
        picker.setMaxValue(Constants.subsQuantityArray.length-1);
        picker.setDisplayedValues(Constants.subsQuantityArray);
        picker.setValue(pickedLike);

        title.setText("EXPECTED LIKES");
        subTitle.setText("Select number of likes that you want to have");
        cancel.setOnClickListener(v -> dialog.dismiss());

        select.setOnClickListener(v -> {
            pickedLike = picker.getValue();
            String picked = picker.getDisplayedValues()[picker.getValue()];
            dialog.dismiss();
            binding.likeView.setText(picked+"");
            binding.pickerLikes.setText(picked+"");
            totalCost = holderSub * (pickedLike+1);
            if (VIP_STATUS){
                totalCost = totalCost - (totalCost * discount);
            }
            binding.totalCoins.setText(totalCost+"");
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);
    }
    private void pickerAllDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.picker_dialog);

        TextView title = dialog.findViewById(R.id.title);
        TextView subTitle = dialog.findViewById(R.id.subTitle);
        Button cancel = dialog.findViewById(R.id.cancel);
        Button select = dialog.findViewById(R.id.select);
        NumberPicker picker = dialog.findViewById(R.id.picker);

        picker.setMinValue(0);
        picker.setMaxValue(Constants.subsQuantityArray.length-1);
        picker.setDisplayedValues(Constants.subsQuantityArray);
        picker.setValue(pickedSub);

        title.setText("EXPECTED SUBSCRIBER");
        subTitle.setText("Select number of subscriber that you want to have");
        cancel.setOnClickListener(v -> dialog.dismiss());

        select.setOnClickListener(v -> {
            pickedSub = picker.getValue();
            String picked = picker.getDisplayedValues()[picker.getValue()];
            dialog.dismiss();
            binding.likeAll.setText(picked+"");
            binding.viewsAll.setText(picked+"");
            binding.pickerSubAll.setText(picked+"");

            totalCost = holderSub * (pickedSub+1);

            if (VIP_STATUS){
                totalCost = totalCost - (totalCost * discount);
            }
            binding.totalCoins.setText(totalCost+"");
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);
    }
    private void initYoutubePlayer() {

        YouTubePlayerView youTubePlayerView = binding.youtubePlayerViewFragmentView;
        getLifecycle().addObserver(youTubePlayerView);

        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                String videoId = Constants.getVideoId(url);
                youTubePlayer.loadVideo(videoId, 0);
            }
        });


        // INIT PLAYER VIEW
        getVideoTitle.setId(url);
        getVideoTitle.execute();

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.youtubePlayerViewFragmentView.release();
        getVideoTitle.cancel(true);
    }
    @Override
    protected void onStop() {
        super.onStop();
        getVideoTitle.cancel(true);
    }
    private class GetVideoTitle extends AsyncTask<String, Void, String> {

        private String id;

        public void setId(String id) {
            this.id = id;
        }

        @Override
        protected String doInBackground(String... strings) {
            Constants.HttpHandler sh = new Constants.HttpHandler();

            String url = "https://www.youtube.com/oembed?format=json&url=" + id;//https://www.youtube.com/watch?v=" + id;

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            String videoTitle;

            Log.e("", "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject o = new JSONObject(jsonStr);

                    videoTitle = o.getString("title");

                    thumbnailUrl = o.getString("thumbnail_url");
                    Stash.put(Constants.RECENT_IMAGE, thumbnailUrl);

                    return videoTitle;

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                            "Json parsing error: " + e.getMessage(),
                                            Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                    e.printStackTrace();
                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                        "Couldn't get json from server. Check LogCat for possible errors!",
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (!isCancelled()) {
                //binding.youtubePlayerViewFragmentView.getPlayerUiController().setVideoTitle(s);
            }

        }
    }

}