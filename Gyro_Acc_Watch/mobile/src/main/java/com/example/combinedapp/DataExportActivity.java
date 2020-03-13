package com.example.combinedapp;

/* --- External Libraries --- */
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/* --- Class --- */
public class DataExportActivity {

    final String TAG = "DBG-DATAEXPORTER:";

    File mExportRoot;
    File mExportDir;
    int currentSession = 0;

    //String mCurrentDateString = "";

    public DataExportActivity() {

        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Log.d(TAG, "No SD card, can't export Data");
        } else {
            //We use the Download directory for saving our .csv file.
            mExportRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            mExportDir = new File(mExportRoot, "ESENSE_DATA");
            if (!mExportDir.exists()) {
                mExportDir.mkdirs();
            }
        }

        currentSession = findCurrentSession();
    }


    public void eSense_writeAccToFile(String packet) {

        File file;
        PrintWriter printWriter = null;

        try {
            file = new File(mExportDir, "eSENSE_ACC_DATA" + Integer.toString(currentSession) + ".txt");
            file.createNewFile();
            printWriter = new PrintWriter(new FileWriter(file, true));


            printWriter.println(packet); //write the record to the mood textfile
            Log.d(TAG, "Successfully wrote String of length" + Long.toString(packet.length()));
        } catch (Exception exc) {
            //if there are any exceptions, return false
            Log.d(TAG, exc.getMessage());
        } finally {
            if (printWriter != null) printWriter.close();
        }
    }

    public void eSense_writeGyroToFile(String packet) {

        File file;
        PrintWriter printWriter = null;

        try {
            file = new File(mExportDir, "eSENSE_GYRO_DATA" + Integer.toString(currentSession) + ".txt");
            file.createNewFile();
            printWriter = new PrintWriter(new FileWriter(file, true));


            printWriter.println(packet); //write the record to the mood textfile
            Log.d(TAG, "Successfully wrote String of length" + Long.toString(packet.length()));
        } catch (Exception exc) {
            //if there are any exceptions, return false
            Log.d(TAG, exc.getMessage());
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

        String timeDate = "[" + getDateString() + " " + getTimeString() + "]";
        String dataToWrite = timeDate + "," + accData;

        try {
            file = new File(mExportDir, "PHONE_ACC_DATA" + Integer.toString(currentSession) + ".txt");

            file.createNewFile();
            printWriter = new PrintWriter(new FileWriter(file, true));

            printWriter.println(dataToWrite); // write the current phone acc values into the text file

            Log.d(TAG, "Successfully wrote String of length" + Long.toString(accData.length()));

        } catch (Exception exc) {
            //if there are any exceptions, return false
            Log.d(TAG, exc.getMessage());

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

        String timeDate = "[" + getDateString() + " " + getTimeString() + "]";
        String dataToWrite = timeDate + "," + gyroData;

        try {
            file = new File(mExportDir, "PHONE_GYRO_DATA" + Integer.toString(currentSession) + ".txt");
            file.createNewFile();

            printWriter = new PrintWriter(new FileWriter(file, true));
            printWriter.println(dataToWrite); // write the current phone gyro values into the text file

            Log.d(TAG, "Successfully wrote String of length" + Long.toString(gyroData.length()));

        } catch (Exception exc) {
            //if there are any exceptions, return false
            Log.d(TAG, exc.getMessage());

        } finally {
            if (printWriter != null) printWriter.close();
        }
    }


    private int findCurrentSession() {
        File[] files = mExportDir.listFiles();
        Log.d(TAG,  "Size: "+ files.length);
        return files.length;
    }


    //returns a string containing today's date
    public static String getDateString(){
        Date time = Calendar.getInstance().getTime();
        SimpleDateFormat outputFmt = new SimpleDateFormat("MM-dd-yyyy");
        return outputFmt.format(time);
    }

    //returns a string containing a timestamp in format (HH-mm-ss)
    public static String getTimeString(){
        Date time = Calendar.getInstance().getTime();
        SimpleDateFormat outputFmt = new SimpleDateFormat("HH-mm-ss");
        return outputFmt.format(time);
    }

    //returns a string containing a timestamp in format (HH:mm:ss)
    public static String getTimeStringWithColons(){
        Date time = Calendar.getInstance().getTime();
        SimpleDateFormat outputFmt = new SimpleDateFormat("HH:mm:ss");
        return outputFmt.format(time);
    }

}

