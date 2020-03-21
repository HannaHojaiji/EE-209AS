package com.example.samebodyauth;

/* --- External Libraries --- */
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


/* --- Class --- */
public class WearActivity extends WearableActivity {

    /* --- Fields --- */
    public static final String MY_INTENT_FILTER = "jeff.liu.my.filter";
    public static final String PHONE_TO_WATCH_TEXT = "jeff.liu.from.phone";

    private static final String LOG_TAG = "MyHeart";


    // --- XML Elements ---
    private Button watch_talkButton;
    private EditText watch_typedPin;
    private TextView watch_messageFromPhone;
    private TextView watch_messageToPhone;


    // --- Activity Elements ---
    String pinText;
    private BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (intent.getAction().equals(MY_INTENT_FILTER)) {
                watch_messageFromPhone.setText(intent.getStringExtra(PHONE_TO_WATCH_TEXT));
            }
        }
    };


    public static ServiceConnection sc;



    /* --- Methods --- */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear);
        setAmbientEnabled();


        // --- Get XML Elements ---
        watch_typedPin = (EditText) findViewById(R.id.watch_TextToPhone);
        watch_messageFromPhone = (TextView) findViewById(R.id.watch_MessageFromPhone);
        watch_talkButton = (Button) findViewById(R.id.watch_TalkButton);
        watch_messageToPhone = (TextView) findViewById(R.id.watch_MessageToPhone);


        // --- Initialize Objects and Event Handlers ---
        watch_talkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinText = watch_typedPin.getText().toString();
                watch_messageToPhone.setText(pinText);
                fireMessage(pinText);
            }
        });


        sc = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder binder) {
                Log.d(LOG_TAG, "connected to service.");
                // set our change listener to get change events
                //((HeartbeatService.HeartbeatServiceBinder)binder).setChangeListener(WearActivity.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        Intent intent = new Intent(WearActivity.this, HeartbeatService.class);
        //startService(intent);
        bindService(intent, sc, Service.BIND_AUTO_CREATE);


    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mWifiScanReceiver, new IntentFilter(MY_INTENT_FILTER));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver();
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(sc);
    }


    private void unregisterReceiver() {
        try {
            if (mWifiScanReceiver != null) {
                unregisterReceiver(mWifiScanReceiver);
            }
        } catch (IllegalArgumentException e) {
            mWifiScanReceiver = null;
        }
    }


    private void fireMessage(String text) {
        Intent msgIntent = new Intent(getApplicationContext(), SendPhoneMessageIntentService.class);
        msgIntent.putExtra(SendPhoneMessageIntentService.INPUT_EXTRA, text);
        startService(msgIntent);
    }

}