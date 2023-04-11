package com.moutamid.typeking.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.fxn.stash.Stash;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.typeking.BillingActivity;
import com.moutamid.typeking.R;
import com.moutamid.typeking.services.ForegroundService;
import com.moutamid.typeking.utilis.Constants;
import com.moutamid.typeking.databinding.FragmentViewBinding;
import com.moutamid.typeking.models.Taskk;
import com.moutamid.typeking.models.UserDetails;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerUtils;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewFragment extends Fragment {
    private ArrayList<Taskk> taskArrayList = new ArrayList<>();
    String videoUrl;
    boolean isAutoPlayEnabled = false;
    FragmentViewBinding binding;
    int currentPoints = 120;
    int currentPosition = 0;
    int CurrentCoins = 0;
    int currentVideoLength = 0;
    boolean VIP_STATUS = false;
    boolean run = true;
    int nmbr = 0;
    ProgressDialog progressDialog;
    YouTubePlayerView youTubePlayerView;
    View customPlayerUi;

    public ViewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentViewBinding.inflate(getLayoutInflater(), container, false);

        youTubePlayerView = binding.youtubePlayerViewFragmentView;
        customPlayerUi = youTubePlayerView.inflateCustomPlayerUi(R.layout.custom_player_ui);
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Stash.put(Constants.COIN, currentPoints);

        Constants.databaseReference().child("user").child(Constants.auth().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            UserDetails userDetails = snapshot.getValue(UserDetails.class);
                            CurrentCoins = userDetails.getCoins();
                            VIP_STATUS = userDetails.isVipStatus();
                            Stash.put(Constants.CURRENT_COINS, userDetails.getCoins());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        binding.switchAuto.setOnClickListener(v -> {
            if (!VIP_STATUS) {
                binding.switchAuto.setChecked(false);
                new AlertDialog.Builder(requireContext())
                        .setTitle("VIP account")
                        .setMessage("You need to upgrade to a vip account to use this function.")
                        .setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss())
                        .setPositiveButton("UPGRADE", (dialog, which) -> {
                            startActivity(new Intent(requireContext(), BillingActivity.class));
                        })
                        .show();
            } else {
                binding.switchAuto.setChecked(true);
            }
        });

        binding.rewardCoins.setText("" + currentPoints);

        binding.seeOther.setOnClickListener(v -> {
            String url = getNextUrl();
            //binding.youtubePlayerViewFragmentView.release();
            youTubePlayerView.getYouTubePlayerWhenReady(new YouTubePlayerCallback() {
                @Override
                public void onYouTubePlayer(@NonNull YouTubePlayer youTubePlayer) {
                    CustomPlayerUiController customPlayerUiController = new CustomPlayerUiController(requireContext(), customPlayerUi, youTubePlayer, youTubePlayerView);
                    youTubePlayer.addListener(customPlayerUiController);

                    YouTubePlayerUtils.loadOrCueVideo(
                            youTubePlayer, getLifecycle(),
                            Constants.getVideoId(url), 0f
                    );
                }
            });
            //initYoutubePlayer(url);
        });

        binding.switchAuto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isAutoPlayEnabled = isChecked;
            }
        });

        Constants.databaseReference().child(Constants.VIEW_TASKS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    taskArrayList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Taskk tasks = dataSnapshot.getValue(Taskk.class);
                        if (snapshot.child(tasks.getTaskKey()).child(Constants.VIEWER_PATH).child(Constants.auth().getCurrentUser().getUid()).exists()) {
//                        model.setSubscribed(true);
                        } else {
                            if (tasks.getCompletedDate() != null)
                                if (tasks.getCompletedDate().equals("error"))
                                    taskArrayList.add(tasks);
                        }
                    }
                    progressDialog.dismiss();
                    if (taskArrayList.size() > 0) {
                        videoUrl = taskArrayList.get(0).getVideoUrl();
                        initYoutubePlayer(videoUrl);
                    } else {
                        Toast.makeText(requireContext(), "No video Found", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "No video Found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return binding.getRoot();
    }

    private void initYoutubePlayer(String url) {
        Log.d("firebaseAuthWithGoogle", url);
        getLifecycle().addObserver(youTubePlayerView);
        YouTubePlayerListener youTubePlayerListener = new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                super.onReady(youTubePlayer);
                CustomPlayerUiController customPlayerUiController = new CustomPlayerUiController(requireContext(), customPlayerUi, youTubePlayer, youTubePlayerView);
                youTubePlayer.addListener(customPlayerUiController);

                YouTubePlayerUtils.loadOrCueVideo(
                        youTubePlayer, getLifecycle(),
                        Constants.getVideoId(url), 0f
                );
            }
        };

