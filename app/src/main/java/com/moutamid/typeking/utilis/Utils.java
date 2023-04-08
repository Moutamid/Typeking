package com.moutamid.typeking.utilis;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;

import androidx.appcompat.app.AlertDialog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;


import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.widget.Toast;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class Utils {

    private static Utils utils;
    private static Context instance;
    private SharedPreferences sp;

    public Utils() {
    }

    public static void init(Context context) {
        utils = new Utils();
        instance = context;
        if (utils.sp == null) {
            utils.sp = context.getSharedPreferences("dev.moutamid.viewplussubsbooster", Context.MODE_PRIVATE);
//            utils.sp = PreferenceManager.getDefaultSharedPreferences(context);
        }

    }

    private static void checkfornull() {
        if (utils == null)
            throw new NullPointerException("Call init() method in application context class for the manifest");
    }

    // toast
    public static void toast(String msg) {
        checkfornull();
        Toast.makeText(instance, msg, Toast.LENGTH_SHORT).show();
    }

    //putString
    public static void store(String key, String value) {
        checkfornull();
        try {
            utils.sp.edit().putString(key, value).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //putInt
    public static void store(String key, int value) {
        checkfornull();
        try {
            utils.sp.edit().putInt(key, value).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //putLong
    public static void store(String key, long value) {
        checkfornull();
        try {
            utils.sp.edit().putLong(key, value).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //putFloat
    public static void store(String key, float value) {
        checkfornull();
        try {
            utils.sp.edit().putFloat(key, value).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //putBoolean
    public static void store(String key, boolean value) {
        checkfornull();
        try {
            utils.sp.edit().putBoolean(key, value).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getString(String key, String defaultvalue) {
        checkfornull();
        try {
            return utils.sp.getString(key, defaultvalue);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultvalue;
        }
    }

    public static String getString(String key) {
        return getString(key, "Error");
    }

    public static int getInt(String key, int defaultvalue) {
        checkfornull();
        try {
            return utils.sp.getInt(key, defaultvalue);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultvalue;
        }
    }

    public static int getInt(String key) {
        return getInt(key, 0);
    }

    public static long getLong(String key, long defaultvalue) {
        checkfornull();
        try {
            return utils.sp.getLong(key, defaultvalue);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultvalue;
        }
    }

    public static long getLong(String key) {
        return getLong(key, (long) 0);
    }

    public static float getFloat(String key, float defaultvalue) {
        checkfornull();
        try {
            return utils.sp.getFloat(key, defaultvalue);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultvalue;
        }
    }

    public static float getFloat(String key) {
        return getFloat(key, 0.0f);
    }

    public static boolean getBoolean(String key, boolean defaultvalue) {
        checkfornull();
        try {
            return utils.sp.getBoolean(key, defaultvalue);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultvalue;
        }
    }

    public static boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    //clear single value
    public static void remove(String key) {
        checkfornull();
        try {
            utils.sp.edit().remove(key).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //clear all preference
    public static void removeSharedPref() {
        checkfornull();
        try {
            utils.sp.edit().clear().apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static int getRandomNmbr(int length) {
        return new Random().nextInt(length) + 100;
    }


    public static String getDate() {

        try {

            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            return sdf.format(date);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Error";

    }

    private static class GenericType implements ParameterizedType {

        private Type type;

        GenericType(Type type) {
            this.type = type;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return new Type[]{type};
        }

        @Override
        public Type getRawType() {
            return ArrayList.class;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }

}

