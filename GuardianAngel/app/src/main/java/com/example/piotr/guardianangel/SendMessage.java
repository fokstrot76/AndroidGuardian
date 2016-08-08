package com.example.piotr.guardianangel;

import android.Manifest;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Piotr on 21/01/2016.
 */
public class SendMessage extends IntentService
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    private SharedPreferences sharedPreferences;
    private SQLiteDatabase db;
    private ArrayList<String> guardiansId, guardiansName;
    private GoogleApiClient googleApiClient;
    private Location location;
    String user, userPhoneNum;
    public SendMessage() {
        super("SendMessage");
    }
    @Override
    public void onCreate(){
        super.onCreate();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        TelephonyManager tm = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        userPhoneNum = tm.getLine1Number();
        sharedPreferences = this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        user = sharedPreferences.getString("username", null);

            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();

    }

    private Content createContent(double lat, double lon, String user, String userPhoneNum) {
        Content c = new Content();
        String latitude = String.valueOf(lat);
        String longitude = String.valueOf(lon);
        TakeListOfGuardians();
        //guardian GCM registration id
        for(int i=0; i< guardiansId.size(); i++){
            c.addRegId(guardiansId.get(i).toString());
            Log.d("tag", "Guardians id from db: "+ guardiansId.get(i).toString());
        }
        c.createData(latitude, longitude, user, userPhoneNum);
        return c;
    }

    private void TakeListOfGuardians() {
        try{
            db = this.openOrCreateDatabase("GuardianAngel", Context.MODE_PRIVATE, null);
            db.execSQL("CREATE TABLE IF NOT EXISTS guardians (phoneNum VARCHAR, userName VARCHAR, regId VARCHAR);");
            Cursor c = db.rawQuery("SELECT * FROM guardians", null);
            if(c.getCount()<1){
                Log.d("tag", "No record found");
            }else {
                int i=0;
                guardiansId = new ArrayList<String>();
                guardiansName = new ArrayList<String>();
                while (c.moveToNext()) {
                    String id = c.getString(2).toString();
                    if(id.equals("0")){
                        Log.d("tag", "No regId found. Found: "+id);
                    }else {
                        i++;
                        guardiansId.add(c.getString(2));
                        guardiansName.add(c.getString(1));
                        Log.d("tag", "Guardian"+guardiansName.get(i-1)+" "+ guardiansId.get(i-1));
                    }

                }
            }
        }catch (SQLiteException e){
            Toast.makeText(this.getApplicationContext(), "No database, no Guardians, please add one",
                    Toast.LENGTH_LONG).show();
        }
    }

    //GCM - Google Cloud Message
   // private void sendAlarm(String apiKey, Content content) {
    private void sendAlarm(Content content) {
        try {
            URL url = new URL("https://gcm-http.googleapis.com/gcm/send");
            //2. Open connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //3. Set the headers
            //Log.d("tag", "apiKey: "+apiKey);
            //conn.setRequestProperty("Authorization", "key=" + apiKey);
            //conn.setRequestProperty("Authorization", "key=AIzaSyABfx1W-OgRwLkrl-ChZ6wGEzgI1qWmSL0");
            conn.setRequestProperty("Authorization", "key=AIzaSyA3oDS6MigIg06gY69bYcFaMcQdIiv9tpo");
            conn.setRequestProperty("Content-Type", "application/json");
            //4. Specify POST method
            conn.setRequestMethod("POST");
            //5.
            conn.setDoOutput(true);
            //6. Add JSON data into POST request body
            //a) Use Jackson object mapper to convert Contnet object into JSON//offline mode
            ObjectMapper mapper = new ObjectMapper();
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            //b)Get connection output stream
            DataOutputStream dataOutputStream = new DataOutputStream(conn.getOutputStream());
            //c)Copy Content "JSON" into
            mapper.writeValue(dataOutputStream, content);
            //dataOutputStream.write(json.getBytes("UTF-8"));
            //d)Send the request
            dataOutputStream.flush();
            //e)Close
            dataOutputStream.close();

            int responseCode = conn.getResponseCode();
            Log.d("tag", "Content Type : " + conn.getRequestProperty("Content-Type") + " Response Code: " + responseCode + " Authorization " + conn.getRequestProperty("Authorization"));
            Log.d("tag", "\nSending 'POST' request to URL : " + url);
            //System.out.println("\nConnection : " + conn.getInputStream().toString());
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
                Log.d("tag", inputLine);
            }
            in.close();
            //7. Print result
            System.out.println(response.toString());/* */
            Log.d("tag", "response " + response.toString());

            Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(1000);
            Toast.makeText(this.getApplicationContext(), "The message is sent",
                    Toast.LENGTH_LONG).show();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if(location != null){
            Log.d("tag", "Get last Location Google Api "+location.getLatitude()+" "+location.getLongitude());
            Content content = createContent(location.getLatitude(), location.getLongitude(), user, userPhoneNum);
            for(int i=0; i< guardiansId.size(); i++){
                Log.d("tag", "Sending message from "+user+" to "+ guardiansName.get(i).toString() + " position " +
                        "lat: " + location.getLatitude() +
                        " lon: " + location.getLongitude() );
                //String apiKey = guardiansId.get(i).toString();
                //sendAlarm(apiKey, content);
                sendAlarm(content);
            }

        }
        else{
            Log.d("tag", "Location is null");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("tag", "Connection suspended");
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("tag", "Connection Failed");
    }
}
