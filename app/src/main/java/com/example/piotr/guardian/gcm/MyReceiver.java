package com.example.piotr.guardian.gcm;

import android.app.Activity;
import android.content.ComponentName;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyReceiver extends WakefulBroadcastReceiver {
    public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Explicitly specify that GcmMessageHandler will handle the intent.
        ComponentName componentName = new ComponentName(context.getPackageName(), GcmMessageHandler.class.getName());
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(componentName)));
        setResultCode(Activity.RESULT_OK);
        Log.d("tag", "My Receiver got a message");
    }
}
