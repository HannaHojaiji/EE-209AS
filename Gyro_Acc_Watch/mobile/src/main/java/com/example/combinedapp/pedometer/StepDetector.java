package com.example.combinedapp.pedometer;

/* --- Class ---*/
public class StepDetector {

    /* --- Fields --- */
    // Buffer size for recorded accelerations and their estimated velocities
    private static final int ACCEL_RING_SIZE = 50;
    private static final int VEL_RING_SIZE = 10;

    // change this threshold according to your sensitivity preferences
    public static float ESENSE_STEP_THRESHOLD = 12f;
    public static float PHONE_STEP_THRESHOLD = 12f;


    private StepListener listener;
    private static final int STEP_DELAY_NS = 250000000;

    private int phone_AccelRingCounter = 0;
    private int phone_VelRingCounter = 0;
    private float[] phone_AccelRingX = new float[ACCEL_RING_SIZE];
    private float[] phone_AccelRingY = new float[ACCEL_RING_SIZE];
    private float[] phone_AccelRingZ = new float[ACCEL_RING_SIZE];
    private float[] phone_VelRing = new float[VEL_RING_SIZE];

    private float phone_OldVelocityEstimate = 0;
    private long phone_lastStepTimeNs = 0;


    private int eSense_AccelRingCounter = 0;
    private int eSense_VelRingCounter = 0;
    private float[] eSense_AccelRingX = new float[ACCEL_RING_SIZE];
    private float[] eSense_AccelRingY = new float[ACCEL_RING_SIZE];
    private float[] eSense_AccelRingZ = new float[ACCEL_RING_SIZE];
    private float[] eSense_VelRing = new float[VEL_RING_SIZE];

    private float eSense_OldVelocityEstimate = 0;
    private long eSense_lastStepTimeNs = 0;



    /* --- Methods --- */
    public void registerListener(StepListener listener) {
        this.listener = listener;
    }


    public void phone_UpdateAcceleration(long timeNs, float x, float y, float z) {
        float[] currentAccel = new float[3];
        currentAccel[0] = x;
        currentAccel[1] = y;
        currentAccel[2] = z;

        // First step is to update our guess of where the global z vector is.
        phone_AccelRingCounter++;
        phone_AccelRingX[phone_AccelRingCounter % ACCEL_RING_SIZE] = currentAccel[0];
        phone_AccelRingY[phone_AccelRingCounter % ACCEL_RING_SIZE] = currentAccel[1];
        phone_AccelRingZ[phone_AccelRingCounter % ACCEL_RING_SIZE] = currentAccel[2];

        float[] worldZ = new float[3];
        worldZ[0] = SensorFilter.sum(phone_AccelRingX) / Math.min(phone_AccelRingCounter, ACCEL_RING_SIZE);
        worldZ[1] = SensorFilter.sum(phone_AccelRingY) / Math.min(phone_AccelRingCounter, ACCEL_RING_SIZE);
        worldZ[2] = SensorFilter.sum(phone_AccelRingZ) / Math.min(phone_AccelRingCounter, ACCEL_RING_SIZE);

        float normalization_factor = SensorFilter.norm(worldZ);

        worldZ[0] = worldZ[0] / normalization_factor;
        worldZ[1] = worldZ[1] / normalization_factor;
        worldZ[2] = worldZ[2] / normalization_factor;

        float currentZ = SensorFilter.dot(worldZ, currentAccel) - normalization_factor;
        phone_VelRingCounter++;
        phone_VelRing[phone_VelRingCounter % VEL_RING_SIZE] = currentZ;

        float velocityEstimate = SensorFilter.sum(phone_VelRing);

        if (velocityEstimate > PHONE_STEP_THRESHOLD && phone_OldVelocityEstimate <= PHONE_STEP_THRESHOLD
                && (timeNs - phone_lastStepTimeNs > STEP_DELAY_NS)) {
            listener.phone_UpdateStep(timeNs);
            phone_lastStepTimeNs = timeNs;
        }
        phone_OldVelocityEstimate = velocityEstimate;
    }



    public void eSense_UpdateAcceleration(long timeNs, float x, float y, float z) {
        float[] currentAccel = new float[3];
        currentAccel[0] = x;
        currentAccel[1] = y;
        currentAccel[2] = z;

        // First step is to update our guess of where the global z vector is.
        eSense_AccelRingCounter++;
        eSense_AccelRingX[eSense_AccelRingCounter % ACCEL_RING_SIZE] = currentAccel[0];
        eSense_AccelRingY[eSense_AccelRingCounter % ACCEL_RING_SIZE] = currentAccel[1];
        eSense_AccelRingZ[eSense_AccelRingCounter % ACCEL_RING_SIZE] = currentAccel[2];

        float[] worldZ = new float[3];
        worldZ[0] = SensorFilter.sum(eSense_AccelRingX) / Math.min(eSense_AccelRingCounter, ACCEL_RING_SIZE);
        worldZ[1] = SensorFilter.sum(eSense_AccelRingY) / Math.min(eSense_AccelRingCounter, ACCEL_RING_SIZE);
        worldZ[2] = SensorFilter.sum(eSense_AccelRingZ) / Math.min(eSense_AccelRingCounter, ACCEL_RING_SIZE);

        float normalization_factor = SensorFilter.norm(worldZ);

        worldZ[0] = worldZ[0] / normalization_factor;
        worldZ[1] = worldZ[1] / normalization_factor;
        worldZ[2] = worldZ[2] / normalization_factor;

        float currentZ = SensorFilter.dot(worldZ, currentAccel) - normalization_factor;
        eSense_VelRingCounter++;
        eSense_VelRing[eSense_VelRingCounter % VEL_RING_SIZE] = currentZ;

        float velocityEstimate = SensorFilter.sum(eSense_VelRing);

        if (velocityEstimate > ESENSE_STEP_THRESHOLD && eSense_OldVelocityEstimate <= ESENSE_STEP_THRESHOLD
                && (timeNs - eSense_lastStepTimeNs > STEP_DELAY_NS)) {
            listener.eSense_UpdateStep(timeNs);
            eSense_lastStepTimeNs = timeNs;
        }
        eSense_OldVelocityEstimate = velocityEstimate;
    }
}
