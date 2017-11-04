package com.startinnovationhub.emmanuel.roommonitordashboard;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Created by Emmanuel on 23/09/2017.
 */

public class RecordsManager {

    //public RecordsManager(){}
    private static final String TAG = RecordsManager.class.getSimpleName();


    public void saveData(Context context, String sRecord) {
        boolean isExist;
        File file = new File(context.getFilesDir(), File.separator + Constants.RECORDS_FILE_NAME);
        if (!file.exists()) {
            // boolean b = file.mkdir();
            isExist = false;
            Log.d(TAG, "File create: " + file.toString());

        } else {
            isExist = true;
            Log.d(TAG, "File exist: " + file.toString());
        }
        try {
            String nRecord = "";
            if (isExist) {
                nRecord = " @ " + sRecord;
            } else {
                nRecord = sRecord;
            }
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(Constants.RECORDS_FILE_NAME, context.MODE_APPEND));
            outputStreamWriter.write(nRecord);
            outputStreamWriter.close();

            Log.d(TAG, "Record Save: " + nRecord);
            Log.d(TAG, "Record Save: " + file.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Records> getRecordsDetails(Context context) {
        String[] recordData = getRecord(context);
        ArrayList<Records> records = new ArrayList<Records>();
        if ((recordData != null)) {
            try {
                for (String aRecordData : recordData) {
                    String[] recordDetail = aRecordData.split("&");
                    if (recordDetail.length >= 3) {
                        Records reCod = new Records(recordDetail[0].trim().concat("°C"), recordDetail[1].trim().concat("%"), recordDetail[2].trim().concat("%"), recordDetail[3].trim(), recordDetail[4].trim());
                        records.add(reCod);
                        Log.d(TAG, "" + reCod);
                    }

                }
            } catch (Exception e) {
                Log.d(TAG, "Data separation failed: " + e.getMessage(), e);
                return null;
            }

        }
        return records;
    }


    private String[] getRecord(Context context) {
        String recordData = readRecord(context);

        if (recordData != null) {
            return recordData.split("@");

            // Toast.makeText(context, "Number of record: " + recordData.length, Toast.LENGTH_LONG).show();
        }

        return null;

    }


    public String readRecord(Context context) {
        String rData = null;

        try {
            StringBuilder recordString = new StringBuilder();
            InputStream inputStream = context.openFileInput(Constants.RECORDS_FILE_NAME);
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String readString;
                while ((readString = bufferedReader.readLine()) != null) {
                    recordString.append(readString);
                }
                inputStream.close();
                bufferedReader.close();
            }
            rData = recordString.toString();
            Log.d(TAG, "Read Record Data: " + rData);

        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.d(TAG, "File write failed: " + e.toString());
        }


        return rData;
    }


    public boolean ClearData(Context context) {
        File file = new File(context.getFilesDir(), File.separator + Constants.RECORDS_FILE_NAME);
        if (file.exists()) {
            return file.delete();
        } else {
            return false;
        }
    }


    public boolean exportData(Context context) {
        File exportDir = new File(Environment.getExternalStorageDirectory(), "Room Monitor");


        Log.d(TAG, "Export dir: " + exportDir);

        if (!exportDir.exists()) {
            exportDir.mkdir();
        }
        String[] recordData = getRecord(context);
        PrintWriter printWriter = null;
        try {
            if ((recordData != null)) {
                File file = new File(exportDir, "Room_Monitor.csv");
                file.createNewFile();
                printWriter = new PrintWriter(new FileWriter(file));

                printWriter.println("THE ROOM MONITOR RECORD");
                printWriter.println("DATE, TEMPERATURE(\u2103)C, HUMIDITY(%), POLUTION LEVEL(%), STATUS");
                Log.d(TAG, "PRINTER WRITER EXPORTING FILE");
                for (int i = 0; i < recordData.length; i++) {
                    String[] recordDetail = recordData[i].split("&");
                    String record = recordDetail[4].trim() + "," + recordDetail[0].trim() + "," + recordDetail[1].trim() + "," + recordDetail[2].trim() + "," + recordDetail[3].trim();
                    printWriter.println(record);
                }
                Toast.makeText(context, "Record successfully export to: " + file.toString(), Toast.LENGTH_LONG).show();
            } else {
                return false;
            }
        } catch (IOException e) {
            Log.d(TAG, "Error: " + e.getMessage(), e);
            return false;
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }
        return true;
    }
}
