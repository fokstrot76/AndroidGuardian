package com.example.piotr.guardian;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {

    EditText password, username;
    //TextView login;
    Button submit, clear;
    SharedPreferences sharedPreferences;
    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        sharedPreferences = getActivity().getSharedPreferences(MainActivity.MyPREFERENCES, Context.MODE_PRIVATE);
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
                SQLiteDatabase db = getActivity().openOrCreateDatabase("Guardian", Context.MODE_PRIVATE, null);
                db.delete("pupils", null, null);
                db.delete("history", null, null);
                db.close();
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
       /* else if(!loginPassword.equals(localPassword)){
            displayToast("Incorrect password, please try again");
        }*/
        else{
            ManagerFragment manager = new ManagerFragment();
            getActivity().getSupportFragmentManager().beginTransaction().
                    replace(R.id.fragment_container, manager).commit();
            manager.setRetainInstance(true);
            username.getText().clear();
            password.getText().clear();
        }
    }

    private void displayToast(String s) {
        Toast.makeText(getActivity().getApplicationContext(), s,
                Toast.LENGTH_LONG).show();
    }
}
