package com.dasmic.android.lib.apk.ViewModel;

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;

import com.dasmic.android.lib.apk.Data.DataPackageDisplay;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 4/16/2017.
 */

public class ViewModelStopProcess {
    Activity _activity;
    ActivityManager _activityManager;

    public ViewModelStopProcess(Activity activity){
        _activity=activity;
        _activityManager = (ActivityManager)_activity.getSystemService(
                Context.ACTIVITY_SERVICE);
    }

    public void stopUserTasks(
            ArrayList<DataPackageDisplay> allDpd) {
        //get a list of installed apps.
        for (DataPackageDisplay dpd : allDpd) {
            stopUserTask(dpd);
        }
    }

    public int stopUserTask(DataPackageDisplay dpd){
        if(!dpd.getIsSystemApp()) {
            //Make sure it is not current package name
            if(!dpd.getPackageName().equals(_activity.getPackageName())) {
                try {
                    _activityManager.killBackgroundProcesses(dpd.getPackageName());
                    return 1;
                }
                catch (Exception ex){
                    SupportFunctions.InfoLog("ViewModelStopProcessing","StopUserTask", "Error:"+ex.getMessage());
                }
            }
        }
        return 0;
    }

    public int stopSystemHardware(){
        int count=0;
        count += stopBluetooth();
        count += stopWireless();
        return count;
    }

    public int stopBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            boolean isEnabled = bluetoothAdapter.isEnabled();
            if (isEnabled && bluetoothAdapter.disable())
                return 1;
        }
        catch(Exception ex){
            SupportFunctions.InfoLog("ViewModelStopProcessing","StopBluetooth", "Error:"+ex.getMessage());
        }
        return 0;
    }

    public int stopWireless() {
        WifiManager wifiManager = (WifiManager)
                _activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        try {
            if (wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(false);
                return 1;
            }
        }
        catch(Exception ex){
            SupportFunctions.InfoLog("ViewModelStopProcessing","StopWireless", "Error:"+ex.getMessage());
        }
        return 0;
    }

    public static int getBatteryPercentage(Context context) {
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        float batteryPct = level / (float) scale;

        return (int) (batteryPct * 100);
    }
}
