/* Source:
 * BeatWatch
 * https://github.com/macsj200/BeatWatch
 */
package com.example.samebodyauth;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

/**
 * This service will keep listening to new heart rate (enabled), accelerations (disabled),
 * angular velocity (disabled) values from the watch's heart rate sensor, accelerometer,
 * and gyroscope, respectively.
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
        Sensor mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // delay SENSOR_DELAY_UI is sufficient for heart rate
        mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mAccSensor, 10); // 100 Hz
        mSensorManager.registerListener(this, mGyroSensor, 10); // 100 Hz

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

        // TODO : Find a way to truly synchronize the sensor data from the watch
        /** It is possible to get all sensor data through the implemented communication channel between
         *  the phone and the Moto 360 watch by concatenating the readings together (as a single String).
         *  However, the different sampling rates across watch sensors remains to be addressed.
         **/


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

        /*
        if(sensorEvent.sensor.getType()==Sensor.TYPE_ACCELEROMETER) {
            // Get watch's current acceleration values
            float[] watch_CurrentAcc = new float[3];
            watch_CurrentAcc[0] = sensorEvent.values[0];
            watch_CurrentAcc[1] = sensorEvent.values[1];
            watch_CurrentAcc[2] = sensorEvent.values[2];

            String toSent = "ACC," +Float.toString(watch_CurrentAcc[0]) + "," +
                    Float.toString(watch_CurrentAcc[1]) + "," + Float.toString(watch_CurrentAcc[2]);

            fireMessage(toSent);
        }

        if(sensorEvent.sensor.getType()==Sensor.TYPE_GYROSCOPE) {
            // Get watch's current acceleration values
            float[] watch_CurrentGyro = new float[3];
            watch_CurrentGyro[0] = sensorEvent.values[0];
            watch_CurrentGyro[1] = sensorEvent.values[1];
            watch_CurrentGyro[2] = sensorEvent.values[2];

            String toSent = "GYRO," +Float.toString(watch_CurrentGyro[0]) + "," +
                    Float.toString(watch_CurrentGyro[1]) + "," + Float.toString(watch_CurrentGyro[2]);

            fireMessage(toSent);
        }
        */
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