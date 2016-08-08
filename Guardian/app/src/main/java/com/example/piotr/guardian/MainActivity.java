package com.example.piotr.guardian;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_WRITE_PERMISSION = 786;
    public static String MyPREFERENCES = "guardian shared preferences";
    //final String [] permission = new String[]{Manifest.permission.SEND_SMS};
    SharedPreferences sharedPreferences;
    RegistrationFragment registrationFragment;
    LoginFragment loginFragment;
    //Google Cloud Messaging
    GoogleCloudMessaging gcm;
    String regId;
    String PROJECT_NUMBER = "913848435805";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        if(regId == null){
            //get device registration id
            getRegId();
        }
        if(sharedPreferences.contains("username")){
            loginFragment = new LoginFragment();
            getSupportFragmentManager().beginTransaction().
                    add(R.id.fragment_container, loginFragment).commit();
        }else {
            registrationFragment = new RegistrationFragment();
            registrationFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().
                    add(R.id.fragment_container, registrationFragment).commit();
        }
    }

    private void requestPermissions() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(new String[]{
                    Manifest.permission.SEND_SMS
            },REQUEST_WRITE_PERMISSION
            );
        }
    }

    private void getRegId() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try{
                    if(gcm == null){
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regId = gcm.register(PROJECT_NUMBER);
                    msg = "Device registered, registration ID = "+regId;
                    Log.d("tag", msg);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("regId", regId);
                    editor.commit();
                } catch (IOException e) {
                    e.printStackTrace();
                    msg = "Error: "+e.getMessage();
                }
                return msg;
            }
            @Override
            protected void onPostExecute(String msg){
                Log.d("log", "Registration id: "+msg);
            }
        }.execute(null, null, null);
    }
}
