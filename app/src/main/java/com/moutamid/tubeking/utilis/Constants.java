package com.moutamid.tubeking.utilis;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import com.google.firebase.database.FirebaseDatabase;
import com.moutamid.tubeking.R;

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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Constants {
    public static final int RC_SIGN_IN = 9001;
    public static final String USER = "user";
    public static final String TIME = "TIME";
    public static final String MODEL = "MODEL";
    public static final String vipStatus = "vipStatus";
    public static final String coins = "coins";
    public static final String COIN = "COIN";
    public static final String CHECK = "CHECK";
    public static final String SUBSCRIBER_PATH = "subscribers";
    public static final String LIKERS_PATH = "subscribers";
    public static final String CURRENT_COINS = "CURRENT_COINS";
    public static final String VIEWER_PATH = "viewers";
    public static final String RECENT_IMAGE = "RECENT_IMAGE";
    public static final String RECENT_LINK = "RECENT_LINK";
    public static final String CAMPAIGN_SELECTION = "CAMPAIGN_SELECTION";
    public static final String VIP_STATUS = "VIP_STATUS";
    public static final String LIKE_TASKS = "like_tasks";
    public static final String VIEW_TASKS = "view_tasks";
    public static final String SUBSCRIBE_TASKS = "subscribe_tasks";
    public static final String TYPE_VIEW = "TYPE_VIEW";
    public static final String TYPE_LIKE = "TYPE_LIKE";
    public static final String TYPE_SUBSCRIBE = "TYPE_SUBSCRIBE";
    public static final String isAutoPlayEnabled = "isAutoPlayEnabled";
    private static RewardedAd rewardedAd;
    private static InterstitialAd mInterstitialAd;
    public static AdRequest adRequest = new AdRequest.Builder().build();

    public static final String LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArBueEiTWfZk7O+BE104DZiZh6Dj8tIMJdZter0tO2KH0MUzn1UB14nrg6bb5IGup29NEvG6UI0ZelHgxClkYvLxzgXjagFgFKN46U6kijHxsccY9evn06NWNyJbPFoAkTQyVIPk44SBufx7g4H5f0azOtY28DL3fg5nvLDJ7yPAejGSvdZXuiVGopwS1A05QrjrgA6ol1YOUzxu22Vanb4ncIDTdF35MA2arbVf74fYFcqkJgWcfWkENRDxoj8IPo1tzWH2rO1/vaILmosJKd1SOrMhrmmyLita0AzJXH/d59Gwu3ed53Ct/Qcq9bDIX3TOALPdQ+NaRkapT2w0twQIDAQAB";
    public static final  String VIP_MONTH = "vip.month.com.moutamid.tubeking";
    public static final  String VIP_YEAR = "vip.year.com.moutamid.tubeking";
    public static final  String COIN_FOUR_THOUSAND = "coin.four.thousand.com.moutamid.tubeking";
    public static final  String COIN_FOUR_HUNDRED_THOUSAND = "coin.four.hundred.thousand.com.moutamid.tubeking";
    public static final  String COIN_TEN_HUNDRED_THOUSAND = "coin.ten.hundred.thousand.com.moutamid.tubeking";
    public static final  String COIN_SIXTY_THOUSAND = "coin.four.thousand.com.moutamid.tubeking";
    public static final  String COIN_TWENTY_FIVE_THOUSAND = "coin.twenty.five.thousand.com.moutamid.tubeking";

    public static final String[] subsQuantityArray = new String[]{
            "10", "20", "30", "40", "50", "60", "70", "80", "90", "100",
            "200", "300", "400", "500", "600", "700", "800", "900", "1000"
    };

    public static final String[] subTimeArray = new String[]{
            "45", "60", "90", "120", "150", "180", "210", "240", "270", "300",
            "330", "360", "390", "420", "450", "480", "510", "540", "570"
    };

    public static final String[] viewQuantityArray = new String[]{
            "10", "50", "100", "150", "200", "250", "300", "350", "400", "450",
            "500", "550", "600", "650", "700", "750", "800", "850", "900", "950", "1000"
    };

    public static final String[] viewTimeArray = new String[] {
            "45", "60", "90", "120", "150", "180", "210", "240", "270", "300",
            "330", "360", "390", "420", "450", "480", "510", "540", "570", "600"
    };
    public static final String[] likeTimeArray = new String[] {
            "30", "60", "90", "120", "150", "180", "210", "240", "270", "300",
            "330", "360", "390", "420", "450", "480", "510", "540", "570"
    };

    public static void checkApp(Activity activity) {
        String appName = "typeking";

        new Thread(() -> {
            URL google = null;
            try {
                google = new URL("https://raw.githubusercontent.com/Moutamid/Moutamid/main/apps.txt");
            } catch (final MalformedURLException e) {
                e.printStackTrace();
            }
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(google != null ? google.openStream() : null));
            } catch (final IOException e) {
                e.printStackTrace();
            }
            String input = null;
            StringBuffer stringBuffer = new StringBuffer();
            while (true) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        if ((input = in != null ? in.readLine() : null) == null) break;
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                stringBuffer.append(input);
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
            String htmlData = stringBuffer.toString();

            try {
                JSONObject myAppObject = new JSONObject(htmlData).getJSONObject(appName);

                boolean value = myAppObject.getBoolean("value");
                String msg = myAppObject.getString("msg");

                if (value) {
                    activity.runOnUiThread(() -> {
                        new AlertDialog.Builder(activity)
                                .setMessage(msg)
                                .setCancelable(false)
                                .show();
                    });
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }).start();
    }

    public static FirebaseAuth auth() {
        return FirebaseAuth.getInstance();
    }

    public static DatabaseReference databaseReference() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("TypeKing");
        db.keepSynced(true);
        return db;
    }

    public static String getVideoId(@NonNull String videoUrl) {
        String videoId = "";
        String regex = "http(?:s)?:\\/\\/(?:m.)?(?:www\\.)?youtu(?:\\.be\\/|be\\.com\\/(?:watch\\?(?:feature=youtu.be\\&)?v=|v\\/|embed\\/|user\\/(?:[\\w#]+\\/)+))([^&#?\\n]+)";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(videoUrl);
        if (matcher.find()) {
            videoId = matcher.group(1);
        }
        return videoId;
    }

    public static class HttpHandler {
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

    public static String getDate() {

        try {
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm aa dd/MM/yyyy");
            return sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Error";

    }

    public static void calledIniti(Context context){
        MobileAds.initialize(context, initializationStatus -> {

        });
    }

    public static void showBannerAd(AdView mAdView){
        mAdView.loadAd(adRequest);
    }

    public static void loadIntersAD(Context context, Activity activity) {
        InterstitialAd.load(context, context.getString(R.string.Interstial_ID), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                        if (mInterstitialAd != null) {
                            mInterstitialAd.show(activity);
                        } else {
                            Log.d("TAG", "The interstitial ad wasn't ready yet.");
                        }
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.d(TAG, loadAdError.toString());
                        mInterstitialAd = null;
                    }
                });
    }

    public static void showNativeAd(Context context, TemplateView myTemplate) {
        AdLoader adLoader = new AdLoader.Builder(context, context.getString(R.string.Native_ID))
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                    @Override
                    public void onNativeAdLoaded(NativeAd nativeAd) {
                        NativeTemplateStyle styles = new NativeTemplateStyle.Builder().build();
                        TemplateView template = myTemplate;
                        template.setStyles(styles);
                        template.setNativeAd(nativeAd);
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        // Handle the failure by logging, altering the UI, and so on.
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        // Methods in the NativeAdOptions.Builder class can be
                        // used here to specify individual options settings.
                        .build())
                .build();

        adLoader.loadAd(adRequest);
    }

}
