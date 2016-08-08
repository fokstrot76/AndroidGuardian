package com.example.piotr.guardianangel;


import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.example.piotr.guardianangel.NoiseSpeech.NoiseAlert;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment implements View.OnClickListener {

    SharedPreferences sharedPreferences;
    CheckBox shaking, screaming, pressing;
    ListView guardians_list;
    Button addGuardian;
    ToggleButton OnOff;
    TextView welcomeLabel, username, alarmLabel, guardiansLabel;
    String user;
    ArrayList guardiansName, guardiansPhone, guardianRegId;
    ArrayAdapter adapter;
    SQLiteDatabase db;
    NewGuardianFragment guardianFragment;
    Snackbar snackbar;
    String [] clickedGuardian;
    NotificationManager notificationManager;
    Boolean isPressing, isShaking, isScreaming, isEN;
    int numberOfGuardians = 0;
    private Intent intentScream;
    private Intent intentShake;

    public SettingsFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationManager = (NotificationManager) getActivity().getSystemService(Context
                .NOTIFICATION_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        guardians_list = (ListView)view.findViewById(R.id.listView);
        checkDB();
        welcomeLabel = (TextView)view.findViewById(R.id.welcomeLabel);
        alarmLabel = (TextView)view.findViewById(R.id.AlarmLabel);
        guardiansLabel = (TextView)view.findViewById(R.id.GuardiansLabel);
        username = (TextView)view.findViewById(R.id.userLabel);
        shaking = (CheckBox)view.findViewById(R.id.checkBoxShaking);
        screaming = (CheckBox)view.findViewById(R.id.checkBoxScreaming);
        pressing = (CheckBox)view.findViewById(R.id.checkBoxPressing);
        addGuardian = (Button)view.findViewById(R.id.buttonAddGuardian);
        if(numberOfGuardians<21){//The maximum number of members allowed for a notification_key in GCM is 20.
            addGuardian.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    guardianFragment = new NewGuardianFragment();
                    getActivity().getSupportFragmentManager().beginTransaction().
                            replace(R.id.fragment_container, guardianFragment).commit();
                }
            });
        }
        OnOff = (ToggleButton)view.findViewById(R.id.toggleButton);
        OnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (guardianRegId.size() > 0) {
                        for (int i = 0; i < guardianRegId.size(); i++) {
                            if (!guardianRegId.get(i).equals("0")) {
                                SwitchOnServices();
                                break;
                            } else {
                                updateGuardianList();
                                if (!guardianRegId.get(i).equals("0")) {
                                    SwitchOnServices();
                                    break;
                                }else {
                                    OnOff.setChecked(false);
                                    Toast.makeText(getActivity().getApplicationContext(),
                                            "You have to add register Id to your guardian",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    } else {
                        OnOff.setChecked(false);
                        Toast.makeText(getActivity().getApplicationContext(),
                                "You have to add minimum one guardian",
                                Toast.LENGTH_LONG).show();
                    }

                } else {
                    SwitchOffServices();
                }
            }
        });
        checkIfCheckBoxesAreChecked();
        return view;
    }

    private void SwitchOffServices() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Log.d("tag", "Let's switch OFF");
        //make pressing off
        editor.putBoolean("isPressing", false);
        notificationManager.cancelAll();
        pressing.setTextColor(Color.BLACK);
        pressing.setChecked(false);
        isPressing = false;
        //make shaking off
        editor.putBoolean("isShaking", false);
        getActivity().stopService(intentShake);
        shaking.setTextColor(Color.BLACK);
        shaking.setChecked(false);
        isShaking = false;
        //make screaming off
        editor.putBoolean("isScreaming", false);
        getActivity().stopService(intentScream);
        screaming.setTextColor(Color.BLACK);
        screaming.setChecked(false);
        isScreaming = false;
        Log.d("tag", "Services switched OFF");
        editor.commit();
    }

    private void SwitchOnServices() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        intentShake = new Intent(getActivity(), ShakeService.class);
        //intentScream = new Intent(getActivity(), ScreamService.class);
        intentScream = new Intent(getActivity(), NoiseAlert.class);
            if(screaming.isChecked()){
                isScreaming = true;
                editor.putBoolean("isScreaming", true);
                getActivity().startService(intentScream);
                screaming.setTextColor(Color.RED);
                Log.d("tag", "screaming is checked");
            }
            if(shaking.isChecked()){
                isShaking = true;
                editor.putBoolean("isShaking", true);
                getActivity().startService(intentShake);
                shaking.setTextColor(Color.RED);
                Log.d("tag", "shaking is checked");
            }
            if(pressing.isChecked()){
                isPressing = true;
                editor.putBoolean("isPressing", true);
                SetNotification();
                pressing.setTextColor(Color.RED);
                Log.d("tag", "pressing is checked");
            }
            editor.commit();
        if(isScreaming || isPressing || isShaking){

        }else {
            Toast.makeText(getActivity().getApplicationContext(),
                    "Alarm method is not chosen",
                    Toast.LENGTH_LONG).show();
            OnOff.setChecked(false);
        }


    }

    private void checkIfCheckBoxesAreChecked() {
        sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        user = sharedPreferences.getString("username", null);
        isShaking = sharedPreferences.getBoolean("isShaking", false);
        isPressing = sharedPreferences.getBoolean("isPressing", false);
        isScreaming = sharedPreferences.getBoolean("isScreaming", false);
        if(user!=null){
            username.setText(user);
        }
        if(isScreaming){
            screaming.setChecked(true);
        }
        if(isPressing){
            pressing.setChecked(true);
        }
        if(isShaking){
            shaking.setChecked(true);
        }

        checkLanguage();
    }

    private void checkLanguage() {
        isEN = sharedPreferences.getBoolean("EN", true);
        if (isEN) {
            welcomeLabel.setText(R.string.WelcomeLabel);
            alarmLabel.setText(R.string.AlarmLabel);
            shaking.setText(R.string.BoxShaking);
            screaming.setText(R.string.BoxScreaming);
            pressing.setText(R.string.BoxPressing);
            guardiansLabel.setText(R.string.GuardianLabel);
            addGuardian.setText(R.string.ButtonAddGuardian);
        }else {
            welcomeLabel.setText(R.string.Witaj);
            alarmLabel.setText(R.string.WybierzAlarm);
            shaking.setText(R.string.Potrząsanie);
            screaming.setText(R.string.Krzyk);
            pressing.setText(R.string.Przycisk);
            guardiansLabel.setText(R.string.Opiekunowie);
            addGuardian.setText(R.string.DodajOpiekuna);
        }
    }

    private void checkDB() {
        try{
            db = getActivity().openOrCreateDatabase("GuardianAngel", Context.MODE_PRIVATE, null);
            db.execSQL("CREATE TABLE IF NOT EXISTS guardians (phoneNum VARCHAR, userName VARCHAR, regId VARCHAR);");
            updateGuardianList();
        }catch (SQLiteException e){
            Toast.makeText(getActivity().getApplicationContext(), "No database, no Guardians, please add one",
                    Toast.LENGTH_LONG).show();
            guardianFragment = new NewGuardianFragment();
            getActivity().getSupportFragmentManager().beginTransaction().
                    replace(R.id.fragment_container, guardianFragment).commit();
        }
    }

    private void updateGuardianList() {
        //add records from database to instantiated array list
        guardiansName = new ArrayList();
        guardiansPhone = new ArrayList();
        guardianRegId = new ArrayList();
        Cursor c = db.rawQuery("SELECT * FROM guardians", null);
        if(c.getCount()==0){
            Log.d("tag", "No record found");
        }else {
            while (c.moveToNext()){
                guardiansPhone.add(c.getString(0));
                guardiansName.add(c.getString(1));
                guardianRegId.add(c.getString(2));
            }
            Log.d("tag", "Guardians: name "+guardiansName+" phone "+guardiansPhone+" regId:"+guardianRegId);
            //insert db records to list view
            adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_expandable_list_item_1, guardiansName);
            guardians_list.setAdapter(adapter);
            //set the list to be clickable
            guardians_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //snackbar to display option for guardian list
                    String message = String.valueOf(guardiansName.get(position));
                    clickedGuardian = new String[]
                            {String.valueOf(guardiansPhone.get(position)),
                            String.valueOf(guardiansName.get(position)),};
                    snackbar = Snackbar.make(getActivity().findViewById(R.id.myCoordinatorLayout), message, Snackbar.LENGTH_LONG);
                    snackbar.setAction(R.string.Update, new UpdateGuardian());
                    snackbar.show();
                }
            });
            guardians_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    String message = String.valueOf(guardiansName.get(position));
                    clickedGuardian = new String[]{String.valueOf(guardiansPhone.get(position))};
                    snackbar = Snackbar.make(getActivity().findViewById(R.id.myCoordinatorLayout), message, Snackbar.LENGTH_LONG);
                    snackbar.setAction(R.string.Remove, new RemoveGuardian());
                    snackbar.show();
                    return true;
                }
            });
        }
    }

    private void SetNotification() {
        Intent sendMessage = new Intent(getActivity(), SendMessage.class);
        sendMessage.putStringArrayListExtra("guard", guardiansPhone);
        sendMessage.putExtra("user", user);
        PendingIntent pendingIntentMessage = PendingIntent.getService(getActivity(), 0, sendMessage, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(getActivity())
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setContentTitle("Guard Angel")
                .setAutoCancel(false)
                .setOngoing(true)
                .addAction(R.drawable.sos_icon, "SOS", pendingIntentMessage)
                .setColor(Color.WHITE)
                .setSmallIcon(R.drawable.guard_angel_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.guard_angel_icon))
                .build();

        notificationManager.notify(0, notification);

    }

    @Override
    public void onClick(View v) {

    }

    private class RemoveGuardian implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if(guardiansName.size()>1){
                db.execSQL("DELETE FROM guardians WHERE phoneNum='" + clickedGuardian[0] + "'");
                updateGuardianList();
            }else {
                Toast.makeText(getActivity().getApplicationContext(),
                                "If you want to remove this guardian \n " +
                                "you have to first add another guardian",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private class UpdateGuardian implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            guardianFragment = new NewGuardianFragment();
            Bundle bundle = new Bundle();
            bundle.putStringArray("guard", clickedGuardian);
            guardianFragment.setArguments(bundle);
            getActivity().getSupportFragmentManager().beginTransaction().
                    replace(R.id.fragment_container, guardianFragment).commit();
        }
    }
}
