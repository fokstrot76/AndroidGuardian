package com.example.piotr.guardian;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ManagerFragment extends Fragment {

    EditText name, phone;
    TextView topLabel, listLabel;
    Button button_submit, buttonCancel, buttonRegId, buttonAddPerson;
    RelativeLayout addView, submitView;
    ListView pupilList;
    ArrayAdapter adapter;
    SQLiteDatabase db;
    ArrayList<String> pupilName, pupilPhone, kidsData, kidsName;
    Snackbar snackbar;
    SharedPreferences sharedPreferences;
    Spinner kidsHistory;
    private String clickedPupil [];
    public ManagerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_manager, container, false);
        sharedPreferences = getActivity().getSharedPreferences(MainActivity.MyPREFERENCES, Context.MODE_PRIVATE);
        name = (EditText)view.findViewById(R.id.name);
        phone = (EditText)view.findViewById(R.id.phoneNumber);
        pupilList = (ListView)view.findViewById(R.id.listView);
        checkDataBase();
        topLabel = (TextView)view.findViewById(R.id.AddChildLabel);
        listLabel = (TextView)view.findViewById(R.id.ListLabel);
        button_submit = (Button)view.findViewById(R.id.buttonSubmit);
       // button_submit.setOnClickListener(this);
        button_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewPupil();
            }
        });
        submitView = (RelativeLayout)view.findViewById(R.id.relativeLayoutAdd);
        addView = (RelativeLayout)view.findViewById(R.id.relativeLayoutButton);
        kidsHistory = (Spinner)view.findViewById(R.id.kidsHistory);
        kidsHistory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                displayHistory(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        getHistory();
        buttonAddPerson = (Button)view.findViewById(R.id.buttonAddPerson);
        buttonAddPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addView.setVisibility(View.INVISIBLE);
                submitView.setVisibility(View.VISIBLE);

            }
        });
        buttonCancel = (Button)view.findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phone.setText("+353");
                name.setText("");
                topLabel.setText("Add Person");
                button_submit.setText("Submit Query");
                addView.setVisibility(View.VISIBLE);
                submitView.setVisibility(View.INVISIBLE);
            }
        });
        //buttonCancel.setVisibility(View.GONE);
        buttonRegId = (Button)view.findViewById(R.id.buttonRegId);
        buttonRegId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    try {
                        SmsManager smsManager = SmsManager.getDefault();
                        String regId = sharedPreferences.getString("regId", null);
                        Log.d("tag", "reg id "+regId);
                        smsManager.sendTextMessage(clickedPupil[0], null, "naidraug "+regId, null, null);
                        Toast.makeText(getActivity(), "SMS sent, thank you", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "SMS failed, please try again.", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                        Log.d("tag", "catch " + e);
                    }


            }
        });
        buttonRegId.setVisibility(View.GONE);
        return view;
    }

    private void displayHistory(int position) {
        if(position!=0){
            final String name = kidsName.get(position);
            final ArrayList<String> lat = new ArrayList<String>();
            final ArrayList<String> lon = new ArrayList<String>();
            final ArrayList<String> ph = new ArrayList<String>();
            final Cursor cursor = db.rawQuery("SELECT * FROM history WHERE userName = '"+name+"';", null);
            if(cursor.getCount()==0){
                Log.d("tag", "No data");
            }else {
                kidsData = new ArrayList<String>();

                while (cursor.moveToNext()){
                    kidsData.add("Date: "+cursor.getString(2).toString()+"\n Address: "+cursor.getString(3).toString());
                    lat.add(cursor.getString(4).toString());
                    lon.add(cursor.getString(5).toString());
                    ph.add(cursor.getString(0).toString());
                }
            }
            //adapter for ListView
            adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_expandable_list_item_1, kidsData);
            listLabel.setText(name+" History");
            pupilList.setAdapter(adapter);
            //set the list of date and position to be clickable
            pupilList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//snackbar to display map
                    snackbar = Snackbar.make(getActivity().findViewById(
                            R.id.myCoordinatorLayout), "Do you want to display map? ", Snackbar.LENGTH_LONG);
                    snackbar.setAction(R.string.Display, new displayMap(name, lat.get(position), lon.get(position), ph.get(position)));
                    snackbar.show();
                }
            });
            pupilList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                    snackbar = Snackbar.make(getActivity().findViewById(
                            R.id.myCoordinatorLayout), "Nothing yet to do", Snackbar.LENGTH_LONG);
                    //snackbar.setAction(R.string.Remove, new RemovePupil());
                    snackbar.show();
                    return false;
                }
            });
            kidsName.remove(0);
            kidsName.add(0, "Display List of Pupils");

        }else {
            adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_expandable_list_item_1, pupilName);
            listLabel.setText("List of Pupils");
            pupilList.setAdapter(adapter);
            kidsName.remove(0);
            kidsName.add(0, "Chose a kid to display history");
        }

    }

    private void getHistory() {
        db.execSQL("CREATE TABLE IF NOT EXISTS history (" +
                "phoneNum VARCHAR, userName VARCHAR, data VARCHAR, address VARCHAR, lat VARCHAR, lon VARCHAR);");
        Cursor cursor = db.rawQuery("SELECT userName FROM history", null);
        if(cursor.getCount()==0){
            Log.d("tag", "No record found");
            kidsName = new ArrayList<String>();
            kidsName.add("No records");
        }else{
            kidsName = new ArrayList<String>();
            kidsName.add("Chose a kid to display history");
            String temp;
            while (cursor.moveToNext()) {
                temp = cursor.getString(0).toString();
                Log.d("tag", "Name: " + temp);
                if(!kidsName.contains(temp)){
                    kidsName.add(temp);
                }
            }
        }
        ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(),
                android.R.layout.simple_expandable_list_item_1,
                kidsName );
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        kidsHistory.setAdapter(arrayAdapter);
    }

    private void checkDataBase() {
        try{
            db = getActivity().openOrCreateDatabase("Guardian", Context.MODE_PRIVATE, null);
            db.execSQL("CREATE TABLE IF NOT EXISTS pupils (phoneNum VARCHAR, userName VARCHAR, regId VARCHAR);");
            updatePupilList();
        }catch (SQLiteException e) {
            Toast.makeText(getActivity().getApplicationContext(), "No database, no Guardians, please add one",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void updatePupilList() {
        pupilName = new ArrayList<String>();
        pupilPhone = new ArrayList<String>();
        Cursor cursor = db.rawQuery("SELECT * FROM pupils", null);
        if(cursor.getCount()==0){
            Log.d("tag", "No record found");
            adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_expandable_list_item_1, pupilName);
        }else {
            while (cursor.moveToNext()){
                pupilPhone.add(cursor.getString(0));
                pupilName.add(cursor.getString(1));
            }
            Log.d("tag", "Guardians: name "+pupilName+" phone "+pupilPhone);
            //insert db records to list view
            adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_expandable_list_item_1, pupilName);
            pupilList.setAdapter(adapter);
            //set the list to be clickable
            pupilList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //snackbar to display option for guardian list
                    clickedPupil = new String[]
                            {String.valueOf(pupilPhone.get(position)),
                                    String.valueOf(pupilName.get(position)),};
                    snackbar = Snackbar.make(getActivity().findViewById(
                            R.id.myCoordinatorLayout), "Do you want to update "+clickedPupil[1], Snackbar.LENGTH_LONG);
                    snackbar.setAction(R.string.Update, new UpdatePupil());
                    snackbar.show();
                }
            });
            pupilList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    clickedPupil = new String[]
                            {String.valueOf(pupilPhone.get(position)),
                                    String.valueOf(pupilName.get(position)),};
                    snackbar = Snackbar.make(getActivity().findViewById(
                            R.id.myCoordinatorLayout), "Do yo want to remove "+clickedPupil[1], Snackbar.LENGTH_LONG);
                    snackbar.setAction(R.string.Remove, new RemovePupil());
                    snackbar.show();
                    return true;
                }
            });
        }
    }

    public void AddNewPupil() {
        String username = name.getText().toString();
        String phoneNum = phone.getText().toString();
        if(username.isEmpty()){
            displayToast("You've not type the username \n please do it now");
        }else if(phoneNum.isEmpty()){
            displayToast("You've not type the email \n please do it now");
        }else {
            db.execSQL("INSERT INTO pupils VALUES('" + phoneNum + "','" + username + "','0');");
            updatePupilList();
            name.setText("");
            phone.setText("");
            addView.setVisibility(View.VISIBLE);
            submitView.setVisibility(View.INVISIBLE);
        }

    }

    private void displayToast(String s) {
        Toast.makeText(getActivity().getApplicationContext(), s,
                Toast.LENGTH_LONG).show();
    }


    private class RemovePupil implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            db.execSQL("DELETE FROM pupils WHERE phoneNum='" + clickedPupil[0] + "'");
            updatePupilList();
        }
    }

    private class UpdatePupil implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            topLabel.setText("Update");
            name.setText(clickedPupil[1]);
            phone.setText(clickedPupil[0]);
            button_submit.setText("Update");
            buttonCancel.setVisibility(View.VISIBLE);
            buttonRegId.setVisibility(View.VISIBLE);
            addView.setVisibility(View.INVISIBLE);
            submitView.setVisibility(View.VISIBLE);
        }
    }

    private class displayMap implements View.OnClickListener {
        String n, ph;
        Double la, lo;
        public displayMap(String name, String latitude, String longitude, String phone) {
            Log.d("tag", "intentMap " + name + " " + latitude + " " + longitude + " " + phone);
            n=name;
            la = Double.valueOf(latitude);
            lo = Double.valueOf(longitude);
            ph = phone;
        }

        @Override
        public void onClick(View v) {
            Intent intentMap = new Intent(getActivity(), MapsActivity.class);
            intentMap.putExtra("lat", la);
            intentMap.putExtra("lon", lo);
            intentMap.putExtra("name", n);
            intentMap.putExtra("phone", ph);
            intentMap.putExtra("isNew", false);
            getActivity().startActivity(intentMap);
            Log.d("tag", "intentMap " + n + " " + la + " " + lo + " " + ph );
        }
    }
}
