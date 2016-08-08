package com.example.piotr.guardianangel;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewGuardianFragment extends Fragment implements View.OnClickListener {

    EditText name, phone;
    TextView guardianLabel, newGuardianLabel, phoneNumberLabel;
    Button submit, cancel;
    SQLiteDatabase db;
    String id[];
    Boolean isUpdate = false;
    public static int newGuardians=0;
    private SharedPreferences sharedPreferences;
    private boolean isEN;

    public NewGuardianFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_guardian, container, false);
        sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        isEN = sharedPreferences.getBoolean("EN", true);
        db = getActivity().openOrCreateDatabase("GuardianAngel", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS guardians (phoneNum VARCHAR, userName VARCHAR, regId VARCHAR);");
        name = (EditText)view.findViewById(R.id.newGuardianUsername);
        phone = (EditText)view.findViewById(R.id.guardianPhone);
        phone.setText("+353");
        guardianLabel = (TextView)view.findViewById(R.id.GuardianLabel);
        newGuardianLabel = (TextView)view.findViewById(R.id.NameGardianLabel);
        phoneNumberLabel = (TextView)view.findViewById(R.id.PhoneLabel);
        try{
            id = getArguments().getStringArray("guard");
            updateMode(id);
            isUpdate = true;
        }catch (Exception e){
            Log.d("tag", "catch and do adding new guardian "+e);
        }
        cancel = (Button)view.findViewById(R.id.buttonCancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsFragment settings = new SettingsFragment();
                getActivity().getSupportFragmentManager().beginTransaction().
                        replace(R.id.fragment_container, settings).
                        commit();
                settings.setRetainInstance(true);
            }
        });
        submit = (Button)view.findViewById(R.id.buttonAddGuardian);
        submit.setOnClickListener(this);
        checkLanguage();
        return view;
    }

    private void checkLanguage() {
        if(isUpdate){
            if(isEN){
                guardianLabel.setText(R.string.Update);
            }else {
                guardianLabel.setText(R.string.Aktualizuj);
            }
        }else {
            if (isEN) {
                guardianLabel.setText(R.string.AddGuardianLabel);
                cancel.setText(R.string.ButtonCancel);
                newGuardianLabel.setText(R.string.UserLabel);
                phoneNumberLabel.setText(R.string.PhoneLabel);
                submit.setText(R.string.ButtonAddGuardian);
            }else {
                guardianLabel.setText(R.string.DodajOpiekuna);
                guardianLabel.setTextSize(35);
                cancel.setText(R.string.Anuluj);
                newGuardianLabel.setText(R.string.Nazwa);
                phoneNumberLabel.setText(R.string.Telefon);
                submit.setText(R.string.PrzyciskSubmit);
                phone.setText("+48");
            }
        }

    }

    private void updateMode(String []id) {
        name.setText(id[1]);
        phone.setText(id[0]);
    }

    @Override
    public void onClick(View v) {
        String username = name.getText().toString();
        String phoneNum = phone.getText().toString();
        if(username.isEmpty()){
            displayToast("You've not type the username \n please do it now");
        }else if(phoneNum.isEmpty()){
            displayToast("You've not type the email \n please do it now");
        }else {
            if(isUpdate){
                db.execSQL("UPDATE guardians SET phoneNum='"+phoneNum+"',userName='"+ username+"' WHERE phoneNum='"+id[0]+"'");
                isUpdate = false;
            }else {
                db.execSQL("INSERT INTO guardians VALUES('"+phoneNum+"','"+ username+"','0');");
            }
            SettingsFragment settings = new SettingsFragment();
            getActivity().getSupportFragmentManager().beginTransaction().
                    replace(R.id.fragment_container, settings).
                    commit();
            settings.setRetainInstance(true);
            newGuardians++;
        }
    }

    private void displayToast(String s) {
        Toast.makeText(getActivity().getApplicationContext(), s,
                Toast.LENGTH_LONG).show();
    }
}
