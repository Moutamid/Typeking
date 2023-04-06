package com.moutamid.typeking.constant;

import android.app.Activity;
import android.os.Build;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Constants {
    public static final int RC_SIGN_IN = 9001;
    public static final String CURRENT_COINS = "CURRENT_COINS";
    public static final String RECENT_IMAGE = "RECENT_IMAGE";
    public static final String RECENT_LINK = "RECENT_LINK";
    public static final String CAMPAIGN_SELECTION = "CAMPAIGN_SELECTION";
    public static final String VIP_STATUS = "VIP_STATUS";

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

}
