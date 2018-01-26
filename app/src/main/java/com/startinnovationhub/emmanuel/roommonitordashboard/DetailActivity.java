package com.startinnovationhub.emmanuel.roommonitordashboard;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = DetailActivity.class.getSimpleName();

    private ListView recordList;
    private RecordsManager recordsManager;

    public static final int MULTIPLE_PERMISSIONS = 10; // code you want.

    String[] permissions= new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };



    private ArrayAdapter<Records> recordsArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        recordList = (ListView) findViewById(R.id.record_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);


        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setIcon(R.mipmap.ic_launcher_round);
            actionBar.setTitle(R.string.app_name);
        }

        recordsManager = new RecordsManager();
        ArrayList<Records> roomRecords = recordsManager.getRecordsDetails(this);
        recordsArrayAdapter = new DetailListAdapter(this, 0, roomRecords);
        if (recordList != null) {
            recordList.setAdapter(recordsArrayAdapter);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_main; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail_menu, menu);

        MenuItem item = menu.findItem(R.id.ExportData);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ExportData:
               try{
                    boolean val = this.exportData(this);
                    if (!val) {
                        Toast.makeText(this, "Unable to export the record", LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(this, "Record export successfully!", LENGTH_SHORT).show();
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                //recordsManager.exportData(this);
                break;
            case R.id.claerData:
                if (recordsManager.ClearData(this)) {
                    Toast.makeText(this, "All data successfully Removed!", LENGTH_SHORT).show();
                    recordsArrayAdapter.clear();
                    recordList.setAdapter(null);
                } else {
                    Toast.makeText(this, "No data to delete!", LENGTH_SHORT).show();
                }
                break;

        }
        return false;
    }


    public boolean exportData(Context context) {

       if(checkPermissions()) {


           // File rFile = Environment.getExternalStorageDirectory();
           File dFile = new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)));
           if (!dFile.exists()) {
               dFile.mkdirs();
           }
           Log.d(TAG, "Export dir Main: " + dFile);
           File rFile = new File(dFile, "Room Monitor");
           if (!rFile.exists()) {
               rFile.mkdirs();
               Log.d(TAG, "Make dir: " + dFile);
           }
           Log.d(TAG, "Export dir: " + rFile);
           File sFile = new File(rFile.getAbsolutePath(), "Room_Monitor.csv");
           if (sFile.exists()) {
               sFile.delete();
           }
           try {
               sFile.createNewFile();
           } catch (IOException e) {
               Log.d(TAG, "File to write: " + e.getMessage(), e);
           }
           if (!dFile.exists()) {
               dFile.mkdir();
               Log.d(TAG, "Make dir: " + dFile);
           }

           String[] recordData = recordsManager.getRecord(context);
           try {
               BufferedWriter writer = new BufferedWriter(new FileWriter(sFile));
               writer.write("THE ROOM MONITOR RECORD");
               writer.append("DATE, TEMPERATURE(\u2103)C, HUMIDITY(%), POLLUTION LEVEL(%), STATUS");
               Log.d(TAG, "PRINTER WRITER EXPORTING FILE");
               for (String aRecordData : recordData) {
                   String[] recordDetail = aRecordData.split("&");
                   String record = recordDetail[4].trim() + "," + recordDetail[0].trim() + "," + recordDetail[1].trim() + "," + recordDetail[2].trim() + "," + recordDetail[3].trim();
                   writer.append(record);
                   writer.append("\n");
               }
               writer.flush();
               writer.close();
               Toast.makeText(context, "Record successfully export to: " + dFile.toString(), Toast.LENGTH_LONG).show();


           } catch (FileNotFoundException e) {
               Log.d(TAG, "Export dir 12: " + e.getMessage(), e);
               return  false;
           } catch (IOException e) {
               Log.d(TAG, "Export dir 13: " + e.getMessage(), e);
               return false;
           }


       }
           return true;


    }

    private  boolean checkPermissions() {
        int result;
        List<String> permissionsNeeded = new ArrayList<>();
        for (String p:permissions) {
            result = ContextCompat.checkSelfPermission(this,p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(p);
            }
        }
        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[permissionsNeeded.size()]),MULTIPLE_PERMISSIONS );
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // permissions granted.

                    Log.d(TAG, "Permission granted ");


                } else {
                    String permissions1 = "";
                    for (String per : permissions) {
                        permissions1 += "\n" + per;
                    }
                    // permissions list of don't granted permission
                    Log.d(TAG, "Permission denie ");
                }
                return;
            }
        }
    }

}
