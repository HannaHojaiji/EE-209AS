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


public class MainActivity extends AppCompatActivity {

    TextToSpeech t1;
    TextView outputText;
    Button b_generate, b_check;
    EditText pinText;
    String output_string, text, toSpeak, ran;
    int num1, num2 = 0;

    Random r;
    int min, max, output;

    String msg1 = "Authentication Success";
    String msg2 = "Authentication Failed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b_generate = (Button) findViewById(R.id.b_generate);
        b_check = (Button) findViewById(R.id.b_check);
        pinText = (EditText) findViewById(R.id.pinText);
        outputText = (TextView) findViewById(R.id.outputText);

        r = new Random();

        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });

        b_generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String temp_min, temp_max;
                //temp_min = et_min.getText().toString();
                //temp_max = et_max.getText().toString();
                //if(!(temp_min.equals("")) && !(temp_max.equals(""))) {
                min = 1000;
                max = 10000;

                if (max > min) {
                    output = r.nextInt((max - min) + 1) + min;


                }
                //num1 = output;
                ran = String.valueOf(output);
                toSpeak = Integer.toString(output);
                //Toast.makeText(getApplicationContext(), toSpeak,Toast.LENGTH_SHORT).show();
                t1.speak(toSpeak, QUEUE_FLUSH, null, null);
                //}

            }

        });

        //text = pinText.getText().toString();


        //num2 = Integer.parseInt(text);
        b_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == b_check.getId()) {
                    text = pinText.getText().toString();
                }
                outputText.setText(text);
                //String temp_min, temp_max;
                //temp_min = et_min.getText().toString();
                //temp_max = et_max.getText().toString();
                //if(!(temp_min.equals("")) && !(temp_max.equals(""))) {
                //outputText.setText(text);
                //t1.speak(msg2, TextToSpeech.QUEUE_FLUSH, null);

                if (ran.equals(text)) {
                    //t1.speak(msg1, QUEUE_FLUSH, null, utteranceID = 2);  //, String utteranceId: setOnUtteranceProgressListener);
                    t1.speak(msg1, QUEUE_FLUSH, null, null);
                    Toast.makeText(getApplicationContext(), msg1,Toast.LENGTH_SHORT).show();
                    //num2 = 1;
                    open_activity_imu_data();


                }
                else
                {
                    t1.speak(msg2, QUEUE_FLUSH, null, null);
                    Toast.makeText(getApplicationContext(), msg2,Toast.LENGTH_SHORT).show();
                }

                //Toast.makeText(getApplicationContext(), toSpeak,Toast.LENGTH_SHORT).show();

                //}

            }

        });


    }

    public void onPause() {
        if (t1 != null) {
            t1.stop();
            t1.shutdown();
        }
        super.onPause();
    }

    public void open_activity_imu_data()
    {
        Intent intent = new Intent(this, SenseIMUActivity.class);
        startActivity(intent);
    }

}
