package com.moutamid.typeking.services;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fxn.stash.Stash;
import com.moutamid.typeking.MainActivity;
import com.moutamid.typeking.R;
import com.moutamid.typeking.utilis.Constants;

public class Window extends ContextWrapper {
    private View mView;
    private View iView;
    private WindowManager.LayoutParams mParams;
    private WindowManager.LayoutParams mParamsB;
    private WindowManager mWindowManager;
    private WindowManager mWindowManagerB;
    private LayoutInflater layoutInflater;
    private LayoutInflater layoutempty;

    TextView coin, time;
    View emptyView;
    LinearLayout success, timer;
    int tt, cc;
    boolean isRunning = false;
    CountDownTimer countDownTimer;
    Button back;
    Context ctx;

    public Window(Context context) {
        super(context);

        ctx = context;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // set the layout parameters of the window
            mParams = new WindowManager.LayoutParams(
                    // Shrink the window to wrap the content rather
                    // than filling the screen
                    WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    //      WindowManager.LayoutParams.TYPE_SYSTEM_ALERT |
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                            WindowManager.LayoutParams.FLAG_FULLSCREEN |
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    // through any transparent parts
                    PixelFormat.TRANSLUCENT);
        } else {
            mParams = new WindowManager.LayoutParams(
                    // Shrink the window to wrap the content rather
                    // than filling the screen
                    WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT |
                            WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                            WindowManager.LayoutParams.FLAG_FULLSCREEN |
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    // through any transparent parts
                    PixelFormat.TRANSLUCENT);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            mParamsB = new WindowManager.LayoutParams(
                    // Shrink the window to wrap the content rather
                    // than filling the screen
                    WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
                    // Display it on top of other application windows
                    //        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT |
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                            WindowManager.LayoutParams.FLAG_FULLSCREEN |
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            mParamsB = new WindowManager.LayoutParams(
                    // Shrink the window to wrap the content rather
                    // than filling the screen
                    WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
                    // Display it on top of other application windows
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT |
                            WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                            WindowManager.LayoutParams.FLAG_FULLSCREEN |
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutempty = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mView = layoutInflater.inflate(R.layout.layout_view, null);
        iView = layoutempty.inflate(R.layout.empty_layout, null);

        coin = mView.findViewById(R.id.rewardCoins);
        time = mView.findViewById(R.id.seconds);
        timer = mView.findViewById(R.id.timer);
        back = mView.findViewById(R.id.back);
        success = mView.findViewById(R.id.success);
        emptyView = iView.findViewById(R.id.view2);

        cc = Stash.getInt(Constants.COIN);
        coin.setText(cc + "");

        tt = Stash.getInt(Constants.TIME);
        time.setText(tt + "");

        emptyView.setOnClickListener(v -> {
            Toast.makeText(context, "Please finish watching the video before like/subscribe to this video", Toast.LENGTH_SHORT).show();
        });

        back.setOnClickListener(v -> {
            Stash.put(Constants.CHECK, true);
            Intent intent = new Intent(ctx, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            close();
            stopService(new Intent(getApplicationContext(), ForegroundService.class));
        });

        mParams.gravity = Gravity.BOTTOM;
        mWindowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);

        mParamsB.gravity = Gravity.CENTER;
        mWindowManagerB = (WindowManager) context.getSystemService(WINDOW_SERVICE);
    }

    public void open() {
        try {
            if (mView.getWindowToken() == null && iView.getWindowToken() == null) {
                if (mView.getParent() == null && iView.getParent() == null) {
                    mWindowManager.addView(mView, mParams);
                    mWindowManagerB.addView(iView, mParamsB);
                    Log.d("ServiceTi", "Open");
                    start();
                }
            }
        } catch (Exception e) {
            Log.d("Error1", e.toString());
        }

    }

    private void start() {
        Log.d("ServiceTi", "Start");
        new Handler().postDelayed(() -> {
            Log.d("ServiceTi", "handler");
            isRunning = true;
            // TODO add tt
            countDownTimer = new CountDownTimer((tt * 1000), 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    // Update the TextView with the remaining time
                    int secondsLeft = (int) (millisUntilFinished / 1000);
                    Log.d("ServiceTi", "onTick" + secondsLeft);
                    time.setText(secondsLeft + "");
                    Log.d("ServiceTi", "onTick 2" + secondsLeft);
                }

                @Override
                public void onFinish() {
                    // Perform any actions you want when the timer finishes
                    if (Stash.getBoolean(Constants.isAutoPlayEnabled, false)) {
                        Stash.put(Constants.CHECK, true);
                        Log.d("ServiceTi", "onFinish IFFF" );
                        Intent intent = new Intent(ctx, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        close();
                        stopService(new Intent(getApplicationContext(), ForegroundService.class));
                    } else {
                        Log.d("ServiceTi", "onFinish" );
                        ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(iView);
                        timer.setVisibility(View.GONE);
                        success.setVisibility(View.VISIBLE);
                    }

                }
            };

            // Start the timer
            countDownTimer.start();

        }, 5000);
    }

    public void close() {
        try {
            ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(mView);
            ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(iView);
            // invalidate the view
            mView.invalidate();
            iView.invalidate();
            // remove all views
            ((ViewGroup) mView.getParent()).removeAllViews();
            ((ViewGroup) iView.getParent()).removeAllViews();
            stopService(new Intent(this, ForegroundService.class));
        } catch (Exception e) {
            Log.d("Error2", e.toString());
        }
    }


}
