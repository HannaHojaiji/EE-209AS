package com.example.combinedapp;

/* --- External Libraries --- */
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import static android.speech.tts.TextToSpeech.QUEUE_FLUSH;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Random;

/* --- Class --- */
public class InitAuthActivity extends AppCompatActivity {

    /* --- Fields --- */
    // --- XML Elements ---
    Button button_GeneratePin, button_VerifyPin;
    EditText text_TypePin;
    TextView text_TypedPin;

    // --- Activity Objects ---
    TextToSpeech TextToSpeech_InitAuth;
    Random PinGenerator;
    int minValidPin, maxValidPin, randomPin;
    String typedPin, randomPinToSpeak, randomPinString;
    String pinAuthMessage_Success = "Authentication Success";
    String pinAuthMessage_Failed = "Authentication Failed";



    /* --- Methods --- */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_auth);

        // --- Get XML Elements ---
        button_GeneratePin = (Button) findViewById(R.id.button_GeneratePin);
        button_VerifyPin = (Button) findViewById(R.id.button_VerifyPin);
        text_TypePin = (EditText) findViewById(R.id.editText_TypePin);
        text_TypedPin = (TextView) findViewById(R.id.textView_TypedPin);


        // --- Initialize Objects and Event Handlers ---
        PinGenerator = new Random();
        TextToSpeech_InitAuth = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    TextToSpeech_InitAuth.setLanguage(Locale.US);
                }
            }
        });

        button_GeneratePin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // --- Check for Valid Pins ---
                minValidPin = 1000;
                maxValidPin = 10000;

                if (maxValidPin > minValidPin) {
                    // --- Get a Valid Pin ---
                    randomPin = PinGenerator.nextInt((maxValidPin - minValidPin) + 1) + minValidPin;
                }

                // --- Spell the Valid Pin through TextToSpeech ---
                randomPinString = String.valueOf(randomPin);
                randomPinToSpeak = Integer.toString(randomPin);
                TextToSpeech_InitAuth.speak(randomPinToSpeak, QUEUE_FLUSH, null, null);
            }
        });

        button_VerifyPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // --- Get Typed Pin ---
                if(view.getId() == button_VerifyPin.getId()) {
                    typedPin = text_TypePin.getText().toString();
                }

                // --- ---
                text_TypedPin.setText(typedPin);


                // --- Verify Typed Pin with the Valid Pin ---
                if (randomPinString.equals(typedPin)) {
                    // Typed Pin and Valid Pin match

                    // --- Speak Success Authentication Message through TextToSpeech ---
                    TextToSpeech_InitAuth.speak(pinAuthMessage_Success, QUEUE_FLUSH, null, null);

                    Toast.makeText(getApplicationContext(), pinAuthMessage_Success,Toast.LENGTH_SHORT).show();


                    // --- Initiate Periodic Verifying Activity ---
                    openPeriodicVerifyActivity();

                } else {
                    // Typed Pin and Valid Pin NOT match

                    // --- Speak Failed Authentication Message through TextToSpeech ---
                    TextToSpeech_InitAuth.speak(pinAuthMessage_Failed, QUEUE_FLUSH, null, null);

                    Toast.makeText(getApplicationContext(), pinAuthMessage_Failed,Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    public void onPause() {
        if (TextToSpeech_InitAuth != null) {
            TextToSpeech_InitAuth.stop();
            TextToSpeech_InitAuth.shutdown();
        }
        super.onPause();
    }

    public void openPeriodicVerifyActivity(){
        Intent intent = new Intent(this, PeriodicVerifyActivity.class);
        startActivity(intent);
    }

}
