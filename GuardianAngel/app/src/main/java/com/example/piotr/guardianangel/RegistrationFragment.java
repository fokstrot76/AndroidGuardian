package com.example.piotr.guardianangel;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


public class RegistrationFragment extends Fragment implements View.OnClickListener {

    public static final String MyPREFERENCES = "MyPrefs";
    EditText user, password1, password2;
    Button submit;
    Switch language;
    public RegistrationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registration, container, false);
        final TextView registration = (TextView)view.findViewById(R.id.registration);
        final TextView userLabel = (TextView)view.findViewById(R.id.userLabel);
        final TextView passwordLabel = (TextView)view.findViewById(R.id.passwordLabel);
        user = (EditText)view.findViewById(R.id.userRegistration);
        password1 = (EditText)view.findViewById(R.id.password1);
        password2 = (EditText)view.findViewById(R.id.password2);
        submit = (Button)view.findViewById(R.id.buttonRegister);
        submit.setOnClickListener(this);
        language = (Switch)view.findViewById(R.id.switchLang);
        language.setChecked(false);
        language.setTextOn("PL");language.setTextOff("EN");
        language.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(language.isChecked()){
                    registration.setText(R.string.Rejestracja);
                    userLabel.setText(R.string.Nazwa);
                    user.setHint(R.string.Podpowiedź);
                    passwordLabel.setText(R.string.Hasło);
                    password2.setHint(R.string.PodpowiedźPowtórz);
                    submit.setText(R.string.PrzyciskSubmit);
                    language.setText(R.string.Language);
                }else {
                    registration.setText(R.string.Registration);
                    userLabel.setText(R.string.UserLabel);
                    user.setHint(R.string.UserHintReg);
                    passwordLabel.setText(R.string.PasswordLabel);
                    password2.setHint(R.string.Password2Hint);
                    submit.setText(R.string.ButtonRegistration);
                    language.setText(R.string.Język);
                }
            }
        });
        return view;
    }

    @Override
    public void onClick(View v) {
        if(user.getText().toString().isEmpty() || user.getText().length()<3){
            displayToast("User Name,\n or the text is to short (minimum 3 letters");
        /*}else if(password1.getText().toString().isEmpty() || password1.getText().length()<8){
            displayToast("Password, \nor password is to short (minimum 8 letters");
        }else if(password2.getText().toString().isEmpty()){
            displayToast("same password again");
        }else if(!password1.getText().toString().equals(password2.getText().toString())){
            displayToast("same password in second box");*/
        }else {
            Context context = getActivity();
            SharedPreferences sharedPreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("username", user.getText().toString());
            editor.putString("password", password2.getText().toString());
            editor.commit();
            Log.d("sp", "username: " + user.getText().toString() +
                    " passwrod: " + password2.getText().toString());
            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
            LoginFragment login = new LoginFragment();
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, login).commit();/**/
                }
    }

    private void displayToast(String s) {
        Toast.makeText(getActivity().getApplicationContext(),
                "You've not type the " + s + " \n please do it now",
                Toast.LENGTH_LONG).show();
    }
}
