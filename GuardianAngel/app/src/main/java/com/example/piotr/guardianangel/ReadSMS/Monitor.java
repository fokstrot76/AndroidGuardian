package com.example.piotr.guardianangel.ReadSMS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;

import com.example.piotr.guardianangel.NewGuardianFragment;
import com.example.piotr.guardianangel.ReadSMS.DialogActivity;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by Piotr on 24/01/2016.
 */
public class Monitor extends BroadcastReceiver {

    ArrayList<String> guardiansPhone;
    SQLiteDatabase db;
    int newGuardians;
    String regId;

    @Override
    public void onReceive(Context context, Intent intent) {
        newGuardians = NewGuardianFragment.newGuardians;//get number of new Guardians//TODO: hold number of new guardians in sharedpreferences
        final Bundle bundle = intent.getExtras();
        Log.d("tag", "On receive start. Number of new Guardians: "+newGuardians);
        try{
            if(bundle !=null) {
                db = context.openOrCreateDatabase("GuardianAngel", Context.MODE_PRIVATE, null);
                db.execSQL("CREATE TABLE IF NOT EXISTS guardians (phoneNum VARCHAR, userName VARCHAR, regId VARCHAR);");
                guardiansPhone = new ArrayList();
                Cursor c = db.rawQuery("SELECT * FROM guardians", null);
                if (c.getCount() == 0) {
                    Log.d("tag", "No record found");
                } else {
                    Log.d("tag", c.getCount()+" records");
                    while (c.moveToNext()) {
                        int x = c.getPosition();
                        Log.d("tag", "Position "+x);
                        if(c.getString(2).toString().equals("0")){
                            guardiansPhone.add(c.getString(0));
                            Log.d("tag", "No reg id. Guardian phone added " + c.getString(2).toString());
                        }else {
                            Log.d("tag", "Guardians: " + guardiansPhone.get(0) + " has reg id "+c.getString(2));
                        }
                    }
                    Log.d("tag", "Guardians: phone " + guardiansPhone + " email ");
                }
            }
            //get/read sms message
            final Object[] objects = (Object[])bundle.get("pdus");
            for(int i=0; i<objects.length; i++){
                SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) objects[i]);
                final String phoneNumber = currentMessage.getDisplayOriginatingAddress();
                Log.d("tag", "Sender Phone Number : "+phoneNumber);
                for(int g = 0; g<guardiansPhone.size(); g++){
                    Log.d("tag", "Guardian Number: "+guardiansPhone.get(g));
                    if(phoneNumber.equals(guardiansPhone.get(g))){
                        String message = currentMessage.getDisplayMessageBody();
                        Log.d("tag", "message "+message);
                        StringTokenizer st = new StringTokenizer(message);
                        if(st.nextToken().equalsIgnoreCase("naidraug")){
                            regId = st.nextToken().toString();
                            Log.d("tag", "New guardian with reg is: " + regId);
                            //insert new guardian reg id into guardians table
                            Intent intentDialog = new Intent(context, DialogActivity.class);
                            intentDialog.putExtra("regId", regId);
                            intentDialog.putExtra("phone", phoneNumber);
                            intentDialog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intentDialog);
                                }
                        }else {
                        Log.d("tag", "Did not find the guardian phone number");
                    }
                }
            }



        }catch (SQLiteException e){
            Log.d("tag", "Can't open data base "+e);
        }
    }

    public void cancel(View view) {
        //db.execSQL("UPDATE guardians SET regId='" + regId + "' WHERE phoneNum='" + phoneNumber + "'");
        /*Only to test if the new record is in the table*/
        Cursor c = db.rawQuery("SELECT * FROM guardians", null);
        if (c.getCount() == 0) {
            Log.d("tag", "No record found");
        } else {
            StringBuffer buffer = new StringBuffer();
            while (c.moveToNext()) {
                Log.d("tag", "Guardian phone: " + c.getString(0) + " name: " + c.getString(1) + " reg Id: " + c.getString(2));
            }
        }
    }
}
