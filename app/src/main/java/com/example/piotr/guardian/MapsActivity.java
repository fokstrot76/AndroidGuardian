package com.example.piotr.guardian;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double lat, lon;
    private String name;
    private String phone;
    private boolean isNew = false;
    Button emergencyButton, pupilButton;
    TextView addressView;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Log.d("tag", "Map class loaded");
        emergencyButton = (Button)findViewById(R.id.buttonEmergency);
        pupilButton = (Button)findViewById(R.id.buttonPupil);
        addressView = (TextView)findViewById(R.id.address);
        Bundle localization = getIntent().getExtras();
        if(localization != null){
            lat = localization.getDouble("lat");
            lon = localization.getDouble("lon");
            name = localization.getString("name");
            phone = localization.getString("phone");
            isNew = localization.getBoolean("isNew");
            pupilButton.setText("Call "+name);
            if(isNew){
                Log.d("tag", "name "+name.toString()+" phone:"+phone+" lat:"+lat+" lon:"+lon);
            }else {
                Log.d("tag", "Is not new. Name "+name.toString()+" phone:"+phone+" lat:"+lat+" lon:"+lon);
            }

        }
        else {
            addressView.setText("Wrong location. Location did not arrive");
            lat = 0;
            lon = 0;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        String address = "";
        Log.d("tag", "Lat:"+lat+" Lon:"+lon);
        try {
            addresses = geocoder.getFromLocation(lat, lon, 1);
            Address geo_address = addresses.get(0);
            address = geo_address.getAddressLine(0).toString()
                    +", "+ geo_address.getAddressLine(1).toString()
                    +", "+ geo_address.getAddressLine(2).toString();
            addressView.setText("Address: "+ address);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(isNew){
            db = this.openOrCreateDatabase("Guardian", Context.MODE_PRIVATE, null);
            db.execSQL("CREATE TABLE IF NOT EXISTS history (" +
                    "phoneNum VARCHAR, userName VARCHAR, data VARCHAR, address VARCHAR, lat VARCHAR, lon VARCHAR);");
            db.execSQL("INSERT INTO history VALUES('" + phone + "'," +
                    "'" + name + "'," +
                    "'"+(DateFormat.format("dd-MM-yyyy hh:mm:ss", new java.util.Date()).toString()) +"',"+
                    "'" + address + "'," +
                    "'" + lat + "'," +
                    "'"+lon+"');");
        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker and move the camera
        LatLng target = new LatLng(lat, lon);
        if(name.equalsIgnoreCase("Hubert")){
            mMap.addMarker(new MarkerOptions().position(target).title(name).icon(BitmapDescriptorFactory.fromResource(R.drawable.hubert)));
        }else {
            mMap.addMarker(new MarkerOptions().position(target).title(name));
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(target, 15));
    }

    public void callPupil(View view) {
        Intent in=new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+phone.toString()));
        try{
            startActivity(in);
        }catch (android.content.ActivityNotFoundException ex){
            Toast.makeText(getApplicationContext(),"I can't make phone call, do manually", Toast.LENGTH_SHORT).show();
        }
    }

    public void callEmergency(View view) {
        Toast.makeText(getApplicationContext(),"Do you really want to call 112? Better get your gun and make this world judicial", Toast.LENGTH_SHORT).show();
        /*Intent in=new Intent(Intent.ACTION_CALL, Uri.parse(""));
        try{
            startActivity(in);
        }catch (android.content.ActivityNotFoundException ex){
            Toast.makeText(getApplicationContext(),"I can't make phone call, do manually", Toast.LENGTH_SHORT).show();
        }*/
    }
}
