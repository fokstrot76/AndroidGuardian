package com.example.piotr.guardianangel.NoiseSpeech;

import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;

public class SoundLevelService {
    // This file is used to record voice
    static final private double EMA_FILTER = 0.6;

    private MediaRecorder mRecorder = null;
    private double mEMA = 0.0;

    public void start() {

        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");
            try {
                mRecorder.prepare();
                mRecorder.start();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                // MediaRecorder: start failed: -38
                //IllegalStateException java.lang.IllegalStateException
                Log.d("tag", "IllegalStateException "+e);
                e.printStackTrace();
                stop();
                start();

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.d("tag", "IOException " + e);
            }

            mEMA = 0.0;
        }else {
            Log.d("tag", "Media recorder was not NULL");
        }
    }

    public void stop() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.reset();
            mRecorder = null;
        }
    }

    public double getAmplitude() {
        if (mRecorder != null)
            //return  (mRecorder.getMaxAmplitude()/2700
            return   20 * Math.log10(mRecorder.getMaxAmplitude() / 2700.0);
        else
            return 0;

    }

    public double getAmplitudeEMA() {
        double amp = getAmplitude();
        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
        return mEMA;
    }
}
