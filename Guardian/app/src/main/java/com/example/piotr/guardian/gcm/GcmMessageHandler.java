package com.example.piotr.guardian.gcm;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.piotr.guardian.MapsActivity;
import com.example.piotr.guardian.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class GcmMessageHandler extends IntentService {

    public GcmMessageHandler() {
        super("GcmMessageHandler");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Bundle extras = intent.getExtras();
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
            //The getMessageType() intent parameter must be the intent you received in your BroadcastReceiver
            String messageType = gcm.getMessageType(intent);
            //title = extras.getString("title");
            //msg = extras.getString("message");
            String lat = extras.getString("lat");
            String lon = extras.getString("lon");
            String name = extras.getString("who");
            String phone = extras.getString("phone");
            Log.d("tag", "Who: " + name+" lat:"+lat+" lon:"+lon);
            MyReceiver.completeWakefulIntent(intent);
            //send to map
            double latitude = Double.parseDouble(lat);
            double longitude = Double.parseDouble(lon);
            notification(latitude, longitude,name, phone);
            Vibrator vibrator =(Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(2000);
        }else{
            Log.d("tag", "GSM Message Handler started, but is no intent");
        }
    }

    private void notification(double latitude, double longitude,  String name, String phone) {
        Intent intentMap = new Intent(GcmMessageHandler.this, MapsActivity.class);
        intentMap.putExtra("lat", latitude);
        intentMap.putExtra("lon", longitude);
        intentMap.putExtra("name", name);
        intentMap.putExtra("phone", phone);
        intentMap.putExtra("isNew", true);
        intentMap.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //startActivity(map);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intentMap,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(name+" is in danger")
                        .setContentText("Display Map")
                        .setContentIntent(pendingIntent)
                        .setCategory(Notification.CATEGORY_ALARM)
                        .setSmallIcon(R.drawable.guard_angel_icon)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setAutoCancel(true)
                                //.addAction(R.drawable.sos_icon, "Your pupil in danger", pendingIntent)
                        .build();
        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        nm.notify(0,notification);
    }
}
