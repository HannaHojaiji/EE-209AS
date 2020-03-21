package com.example.samebodyauth;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;

/**
 * Created by Jeffrey Liu on 12/2/15.
 * This service will keep listening to all the message coming from the watch
 */
public class PhoneWearableListenerService extends WearableListenerService {

    public static final String WATCH_TO_PHONE_MESSAGE_PATH = "/watchToPhone";
    public static final String PHONE_TO_WATCH_MESSAGE_PATH = "/phoneToWatch";
    public static final String MY_INTENT_FILTER = "jeff.liu.my.filter";
    public static final String PHONE_TO_WATCH_TEXT = "jeff.liu.from.phone";

    private static int currentValue = 0;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        ActivityManager am = (ActivityManager) this .getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        ComponentName componentInfo = taskInfo.get(0).topActivity;
        String Actvity_Name = taskInfo.get(0).topActivity.getClassName();
        String PKG_Name = componentInfo.getPackageName();

        if (messageEvent.getPath().equalsIgnoreCase(WATCH_TO_PHONE_MESSAGE_PATH)) {

            String receivedText = new String(messageEvent.getData());

            if(Actvity_Name.equals("com.example.samebodyauth.PeriodicVerifyActivity")) {
                currentValue = Integer.parseInt(receivedText);
            }

            broadcastIntent(receivedText);

        } else {
            super.onMessageReceived(messageEvent);
        }
    }

    // broadcast a custom intent.
    public void broadcastIntent(String text) {
        Intent intent = new Intent();
        intent.setAction(MY_INTENT_FILTER);
        intent.putExtra(PHONE_TO_WATCH_TEXT, text);
        sendBroadcast(intent);
    }
}