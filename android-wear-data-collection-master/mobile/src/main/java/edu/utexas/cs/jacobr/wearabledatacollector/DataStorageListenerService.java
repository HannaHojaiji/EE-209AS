package edu.utexas.cs.jacobr.wearabledatacollector;

//import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

public class DataStorageListenerService extends WearableListenerService{
    private static final String TAG = "DataStorageListenerService";
    //private static final String SENSOR_DATA_PATH = "/sensor-data";
    private static final String SHARED_PREFS_KEY = "data-collection-prefs";
    private static final String SLEEPING_KEY = "sleeping";

    private GoogleApiClient mGoogleApiClient;
    private SharedPreferences preferences;

    //Used for recording data
    private File dirpath;
    private FileOutputStream outputStream;
    private boolean isRecording;
    private String recentTitle;

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();


        //Create file to write on and starts writing
        if(isExternalStorageWritable()) {
            dirpath = createFile();
            try {
                outputStream = new FileOutputStream(dirpath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            isRecording = true;
            //Write a header
            String mess = "Date,Time,Seconds,Sensing Current µA, Slope, Intercept, Concentration\n";
            try{
                outputStream.write(mess.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    //Create a file to write on
    private File createFile(){

        String timeDate = "[" + getDateString() + " " + getTimeString() + "]";
        //Toast.makeText(this,"Clicked!",Toast.LENGTH_LONG).show();
        File Root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File dir = new File(Root.getAbsolutePath() + "/209CPS");
        if(!dir.exists())
            dir.mkdir();
        recentTitle = "AGH-209AS" + timeDate + ".txt";
        File file = new File(dir, recentTitle);
        recentTitle = dir + "/" + recentTitle;
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
    //Other auxilary functions

    //returns a string containing today's date
    public static String getDateString(){
        Date time = Calendar.getInstance().getTime();
        SimpleDateFormat outputFmt = new SimpleDateFormat("MM-dd-yyyy");
        return outputFmt.format(time);
    }

    //returns a string containing a timestamp in format (HH-mm-ss)
    public static String getTimeString(){
        Date time = Calendar.getInstance().getTime();
        SimpleDateFormat outputFmt = new SimpleDateFormat("HH-mm-ss");
        return outputFmt.format(time);
    }

    public static String getTimeStringWithColons(){
        Date time = Calendar.getInstance().getTime();
        SimpleDateFormat outputFmt = new SimpleDateFormat("HH:mm:ss");
        return outputFmt.format(time);
    }

    //@SuppressLint("LongLogTag")
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged: " + dataEvents);
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();
        if(!mGoogleApiClient.isConnected()) {
            ConnectionResult connectionResult = mGoogleApiClient
                    .blockingConnect(30, TimeUnit.SECONDS);
            if (!connectionResult.isSuccess()) {
                Log.e(TAG, "DataLayerListenerService failed to connect to GoogleApiClient.");
                Toast.makeText(getApplicationContext(),"GoogleApiClient", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Loop through the events and send a message back to the node that created the data item.
        for (DataEvent event : events) {
            Uri uri = event.getDataItem().getUri();
           // String path = uri.getPath();
           // if (SENSOR_DATA_PATH.equals(path) && event.getType() == DataEvent.TYPE_CHANGED) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                byte[] rawData = event.getDataItem().getData();
                DataMap sensorData = DataMap.fromByteArray(rawData);
                sensorData.putBoolean(SLEEPING_KEY, preferences.getBoolean(SLEEPING_KEY, false));
                Log.d(TAG, "Recording new data item: " + sensorData);
                //saveData(sensorData);
                String dataJSON = dataMapAsJSONObject(sensorData).toString() + "\n";
                try {
                    outputStream.write(dataJSON.getBytes());
                    //Close writing file
                    try {
                        outputStream.close();
                        outputStream = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    Log.d(TAG, "Error Saving");
                    Toast.makeText(getApplicationContext(),"Error Saving", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    }

    private JSONObject dataMapAsJSONObject(DataMap data) {
        Bundle bundle = data.toBundle();
        JSONObject json = new JSONObject();
        Set<String> keys = bundle.keySet();
        for (String key : keys) {
            try {
                // json.put(key, bundle.get(key)); see edit below
                json.put(key, JSONObject.wrap(bundle.get(key)));
            } catch(JSONException e) {
                //Handle exception here
            }
        }
        return json;
    }

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

   /* @SuppressLint("LongLogTag")
    private void saveData(DataMap data) {
        if (!isExternalStorageWritable()) {
            Log.d(TAG, "External Storage Not Writable");
            return;
        }
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        directory.mkdirs();
        File file = new File(directory, "wearable_data.txt");
        String dataJSON = dataMapAsJSONObject(data).toString() + "\n";
        try {
            FileOutputStream stream = new FileOutputStream(file, true);
            OutputStreamWriter writer = new OutputStreamWriter(stream);
            writer.write(dataJSON);
            writer.close();

        } catch (Exception e) {
            Log.d(TAG, "Error Saving");
            e.printStackTrace();
        }
    }*/


    //@SuppressLint("LongLogTag")
    @Override
    public void onPeerConnected(Node peer) {
        Log.d(TAG, "onPeerConnected: " + peer);
        Toast.makeText(getApplicationContext(),"onPeerConnected", Toast.LENGTH_SHORT).show();
    }

    //@SuppressLint("LongLogTag")
    @Override
    public void onPeerDisconnected(Node peer) {
        Log.d(TAG, "onPeerDisconnected: " + peer);
        Toast.makeText(getApplicationContext(),"onPeerDisconnected", Toast.LENGTH_SHORT).show();
    }
}
