package com.example.samebodyauth;

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
    private Button phone_GeneratePin, phone_VerifyPin;
    private EditText phone_TypePin;

    // --- Activity Objects ---
    private TextToSpeech TextToSpeech_InitAuth;
    private Random PinGenerator;
    private int minValidPin, maxValidPin, randomPin;
    private String typedPin, randomPinToSpeak, randomPinString;
    private String pinAuthMessage_Success = "Authentication Success";
    private String pinAuthMessage_Failed = "Authentication Failed";



    /* --- Methods --- */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_auth);

        // --- Get XML Elements ---
        phone_GeneratePin = findViewById(R.id.phone_GeneratePin);
        phone_VerifyPin = findViewById(R.id.phone_VerifyPin);
        phone_TypePin = findViewById(R.id.phone_TypePin);


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

        phone_GeneratePin.setOnClickListener(new View.OnClickListener() {
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

        phone_VerifyPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // --- Get Typed Pin ---
                if(view.getId() == phone_VerifyPin.getId()) {
                    typedPin = phone_TypePin.getText().toString();
                }


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


    // --- openPeriodicVerifyActivity ---
    // Function to transit to Periodic Verify Activity
    public void openPeriodicVerifyActivity(){
        Intent intent = new Intent(this, PeriodicVerifyActivity.class);
        startActivity(intent);
    }
}
