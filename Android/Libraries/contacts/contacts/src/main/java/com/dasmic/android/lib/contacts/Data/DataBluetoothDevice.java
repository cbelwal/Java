package com.dasmic.android.lib.contacts.Data;

/**
 * Created by Chaitanya Belwal on 2/9/2016.
 */
public class DataBluetoothDevice {
    String _name;
    String _mac;

    public DataBluetoothDevice(String name,
                               String mac){
        _name=name;
        _mac=mac;
    }

    public String get_mac() {
        return _mac;
    }

    public String get_name() {
        return _name;
    }

    @Override
    public String toString(){
        return get_name();
    }
}
