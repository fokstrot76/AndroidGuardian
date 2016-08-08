package com.example.piotr.guardianangel;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {

    EditText password, username;
    TextView loginLabel, userLabel, passwordLabel;
    Button submit, clear;
    SharedPreferences sharedPreferences;
    private Switch language;

    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        username = (EditText)view.findViewById(R.id.userLogin);
        password = (EditText)view.findViewById(R.id.passwordLogin);
        submit = (Button)view.findViewById(R.id.buttonLogin);
        submit.setOnClickListener(this);
        clear = (Button)view.findViewById(R.id.button);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.commit();
                SQLiteDatabase db = getActivity().openOrCreateDatabase("GuardianAngel", Context.MODE_PRIVATE, null);
                db.delete("guardians", null, null);
            }
        });
        loginLabel = (TextView)view.findViewById(R.id.LoginLabel);
        userLabel = (TextView)view.findViewById(R.id.userLabel);
        passwordLabel = (TextView)view.findViewById(R.id.password);
        language = (Switch)view.findViewById(R.id.switchLang);
        language.setChecked(false);
        language.setTextOn("PL");language.setTextOff("EN");
        language.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (language.isChecked()) {
                    loginLabel.setText(R.string.Zaloguj);
                    userLabel.setText(R.string.Nazwa);
                    passwordLabel.setText(R.string.Hasło);
                    submit.setText(R.string.PrzyciskSubmit);
                    clear.setText(R.string.Wyczyść);
                    language.setText(R.string.Language);
                } else {
                    loginLabel.setText(R.string.LoginLabel);
                    userLabel.setText(R.string.UserLabel);
                    passwordLabel.setText(R.string.PasswordLabel);
                    submit.setText(R.string.ButtonRegistration);
                    clear.setText(R.string.Clear);
                    language.setText(R.string.Język);
                }
            }
        });
        return view;
    }

    @Override
    public void onClick(View v) {
        String loginUsername = username.getText().toString();
        String loginPassword = password.getText().toString();
        String localUsername = sharedPreferences.getString("username", null);
        String localPassword = sharedPreferences.getString("password", null);
        if(loginUsername.isEmpty()){
            displayToast("You've not type the username \n please do it now");
        }
        /*else if(loginPassword.isEmpty()){
            displayToast("You've not type the password \n please do it now");
        }*/
        else if(!loginUsername.equals(localUsername)){
            displayToast("Incorrect username, please try again");
        }
        /*else if(!loginPassword.equals(localPassword)){
            displayToast("Incorrect password, please try again");
        }*/
        else{
            SettingsFragment settings = new SettingsFragment();
            getActivity().getSupportFragmentManager().beginTransaction().
                    replace(R.id.fragment_container, settings).commit();
            settings.setRetainInstance(true);
            username.getText().clear();
            password.getText().clear();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if(language.isChecked()){
                editor.putBoolean("EN",false);
            }else {
                editor.putBoolean("EN",true);
            }
            editor.commit();
        }
    }

    private void displayToast(String s) {
        Toast.makeText(getActivity().getApplicationContext(), s,
                Toast.LENGTH_LONG).show();
    }
}
