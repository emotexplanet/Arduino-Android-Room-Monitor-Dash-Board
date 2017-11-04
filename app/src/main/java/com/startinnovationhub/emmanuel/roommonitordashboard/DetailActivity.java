package com.startinnovationhub.emmanuel.roommonitordashboard;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.widget.Toast.LENGTH_SHORT;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = DetailActivity.class.getSimpleName();

    private ListView recordList;
    private RecordsManager recordsManager;


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
                if (!recordsManager.exportData(this)) {
                    Toast.makeText(this, "Unable to export the record", LENGTH_SHORT).show();
                }
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


}