//        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
//            @Override
//            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
//                String videoId = Constants.getVideoId(url);
//                youTubePlayer.loadVideo(videoId, 0);
//                youTubePlayer.addListener(listner);
//            }
//        });

        IFramePlayerOptions options = new IFramePlayerOptions.Builder().controls(0).build();

        youTubePlayerView.initialize(youTubePlayerListener, options);


        binding.seconds.setText(taskArrayList.get(currentPosition).getTotalViewTimeQuantity());
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.youtubePlayerViewFragmentView.matchParent();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.youtubePlayerViewFragmentView.wrapContent();
        }
    }

    private void setNewVideoPlayerDetails() {
        String url = getNextUrl();

        if (url != null){
            //binding.youtubePlayerViewFragmentView.release();
            youTubePlayerView.getYouTubePlayerWhenReady(new YouTubePlayerCallback() {
                @Override
                public void onYouTubePlayer(@NonNull YouTubePlayer youTubePlayer) {
                    CustomPlayerUiController customPlayerUiController = new CustomPlayerUiController(requireContext(), customPlayerUi, youTubePlayer, youTubePlayerView);
                    youTubePlayer.addListener(customPlayerUiController);

                    YouTubePlayerUtils.loadOrCueVideo(
                            youTubePlayer, getLifecycle(),
                            Constants.getVideoId(url), 0f
                    );
                }
            });

            binding.seconds.setText(taskArrayList.get(currentPosition).getTotalViewTimeQuantity());

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                    run = true;
                }
            }, 1000);

        } else {
            //Toast.makeText(requireContext(), "No Video Found", Toast.LENGTH_SHORT).show();
            return;
        }


    }

    private String getNextUrl() {
        if (currentPosition < taskArrayList.size()-1) {
            currentPosition = currentPosition + 1;
            return taskArrayList.get(currentPosition).getVideoUrl();
        } else {
            currentPosition = 0;
            if (taskArrayList.size() > 0) {
                return taskArrayList.get(currentPosition).getVideoUrl();
            } else {
                Toast.makeText(requireContext(), "No video found to watch!", Toast.LENGTH_SHORT).show();
                return null;
            }

        }

    }

    private void showToastOnDifferentSec(int sec) {
        if (!run)
            return;
        if (sec != nmbr) {
            if (sec == Integer.parseInt(taskArrayList.get(currentPosition).getTotalViewTimeQuantity()) + 1
                    || sec == currentVideoLength
            ) {
                run = false;

                Toast.makeText(getActivity(), "Completed!", Toast.LENGTH_SHORT).show();

                uploadAddedVideoViews();

            } else {

                int currentValue = Integer.parseInt(binding.seconds.getText().toString());
                String newValue = String.valueOf(currentValue - 1);

                if (currentValue - 1 >= 0) {
                    binding.progressIndicator.setProgress((currentValue - 1));
                } else {
                    binding.progressIndicator.setProgress(0);
                }

                if ((currentValue - 1) < 0)
                    return;

                binding.seconds.setText(newValue);

                nmbr = sec;
            }
        }
    }

    private void uploadAddedVideoViews() {
        progressDialog.show();
        Constants.databaseReference().child(Constants.VIEW_TASKS)
                .child(taskArrayList.get(currentPosition).getTaskKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Taskk taskk = snapshot.getValue(Taskk.class);

                        String currentViews = String.valueOf(taskk.getCurrentViewsQuantity());

                        if (currentViews.equals(taskk.getTotalViewsQuantity())) {

                            Constants.databaseReference().child(Constants.VIEW_TASKS)
                                    .child(taskArrayList.get(currentPosition).getTaskKey())
                                    .child("completedDate")
                                    .setValue(Constants.getDate())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            uploadAddedCoins();
                                        }
                                    });

                        } else {

                            Constants.databaseReference().child(Constants.VIEW_TASKS)
                                    .child(taskArrayList.get(currentPosition).getTaskKey())
                                    .child("currentViewsQuantity")
                                    .setValue(taskk.getCurrentViewsQuantity() + 1)
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
                        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
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

                                        Constants.databaseReference().child(Constants.VIEW_TASKS)
                                                .child(taskArrayList.get(currentPosition).getTaskKey())
                                                .child(Constants.VIEWER_PATH)
                                                .push()
                                                .setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Stash.put(Constants.CHECK, false);
                                                        if (VIP_STATUS) {
                                                            setNewVideoPlayerDetails();
                                                        }
                                                    }
                                                });

                                    }
                                });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.youtubePlayerViewFragmentView.release();
    }

    public class CustomPlayerUiController extends AbstractYouTubePlayerListener {
        private final YouTubePlayerTracker playerTracker;
        private final Context context;
        private final YouTubePlayer youTubePlayer;
        private final YouTubePlayerView youTubePlayerView;
        // panel is used to intercept clicks on the WebView, I don't want the user to be able to click the WebView directly.
        private View panel;
        TextView videoDurationTextView;
        private LinearProgressIndicator progressbar;
        private boolean fullscreen = false;

        public CustomPlayerUiController(Context context, View customPlayerUi, YouTubePlayer youTubePlayer, YouTubePlayerView youTubePlayerView) {
            this.context = context;
            this.youTubePlayer = youTubePlayer;
            this.youTubePlayerView = youTubePlayerView;

            playerTracker = new YouTubePlayerTracker();
            youTubePlayer.addListener(playerTracker);

            initViews(customPlayerUi);
        }

        private void initViews(View playerUi) {
            panel = playerUi.findViewById(R.id.panel);
            progressbar = playerUi.findViewById(R.id.progressbar);
            videoDurationTextView = playerUi.findViewById(R.id.videoDurationTextView);
        }

        @Override
        public void onReady(@NonNull YouTubePlayer youTubePlayer) {

            //progressbar.setVisibility(View.GONE);
        }

        @Override
        public void onStateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerState state) {
            if (state == PlayerConstants.PlayerState.PLAYING || state == PlayerConstants.PlayerState.PAUSED || state == PlayerConstants.PlayerState.VIDEO_CUED)
                panel.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
            else if (state == PlayerConstants.PlayerState.BUFFERING)
                panel.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        }
        int dur = 00;

        @Override
        public void onError(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerError playerError) {
            setNewVideoPlayerDetails();
        }
        @SuppressLint("SetTextI18n")
        @Override
        public void onCurrentSecond(@NonNull YouTubePlayer youTubePlayer, float v) {
            progressbar.setProgress(Math.round(v));
            videoDurationTextView.setText(Math.round(v)+":"+dur);
            showToastOnDifferentSec(Math.round(v));

            if (Math.round(v) == 5) {
                if (checkOverlayPermission()){
                    startService();
                    String url = taskArrayList.get(currentPosition).getVideoUrl();
                    Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + Constants.getVideoId(url)));
                    Intent webIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://www.youtube.com/watch?v=" + Constants.getVideoId(url)));
                    binding.youtubePlayerViewFragmentView.release();
                    try {
                        context.startActivity(appIntent);
                    } catch (ActivityNotFoundException ex) {
                        context.startActivity(webIntent);
                    }
                }
            }

        }

        @Override
        public void onVideoDuration(@NonNull YouTubePlayer youTubePlayer, float v) {
            dur = Math.round(v);
            videoDurationTextView.setText("00:"+dur);
            currentVideoLength = Math.round(v);
            int s = 0;
            try {
                s = Integer.parseInt(binding.seconds.getText().toString());
            } catch (Exception e) {e.printStackTrace();}
            binding.progressIndicator.setMax(s);
            Stash.put(Constants.TIME, s);


        }

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
            uploadAddedVideoViews();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.youtubePlayerViewFragmentView.release();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding.youtubePlayerViewFragmentView.release();
    }
}