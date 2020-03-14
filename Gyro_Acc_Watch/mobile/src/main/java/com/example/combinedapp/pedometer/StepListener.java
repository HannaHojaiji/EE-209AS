package com.example.combinedapp.pedometer;


/* --- Interface ---*/
public interface StepListener {

    void phone_UpdateStep(long timeNs);

    void eSense_UpdateStep(long timeNs);

}
