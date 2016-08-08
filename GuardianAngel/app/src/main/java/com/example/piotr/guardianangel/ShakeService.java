package com.example.piotr.guardianangel;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class ShakeService extends Service
        implements SensorEventListener{

    private SensorManager sm;
    private Sensor accelerometer;
    private float ERROR = (float) 9;
    private float xStart, yStart, zStart;
    private boolean isStart = true;
    int count = 0;
    long startTime , endTime;
    public ShakeService() {
    }

    @Override
    public void onCreate(){
        super .onCreate();
        sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        Log.d("sensor", "Accelerometer registered");
        return START_STICKY;
    }

        @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        sm.unregisterListener(this, accelerometer);
        super.onDestroy();
        Toast.makeText(this, "Shake Service Stopped.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        if(isStart){
            xStart = x;
            yStart = y;
            zStart = z;
            isStart = false;
        }else {
            float differentX = Math.abs(xStart - x);//absolute value
            //Log.d("sensor", "differentX "+differentX);
            float differentY = Math.abs(yStart - y);
            //Log.d("sensor", "differentY "+differentY);
            float differentZ = Math.abs(zStart - z);
            //Handling ACCELEROMETER Noise
            if(differentX < ERROR){
                differentX = (float)0.0;
            }
            if(differentY < ERROR){
                differentY = (float)0.0;
            }
            if(differentZ < ERROR){
                differentZ = (float)0.0;
            }
            xStart = x;
            yStart = y;
            zStart = z;

            //Horizontal Shake Detected
            if(differentX >differentY) {
                //reset counter to 0 if 3 second gone
                Log.d("Sensor", "Shaking..."+count);
                if ((System.currentTimeMillis() / 1000L) - startTime > 3) {
                    count = 0;
                    Log.d("sensor", "Three second gone, count=0");
                }
                count++;
                Log.d("sensor", "Counter: " + count);
                if (count < 2) {
                    startTime = System.currentTimeMillis() / 1000L;
                    Log.d("sensor", "Start Time: " + startTime);
                } else if (count > 10) {
                    if ((System.currentTimeMillis() / 1000L) - startTime < 4) {
                        this.startService(new Intent(this, SendMessage.class));
                        Toast.makeText(this.getApplicationContext(), "11 shakes detected",
                                Toast.LENGTH_LONG).show();
                        Log.d("sensor", "Done, 6 shakes");
                        count = -1;
                    } else {
                        count = 0;
                        Long time5 = (System.currentTimeMillis() / 1000L) - startTime;
                        Log.d("sensor", "Time of 6 shakes: " + time5);
                    }
                }
            }
        }//end of else
       /*
           //phone on right edge x=-10, y=0, z=0;
           //phone flat x=0; y=0; z=10;
           //phone on left edge x=10; y=0; z=0;
        */
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
