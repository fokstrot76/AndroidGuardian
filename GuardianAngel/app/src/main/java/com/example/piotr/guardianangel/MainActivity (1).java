package com.example.piotr.guardianangel;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.example.piotr.guardianangel.ExceptionHandlerAndRestart.DefaultExceptionHandler;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    RegistrationFragment registrationFragment;
    LoginFragment loginFragment;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler(this));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            checkAndRequestPermissions();
        }
        if(checkAndRequestPermissions()) {
            sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            if (sharedPreferences.contains("username")) {
                loginFragment = new LoginFragment();
                getSupportFragmentManager().beginTransaction().
                        add(R.id.fragment_container, loginFragment).commit();
            } else {
                registrationFragment = new RegistrationFragment();
                registrationFragment.setArguments(getIntent().getExtras());
                getSupportFragmentManager().beginTransaction().
                        add(R.id.fragment_container, registrationFragment).commit();
            }
        }
    }

    private boolean checkAndRequestPermissions() {
        int RECORD_AUDIO = ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO);
        int RECEIVE_SMS = ContextCompat.checkSelfPermission(this,Manifest.permission.RECEIVE_SMS);
        int ACCESS_FINE_LOCATION = ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION);
        int READ_PHONE_STATE = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE);
        int WRITE_EXTERNAL_STORAGE = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if(RECORD_AUDIO!=PackageManager.PERMISSION_GRANTED){
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }
        if(RECEIVE_SMS!=PackageManager.PERMISSION_GRANTED){
            listPermissionsNeeded.add(Manifest.permission.RECEIVE_SMS);
        }
        if(ACCESS_FINE_LOCATION!=PackageManager.PERMISSION_GRANTED){
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(READ_PHONE_STATE!=PackageManager.PERMISSION_GRANTED){
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(WRITE_EXTERNAL_STORAGE!=PackageManager.PERMISSION_GRANTED){
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if(!listPermissionsNeeded.isEmpty()){
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }
}
