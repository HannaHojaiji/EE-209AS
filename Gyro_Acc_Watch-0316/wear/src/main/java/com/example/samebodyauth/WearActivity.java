package com.example.samebodyauth;

/* --- External Libraries --- */
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


/* --- Class --- */
public class WearActivity extends WearableActivity {

    /* --- Fields --- */
    // --- XML Elements ---
    private Button watch_talkButton;
    private EditText watch_typedPin;
    private TextView watch_messageStatus;

    // --- Activity Elements ---
    String pinText;


    /* --- Methods --- */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear);


        // --- Get XML Elements ---
        watch_typedPin = (EditText) findViewById(R.id.watch_TypePin);
        watch_messageStatus = (TextView) findViewById(R.id.watch_MessageStatus);
        watch_talkButton = findViewById(R.id.watch_TalkButton);


        // --- Initialize Objects and Event Handlers ---
        watch_talkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == watch_talkButton.getId()) {
                    pinText = watch_typedPin.getText().toString();
                }
                String onClickMessage = pinText;
                watch_messageStatus.setText(onClickMessage);
            }
        });
    }
}