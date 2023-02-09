package com.dasmic.android.lib.contacts.ViewModel;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.dasmic.android.lib.contacts.Data.DataBluetoothDevice;
import com.dasmic.android.lib.contacts.Interface.BluetoothConstants;
import com.dasmic.android.lib.contacts.R;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


/**
 * Created by Chaitanya Belwal on 1/30/2016.
 */
public class ViewModelBluetoothConnection {
    BluetoothAdapter _btAdapter;
    Activity _activity;
    OnDeviceDiscoveryFinishedListener _listnerDevDiscFinished;
    OnNewDeviceDiscoveredListener _listnerNewDevDisc;
    private int mState;

    // Constants that indicate the current connection state
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private Handler mHandler;

    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("0ec75ee2-8570-4de2-883b-07e5ebf6e668");
    private static final String TAG = "dasmic.contacts";
    private static final String NAME_SECURE = "dasmic.contacts";

    public ViewModelBluetoothConnection(Activity activity,
                                        Handler handler,
                                        final OnNewDeviceDiscoveredListener listNewDeviceDiscovered,
                                        final OnDeviceDiscoveryFinishedListener listDevDiscoveryFinished){
        _activity=activity;
        _listnerDevDiscFinished =listDevDiscoveryFinished;
        _listnerNewDevDisc =listNewDeviceDiscovered;
        mHandler = handler;
        setupBlueTooth();
        SupportFunctions.DebugLog("BTVM",
                "Constructor", "Calling Listen Mode");
        startListenMode();
    }

    @Override
    public void finalize(){
        SupportFunctions.DebugLog("BTVM","Finalize()","Finalize Entry #1");
        if(mConnectThread!=null){
            mConnectThread.cancel();
            mConnectThread=null;
        }

        SupportFunctions.DebugLog("BTVM","Finalize()","Finalize Entry #2");
        if(mConnectedThread!=null){
            mConnectedThread.cancel();
            mConnectedThread=null;
        }
        SupportFunctions.DebugLog("BTVM","Finalize()","Finalize Entry #3");
        // Unregister broadcast listeners
        if(_activity != null) {
            _activity.unregisterReceiver(mReceiver);
            _activity = null;
        }

        SupportFunctions.DebugLog("BTVM","Finalize()","Finalize Entry #4");
        //Accept Thread should be last
        if(mAcceptThread !=null){
            mAcceptThread.cancel();
            mAcceptThread =null;
        }

        //Do this last - VERY Imp
        if (_btAdapter != null) {
            _btAdapter.cancelDiscovery();
            _btAdapter=null;
        }
        SupportFunctions.DebugLog("BTVM","Finalize()","Finalize Exit");
    }

    /**
     * Makes this device discoverable.
     */
    public void ensureDiscoverable() {
        if (_btAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            _activity.startActivity(discoverableIntent);
        }
    }

    private void connectDevice(String devMAC,boolean secure) {
        // Get the device MAC address
        BluetoothDevice device = _btAdapter.getRemoteDevice(
                devMAC);
    }

    private void setupBlueTooth(){
        _btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(_btAdapter==null){
            SupportFunctions.DebugLog("VMBTConnection","setupBlutooth","btAdapter is Null");
            throw new RuntimeException("BluetoothAdapter.getDefaultAdapter() returned null adapter");
        }

        if (!_btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            _activity.startActivityForResult(enableIntent,
                    BluetoothConstants.REQUEST_ENABLE_BT);
        }
        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        _activity.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        _activity.registerReceiver(mReceiver, filter);
    }

    public void CancelDiscovery(){
        try {
            if (_btAdapter.isDiscovering())
                _btAdapter.cancelDiscovery();
        }
        catch(Exception ex){ //Exception reported on May, 2016
            SupportFunctions.InfoLog("VMModelBluetooth",
                    "CancelDiscovery","Exception:"+ex.getMessage());
        }
    }

    //Events to consumer
    public interface OnDeviceDiscoveryFinishedListener {
        void onDeviceDiscoveryFinished();
    }

