package com.moutamid.typeking;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
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
import com.google.firebase.database.annotations.NotNull;
import com.moutamid.typeking.constant.Constants;
import com.moutamid.typeking.databinding.ActivityCreateCampaignBinding;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class CreateCampaignActivity extends AppCompatActivity {
    ActivityCreateCampaignBinding binding;
    int CAMPAIGN_SELECTION = Stash.getInt(Constants.CAMPAIGN_SELECTION);
    int CurrentCoins = Stash.getInt(Constants.CURRENT_COINS);
    boolean VIP_STATUS = Stash.getBoolean(Constants.VIP_STATUS);
    String url;
    int totalCost = 0;
    int subDefault = 2100;
    int likeDefault = 1700;
    int pickedSub = 0;
    int pickedView = 1;
    int pickedLike = 0;
    int pickedSubTime = 1;
    int holderSub = 0;
    int pickedViewTime = 1;
    int pickedLikeTime = 1;
    String thumbnailUrl;
    GetVideoTitle getVideoTitle = new GetVideoTitle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateCampaignBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        url = Stash.getString(Constants.RECENT_LINK);

        binding.back.setOnClickListener(v -> onBackPressed());

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
            binding.totalCoins.setText(totalCost+"");

        } else if (CAMPAIGN_SELECTION == 1){
            binding.allLayout.setVisibility(View.GONE);
            binding.likesLayout.setVisibility(View.GONE);
            binding.viewsLayout.setVisibility(View.VISIBLE);

            totalCost = Integer.valueOf(Constants.viewQuantityArray[pickedView]) * Integer.valueOf(Constants.viewTimeArray[pickedViewTime]);
            binding.totalCoins.setText(totalCost+"");
            binding.pickerViews.setText("50");
        } else if (CAMPAIGN_SELECTION == 2){
            binding.allLayout.setVisibility(View.GONE);
            binding.likesLayout.setVisibility(View.VISIBLE);
            binding.viewsLayout.setVisibility(View.GONE);

            totalCost = ((300 * pickedLikeTime) + likeDefault );
            holderSub = totalCost;
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
                totalCost = totalCost - ((int) (totalCost * (20.0f / 100.0f)));
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
                totalCost = totalCost - ((int) (totalCost * (20.0f / 100.0f)));
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
                totalCost = totalCost - ((int) (totalCost * (20.0f / 100.0f)));
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
            HttpHandler sh = new HttpHandler();

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
    private static class HttpHandler {

        private String TAG = "HttpHandler";

        public HttpHandler() {
        }

        public String makeServiceCall(String reqUrl) {
            String response = null;
            try {
                URL url = new URL(reqUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                // read the response
                InputStream in = new BufferedInputStream(conn.getInputStream());

                response = convertStreamToString(in);
            } catch (MalformedURLException e) {
                Log.e(TAG, "MalformedURLException: " + e.getMessage());
            } catch (ProtocolException e) {
                Log.e(TAG, "ProtocolException: " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "IOException: " + e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
            return response;
        }

        private String convertStreamToString(InputStream is) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            String line;
            try {

                while ((line = reader.readLine()) != null) {

                    sb.append(line).append('\n');

                }

            } catch (IOException e) {

                e.printStackTrace();

            } finally {

                try {

                    is.close();

                } catch (IOException e) {

                    e.printStackTrace();
                }
            }

            return sb.toString();
        }
    }

}