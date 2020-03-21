package com.example.samebodyauth;

/* --- External Libraries --- */
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;


/* --- Class --- */
public class DataExportActivity {

    /* --- Fields --- */
    // LOG ID
    private static final String TAG_DATAEXPORT = "Data-Exporter";

    // File Path
    private File mExportRootPath;
    private File mExportDir;

    private int currentSession = 0;



    /* --- Methods --- */
    // Constructor
    public DataExportActivity() {

        // --- Request Permissions ---
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Log.d(TAG_DATAEXPORT, "No SD card, can't export Data");
        } else {
            // Use the Document directory for saving text file
            mExportRootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            mExportDir = new File(mExportRootPath, "209AS_DATA");
            if (!mExportDir.exists()) {
                mExportDir.mkdirs();
            }
        }

        // --- Get current App session ---
        currentSession = findCurrentSession();
    }


    // Function to find current App session
    private int findCurrentSession() {
        // --- Determine session number with number of existing files ---
        File[] files = mExportDir.listFiles();
        Log.d(TAG_DATAEXPORT,  "Size: "+ files.length);
        return files.length;
    }



    public void eSense_writeAccToFile(String accData) {
        // --- Initialize Objects ---
        File file;
        PrintWriter printWriter;

        // --- Write data into Text File ---
        printWriter = null;

        try {
            file = new File(mExportDir, "eSENSE_ACC_DATA" + Integer.toString(currentSession) + ".txt");
            file.createNewFile();

            printWriter = new PrintWriter(new FileWriter(file, true));
            printWriter.println(accData); // write the current eSense acc values into the text file
            Log.d(TAG_DATAEXPORT, "Successfully wrote String of length" + Long.toString(accData.length()));

        } catch (Exception exc) {
            //if there are any exceptions, return false
            Log.d(TAG_DATAEXPORT, exc.getMessage());

        } finally {
            if (printWriter != null) printWriter.close();

        }
    }

    public void eSense_writeGyroToFile(String gyroData) {
        // --- Initialize Objects ---
        File file;
        PrintWriter printWriter;

        // --- Write data into Text File ---
        printWriter = null;

        try {
            file = new File(mExportDir, "eSENSE_GYRO_DATA" + Integer.toString(currentSession) + ".txt");
            file.createNewFile();

            printWriter = new PrintWriter(new FileWriter(file, true));
            printWriter.println(gyroData); // write the current eSense gyro values into the text file
            Log.d(TAG_DATAEXPORT, "Successfully wrote String of length" + Long.toString(gyroData.length()));

        } catch (Exception exc) {
            //if there are any exceptions, return false
            Log.d(TAG_DATAEXPORT, exc.getMessage());

        } finally {
            if (printWriter != null) printWriter.close();
        }
    }


    public void Phone_writeAccToFile(String accData) {
        // --- Initialize Objects ---
        File file;
        PrintWriter printWriter;

        // --- Write data into Text File ---
        printWriter = null;

        try {
            file = new File(mExportDir, "PHONE_ACC_DATA" + Integer.toString(currentSession) + ".txt");
            file.createNewFile();

            printWriter = new PrintWriter(new FileWriter(file, true));
            printWriter.println(accData); // write the current phone acc values into the text file
            Log.d(TAG_DATAEXPORT, "Successfully wrote String of length" + Long.toString(accData.length()));

        } catch (Exception exc) {
            //if there are any exceptions, return false
            Log.d(TAG_DATAEXPORT, exc.getMessage());

        } finally {
            if (printWriter != null) printWriter.close();

        }
    }


    public void Phone_writeGyroToFile(String gyroData) {
        // --- Initialize Objects ---
        File file;
        PrintWriter printWriter;

        // --- Write data into Text File ---
        printWriter = null;

        try {
            file = new File(mExportDir, "PHONE_GYRO_DATA" + Integer.toString(currentSession) + ".txt");
            file.createNewFile();

            printWriter = new PrintWriter(new FileWriter(file, true));
            printWriter.println(gyroData); // write the current phone gyro values into the text file
            Log.d(TAG_DATAEXPORT, "Successfully wrote String of length" + Long.toString(gyroData.length()));

        } catch (Exception exc) {
            //if there are any exceptions, return false
            Log.d(TAG_DATAEXPORT, exc.getMessage());

        } finally {
            if (printWriter != null) printWriter.close();
        }
    }
}

