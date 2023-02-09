package com.dasmic.android.lib.calllog.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.dasmic.android.lib.calllog.Data.DataBluetoothDevice;
import com.dasmic.android.lib.calllog.Data.DataCallLogDisplay;
import com.dasmic.android.lib.calllog.Enum.BluetoothConstants;
import com.dasmic.android.lib.calllog.R;
import com.dasmic.android.lib.calllog.ViewModel.ViewModelBluetoothConnection;
import com.dasmic.android.lib.calllog.ViewModel.ViewModelCallLogDisplay;
import com.dasmic.android.lib.calllog.ViewModel.ViewModelExport;
import com.dasmic.android.lib.calllog.ViewModel.ViewModelImport;
import com.dasmic.android.lib.support.Extension.ProgressDialogHorizontal;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 3/19/2017.
 */

public class ActivityBluetooth extends AppCompatActivity {
    private final String tagUpdateDone="Update Done";
    protected String _appFolder;
    ViewModelImport _vmImport;
    boolean _updateDone;
    boolean _isForWhatsApp;
    ProgressDialogHorizontal _pdHori;

    String _combRecvMsg="";

    ArrayAdapter<DataBluetoothDevice> mNewDevicesArrayAdapter;
    String _connectedToDevice;

    String [] _recvContacts;
    int _totContactsToBeRecv;
    int _recvContactsCount;
    int _createdContactsCount;
    ViewModelImport _vmi;
    int _oldOrientation;
    int mCreateContactError;

    ViewModelBluetoothConnection _vmBTC;
    private boolean _isReceivingData;
    private final String SEP="~~";
    private final String EOT ="END:TRANS"+SEP;
    private final String SOT ="START:TRANS"+SEP;
    private final String EOCT ="END:CONTACTTRANS"+SEP;
    private final long MAXDELAY =150000; //2.5minutes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_bluetooth_cl);
        setLocalVariablesAndEventHandlers(); //Do not move this from here
        _appFolder = getString(R.string.app_folder);
        if(savedInstanceState != null)
            _updateDone = savedInstanceState.getBoolean(
                    tagUpdateDone);


