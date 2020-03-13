package com.example.combinedapp;

/* --- External Libraries --- */
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/* --- Internal Libraries --- */
import com.example.combinedapp.esenselib.ESenseConfig;
import com.example.combinedapp.esenselib.ESenseConnectionListener;
import com.example.combinedapp.esenselib.ESenseEvent;
import com.example.combinedapp.esenselib.ESenseEventListener;
import com.example.combinedapp.esenselib.ESenseManager;
import com.example.combinedapp.esenselib.ESenseSensorListener;

import com.example.combinedapp.pedometer.StepDetector;
import com.example.combinedapp.pedometer.StepListener;


/* --- Class --- */
public class PeriodicVerifyActivity extends AppCompatActivity
        implements ESenseSensorListener,ESenseConnectionListener, ESenseEventListener,
        SensorEventListener, StepListener {

    /* --- Fields --- */
    // --- XML Elements ---
    Button submitDeviceName;
    EditText deviceNameBox; //OnClick registered to beginTracking
    TextView statusBox;
    TextView acc_table;
    TextView gyro_table;
    TextView xGyroValue, yGyroValue, zGyroValue, xAccValue, yAccValue, zAccValue;

    private TextView stepsview,seekbartext;
    private Button register,unregister,reset;


    // --- Activity Objects ---
    //LOG ID
    final private String TAG = "DBG-MainActivity";

    //Location permissions
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 999;

    //Bluetooth manager
    private BluetoothManager mBluetoothManager = null;
    private BluetoothAdapter mBtAdapter = null;

    //ESenseManager
    ESenseManager manager = null;

    //Sampling Rate?
    private int sampling_rate = 100; //1 is the min, 100 is the max.

    //ESenseConfig
    ESenseConfig esg = null;
    boolean received_configs = false;

    // Sampling Rate Measurements
    long last_sampled_time = 0;
    long last_sampled_time_2 = 0;
    int num_acc_samples = 0;
    int num_gyro_samples = 0;

    //Timeout for Bluetooth connections in milliseconds
    int delay_milliseconds = 2000;

    //Message to write to file
    String messageToWrite = "";
    String messageToWrite_gyro = "";

    //Data Exporter
    DataExportActivity dex;

    //Starting/Ending connection
    boolean isConnected = false;

    //Threads for Writing
    List<String> writeQueue;  //This is created from a synchronized list
    List<String> writeQueue_gyro;
    Thread writeThread;
    long last_written_time = 0;

    //For calculating the mean and standard deviation of received packets:
    double mean = 0.0;
    double std_dev = 0.0;
    long total_recording_time = 5; //seconds for outputting the mean and std dev


    // Pedometer Items
    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;
    private int numSteps=0;

    private SeekBar seekBar;

    //Gyroscope Values
    private static final String TAG1 = "SenseIMUActivity";

    private Sensor mGyro;




    /* ---------- New Stuffs 03-12-2020 ---------- */
    private static final int ACCELERATION_BUFFER_SIZE = 50;
    private float[] buffer_accelerationX = new float[ACCELERATION_BUFFER_SIZE];
    private float[] buffer_accelerationY = new float[ACCELERATION_BUFFER_SIZE];
    private float[] buffer_accelerationZ = new float[ACCELERATION_BUFFER_SIZE];






    /* --- Methods --- */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_periodic_verify);

        // --- Request Permissions ---
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        }



        // --- Get XML Elements ---
        submitDeviceName = (Button)findViewById(R.id.button);
        deviceNameBox   = (EditText)findViewById(R.id.deviceNameBox); //OnClick registered to beginTracking
        statusBox = (TextView)findViewById(R.id.statusBox);
        acc_table = (TextView) findViewById(R.id.acc_table);
        gyro_table = (TextView) findViewById(R.id.gyro_table);

        //Save Text File
        //String filename = et_name;
        //String content = content_one;

        // Gyroscope variables initialized
        xGyroValue = (TextView) findViewById(R.id.xValue);
        yGyroValue = (TextView) findViewById(R.id.yValue);
        zGyroValue = (TextView) findViewById(R.id.zValue);

        // Accelerometer variables initialized
        xAccValue = (TextView) findViewById(R.id.xAccValue);
        yAccValue = (TextView) findViewById(R.id.yAccValue);
        zAccValue = (TextView) findViewById(R.id.zAccValue);

        //For gyroscope
        Log.d(TAG1, "onCreate: Initializing Sensor Services");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        if (mGyro != null) {
            sensorManager.registerListener((SensorEventListener) PeriodicVerifyActivity.this, mGyro, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "onCreate: Registered gyroscope listener");
        } else {
            xGyroValue.setText("Gyroscope not supported");
            yGyroValue.setText("Gyroscope not supported");
            zGyroValue.setText("Gyroscope not supported");
        }


        // *** Pedometer Items
        stepsview = (TextView)findViewById(R.id.stepstextview);
        register = (Button)findViewById(R.id.pedometer_register);
        unregister = (Button)findViewById(R.id.pedometer_unregister);
        reset = (Button)findViewById(R.id.pedometer_reset);

        sensorManager = (SensorManager)
                getSystemService(Context.SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.STEP_THRESHOLD = 12;
        simpleStepDetector.registerListener(this);

        seekBar=(SeekBar)findViewById(R.id.seekBar);
        seekbartext=(TextView)findViewById(R.id.seekbar_text);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                simpleStepDetector.STEP_THRESHOLD=seekBar.getProgress();
                seekbartext.setText(String.valueOf(simpleStepDetector.STEP_THRESHOLD));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
        unregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unregister();
            }
        });
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });
        // *** End of Pedometer Items



        //Request location permissions for BLE use
        requestLocationPermissions();
        //Request write permissions
        requestExternalWriteAccess();

        //Initialize Bluetooth manager
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(this.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.d(TAG, "ERROR! Unable to initialize BluetoothManager.");
            }
        }
        //Get bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBtAdapter == null) {
            Log.d(TAG, "Device does not have Bluetooth capability!");
        }

        //Create data exporter
        dex = new DataExportActivity();

        //Create Queue for messages to be written to file
        writeQueue = Collections.synchronizedList(new ArrayList<String>());
        writeQueue_gyro = Collections.synchronizedList(new ArrayList<String>());


        //Run a background thread for writing to CSV files
        writeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    writeToFile();
                }
            }
        });
        writeThread.start();

    }

    //Function to save Text File
    private void saveTextAsFile(String filename, String content)
    {
        String fileName = filename + ".txt";

        //create file
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),fileName); //Ext.StorageState(filepath) works same as externalstoragedirectory.absolutepath(
        //File file = new File(Environment.get);
        //write to file
        try {

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.close();
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            Toast.makeText(this, "Error saving file", Toast.LENGTH_SHORT).show();
        }
    }


    public void requestExternalWriteAccess() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    100);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestLocationPermissions() {

        //Check to see if this app can get location access
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect beacons.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }
    }
















    //Scan Bluetooth Devices.
    private void scanDevices() {
        mBtAdapter.getBluetoothLeScanner().startScan(mLeScanCallback);
    }

    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {


            final BluetoothDevice device = result.getDevice();
            final int rssi = result.getRssi();
            String deviceName = device.getName();
            String deviceAddr = device.getAddress();

            if (deviceName != null && deviceName.contains("eSense")) {
                Log.d(TAG, "Device Name: .." + deviceName + ".. Device Addr: " + deviceAddr);
            }



        }
    };




    public void eSense_BeginIMUTracking(View view) {

        if (isConnected) {
            manager.disconnect();
            submitDeviceName.setText("CONNECT FROM DEVICE");
            isConnected = false;

        }

        else {
            String devicename = deviceNameBox.getText().toString();
            String deviceAddr = "00:04:79:00:0E:D6";
            if(devicename.equals("")) {
                //devicename = "eSense-1171";  // Sometimes if one doesn't work, try the other earbud
                devicename = "eSense-0798";  // Device Addr = 00:04:79:00:0E:D6
                //devicename = "(.*)eSense(.*)";  // Use RegEx expression for being desperate

            }

            Log.d(TAG, "Begin Tracking Device " + devicename);

            manager = new ESenseManager(devicename,
                    PeriodicVerifyActivity.this.getApplicationContext(), this);
            statusBox.setText("Scanning for Devices...");

            // Only scan bluetooth devices for debugging
            //scanDevices();

            manager.connect(delay_milliseconds);
            submitDeviceName.setText("DISCONNECT FROM DEVICE");
            isConnected = true;
        }



    }

    //Functions for writing to file
    private void writeToFile() {
        if(!writeQueue.isEmpty()) {

            if(System.currentTimeMillis() > last_written_time + 1000) {

                last_written_time = System.currentTimeMillis();
                Log.d(TAG, "Current Queue Size: " + Long.toString(writeQueue.size()));
            }

            //Get the first object in the writeQueue
            String toWrite = writeQueue.get(0);
            //Remove the first object from the queue
            writeQueue.remove(0);
            //Buffer the data for writing to a CSV
            dex.eSense_writeAccToFile(toWrite);

        }

        if(!writeQueue_gyro.isEmpty()) {

            if(System.currentTimeMillis() > last_written_time + 1000) {

                last_written_time = System.currentTimeMillis();
                Log.d(TAG, "Current Queue Size: " + Long.toString(writeQueue_gyro.size()));
            }

            //Get the first object in the writeQueue
            String toWrite_gyro = writeQueue_gyro.get(0);
            //Remove the first object from the queue
            writeQueue_gyro.remove(0);
            //Buffer the data for writing to a CSV
            dex.eSense_writeGyroToFile(toWrite_gyro);

        }
    }

    //Write the rest of the writeQueue to file - this is called before this service is destroyed
    private void completeWriting() {
        Log.d(TAG, " Completing Writes to file!");
        synchronized (writeQueue) {
            Iterator i = writeQueue.iterator(); // Must be in synchronized block
            while (i.hasNext()) {
                String toWrite = (String) i.next();
                dex.eSense_writeAccToFile(toWrite);
            }
            writeQueue.clear();
        }
        Log.d(TAG, " Completed all Writes!");

        Log.d(TAG, " Completing Writes to file!");
        synchronized (writeQueue_gyro) {
            Iterator i = writeQueue_gyro.iterator(); // Must be in synchronized block
            while (i.hasNext()) {
                String toWrite_gyro = (String) i.next();
                dex.eSense_writeGyroToFile(toWrite_gyro);
            }
            writeQueue_gyro.clear();
        }
        Log.d(TAG, " Completed all Writes!");

    }







    @Override  //Android now needs Coarse access location to do BT scans - Idk why but it wont work otherwise
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permissions", "fine location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }

    public void displayStatus(final String stat) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                statusBox.setText(stat);
            }
        });
    }
    public void displayACCData(final String data) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                acc_table.setText(data);
            }
        });

    }
    public void displayGYROData(final String data_one) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                gyro_table.setText(data_one);
            }
        });
    }


    // All methods relevant to sensor Connection listener

    @Override
    public void onDeviceFound(ESenseManager eSenseManager) {

        Log.d(TAG, "Found Device During Scan! ");
        displayStatus("Found Device!");
    }

    @Override
    public void onDeviceNotFound(ESenseManager eSenseManager) {

        Log.d(TAG, "Did not find device during scan!");
        displayStatus("Did not find Device!");

    }

    Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {

            // Get Sensor configuration
            boolean sensor_configs = manager.getSensorConfig();
            Log.d(TAG, "Received Sensor Configuration! " + sensor_configs);
            return sensor_configs;
        }
    });

    @Override
    public void onConnected(ESenseManager eSenseManager) {

        Log.d(TAG, "Connected to Device!");
        displayStatus("Connected to Device!");

        //Begin Sensor Transmissions
        manager.registerSensorListener(this, sampling_rate);
        Log.d(TAG, "Register Listener for Sensors!");

        //TODO: Something is wrong with the event listener - it doesn't seem to be able to
        // retrieve the sensor configurations from the bluetoothGATT

        //Register Event listener
        boolean correctly_registered = manager.registerEventListener(this);
        Log.d(TAG, "Registered Event Listener! " + correctly_registered);


        new Thread()
        {
            public void run()
            {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Message msg = new Message();
                handler.sendMessage(msg);
                //Log.d(TAG, "BLARGH!");
            }
        }.start();


    }

    @Override
    public void onDisconnected(ESenseManager eSenseManager) {

        Log.d(TAG, "Disconnected from Device!");
        displayStatus("Disconnected from Device!");

        completeWriting();

    }

    // Methods relevant to SensorChanged event

    @Override
    public void onSensorChanged(ESenseEvent evt) {

        // If we have received the sensor configurations, we save the data.
        if(received_configs) {

            //Log.d(TAG, "PACKET: " + java.util.Arrays.toString(evt.getAccel()));

            //short[] acc = evt.getAccel(); //Acceleration Values
            //short[] gyro = evt.getGyro(); //Rotation values
            double[] acc = evt.convertAccToG(esg);  //Acceleration in g
            double[] gyro = evt.convertGyroToDegPerSecond(esg); // Rotation in degrees/sec





            long timestamp = evt.getTimestamp();  //Get timestamp in system milliseconds.

            num_acc_samples += 1;
            num_gyro_samples += 1;


//            messageToWrite += "\n" + Long.toString(timestamp) + "," + Short.toString(acc[0]) + "," + Short.toString(acc[1]) +
//                    "," + Short.toString(acc[2]) + "," + Short.toString(gyro[0]) + "," + Short.toString(gyro[1]) + "," +
//                    Short.toString(gyro[2]);

            messageToWrite += "\n" + Long.toString(timestamp) + "," + Double.toString(acc[0]) + "," + Double.toString(acc[1]) +
                    "," + Double.toString(acc[2]);

            messageToWrite_gyro += "\n" + Long.toString(timestamp) + "," + Double.toString(gyro[0]) + "," + Double.toString(gyro[1]) + "," +
                    Double.toString(gyro[2]);

            long current_time = System.currentTimeMillis();

            //Check mean/standard dev of counted transmissions:
            if (last_sampled_time_2 + (total_recording_time*1000) < current_time) {

                last_sampled_time_2 = current_time;

                mean = mean / total_recording_time;

                Log.d(TAG, "Number of ACC samples/sec: " + Long.toString(num_acc_samples));
                Log.d(TAG, "Mean: " + Double.toString(mean) + " Std Dev: " + Double.toString(std_dev));

                mean = 0.0;
                std_dev = 0.0;
            }

            // Check incomingg sensor rate:
            if (last_sampled_time + 1000 < current_time) {
                last_sampled_time = current_time;

                Log.d(TAG, "Number of ACC samples/sec: " + Long.toString(num_acc_samples));
                Log.d(TAG, "Number of GYRO samples/sec: " + Long.toString(num_gyro_samples));

                // Format for output is similar to "ACC values: [] Sampling Rate: 0"
                displayACCData("ACC values: " + java.util.Arrays.toString(acc) + " Sampling Rate: " + Long.toString(num_acc_samples));
                displayGYROData("GYRO values: " + java.util.Arrays.toString(gyro) + " Sampling Rate: " + Long.toString(num_gyro_samples));

                //Add to the mean
                mean += num_acc_samples;

                num_acc_samples = 0;
                num_gyro_samples = 0;

                //Write the message to file
                Log.d(TAG, "Writing Message of length to Queue" + Long.toString(messageToWrite.length()));
                writeQueue.add(messageToWrite);
                //dex.directWriteToFile(messageToWrite);
                //Reset the message to nothing.
                messageToWrite = "";

                //Write the message to file for GYRO
                Log.d(TAG, "Writing Message of length to Queue" + Long.toString(messageToWrite_gyro.length()));
                writeQueue_gyro.add(messageToWrite_gyro);
                //dex.directWriteToFile(messageToWrite);
                //Reset the message to nothing.
                messageToWrite_gyro = "";
            }
        }

    }

    @Override
    public void onBatteryRead(double v) {

    }

    @Override
    public void onButtonEventChanged(boolean b) {

    }

    @Override
    public void onAdvertisementAndConnectionIntervalRead(int i, int i1, int i2, int i3) {

    }

    @Override
    public void onDeviceNameRead(String s) {
        Log.d(TAG, "READ NAME: " + s);
    }

    @Override
    public void onSensorConfigRead(ESenseConfig eSenseConfig) {
        esg = eSenseConfig;
        received_configs = true;
        Log.d(TAG, "Found Sensor Configurations!");
    }

    @Override
    public void onAccelerometerOffsetRead(int i, int i1, int i2) {

    }






    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            Log.d(TAG, "onSensorChanged: accX: " + event.values[0] + "accY: " + event.values[1] + "accZ: " + event.values[2]);

            float[] currentAcc = new float[3];
            currentAcc[0] = event.values[0];
            currentAcc[1] = event.values[1];
            currentAcc[2] = event.values[2];

            simpleStepDetector.updateAccel(
                    event.timestamp, currentAcc[0], currentAcc[1], currentAcc[2]);

            xAccValue.setText(Float.toString(currentAcc[0]));
            yAccValue.setText(Float.toString(currentAcc[1]));
            zAccValue.setText(Float.toString(currentAcc[2]));


            String accData = " " + Float.toString(currentAcc[0]) + ", " +
                    Float.toString(event.values[1]) + ", " + Float.toString(currentAcc[2]);

            dex.Phone_writeAccToFile(accData);

        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {
            Log.d(TAG, "onSensorChanged: gyroX: " + event.values[0] + "gyroY: " + event.values[1] + "gyroZ: " + event.values[2]);


            float[] currentGyro = new float[3];
            currentGyro[0] = event.values[0];
            currentGyro[1] = event.values[1];
            currentGyro[2] = event.values[2];

            xGyroValue.setText("xValue: " + currentGyro[0]);
            yGyroValue.setText("yValue: " + currentGyro[1]);
            zGyroValue.setText("zValue: " + currentGyro[2]);


            String gyroData = " " + Float.toString(currentGyro[0]) + ", " +
                    Float.toString(currentGyro[1]) + ", " +Float.toString(currentGyro[2]);

            dex.Phone_writeAccToFile(gyroData);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void step(long timeNs) {
        numSteps++;
        stepsview.setText(String.valueOf(numSteps));
    }


    private void register(){
        if(sensorManager == null) {
            sensorManager = (SensorManager)
                    getSystemService(Context.SENSOR_SERVICE);
        }
        sensorManager.registerListener(PeriodicVerifyActivity.this, accel, SensorManager.SENSOR_DELAY_GAME);
    }

    private void unregister(){
        if (sensorManager != null) {
            sensorManager.unregisterListener(PeriodicVerifyActivity.this);
        }
    }

    private void reset(){
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        numSteps=0;
        sensorManager =null;
        stepsview.setText("0");
    }


    public void openInitAuthActivity(){
        Intent intent = new Intent(this, InitAuthActivity.class);
        startActivity(intent);
    }
}


