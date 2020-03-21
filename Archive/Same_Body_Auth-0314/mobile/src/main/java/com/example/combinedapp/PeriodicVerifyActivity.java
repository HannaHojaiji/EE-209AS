package com.example.combinedapp;

/* --- External Libraries --- */
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
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
        implements ESenseSensorListener, ESenseConnectionListener, ESenseEventListener,
        SensorEventListener, StepListener {

    /* --- Fields --- */
    // --- XML Elements ---
    /** Start of eSense Items **/
    private Button submitDeviceName;
    private EditText deviceNameBox; //OnClick registered to beginTracking
    private TextView statusBox;
    private TextView acc_table;
    private TextView gyro_table;
    /** End of eSense Items **/

    /** Start of Pedometer Items **/
    private TextView phone_Acceleration_Table;
    private TextView phone_Gyroscope_Table;

    private TextView phone_StepValue;
    private SeekBar phone_StepSensitivitySeekBar;
    private TextView phone_StepSensitivityValue;
    private TextView eSense_StepValue;
    private SeekBar eSense_StepSensitivitySeekBar;
    private TextView eSense_StepSensitivityValue;

    private Button register, unregister, pedometer_ResetStep;
    /** End of Pedometer Items **/



    // --- Activity Objects ---
    //LOG ID
    private static final String TAG_eSENSE = "eSense Earable";
    private static final String TAG_PHONE = "Phone";

    //Location permissions
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 999;

    /** Start of eSense Items **/
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
    private String messageToWrite = "";
    private String messageToWrite_gyro = "";

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
    /** End of eSense Items **/


    /** Start of Pedometer Items **/
    private SensorManager phone_SensorManager;
    private Sensor phone_Accelerometer;
    private Sensor phone_Gyroscope;

    private StepDetector pedometer_StepDetector;
    private int phone_numSteps = 0;
    private int eSense_numSteps = 0;
    /** End of Pedometer Items **/


    /** Start of Data Analysis Items **/
    private static final int ACCELERATION_BUFFER_SIZE = 100;
    private float[] eSense_buffer_acc_x = new float[ACCELERATION_BUFFER_SIZE];
    private float[] eSense_buffer_acc_y = new float[ACCELERATION_BUFFER_SIZE];
    private float[] eSense_buffer_acc_z = new float[ACCELERATION_BUFFER_SIZE];

    private float[] phone_buffer_acc_x = new float[ACCELERATION_BUFFER_SIZE];
    private float[] phone_buffer_acc_y = new float[ACCELERATION_BUFFER_SIZE];
    private float[] phone_buffer_acc_z = new float[ACCELERATION_BUFFER_SIZE];

    private int eSense_buffer_acc_counter = 0;
    private int phone_buffer_acc_counter = 0;

    private static final int STEP_BUFFER_SIZE = 50;
    private float[] eSense_buffer_step = new float[STEP_BUFFER_SIZE];
    private float[] phone_buffer_step = new float[STEP_BUFFER_SIZE];
    /** End of Data Analysis Items **/


    /* --- Methods --- */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_periodic_verify);

        // --- Request Permissions ---
        // Request location permissions for Bluetooth
        requestLocationPermissions();
        // Request external write permissions for saving sensor data
        requestExternalWriteAccess();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        }


        /** Start of eSense Items **/
        // --- Get XML Elements ---
        submitDeviceName = (Button)findViewById(R.id.button);
        deviceNameBox   = (EditText)findViewById(R.id.deviceNameBox); //OnClick registered to beginTracking
        statusBox = (TextView)findViewById(R.id.statusBox);
        acc_table = (TextView) findViewById(R.id.eSense_acc_table);
        gyro_table = (TextView) findViewById(R.id.eSense_gyro_table);


        // --- Initialize Objects and Event Handlers ---
        //Initialize Bluetooth manager
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(this.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.d(TAG_eSENSE, "ERROR! Unable to initialize BluetoothManager.");
            }
        }
        //Get bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBtAdapter == null) {
            Log.d(TAG_eSENSE, "Device does not have Bluetooth capability!");
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
        /** End of eSense Items **/



        /** Start of Pedometer Items **/
        // --- Get XML Elements ---
        phone_Acceleration_Table = (TextView) findViewById(R.id.phone_acc_table);
        phone_Gyroscope_Table = (TextView) findViewById(R.id.phone_gyro_table);


        phone_StepValue = (TextView) findViewById(R.id.pedometer_phoneStepValue);
        eSense_StepValue = (TextView) findViewById(R.id.pedometer_eSenseStepValue);

        phone_StepSensitivitySeekBar = (SeekBar) findViewById(R.id.pedometer_phoneSensitivityBar);
        eSense_StepSensitivitySeekBar = (SeekBar) findViewById(R.id.pedometer_eSenseSensitivityBar);
        phone_StepSensitivityValue = (TextView) findViewById(R.id.pedometer_phoneSensitivityValue);
        eSense_StepSensitivityValue = (TextView) findViewById(R.id.pedometer_eSenseSensitivityValue);


        //register = (Button)findViewById(R.id.pedometer_register);
        //unregister = (Button)findViewById(R.id.pedometer_unregister);
        pedometer_ResetStep = (Button)findViewById(R.id.pedometer_reset);



        // --- Initialize Objects and Event Handlers ---
        phone_SensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        phone_Accelerometer = phone_SensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (phone_Accelerometer != null)
            phone_SensorManager.registerListener(PeriodicVerifyActivity.this, phone_Accelerometer,
                    10); // Sampling rate: 100 Hz

        phone_Gyroscope = phone_SensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        if (phone_Gyroscope != null)
            phone_SensorManager.registerListener(PeriodicVerifyActivity.this, phone_Gyroscope,
                    10); // Sampling rate: 100 Hz



        pedometer_StepDetector = new StepDetector();
        pedometer_StepDetector.PHONE_STEP_THRESHOLD = 12;
        pedometer_StepDetector.ESENSE_STEP_THRESHOLD = 12;
        pedometer_StepDetector.registerListener(this);



        phone_StepSensitivitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pedometer_StepDetector.PHONE_STEP_THRESHOLD = seekBar.getProgress();
                phone_StepSensitivityValue.setText("Phone: " + String.valueOf(StepDetector.PHONE_STEP_THRESHOLD));
            }
        });

        eSense_StepSensitivitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pedometer_StepDetector.ESENSE_STEP_THRESHOLD = seekBar.getProgress();
                eSense_StepSensitivityValue.setText("eSense: " + String.valueOf(StepDetector.ESENSE_STEP_THRESHOLD));
            }
        });

        /*
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
         */

        pedometer_ResetStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });

        /** End of Pedometer Items **/
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


    // --- requestExternalWriteAccess ---
    // Function to request External Write Permission from the phone
    public void requestExternalWriteAccess() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
    }



    // --- requestLocationPermissions ---
    // Function to request Location Permission from the phone
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
                Log.d(TAG_eSENSE, "Device Name: .." + deviceName + ".. Device Addr: " + deviceAddr);
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
            //String devicename = deviceNameBox.getText().toString();

            String devicename = "eSense-0831";

            String deviceAddr = "00:04:79:00:0E:D6";
            if(devicename.equals("")) {
                //devicename = "eSense-1171";  // Sometimes if one doesn't work, try the other earbud
                devicename = "eSense-0798";  // Device Addr = 00:04:79:00:0E:D6
                //devicename = "(.*)eSense(.*)";  // Use RegEx expression for being desperate

            }

            Log.d(TAG_eSENSE, "Begin Tracking Device " + devicename);

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
                Log.d(TAG_eSENSE, "Current Queue Size: " + Long.toString(writeQueue.size()));
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
                Log.d(TAG_eSENSE, "Current Queue Size: " + Long.toString(writeQueue_gyro.size()));
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
        Log.d(TAG_eSENSE, " Completing Writes to file!");
        synchronized (writeQueue) {
            Iterator i = writeQueue.iterator(); // Must be in synchronized block
            while (i.hasNext()) {
                String toWrite = (String) i.next();
                dex.eSense_writeAccToFile(toWrite);
            }
            writeQueue.clear();
        }
        Log.d(TAG_eSENSE, " Completed all Writes!");

        Log.d(TAG_eSENSE, " Completing Writes to file!");
        synchronized (writeQueue_gyro) {
            Iterator i = writeQueue_gyro.iterator(); // Must be in synchronized block
            while (i.hasNext()) {
                String toWrite_gyro = (String) i.next();
                dex.eSense_writeGyroToFile(toWrite_gyro);
            }
            writeQueue_gyro.clear();
        }
        Log.d(TAG_eSENSE, " Completed all Writes!");

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

        Log.d(TAG_eSENSE, "Found Device During Scan! ");
        displayStatus("Found Device!");
    }

    @Override
    public void onDeviceNotFound(ESenseManager eSenseManager) {

        Log.d(TAG_eSENSE, "Did not find device during scan!");
        displayStatus("Did not find Device!");

    }

    Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {

            // Get Sensor configuration
            boolean sensor_configs = manager.getSensorConfig();
            Log.d(TAG_eSENSE, "Received Sensor Configuration! " + sensor_configs);
            return sensor_configs;
        }
    });

    @Override
    public void onConnected(ESenseManager eSenseManager) {

        Log.d(TAG_eSENSE, "Connected to Device!");
        displayStatus("Connected to Device!");

        //Begin Sensor Transmissions
        manager.registerSensorListener(this, sampling_rate);
        Log.d(TAG_eSENSE, "Register Listener for Sensors!");

        //TODO: Something is wrong with the event listener - it doesn't seem to be able to
        // retrieve the sensor configurations from the bluetoothGATT

        //Register Event listener
        boolean correctly_registered = manager.registerEventListener(this);
        Log.d(TAG_eSENSE, "Registered Event Listener! " + correctly_registered);


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

        Log.d(TAG_eSENSE, "Disconnected from Device!");
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

            /*
            messageToWrite += "\n" + Long.toString(timestamp) + "," + Short.toString(acc[0]) + "," + Short.toString(acc[1]) +
                    "," + Short.toString(acc[2]) + "," + Short.toString(gyro[0]) + "," + Short.toString(gyro[1]) + "," +
                    Short.toString(gyro[2]);
            */

            /*
            messageToWrite += "\n" + Long.toString(timestamp) + "," + Double.toString(acc[0]) + "," + Double.toString(acc[1]) +
                    "," + Double.toString(acc[2]);

            messageToWrite_gyro += "\n" + Long.toString(timestamp) + "," + Double.toString(gyro[0]) + "," + Double.toString(gyro[1]) + "," +
                    Double.toString(gyro[2]);
            */

            // Check for update in step count from eSense
            pedometer_StepDetector.eSense_UpdateAcceleration(
                    timestamp, (float) acc[0], (float) acc[1], (float) acc[2]);


            String timeDate_acc = getDateString() + "," + getTimeStringWithColons();
            String messageToWrite_1 = Double.toString(acc[0]) + "," + Double.toString(acc[1]) +
                    "," + Double.toString(acc[2]);
            messageToWrite +=  timeDate_acc + "," + messageToWrite_1 + "," + "\n";


            String timeDate_gyro = getDateString() + "," + getTimeStringWithColons();
            String messageToWrite_gyro_1 = Double.toString(gyro[0]) + "," + Double.toString(gyro[1]) + "," +
                    Double.toString(gyro[2]);
            messageToWrite_gyro += timeDate_gyro + "," + messageToWrite_gyro_1 + "," + "\n";


            /*
            String eSense_AccelerationTableText = "eSense accelerations:\n" + "[" +
                    "x = " + Double.toString(acc[0]) + ", " +
                    "y = " + Double.toString(acc[1)+ ", " +
                    "z = " + Double.toString(acc[2])+ "]";

            String eSense_GyroscopeTableText = "eSense angular velocities:\n" + "[" +
                    "x = " + Double.toString(gyro[0]) + ", " +
                    "y = " + Double.toString(gyro[1]) + ", " +
                    "z = " + Double.toString(gyro[2]) + "]";
            */

            long current_time = System.currentTimeMillis();

            //Check mean/standard dev of counted transmissions:
            if (last_sampled_time_2 + (total_recording_time*1000) < current_time) {

                last_sampled_time_2 = current_time;

                mean = mean / total_recording_time;

                Log.d(TAG_eSENSE, "Number of ACC samples/sec: " + Long.toString(num_acc_samples));
                Log.d(TAG_eSENSE, "Mean: " + Double.toString(mean) + " Std Dev: " + Double.toString(std_dev));

                mean = 0.0;
                std_dev = 0.0;
            }

            // Check incomingg sensor rate:
            if (last_sampled_time + 1000 < current_time) {
                last_sampled_time = current_time;

                Log.d(TAG_eSENSE, "Number of ACC samples/sec: " + Long.toString(num_acc_samples));
                Log.d(TAG_eSENSE, "Number of GYRO samples/sec: " + Long.toString(num_gyro_samples));

                // Format for output is similar to "ACC values: [] Sampling Rate: 0"
                displayACCData("ACC values: " + java.util.Arrays.toString(acc) + " Sampling Rate: " + Long.toString(num_acc_samples));
                displayGYROData("GYRO values: " + java.util.Arrays.toString(gyro) + " Sampling Rate: " + Long.toString(num_gyro_samples));

                //Add to the mean
                mean += num_acc_samples;

                num_acc_samples = 0;
                num_gyro_samples = 0;

                //Write the message to file
                Log.d(TAG_eSENSE, "Writing Message of length to Queue" + Long.toString(messageToWrite.length()));
                writeQueue.add(messageToWrite);
                //dex.directWriteToFile(messageToWrite);
                //Reset the message to nothing.
                messageToWrite = "";

                //Write the message to file for GYRO
                Log.d(TAG_eSENSE, "Writing Message of length to Queue" + Long.toString(messageToWrite_gyro.length()));
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
        Log.d(TAG_eSENSE, "READ NAME: " + s);
    }

    @Override
    public void onSensorConfigRead(ESenseConfig eSenseConfig) {
        esg = eSenseConfig;
        received_configs = true;
        Log.d(TAG_eSENSE, "Found Sensor Configurations!");
    }

    @Override
    public void onAccelerometerOffsetRead(int i, int i1, int i2) {

    }



    /** Start of Pedometer Items **/
    // --- onSensorChanged ---
    // Function to handle sensor value changes from the phone
    // From SensorListener
    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            Log.d(TAG_PHONE, "onSensorChanged: accX: " + event.values[0] + " accY: " +
                    event.values[1] + " accZ: " + event.values[2]);

            // Get smartphone's current acceleration values
            float[] phone_CurrentAcc = new float[3];
            phone_CurrentAcc[0] = event.values[0];
            phone_CurrentAcc[1] = event.values[1];
            phone_CurrentAcc[2] = event.values[2];

            // Check for update in step count from phone
            pedometer_StepDetector.phone_UpdateAcceleration(
                    event.timestamp, phone_CurrentAcc[0], phone_CurrentAcc[1], phone_CurrentAcc[2]);

            // Update smartphone's current acceleration values to XML element
            //String phone_CurrentAcc_X = String.format(Locale.US,"%.4f", phone_CurrentAcc[0]);
            //String phone_CurrentAcc_Y = String.format(Locale.US,"%.4f", phone_CurrentAcc[1]);
            //String phone_CurrentAcc_Z = String.format(Locale.US,"%.4f", phone_CurrentAcc[2]);

            String phone_CurrentAcc_X = Float.toString(phone_CurrentAcc[0]);
            String phone_CurrentAcc_Y = Float.toString(phone_CurrentAcc[1]);
            String phone_CurrentAcc_Z = Float.toString(phone_CurrentAcc[2]);

            String phone_AccelerationTableText = "Phone accelerations:" + "\n" + "[" +
                    "x = " + phone_CurrentAcc_X + ", " +
                    "y = " + phone_CurrentAcc_Y + ", " +
                    "z = " + phone_CurrentAcc_Z + "]";

            phone_Acceleration_Table.setText(phone_AccelerationTableText);

            // Save smartphone's current acceleration values to text file
            String accData = Float.toString(phone_CurrentAcc[0]) + "," +
                    Float.toString(event.values[1]) + "," + Float.toString(phone_CurrentAcc[2]);
            dex.Phone_writeAccToFile(accData);
        }


        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {
            Log.d(TAG_PHONE, "onSensorChanged: gyroX: " + event.values[0] + "gyroY: " +
                    event.values[1] + "gyroZ: " + event.values[2]);

            // Get smartphone's current gyroscope (angular velocity) values
            float[] phone_currentGyro = new float[3];
            phone_currentGyro[0] = event.values[0];
            phone_currentGyro[1] = event.values[1];
            phone_currentGyro[2] = event.values[2];


            // Update smartphone's current gyroscope (angular velocity) values to XML element
            //String phone_CurrentGyro_X = String.format("%.4f", phone_currentGyro[0]);
            //String phone_CurrentGyro_Y = String.format("%.4f", phone_currentGyro[1]);
            //String phone_CurrentGyro_Z = String.format("%.4f", phone_currentGyro[2]);

            String phone_CurrentGyro_X = Float.toString(phone_currentGyro[0]);
            String phone_CurrentGyro_Y = Float.toString(phone_currentGyro[1]);
            String phone_CurrentGyro_Z = Float.toString(phone_currentGyro[2]);

            String phone_GyroscopeTableText = "Phone angular velocities:\n" + "[" +
                    "x = " + phone_CurrentGyro_X + ", " +
                    "y = " + phone_CurrentGyro_Y + ", " +
                    "z = " + phone_CurrentGyro_Z + "]";

            phone_Gyroscope_Table.setText(phone_GyroscopeTableText);


            // Save smartphone's current gyroscope (angular velocity) to text file
            String gyroData = Float.toString(phone_currentGyro[0]) + "," +
                    Float.toString(phone_currentGyro[1]) + "," +Float.toString(phone_currentGyro[2]);
            dex.Phone_writeGyroToFile(gyroData);
        }
    }

    // --- onAccuracyChanged ---
    // Function to handle senor accuracy changes from the phone
    // From SensorListener
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /*
    private void register(){
        if(phone_SensorManager == null) {
            phone_SensorManager = (SensorManager)
                    getSystemService(Context.SENSOR_SERVICE);
        }
        phone_SensorManager.registerListener(PeriodicVerifyActivity.this, phone_Accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    private void unregister(){
        if (phone_SensorManager != null) {
            phone_SensorManager.unregisterListener(PeriodicVerifyActivity.this);
        }
    }
    */

    private void reset(){
        /*
        if (phone_SensorManager != null) {
            phone_SensorManager.unregisterListener(this);
        }
        */
        phone_numSteps = 0;
        eSense_numSteps = 0;
        // phone_SensorManager =null;
        phone_StepValue.setText("Phone: 0");
        eSense_StepValue.setText("eSense: 0");
    }

    // --- eSense_UpdateStep ---
    // Function to update counted steps from eSense
    // From StepListener
    @Override
    public void eSense_UpdateStep(long timeNs) {
        eSense_numSteps++;
        eSense_StepValue.setText("eSense: " + String.valueOf(eSense_numSteps));
    }

    // --- phone_UpdateStep ---
    // Function to update counted steps from phone
    // From StepListener
    @Override
    public void phone_UpdateStep(long timeNs) {
        phone_numSteps++;
        phone_StepValue.setText("Phone: " + String.valueOf(phone_numSteps));
    }
    /** End of Pedometer Items **/


    /** Start of Utility Items **/
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

    //returns a string containing a timestamp in format (HH:mm:ss)
    public static String getTimeStringWithColons() {
        Date time = Calendar.getInstance().getTime();
        SimpleDateFormat outputFmt = new SimpleDateFormat("HH:mm:ss");
        return outputFmt.format(time);
    }

    // --- openInitAuthActivity ---
    // Function to transit back to Initial Authentication Activity
    public void openInitAuthActivity(){
        Intent intent = new Intent(this, InitAuthActivity.class);
        startActivity(intent);
    }
    /** End of Utility Items **/

}