        //Never let goto sleep
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initializeDisplay();
    }

    //This is very important since if Activity is restarted in
    //screen change, there is a major conflict with ReloadListView
    //This happens when ReloadListView is called along
    //with ActivityLoad and happens when screen orientation
    //is changed along with some update
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(_pdHori!=null){
            _pdHori.dismiss();
            _pdHori=null;
        }
        // Make sure we're not doing discovery anymore
        _vmBTC.finalize();
        _vmBTC=null;
        SupportFunctions.DebugLog("BT","onDestroy","In onDestroy");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putBoolean(tagUpdateDone,
                _updateDone);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
    }

    private void onButtonCancel() {
        Intent resultData = new Intent();
        if (_updateDone)
            setResult(Activity.RESULT_OK, resultData);
        else
            setResult(Activity.RESULT_CANCELED, resultData);
        finish();
    }

    private void onButtonSend() {
        //Start Contact transfer
        SendContacts();
        //_vmBTC.sendTestData();
    }

    private void SendContacts(){
        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {
            Activity _context;
            ArrayList<DataCallLogDisplay> _selContacts;
            int _sentCount;
            int _sentErrorCount;
            int _byteCount;

            BasicAsyncTask(Activity context)
            {
                super();
                _context = context;
                _sentCount=0;
            }

            @Override
            protected Void doInBackground(Void... params) {
                long delay;
                ViewModelExport vme =
                        new ViewModelExport(_context,
                                _appFolder);
                String vcard;
                ArrayList<DataCallLogDisplay> dcTransfers;

                //dcTransfers =
                //            vme.getDataCallLogDisplays(_selContacts,7);//Only get phone, email, address

                for(DataCallLogDisplay dcld:_selContacts){
                    try {
                        vcard=dcld.getCLMString();
                        _byteCount=vcard.length();
                        _sentCount++;
                        this.publishProgress();
                        Thread.sleep(50); //To update progress
                        _vmBTC.sendData(vcard);
                        //Deliberate delay based on size of data to sync up buffers
                        //Not more than MAXDELAY ms delay
                        delay=vcard.length()>MAXDELAY?MAXDELAY:vcard.length();

                        Thread.sleep(delay);
                        SupportFunctions.DebugLog("BT",
                                "SendData", "Delay:" +
                                        String.valueOf(delay));
                        _vmBTC.sendData(EOCT); //End of contact trans
                        Thread.sleep(50);
                    }
                    catch(Exception ex)
                    {
                        SupportFunctions.DebugLog("BT",
                                "SendContacts","Error:"+ex.getMessage());
                        _sentErrorCount++;
                        _vmBTC.sendData(EOCT);
                    }
                }
                _vmBTC.sendData(EOT);//send end of message, even if exception
                return null;
            }

            @Override
            protected void onPostExecute(Void param) {
                super.onPostExecute(param);
                Log.i("CKIT", "thread PostExecute");

                if(_pdHori != null)
                    _pdHori.dismiss();
                SupportFunctions.DisplayToastMessageShort(_context,
                        getString(R.string.message_bt_sent_complete)+
                                String.valueOf(_sentCount));
                if(_sentErrorCount>0) {
                    SupportFunctions.DisplayToastMessageLong(_context,
                            getString(R.string.message_bt_sent_error)+
                                    String.valueOf(_sentErrorCount));
                }

                setRequestedOrientation(_oldOrientation);

            }

            @Override
            protected void onPreExecute() {
                SupportFunctions.DebugLog("BT", "SendData",
                        "in Send::PreExecute");
                super.onPreExecute();
                _sentErrorCount=0;
                _oldOrientation= getRequestedOrientation();
                setRequestedOrientation(
                        ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

                ViewModelCallLogDisplay vmContactsDisplay =
                        ViewModelCallLogDisplay.getInstance(_context);
                _selContacts = vmContactsDisplay.getCheckedItems();


                if(_selContacts.size()==0){
                    SupportFunctions.DisplayToastMessageShort(_context,
                            getString(R.string.message_bt_no_contacts));
                    return;
                }

                //Send start trans message
                _vmBTC.sendData(SOT+String.valueOf(
                        _selContacts.size())+SEP);
                _pdHori=
                        new ProgressDialogHorizontal(_context,
                                getString(R.string.progressbar_prepare_data));
                _pdHori.setMax(_selContacts.size());
                _pdHori.show();
            }

            @Override
            protected void onProgressUpdate(Void... params) {
                _pdHori.setProgress(_sentCount);
                _pdHori.setMessage(getString(
                        R.string.progressbar_bt_send_data)+
                        " (" +
                        String.valueOf(_byteCount) + " " +
                        getString(R.string.progressbar_bt_bytes) + ")"
                        + "...");
            }
        }

        AsyncTask<Void,Void,Void> aTask= new BasicAsyncTask(this);
        try {
            if(_vmBTC.getState() == BluetoothConstants.STATE_CONNECTED) {
                //Do not bloc, else progress dialog will not come up

            }
            else{
                SupportFunctions.DisplayToastMessageLong(this,
                        getString(R.string.message_bt_not_connected));
            }
            aTask.execute();
        }
        catch(Exception ex){
            Log.i("CKIT", "Exception in Export::doInBackground"
                    + ex.getMessage());
            throw new RuntimeException(ex);
        }
        Log.i("CKIT", "Thread finished execution");

    }

    private void onButtonScan() {
        mNewDevicesArrayAdapter.clear();
        _vmBTC.doDeviceDiscovery();
    }

    //Set defaults
    private void initializeDisplay() {
        //Display selected contacts in UI
        ViewModelCallLogDisplay vmContactsDisplay=
                ViewModelCallLogDisplay.getInstance(this);
        int count= vmContactsDisplay.getCheckedItems().size();
        TextView tv = (TextView) findViewById(R.id.textBtContactCount);
        tv.setText(getString(R.string.text_bt_number_of_contacts) +
                String.valueOf(count));

        setPairedBluetoothDevices();
    }


    private void onMenuMakeDesc() {
        _vmBTC.ensureDiscoverable();
    }

    private void setPairedBluetoothDevices() {
        ArrayList<DataBluetoothDevice> devPaired=
                _vmBTC.getListOfPairedDevices();

        ArrayAdapter<DataBluetoothDevice>
                pairedDevicesArrayAdapter =
                new ArrayAdapter<DataBluetoothDevice>(this,
                        R.layout.ui_btlistviewitem_cl,devPaired);

        // Find and set up the ListView for paired devices
        ListView pairedListView = (ListView) findViewById(
                R.id.listViewPairedDevices);

        // Get a set of currently paired devices
        pairedListView.setAdapter(pairedDevicesArrayAdapter);

        Log.i("CKIT", "Thread finished execution");
    }

    private final ViewModelBluetoothConnection.OnNewDeviceDiscoveredListener
            newDevDiscoveredReceiver = new
            ViewModelBluetoothConnection.OnNewDeviceDiscoveredListener(){

                public void onNewDeviceDiscovered(String devName,String devMAC) {
                    DataBluetoothDevice dpd=new DataBluetoothDevice(devName,
                            devMAC);
                    // If it's already paired, skip it, because it's been listed already
                    mNewDevicesArrayAdapter.add(dpd);
                }
            };

    private final ViewModelBluetoothConnection.OnDeviceDiscoveryFinishedListener
            newDiscoveryFinished = new
            ViewModelBluetoothConnection.OnDeviceDiscoveryFinishedListener() {

                public void onDeviceDiscoveryFinished() {
                    if (mNewDevicesArrayAdapter.getCount() == 0) {
                        DataBluetoothDevice dpd=new
                                DataBluetoothDevice(getResources().getText(
                                R.string.message_bt_new_none).toString(),
                                "");
                        mNewDevicesArrayAdapter.add(dpd);
                    }
                }
            };




    private AdapterView.OnItemClickListener listViewItemSelected
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v,
                                int arg2, long arg3) {

            _vmBTC.CancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            DataBluetoothDevice dbd =
                    (DataBluetoothDevice) av.getItemAtPosition(arg2);
            String address = dbd.get_mac();

            try {
                _vmBTC.connectToDevice(address);
            }
            catch(Exception ex){
                SupportFunctions.DisplayToastMessageLong(getActivity(),
                        getString(R.string.message_bt_conn_failed)+ex.getMessage());
            }
        }
    };

    private void setupBlueToothListViews(){
        mNewDevicesArrayAdapter = new ArrayAdapter<DataBluetoothDevice>
                (this, R.layout.ui_btlistviewitem_cl);

        ListView pairedListView = (ListView) findViewById(
                R.id.listViewPairedDevices);
        pairedListView.setOnItemClickListener(listViewItemSelected);

        // Find and set up the ListView for newly discovered devices
        ListView newDevicesListView = (ListView) findViewById(
                R.id.listViewNewDevices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(listViewItemSelected);
    }

    private void setLocalVariablesAndEventHandlers() {
        _vmImport = new ViewModelImport(this,_appFolder,null );

        try {
            _vmBTC = new ViewModelBluetoothConnection(this,
                    mHandler,
                    newDevDiscoveredReceiver,
                    newDiscoveryFinished);
            setupBlueToothListViews();
        }
        catch(Exception ex){
            SupportFunctions.DisplayToastMessageLong(this,getString(
                    R.string.message_bt_adapter_error));
            finish();
        }

        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonCancel();
            }
        });

        Button btnConfirm = (Button) findViewById(R.id.btnSend);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonSend();
            }
        });

        Button btnBrowse = (Button) findViewById(R.id.btnScan);
        btnBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonScan();
            }
        });

    }

    //Add Items to the action bar here
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(
                R.menu.menu_bluetooth_cl, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id==R.id.action_make_disc) {
            onMenuMakeDesc();
        }

        return super.onOptionsItemSelected(item);
    }

    //Load files
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String fileName = "";
        String sFileSize="";
        Uri uri=null;

        if (requestCode == BluetoothConstants.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                initializeDisplay(); //Redo everything
            } else {
                // User did not enable Bluetooth or an error occurred
                SupportFunctions.DisplayToastMessageLong(getActivity(),
                        getString(R.string.message_bt_not_enabled));
                onButtonCancel(); //Close this no point in continuing
            }
        }

    }

    /**
     * Updates the status on the action bar.
     *
     */
    private void setStatus(String msgStatus) {
        FragmentActivity activity = this;
        if (null == activity) {
            return;
        }
        ActionBar actionBar = getSupportActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(msgStatus);
        //setProgressBarIndeterminateVisibility(false);
        //setTitle(resId);
    }

    private Activity getActivity(){
        return this;
    }

    private void createReceivedContact(int idx){
        class BasicAsyncTask extends AsyncTask<Void, Void, Void> {
            int _idx;

            BasicAsyncTask(int idx)
            {
                super();
                _idx=idx;
            }

            @Override
            protected Void doInBackground(Void... params) {
                try{
                    if(_idx>=0){
                            _vmi.createContactFromCLM(
                                    _recvContacts[_idx]);
                    }
                    this.publishProgress();
                    Thread.sleep(50);
                }
                catch(Exception ex) {
                    SupportFunctions.DebugLog("BT",
                            "CreateContacts","Error:"+
                                    ex.getMessage()+"\r\n"+"Value:"+
                                    _recvContacts[_idx]);
                    mCreateContactError++;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void param) {
                super.onPostExecute(param);

                if(_idx < 0) return;//Dont do anything for special condition
                _updateDone=true;
                _createdContactsCount++;

                SupportFunctions.DebugLog("BT", "createContacts",
                        "Created Contact: " +
                                String.valueOf(_createdContactsCount));

                //Free up memory
                _recvContacts[_idx]=""; //Show be done here as _idx is local variable
                if(_createdContactsCount+mCreateContactError >=
                        _totContactsToBeRecv) {
                    cleanUpRecvOp();
                }
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                _oldOrientation= getRequestedOrientation();
                setRequestedOrientation(
                        ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

                if(_pdHori == null) {
                    initProgressBar();
                }
                else if (!_pdHori.isShowing()) {
                    initProgressBar();
                }

            }

            private void initProgressBar(){
                _pdHori = new ProgressDialogHorizontal(getActivity(),
                        getString(R.string.progressbar_bt_recv_data));
                _pdHori.setMax(_totContactsToBeRecv);
                _pdHori.show();
            }

            @Override
            protected void onProgressUpdate(Void... params) {
                //Update is being on progress bar display
                if(_createdContactsCount==0){
                    _pdHori.setMessage(getString(
                            R.string.progressbar_bt_recv_data));
                }
                else {
                    if(_pdHori.isShowing()) {
                        _pdHori.setProgress(_createdContactsCount);
                        _pdHori.setMessage(getString(
                                R.string.progressbar_bt_create_contacts)+
                                "...");
                    }
                }
            }
        }

        AsyncTask<Void,Void,Void> aTask= new BasicAsyncTask(idx);
        try {
            //Do not bloc, else progress dialog will not come up
            aTask.execute();
        }
        catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    private void cleanUpRecvOp(){
        //Remove EOT and SOT
        SupportFunctions.DebugLog("BT",
                "cleanUpRevMessage:", _combRecvMsg);
        if(_pdHori != null) {
            if(_pdHori.isShowing()) {
                _pdHori.dismiss();
                setRequestedOrientation(_oldOrientation);
                if(_createdContactsCount > 0)
                    SupportFunctions.DisplayToastMessageShort(getActivity(),
                            getString(R.string.message_bt_create_complete)+
                                    String.valueOf(_createdContactsCount));
                if(mCreateContactError > 0)
                    SupportFunctions.DisplayToastMessageLong(getActivity(),
                            getString(R.string.message_bt_recv_error)+
                                    String.valueOf(mCreateContactError));
                //Do not null _recvContacts
            }
        }
    }

    //Message will be in format:SOT SEP count SEP
    //eg: SOR**count**
    private void initForRecvContacts(String message)
    {
        String [] data=message.split(SEP);

        if(data.length>=2){
            _totContactsToBeRecv =
                    Integer.valueOf(data[1]);
            _recvContacts = new String[_totContactsToBeRecv];
            //Init Values
            for(int ii=0;ii<_recvContacts.length;ii++) {
                _recvContacts[ii] = "";
            }
            _recvContactsCount=0;
            _createdContactsCount=0;
            mCreateContactError=0;
            _vmi=new ViewModelImport(this,_appFolder,null);
        }
        //init array
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //FragmentActivity activity = getActivity();
            String deviceName="";
            switch (msg.what) {
                case  BluetoothConstants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothConstants.STATE_CONNECTED:
                            setStatus(getString(R.string.message_bt_status_connected));
                            break;
                        case BluetoothConstants.STATE_CONNECTING:
                            setStatus(getString(R.string.message_bt_status_connecting));
                            break;
                        case BluetoothConstants.STATE_LISTEN:
                        case BluetoothConstants.STATE_NONE:
                            setStatus(getString(
                                    R.string.message_bt_status_listening));
                            //Make sure progress bar is not shown
                            cleanUpRecvOp();
                            break;
                    }
                    break;
                case BluetoothConstants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;
                case BluetoothConstants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);

                    //Message Transmission started
                    if(readMessage.contains(SOT)){
                        //Set system for contacts to be received
                        initForRecvContacts(readMessage);
                        createReceivedContact(-1);
                    }
                    else if(readMessage.contains(EOCT)){
                        createReceivedContact(_recvContactsCount);
                        _recvContactsCount++; //Do this operation only here else we run into thread sync issues
                    }
                    else if(readMessage.contains(EOT)){
                        SupportFunctions.DisplayToastMessageLong(
                                getActivity(),
                                getString(
                                        R.string.message_bt_recv_complete));
                    }
                    else{
                        if(_recvContacts != null &&
                                _recvContacts.length>_recvContactsCount) {
                            _recvContacts[_recvContactsCount] =
                                    _recvContacts[_recvContactsCount] +
                                            readMessage;
                        }
                    }
                    readMessage=null;
                    readBuf=null;
                    break;
                case BluetoothConstants.MESSAGE_DEVICE_CONNECTED:
                    // save the connected device's name
                    deviceName = msg.getData().getString(
                            BluetoothConstants.DEVICE_NAME);
                    _connectedToDevice=deviceName;
                    SupportFunctions.DisplayToastMessageLong(getActivity(),
                            getString(R.string.message_bt_connected)+deviceName);
                    break;
                case BluetoothConstants.MESSAGE_DEVICE_CONNECTING:
                    // save the connected device's name
                    deviceName = msg.getData().getString(
                            BluetoothConstants.DEVICE_NAME);
                    SupportFunctions.DisplayToastMessageLong(getActivity(),
                            getString(R.string.message_bt_connecting) +
                                    deviceName );
                    break;
                case BluetoothConstants.MESSAGE_CONNECTION_FAILED:
                    // save the connected device's name
                    deviceName = msg.getData().getString(
                            BluetoothConstants.DEVICE_NAME);
                    SupportFunctions.DisplayToastMessageLong(getActivity(),
                            getString(R.string.message_bt_conn_failed)+
                                    deviceName);
                    break;
                case BluetoothConstants.MESSAGE_SCAN_DEVICES:
                    SupportFunctions.DisplayToastMessageLong(getActivity(),
                            getString(R.string.message_bt_scan_devices) +
                                    deviceName );
                    setStatus(getString(R.string.message_bt_scan_devices));
                    break;
                case BluetoothConstants.MESSAGE_SCAN_COMPLETE:
                    SupportFunctions.DisplayToastMessageShort(getActivity(),
                            getString(R.string.message_bt_scan_complete) +
                                    deviceName);
                    setStatus(getString(
                            R.string.message_bt_status_listening));
                    break;
                case BluetoothConstants.MESSAGE_TESTDATA_SENT:
                    SupportFunctions.DisplayToastMessageShort(getActivity(),
                            getString(R.string.message_bt_testdata_sent) +
                                    deviceName);
                    break;


            }
        }
    };
}
