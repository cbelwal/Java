package com.dasmic.android.lib.calllog.Data;

/**
 * Created by Chaitanya Belwal on 3/19/2017.
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
