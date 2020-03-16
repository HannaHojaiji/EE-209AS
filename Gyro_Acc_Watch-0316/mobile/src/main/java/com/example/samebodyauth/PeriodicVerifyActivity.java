package com.example.samebodyauth;

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
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/* --- Internal Libraries --- */
import com.example.samebodyauth.esenselib.ESenseConfig;
import com.example.samebodyauth.esenselib.ESenseConnectionListener;
import com.example.samebodyauth.esenselib.ESenseEvent;
import com.example.samebodyauth.esenselib.ESenseEventListener;
import com.example.samebodyauth.esenselib.ESenseManager;
import com.example.samebodyauth.esenselib.ESenseSensorListener;

import com.example.samebodyauth.pedometer.StepDetector;
import com.example.samebodyauth.pedometer.StepListener;


/* --- Class --- */
public class PeriodicVerifyActivity extends AppCompatActivity
        implements ESenseSensorListener, ESenseConnectionListener, ESenseEventListener,
        SensorEventListener, StepListener {

    /* --- Fields --- */
    // --- XML Elements ---
    /** Bluetooth Items **/
    private Button connectWearables;
    private TextView connectStatus;


    /** eSense Items **/
    private TextView eSense_Acceleration_Table;
    private TextView eSense_Gyroscope_Table;
    private TextView eSense_StepValue;
    private SeekBar eSense_StepSensitivitySeekBar;
    private TextView eSense_StepSensitivityValue;


    /** Phone Items **/
    private Button register, unregister, pedometer_ResetStep;
    private TextView phone_Acceleration_Table;
    private TextView phone_Gyroscope_Table;
    private TextView phone_StepValue;
    private SeekBar phone_StepSensitivitySeekBar;
    private TextView phone_StepSensitivityValue;



    // --- Activity Objects ---
    // LOG ID
    private static final String TAG_eSENSE = "eSense-Earable";
    private static final String TAG_PHONE = "Phone";
    private static final String TAG_BLE = "Bluetooth";

    // Data Exporter
    DataExportActivity dataExporter;


    /** Bluetooth Items **/
    // Bluetooth manager
    private BluetoothManager mBluetoothManager = null;
    private BluetoothAdapter mBtAdapter = null;

    // Timeout for Bluetooth connections in milliseconds
    int delay_milliseconds = 2000;


    /** eSense Items **/
    // ESenseManager
    ESenseManager eSense_manager = null;

    // ESenseConfig
    ESenseConfig eSense_config = null;
    boolean received_eSense_configs = false;

    // Starting/Ending connection
    boolean eSense_isConnected = false;


    /** Phone Items **/
    // Sensor Manager
    private SensorManager phone_SensorManager = null;

    // Sensors
    private Sensor phone_Accelerometer = null;
    private Sensor phone_Gyroscope = null;


    /** Pedometer Items **/
    private StepDetector pedometer_StepDetector = null;
    private int phone_numSteps = 0;
    private int eSense_numSteps = 0;


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
    // --- Activity Methods ---
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_periodic_verify);


        // Request location permission for Bluetooth
        requestLocationPermissions();

        // Request external write permission for saving sensor data
        requestExternalWriteAccess();

        // Request external write permission for saving sensor data
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        }


        // --- Get XML Elements ---
        /** Bluetooth Items **/
        connectWearables = findViewById(R.id.startSensing);
        connectStatus = findViewById(R.id.deviceStatus);

        /** eSense Items **/
        eSense_Acceleration_Table = findViewById(R.id.eSense_acc_table);
        eSense_Gyroscope_Table = findViewById(R.id.eSense_gyro_table);
        eSense_StepValue = findViewById(R.id.pedometer_eSenseStepValue);

        eSense_StepSensitivitySeekBar = findViewById(R.id.pedometer_eSenseSensitivityBar);
        eSense_StepSensitivityValue = findViewById(R.id.pedometer_eSenseSensitivityValue);

        /** Phone Items **/
        phone_Acceleration_Table = findViewById(R.id.phone_acc_table);
        phone_Gyroscope_Table = findViewById(R.id.phone_gyro_table);
        phone_StepValue = findViewById(R.id.pedometer_phoneStepValue);

        phone_StepSensitivitySeekBar = findViewById(R.id.pedometer_phoneSensitivityBar);
        phone_StepSensitivityValue = findViewById(R.id.pedometer_phoneSensitivityValue);

        /** Pedometer Items **/
        //register = findViewById(R.id.pedometer_register);
        //unregister = findViewById(R.id.pedometer_unregister);
        pedometer_ResetStep = findViewById(R.id.pedometer_reset);



        // --- Initialize Objects and Event Handlers ---
        // Initialize Bluetooth manager
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(this.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.d(TAG_eSENSE, "ERROR! Unable to initialize BluetoothManager.");
            }
        }

        // Get Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBtAdapter == null) {
            Log.d(TAG_eSENSE, "Device does not have Bluetooth capability!");
        }

        // Create data exporter
        dataExporter = new DataExportActivity();


        // Get phone sensor manager
        phone_SensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // Get phone accelerometer
        phone_Accelerometer = phone_SensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        // Get phone gyroscope
        phone_Gyroscope = phone_SensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);


        // Initialize step detector
        pedometer_StepDetector = new StepDetector();
        pedometer_StepDetector.PHONE_STEP_THRESHOLD = 1.2f;
        pedometer_StepDetector.ESENSE_STEP_THRESHOLD = 1.2f;


        // Register phone accelerometer sensing
        if (phone_Accelerometer != null)
            phone_SensorManager.registerListener(PeriodicVerifyActivity.this, phone_Accelerometer,
                    10); // Sampling rate: 100 Hz

        // Register phone gyroscope sensing
        if (phone_Gyroscope != null)
            phone_SensorManager.registerListener(PeriodicVerifyActivity.this, phone_Gyroscope,
                    10); // Sampling rate: 100 Hz

        // Register pedometer step detector sensing
        if (pedometer_StepDetector != null)
            pedometer_StepDetector.registerListener(this);


        // SeekBar for step count sensitivities
        phone_StepSensitivitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pedometer_StepDetector.PHONE_STEP_THRESHOLD = (float) seekBar.getProgress() / (float) 10;
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
                pedometer_StepDetector.ESENSE_STEP_THRESHOLD = (float) seekBar.getProgress() / (float) 10;
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

    }


    public void beginSensing(View view) {

        if (eSense_isConnected) {
            eSense_manager.disconnect();
            connectWearables.setText("CONNECT FROM DEVICE");
            eSense_isConnected = false;

        } else {

            String devicename = "eSense-0831";
            String deviceAddr = "00:04:79:00:0E:D6";

            /*
            if(devicename.equals("")) {
                //devicename = "eSense-1171";  // Sometimes if one doesn't work, try the other earbud
                devicename = "eSense-0798";  // Device Addr = 00:04:79:00:0E:D6
                //devicename = "(.*)eSense(.*)";  // Use RegEx expression for being desperate
            }
            */

            Log.d(TAG_eSENSE, "Begin Tracking Device " + devicename);

            eSense_manager = new ESenseManager(devicename,
                    PeriodicVerifyActivity.this.getApplicationContext(), this);
            connectStatus.setText("Scanning for Devices...");

            // Only scan bluetooth devices for debugging
            //scanDevices();

            eSense_manager.connect(delay_milliseconds);
            connectWearables.setText("DISCONNECT FROM DEVICE");
            eSense_isConnected = true;
        }

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




    // --- openInitAuthActivity ---
    // Function to transit back to Initial Authentication Activity
    public void openInitAuthActivity(){
        Intent intent = new Intent(this, InitAuthActivity.class);
        startActivity(intent);
    }



    // --- Permission Request Methods ---
    // Function to request External Write Permission from the phone
    public void requestExternalWriteAccess() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
    }

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
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 999);
                }
            });
            builder.show();
        }
    }

    // Android now needs Coarse access location to do BT scans
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case 999: { //PERMISSION_REQUEST_COARSE_LOCATION
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



    // --- Bluetooth Methods ---
    // Function to scan Bluetooth Devices
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


    boolean waitingForBonding;

    private void pairDevice(BluetoothDevice device) {
        try {
            Log.d(TAG_BLE, "Start Pairing...");

            waitingForBonding = true;

            Method m = device.getClass()
                    .getMethod("createBond", (Class[]) null);
            m.invoke(device, (Object[]) null);

            Log.d(TAG_BLE, "Pairing finished.");
        } catch (Exception e) {
            Log.e(TAG_BLE, e.getMessage());
        }
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            Log.e(TAG_BLE, e.getMessage());
        }
    }

    private void sensorUnregister(){
        if (phone_SensorManager != null) {
            phone_SensorManager.unregisterListener(this);
        }

        if (eSense_manager != null) {
            eSense_manager.unregisterEventListener();
        }
    }





    // --- eSense Connection Listener Methods ---
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

    @Override
    public void onConnected(ESenseManager eSenseManager) {

        Log.d(TAG_eSENSE, "Connected to Device!");
        displayStatus("Connected to Device!");

        //Begin Sensor Transmissions
        eSense_manager.registerSensorListener(this, 100);
        Log.d(TAG_eSENSE, "Register Listener for Sensors!");

        // TODO: Something is wrong with the event listener - it doesn't seem to be able to
        // retrieve the sensor configurations from the bluetoothGATT

        //Register Event listener
        boolean correctly_registered = eSense_manager.registerEventListener(this);
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
            }
        }.start();
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            // Get Sensor configuration
            boolean sensor_configs = eSense_manager.getSensorConfig();
            Log.d(TAG_eSENSE, "Received Sensor Configuration! " + sensor_configs);
            return sensor_configs;
        }
    });

    @Override
    public void onDisconnected(ESenseManager eSenseManager) {
        Log.d(TAG_eSENSE, "Disconnected from Device!");
        displayStatus("Disconnected from Device!");
    }

    public void displayStatus(final String stat) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                connectStatus.setText(stat);
            }
        });
    }



    // --- eSense Event Listener Methods ---
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
        eSense_config = eSenseConfig;
        received_eSense_configs = true;
        Log.d(TAG_eSENSE, "Found Sensor Configurations!");
    }

    @Override
    public void onAccelerometerOffsetRead(int i, int i1, int i2) {

    }



    // --- eSense Sensor Listener Methods ---
    @Override
    public void onSensorChanged(ESenseEvent event) {

        // If we have received the sensor configurations, we save the data.
        if(received_eSense_configs) {
            
            // Get eSense's current acceleration values
            double[] acc = event.convertAccToG(eSense_config);  // Acceleration in g

            // Get eSense's current rotation (angular velocity) values
            double[] gyro = event.convertGyroToDegPerSecond(eSense_config); // Rotation in degrees/sec

            // Get sensor event timestamp in system milliseconds
            long timestamp = event.getTimestamp();

            // Check for update in step count from eSense
            pedometer_StepDetector.eSense_UpdateAcceleration(
                    timestamp, (float) acc[0], (float) acc[1], (float) acc[2]);

            // Update smartphone's current acceleration values to XML element
            String eSense_CurrentAcc_X = String.format(Locale.US,"%.6f", acc[0]);
            String eSense_CurrentAcc_Y = String.format(Locale.US,"%.6f", acc[1]);
            String eSense_CurrentAcc_Z = String.format(Locale.US,"%.6f", acc[2]);
            //String eSense_CurrentAcc_X = Double.toString(acc[0]);
            //String eSense_CurrentAcc_Y = Double.toString(acc[1]);
            //String eSense_CurrentAcc_Z = Double.toString(acc[2]);
            String eSense_AccelerationTableText = "eSense Accelerations:" + "\n" + "[" +
                    "x = " + eSense_CurrentAcc_X + ", " +
                    "y = " + eSense_CurrentAcc_Y + ", " +
                    "z = " + eSense_CurrentAcc_Z + "]";
            eSense_Acceleration_Table.setText(eSense_AccelerationTableText);

            // Update smartphone's current acceleration values to XML element
            String eSense_CurrentGyro_X = String.format(Locale.US,"%.6f", gyro[0]);
            String eSense_CurrentGyro_Y = String.format(Locale.US,"%.6f", gyro[1]);
            String eSense_CurrentGyro_Z = String.format(Locale.US,"%.6f", gyro[2]);
            //String eSense_CurrentGyro_X = Double.toString(acc[0]);
            //String eSense_CurrentGyro_Y = Double.toString(acc[1]);
            //String eSense_CurrentGyro_Z = Double.toString(acc[2]);
            String eSense_GyroscopeTableText = "eSense Rotations:" + "\n" + "[" +
                    "x = " + eSense_CurrentGyro_X + ", " +
                    "y = " + eSense_CurrentGyro_Y + ", " +
                    "z = " + eSense_CurrentGyro_Z + "]";
            eSense_Gyroscope_Table.setText(eSense_GyroscopeTableText);


            String eSense_currTime = getDateString() + "," + getTimeStringWithColons();

            // Save eSense current acceleration values to Text file
            String eSense_currAccData = Double.toString(acc[0]) + "," + Double.toString(acc[1]) +
                    "," + Double.toString(acc[2]);
            dataExporter.eSense_writeAccToFile(eSense_currTime + "," + eSense_currAccData + ",");

            // Save eSense current rotation (angular velocity) values to Text file
            String eSense_currGyroData = Double.toString(gyro[0]) + "," + Double.toString(gyro[1]) +
                    "," + Double.toString(gyro[2]);
            dataExporter.eSense_writeGyroToFile(eSense_currTime + "," + eSense_currGyroData + ",");

        }

    }



    // --- Android Sensor Listener Methods ---
    // Function to handle sensor value changes from the phone
    @Override
    public void onSensorChanged(SensorEvent event) {

        String phone_currTime = getDateString() + "," + getTimeStringWithColons();

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            // Get smartphone's current acceleration values
            float[] phone_CurrentAcc = new float[3];
            phone_CurrentAcc[0] = event.values[0];
            phone_CurrentAcc[1] = event.values[1];
            phone_CurrentAcc[2] = event.values[2];


            // Check for update in step count from phone
            pedometer_StepDetector.phone_UpdateAcceleration(
                    event.timestamp, phone_CurrentAcc[0], phone_CurrentAcc[1], phone_CurrentAcc[2]);


            // Update smartphone's current acceleration values to XML element
            String phone_CurrentAcc_X = String.format(Locale.US,"%.6f", phone_CurrentAcc[0]);
            String phone_CurrentAcc_Y = String.format(Locale.US,"%.6f", phone_CurrentAcc[1]);
            String phone_CurrentAcc_Z = String.format(Locale.US,"%.6f", phone_CurrentAcc[2]);
            //String phone_CurrentAcc_X = Float.toString(phone_CurrentAcc[0]);
            //String phone_CurrentAcc_Y = Float.toString(phone_CurrentAcc[1]);
            //String phone_CurrentAcc_Z = Float.toString(phone_CurrentAcc[2]);
            String phone_AccelerationTableText = "Phone Accelerations:" + "\n" + "[" +
                    "x = " + phone_CurrentAcc_X + ", " +
                    "y = " + phone_CurrentAcc_Y + ", " +
                    "z = " + phone_CurrentAcc_Z + "]";
            phone_Acceleration_Table.setText(phone_AccelerationTableText);


            // Save smartphone's current acceleration values to text file
            String phone_currAccData = Float.toString(phone_CurrentAcc[0]) + "," +
                    Float.toString(event.values[1]) + "," + Float.toString(phone_CurrentAcc[2]);
            dataExporter.Phone_writeAccToFile(phone_currTime + "," + phone_currAccData + ",");
        }


        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            Log.d(TAG_PHONE, "onSensorChanged: gyroX: " + event.values[0] + "gyroY: " +
                    event.values[1] + "gyroZ: " + event.values[2]);

            // Get smartphone's current gyroscope (angular velocity) values
            float[] phone_currentGyro = new float[3];
            phone_currentGyro[0] = event.values[0];
            phone_currentGyro[1] = event.values[1];
            phone_currentGyro[2] = event.values[2];


            // Update smartphone's current gyroscope (angular velocity) values to XML element
            String phone_CurrentGyro_X = String.format(Locale.US, "%.6f", phone_currentGyro[0]);
            String phone_CurrentGyro_Y = String.format(Locale.US, "%.6f", phone_currentGyro[1]);
            String phone_CurrentGyro_Z = String.format(Locale.US, "%.6f", phone_currentGyro[2]);
            //String phone_CurrentGyro_X = Float.toString(phone_currentGyro[0]);
            //String phone_CurrentGyro_Y = Float.toString(phone_currentGyro[1]);
            //String phone_CurrentGyro_Z = Float.toString(phone_currentGyro[2]);
            String phone_GyroscopeTableText = "Phone Rotations:\n" + "[" +
                    "x = " + phone_CurrentGyro_X + ", " +
                    "y = " + phone_CurrentGyro_Y + ", " +
                    "z = " + phone_CurrentGyro_Z + "]";
            phone_Gyroscope_Table.setText(phone_GyroscopeTableText);


            // Save smartphone's current gyroscope (angular velocity) to text file
            String phone_currGyroData = Float.toString(phone_currentGyro[0]) + "," +
                    Float.toString(phone_currentGyro[1]) + "," +Float.toString(phone_currentGyro[2]);
            dataExporter.Phone_writeGyroToFile(phone_currTime + "," + phone_currGyroData + ",");
        }
    }


    // Function to handle senor accuracy changes from the phone
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }



    // --- Pedometer Step Listener Methods ---
    // Function to update counted steps from eSense
    @Override
    public void eSense_UpdateStep(long timeNs) {
        eSense_numSteps++;
        eSense_StepValue.setText("eSense: " + String.valueOf(eSense_numSteps));
    }

    // Function to update counted steps from phone
    @Override
    public void phone_UpdateStep(long timeNs) {
        phone_numSteps++;
        phone_StepValue.setText("Phone: " + String.valueOf(phone_numSteps));
    }



    // --- Time and Date Methods ---
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

}