    public interface OnNewDeviceDiscoveredListener {
        void onNewDeviceDiscovered(String deviceName,
                                   String deviceMAC);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    _listnerNewDevDisc.onNewDeviceDiscovered(
                            device.getName(),
                            device.getAddress());
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(
                    action)) {
                sendHandlerMessage(BluetoothConstants.MESSAGE_SCAN_COMPLETE,
                        0,"");
                _listnerDevDiscFinished.onDeviceDiscoveryFinished();
            }
        }
    };

    public void doDeviceDiscovery() {
        // If we're already discovering, stop it
        CancelDiscovery();
        sendHandlerMessage(BluetoothConstants.MESSAGE_SCAN_DEVICES,0,"");
        // Request discover from BluetoothAdapter
        _btAdapter.startDiscovery();
    }



    public void sendTestData(){
        String s= "Test*Message*123*";
        byte[] data=s.getBytes();
        write(data);
        sendHandlerMessage(BluetoothConstants.MESSAGE_TESTDATA_SENT,0,"");
    }

    public void sendData(String s){
        byte[] data=s.getBytes();
        write(data);
    }

    public ArrayList<DataBluetoothDevice> getListOfPairedDevices(){
        Set<BluetoothDevice> pairedDevices =
                _btAdapter.getBondedDevices();

        ArrayList<DataBluetoothDevice> devPaired=new ArrayList<>();
        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                DataBluetoothDevice dpd=new DataBluetoothDevice(
                        device.getName(),
                        device.getAddress()
                );

                devPaired.add(dpd);
            }
        } else {
            DataBluetoothDevice dpd=new DataBluetoothDevice(
                    _activity.getResources().getText
                            (R.string.message_bt_pair_none).toString(),
                    "");
            devPaired.add(dpd);
        }
        return devPaired;
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        //Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        sendHandlerMessage(BluetoothConstants.MESSAGE_STATE_CHANGE,
                state,"");
    }

    public int getState(){
        return  mState;
    }

    private void connectionFailed(String devName) {
        sendHandlerMessage(BluetoothConstants.MESSAGE_CONNECTION_FAILED,
                0,devName);
        SupportFunctions.DebugLog("BTVM",
                "connectionFailed", "Calling Listen Mode");
        startListenMode(); //Switch to listen mode
    }

    public synchronized void connectToDevice
            (String address) {
        // Cancel discovery because it's costly and we're about to connectToDevice
        CancelDiscovery();

        // Get the BluetoothDevice object

        BluetoothDevice device = _btAdapter.getRemoteDevice(address);

        SupportFunctions.DebugLog("VMBluetooth",
                "Connect", "connectToDevice to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == BluetoothConstants.STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel any thread currently listening
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        // Start the thread to connectToDevice with the given device
        mConnectThread = new ConnectThread(device, true);
        mConnectThread.start();
        setState(BluetoothConstants.STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket,
                                       BluetoothDevice device,
                                       final String socketType) {
        Log.d(TAG, "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connectToDevice to one device
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        setState(BluetoothConstants.STATE_CONNECTED);

        //Send the name of the connected device back to the UI Activity
        sendHandlerMessage(BluetoothConstants.MESSAGE_DEVICE_CONNECTED,
                0, device.getName());

    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
        sendHandlerMessage(BluetoothConstants.MESSAGE_CONNECTION_LOST,
                0,"");
        SupportFunctions.DebugLog("BTVM",
                "connectionLost", "Calling Listen Mode");
        startListenMode();
    }

    /**
     * Start the chat service. Specifically startListenMode AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void startListenMode() {
        Log.d(TAG, "startListenMode");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(BluetoothConstants.STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread(true);
            mAcceptThread.start();
        }

    }

    private void sendHandlerMessage(int messageIdx,
                                    int arg1,
                                    String devName){
        Message msg = mHandler.obtainMessage(
                messageIdx,arg1,-1);

        Bundle bundle = new Bundle();
        bundle.putString(BluetoothConstants.DEVICE_NAME, devName);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != BluetoothConstants.STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }
    //------------------------------------------
    // Private classes beyond this point
    //------------------------------------------

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device,
                             boolean secure) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = "Secure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                    tmp = device.createRfcommSocketToServiceRecord(
                            MY_UUID_SECURE);

            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            //Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);
            sendHandlerMessage(BluetoothConstants.MESSAGE_DEVICE_CONNECTING,
                    0, mmDevice.getName());
            // Always cancel discovery because it will slow down a connection
            CancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                connectionFailed(mmDevice.getName());
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel() {
            try {
                if(mmSocket != null)
                    mmSocket.close();
            } catch (IOException e) {
                //Log.e(TAG, "close() of connectToDevice " + mSocketType + " socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private boolean mThreadCancel;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            mThreadCancel=false;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[2048]; //Change size of buffer
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    // Send the obtained bytes to the UI Activity
                    //Do not use send handler function
                    mHandler.obtainMessage(
                            BluetoothConstants.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                    buffer = new byte[2048]; //Clean the buffer since it messes it up

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    // Start the service over to restart listening mode
                    if(!mThreadCancel) connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                //mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
                //        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mThreadCancel=true;
                if(mmSocket != null)
                    mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connectToDevice socket failed", e);
            }

        }
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread(boolean secure) {
            BluetoothServerSocket tmp = null;
            mSocketType = "Secure";
            // Create a new listening server socket
            try {

                if(_btAdapter != null)
                    tmp = _btAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,
                            MY_UUID_SECURE);
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            SupportFunctions.DebugLog("BTVM", "AcceptThread::run()", "** In Run");

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != BluetoothConstants.STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    if(mmServerSocket != null)
                        socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (this) {
                        switch (mState) {
                            case BluetoothConstants.STATE_LISTEN:
                            case BluetoothConstants.STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice(),
                                        mSocketType);
                                break;
                            case BluetoothConstants.STATE_NONE:
                            case BluetoothConstants.STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);

        }

        public void cancel() {
            try {
                if(mmServerSocket != null) {
                    mmServerSocket.close();
                    SupportFunctions.DebugLog("BTVM", "AcceptThread::cancel()", "** Cancel Success");
                }
            } catch (IOException e) {
                SupportFunctions.DebugLog("BTVM","AcceptThread::cancel()","*** Cancel Failed ***");
            }
        }
    }

}
