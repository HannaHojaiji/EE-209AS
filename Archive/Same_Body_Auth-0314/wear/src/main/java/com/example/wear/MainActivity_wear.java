package com.example.wear;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static android.speech.tts.TextToSpeech.QUEUE_FLUSH;

public class MainActivity_wear extends WearableActivity {

    private TextView mTextView;
    String text;
    EditText pinText;
    Button send;
    TextView outputText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_wear);

        mTextView = (TextView) findViewById(R.id.text);

        // Enables Always-on
        setAmbientEnabled();
        send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == send.getId()) {
                    text = pinText.getText().toString();
                }
                outputText.setText(text);
            }
        });
    }
}