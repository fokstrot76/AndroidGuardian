package com.example.piotr.guardianangel.NoiseAndSpeechRecognizer;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import com.example.piotr.guardianangel.GCM.SendMessage;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class SpeechService extends Service {
    private ArrayList<String> spoken;
    private Intent noiseAlertIntent;
    private NoiseAlert noiseAlert;
    private StringTokenizer token;

    public SpeechService() {
    }

    protected SpeechRecognizer mSpeechRecognizer;
    protected Intent mSpeechRecognizerIntent;
    static final int MSG_RECOGNIZER_START_LISTENING = 1;
    static final int MSG_RECOGNIZER_CANCEL = 2;

    @Override
    public void onCreate()
    {
        super.onCreate();

        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());
        Log.d("tag", "created mSpeechRecognizer");
        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);

    }

    // Count down timer for Jelly Bean work around
    protected CountDownTimer mNoSpeechCountDown = new CountDownTimer(2500, 1000)
    {

        @Override
        public void onTick(long millisUntilFinished) {
            // TODO Auto-generated method stub
            Log.d("tag", "millisUntilFinished "+millisUntilFinished);
        }

        @Override
        public void onFinish()
        {
            Message message = Message.obtain(null, MSG_RECOGNIZER_CANCEL);
        }
    };

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (mSpeechRecognizer != null)
        {
            mSpeechRecognizer.destroy();
            Log.d("tag", "mSpeechRecognizer destroy");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected class SpeechRecognitionListener implements RecognitionListener
    {

        @Override
        public void onBeginningOfSpeech()
        {
            Log.d("tag", "onBeginingOfSpeech"); //$NON-NLS-1$
        }

        @Override
        public void onBufferReceived(byte[] buffer)
        {

        }

        @Override
        public void onEndOfSpeech()
        {
            Log.d("tag", "onEndOfSpeech"); //$NON-NLS-1$

        }

        @Override
        public void onError(int error)
        {
            /**
             * Error 1 ERROR_NETWORK_TIMEOUT
             * Error 2 ERROR_NETWORK
             * Error 3 ERROR_AUDIO
             * Errro 4 ERROR_SERVER
             * Error 5 ERROR_CLIENT
             * Error 6 ERROR_SPEECH_TIMEOUT
             * Error 7 ERROR_NO_MATCH
             * Error 8 ERROR_RECOGNIZER_BUSY
             * Error 9 ERROR_INSUFFICIENT_PERMISSIONS
             */
            Log.d("tag", "error = " + error); //$NON-NLS-1$
            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
        }

        @Override
        public void onEvent(int eventType, Bundle params)
        {

        }

        @Override
        public void onPartialResults(Bundle partialResults)
        {
            if(partialResults.toString().contains("help")){
                Log.d("tag", "onPartialResults send alarm message !!!!");
            }
        }

        @Override
        public void onReadyForSpeech(Bundle params)
        {
            Log.d("tag", "onReadyForSpeech"); //$NON-NLS-1$
        }

        @Override
        public void onResults(Bundle results)
        {
            if((results!=null) && results.containsKey(SpeechRecognizer.RESULTS_RECOGNITION)){
                spoken = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                Log.d("tag", "Result " + spoken);
                for(int i=0; i<spoken.size(); i++){
                    token = new StringTokenizer(spoken.get(i));
                    while (token.hasMoreElements()){
                        if(token.nextElement().toString().contains("help")){
                            Log.d("tag", "Found Token. Send Alarm Message!!!!!!!!!!!!!!!!!");
                            mSpeechRecognizer.cancel();
                            startService(new Intent(getApplicationContext(), SendMessage.class));
                            onDestroy();
                            //noiseAlertIntent = new Intent(getApplicationContext(), NoiseAlert.class);
                            //noiseAlertIntent.putExtra("start", true);
                            //startService(noiseAlertIntent);
                            break;
                        }
                    }
                }

                if(spoken.contains("help") || spoken.contains("help me") || spoken.contains("help me please")){
                    Log.d("tag", "Send Alarm Message!!!!!!!!!!!!!!!!!");
                    mSpeechRecognizer.cancel();
                    startService(new Intent(getApplicationContext(), SendMessage.class));
                    onDestroy();
                    noiseAlertIntent = new Intent(getApplicationContext(), NoiseAlert.class);
                    noiseAlertIntent.putExtra("start", true);
                    startService(noiseAlertIntent);
                }else
                    mSpeechRecognizer.startListening(mSpeechRecognizerIntent);

            }
        }

        @Override
        public void onRmsChanged(float rmsdB)
        {

        }

    }
}
