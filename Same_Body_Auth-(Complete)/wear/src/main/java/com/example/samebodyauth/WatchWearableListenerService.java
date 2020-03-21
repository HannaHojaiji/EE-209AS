/* Source:
 * Android-Wearable-Send-Message-bi-directional
 * https://github.com/jeffreyliu8/Android-Wearable-Send-Message-bi-directional
 */
package com.example.samebodyauth;

import android.content.Intent;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * This service will keep listening to all the message coming from the phone
 */
public class WatchWearableListenerService extends WearableListenerService {

    public static final String WATCH_TO_PHONE_MESSAGE_PATH = "/watchToPhone";
    public static final String PHONE_TO_WATCH_MESSAGE_PATH = "/phoneToWatch";

    public static final String MY_INTENT_FILTER = "jeff.liu.my.filter";
    public static final String PHONE_TO_WATCH_TEXT = "jeff.liu.from.phone";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equalsIgnoreCase(PHONE_TO_WATCH_MESSAGE_PATH)) {
            String receivedText = new String(messageEvent.getData());

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