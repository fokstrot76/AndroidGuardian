package com.example.piotr.guardianangel.ExceptionHandlerAndRestart;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by Piotr on 01/02/2016.
 */
public class GuardianAngelApp extends Application {
    public static final boolean DEV_MODE = true;
    public static final int LOG_LEVEL = DEV_MODE ? Log.VERBOSE : Log.ERROR;

    /**
     * key to hold boolean whether application is registered.
     */
    private static final String UUID = "UUID";

    private ConnectivityManager connectivityManager;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        super.onCreate();
    }

    public NetworkInfo getNetworkInfo() {
        return connectivityManager.getActiveNetworkInfo();
    }

    public NetworkInfo[] getAllNetworkInfo() {
        return connectivityManager.getAllNetworkInfo();
    }

    public boolean isRegistered() {
        return !sharedPreferences.getString(UUID, "0").equalsIgnoreCase("0");
    }

    public void setRegistered(String uuid) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(UUID, uuid);
    }
}
