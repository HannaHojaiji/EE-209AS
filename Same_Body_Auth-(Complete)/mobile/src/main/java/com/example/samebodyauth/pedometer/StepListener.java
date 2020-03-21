/* Source:
 * Google Simple Pedometer
 * https://github.com/google/simple-pedometer
 */
package com.example.samebodyauth.pedometer;

/* --- Interface ---*/
public interface StepListener {

    void phone_UpdateStep(long timeNs);

    void eSense_UpdateStep(long timeNs);

}
