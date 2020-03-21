package com.example.samebodyauth;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

/**
 * Created by uwe on 01.04.15.
 */
public class HeartbeatService extends Service implements SensorEventListener {


    private static final String LOG_TAG = "MyHeart";

    private SensorManager mSensorManager;
    private GoogleApiClient mGoogleApiClient;

    private int currentValue = 0;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // register us as a sensor listener
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);


        // delay SENSOR_DELAY_UI is sufficient
        boolean res = mSensorManager.registerListener(this, mHeartRateSensor,  SensorManager.SENSOR_DELAY_UI);
        Log.d(LOG_TAG, " sensor registered: " + (res ? "yes" : "no"));

        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
        mGoogleApiClient.connect();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
        Log.d(LOG_TAG," sensor unregistered");
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // is this a heartbeat event and does it have data?
        if(sensorEvent.sensor.getType()==Sensor.TYPE_HEART_RATE && sensorEvent.values.length > 0) {
            int newValue = Math.round(sensorEvent.values[0]);

            // only do something if the value differs from the value before and the value is not 0.
            if(currentValue != newValue && newValue!=0) {
                // save the new value
                currentValue = newValue;
                // send the value to the listener
                Log.d(LOG_TAG,"sending new value to listener: " + newValue);
                //sendMessageToHandheld(Integer.toString(newValue));
                fireMessage(Integer.toString(newValue));

            }
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    public void fireMessage(String text) {
        Intent msgIntent = new Intent(getApplicationContext(), SendPhoneMessageIntentService.class);
        msgIntent.putExtra(SendPhoneMessageIntentService.INPUT_EXTRA, text);
        startService(msgIntent);
    }

}