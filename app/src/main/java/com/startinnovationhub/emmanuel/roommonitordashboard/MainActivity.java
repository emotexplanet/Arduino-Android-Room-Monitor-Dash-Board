package com.startinnovationhub.emmanuel.roommonitordashboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.widget.Toast.LENGTH_SHORT;


public class MainActivity extends AppCompatActivity {

    RecordsManager recordsManager = new RecordsManager();

    private static final String TAG = MainActivity.class.getSimpleName();

    private static String ADDRESS;
    private static boolean ADDRESS_FLAG;
    private static boolean CONNECTION_FLAG;
    private static final String Connection_pass = "*MRMDWBT*0000#";

    public static String EXTRA_DEVICE_ADDRESS = "extra_device_address";

    private StringBuffer bOutStringBuffer;
    private StringBuilder dataStringBuilder = new StringBuilder();

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private TextView tempView;
    private TextView humView;
    private TextView polu_levView;
    private TextView statusView;

    private BluetoothAdapter bluetoothAdapter = null;

    private BluetoothService bluetoothService = null;

    /**
     * Name of the connected device
     */
    private String bConnectedDeviceName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setIcon(R.mipmap.ic_launcher_round);
            actionBar.setTitle(R.string.app_name);
        }


        tempView = (TextView) findViewById(R.id.temp_data);
        humView = (TextView) findViewById(R.id.hum_data);
        polu_levView = (TextView) findViewById(R.id.polu_data);
        statusView = (TextView) findViewById(R.id.status_data);

        Button btnData = (Button) findViewById(R.id.btn_load);
        Button btnDiscBT = (Button)findViewById(R.id.btn_disconnect);

        btnData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showData();
            }
        });

        btnDiscBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothService.stop();
            }
        });

        //get local bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //If the adapter is null, bluetooth is not supported
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            this.finish();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (bluetoothService == null) {
            setupBT();
        }
    }

    private void setupBT() {
        Log.d(TAG, "setupBT");
        bluetoothService = new BluetoothService(this, bHandler);
        bOutStringBuffer = new StringBuffer("");
        if (ADDRESS_FLAG) {
            connectDevice(ADDRESS, false);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothService != null) {
            bluetoothService.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
       /* Log.d(TAG, "On Resume...");
        if(!bluetoothAdapter.isEnabled()){
            Intent enbleBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.startActivityForResult(enbleBT, REQUEST_ENABLE_BT);
        }else{*/
        if (bluetoothService != null) {
            if (bluetoothService.getState() == BluetoothService.STATE_NONE) {
                //start the Bluetooth communication
                bluetoothService.start();
                Log.d(TAG, "On Resume 2...");
            }
        }
        Log.d(TAG, "On Resume 3...");
        //}
    }

    private void showData() {
        Intent intent = new Intent(this, DetailActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_main; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // MenuItem item = menu.findItem(R.id.connect);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.connect: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                this.startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            }
            case R.id.discoverable: {
                //Ensure the device is discoverable by others
                ensureDiscoverable();
                return true;
            }

        }
        return false;
    }

    private void setStatus(int resId) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(resId);
        }
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(subTitle);
        }

    }

    /**
     * Makes this device discoverable for 300 seconds.
     */
    private void ensureDiscoverable() {
        if (bluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }


    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (bluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(MainActivity.this, R.string.title_not_connected, LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            bluetoothService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            bOutStringBuffer.setLength(0);
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "THe request code is: " + requestCode);
        Log.d(TAG, "THe result code is: " + resultCode);
        switch (requestCode) {

            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                Log.d(TAG, "THe result code is: " + requestCode);
                if (resultCode == RESULT_OK) {
                    // Bluetooth is now enabled, Conect to the Room Monitor device
                    setupBT();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Please, enable Bluetooth to used this App.", LENGTH_SHORT).show();
                    this.finish();
                }
                break;

            case REQUEST_CONNECT_DEVICE_INSECURE:
                //
                Log.d(TAG, "Device connection...");
                if (resultCode == Activity.RESULT_OK) {
                    ADDRESS = data.getExtras().getString(EXTRA_DEVICE_ADDRESS);
                    ADDRESS_FLAG = true;
                    connectDevice(ADDRESS, false);
                }
                break;
        }
    }


    private void connectDevice(String address, boolean secure) {
        // Get the device MAC address
        // Get the BluetoothDevice object
        Log.d(TAG, "The device address is: " + address);
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        if (device != null) {
            bluetoothService.connect(device, secure);

        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler bHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ComponentName activity = getCallingActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, bConnectedDeviceName));
                            if (!CONNECTION_FLAG) {
                                byte[] send = Connection_pass.getBytes();
                                bluetoothService.write(send);
                                Log.d(TAG, "...Message Send: " + Connection_pass);
                            }
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            CONNECTION_FLAG = false;
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);

                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    dataStringBuilder.append(readMessage);
                    int endOfLineIndex = dataStringBuilder.indexOf("\r\n");
                    if (endOfLineIndex > 0) {
                        Log.d(TAG, "...Message Received: " + dataStringBuilder.toString() + " Byte: " + msg.arg1);
                        String rMsg = dataStringBuilder.substring(0, endOfLineIndex);
                        dataStringBuilder.delete(0, dataStringBuilder.length());
                        if (rMsg.equals(Connection_pass)) {
                            CONNECTION_FLAG = true;
                            Toast.makeText(MainActivity.this, "Ready to receive data from the room monitoring device!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            if (!rMsg.equals(Connection_pass) && CONNECTION_FLAG) {
                                boolean ft1 = rMsg.contains("*");
                                boolean ft2 = rMsg.contains("#");
                                if ((ft1) && (ft2)) {
                                    int fCh = rMsg.indexOf("*");
                                    int sCh = rMsg.indexOf("#");

                                    String rData = rMsg.substring(fCh + 1, sCh);
                                    showRecord(rData);
                                }
                            }
                        }
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    bConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(MainActivity.this, "Connected to "
                                + bConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(MainActivity.this, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }


    };


    private void showRecord(String data) {
        Log.d(TAG, "Data update: " + data);
        String[] str = data.split("&");
        if(str.length > 3) {
            tempView.setText(str[0].concat("°C"));
            humView.setText(str[1].concat("%"));
            polu_levView.setText(str[2].concat("%"));
            statusView.setText(str[3]);
            Calendar calendar = Calendar.getInstance();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa");
            String datetime = simpleDateFormat.format(calendar.getTime());
            Log.d(TAG, "On Create save data...");
            String record = data.concat("&").concat(datetime);
            recordsManager.saveData(this, record);
            String see = recordsManager.readRecord(this);
            Log.d(TAG, "On Create read data:::   " + see);
        }
    }

}
