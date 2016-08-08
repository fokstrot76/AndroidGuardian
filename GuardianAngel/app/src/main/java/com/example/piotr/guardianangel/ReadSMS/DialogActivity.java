package com.example.piotr.guardianangel.ReadSMS;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.example.piotr.guardianangel.R;

public class DialogActivity extends AppCompatActivity {

    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        TextView alert = (TextView)findViewById(R.id.alert);
        try{
            db = SQLiteDatabase.openDatabase
                    ("/data/data/com.example.piotr.guardianangel/databases/GuardianAngel", null, 0);
            displayAlert();

        }catch (SQLiteException e){
            Log.d("tag", "No database "+e);
            alert.setText("You have to add a Guardians first to your database");
        }

    }

    private void displayAlert() {
        final String regId = getIntent().getExtras().getString("regId");
        final String phoneNumber = getIntent().getExtras().getString("phone");
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("New Guardian")
                .setMessage("Do you want to save new Guardian reg ID?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                db.execSQL("UPDATE guardians SET regId='" + regId + "' WHERE phoneNum='" + phoneNumber + "'");
                                dialog.cancel();
                                //Only to test if the new record is in the table
                                Cursor c = db.rawQuery("SELECT * FROM guardians", null);
                                if (c.getCount() == 0) {
                                    Log.d("tag", "No record found");
                                } else {
                                    StringBuffer buffer = new StringBuffer();
                                    while (c.moveToNext()) {
                                        Log.d("tag", "Guardian phone: " + c.getString(0) + " name: " + c.getString(1) + " reg Id: " + c.getString(2));
                                    }
                                }/**/
                                dialog.cancel();
                                finish();
                            }
                        }
                ).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                finish();
                            }
                        }
                ).setIcon(R.drawable.guard_angel_icon).show();/**/
    }
}
