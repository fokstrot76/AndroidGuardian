package com.example.piotr.guardianangel.NoiseAndSpeechRecognizer;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class NoiseAlert extends Service {
    private Intent speechServiceIntent;
    private AudioManager audioManager;

    public NoiseAlert() {
        soundLevelService = new SoundLevelService();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static final int POLL_INTERVAL = 500;

    /** running state **/
    private boolean mRunning = false;

    /** config state **/
    private int mThreshold=21;

    //private PowerManager.WakeLock mWakeLock;

    private Handler mHandler = new Handler();


    /* sound data source */
    private SoundLevelService soundLevelService;

    /****************** Define runnable thread again and again detect noise *********/

    private Runnable mSleepTask = new Runnable() {
        public void run() {
            //Log.i("Noise", "runnable mSleepTask");

            start();
        }
    };

    // Create runnable thread to Monitor Voice
    private Runnable mPollTask = new Runnable() {
        public void run() {
            double amp = soundLevelService.getAmplitude();
            //Log.i("Noise", "runnable mPollTask");
            updateDisplay("Monitoring Voice...", amp);

            if ((amp > mThreshold)) {
                callForHelp(amp);
                //Log.i("Noise", "==== onCreate ===");
            }
            // Runnable(mPollTask) will again execute after POLL_INTERVAL
            mHandler.postDelayed(mPollTask, POLL_INTERVAL);
        }
    };
    /** Called when the activity is first created. */
    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        start();
    }
    //@Override
    public void onResume() {
        //super.onResume();
        //Log.i("Noise", "==== onResume ===");
        if (!mRunning) {
            mRunning = true;
            Log.d("tag", "End thread");
            start();
        }
    }

    public void start() {
        if(isMyServiceRunning(SoundLevelService.class)){
            Log.d("tag", "SoundLevel class is running");
        }else{
            Log.d("tag", "SoundLevel class is NOT running");
        }
        Log.i("tag", "==== start ===");
        soundLevelService.stop();
        soundLevelService.start();
        //Noise monitoring start
        // Runnable(mPollTask) will execute after POLL_INTERVAL
        mHandler.postDelayed(mPollTask, POLL_INTERVAL);
    }
    public void stop() {
        Log.i("tag", "==== Stop Noise Monitoring===");
        /*if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }*/
        mHandler.removeCallbacks(mSleepTask);

        mHandler.removeCallbacks(mPollTask);
        soundLevelService.stop();
        //updateDisplay("stopped...", 0.0);
        mRunning = false;

    }

    @Override
    public void onDestroy() {
        Log.d("tag", "NoiseAlert Service Stopped");
        stop();
    }

    private void updateDisplay(String status, double signalEMA) {
        Log.d("tag", "Status "+status+", noise "+signalEMA+" dB");

    }

    protected CountDownTimer mNoSpeechCountDown = new CountDownTimer(20000, 10000){

        @Override
        public void onTick(long millisUntilFinished) {
            Log.d("tag", "A half way of time");
        }

        @Override
        public void onFinish() {
            stopService(speechServiceIntent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
            } else {
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            }
            Log.d("tag", "Speech stopped");
            onResume();
            mNoSpeechCountDown.cancel();
        }
    };


    private void callForHelp(double signalEMA) {
        stop();
        // Show alert when noise thersold crossed
        Toast.makeText(getApplicationContext(), "Noise Thersold Crossed, do here your stuff.",
                Toast.LENGTH_LONG).show();
        Log.d("tag", "Call for help " + String.valueOf(signalEMA) + "dB");
        // Change the stream to your stream of choice.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
        } else {
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        }
        speechServiceIntent = new Intent(getApplicationContext(), SpeechService.class);
        startService(speechServiceIntent);
        mNoSpeechCountDown.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        boolean ser = intent.getBooleanExtra("start", false);
        if(ser){
            Log.d("tag", "start service again");
            start();
        }
        Log.d("tag", "flags " + flags + " startid " + startId);
        return 0;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
