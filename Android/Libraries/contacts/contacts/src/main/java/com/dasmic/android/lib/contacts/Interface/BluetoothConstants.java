package com.dasmic.android.lib.contacts.Interface;


/**
 * Created by Chaitanya Belwal on 1/31/2016.
 */
public interface BluetoothConstants {
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_CONNECTION_FAILED = 5;
    public static final int MESSAGE_DEVICE_CONNECTING = 6;
    public static final int MESSAGE_DEVICE_CONNECTED = 7;
    public static final int MESSAGE_SCAN_DEVICES = 9;
    public static final int MESSAGE_SCAN_COMPLETE = 11;
    public static final int MESSAGE_CONNECTION_LOST = 13;
    public static final int MESSAGE_TESTDATA_SENT = 15;
    public static final int REQUEST_ENABLE_BT = 103;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "ui_btlistviewitem";

    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
}

