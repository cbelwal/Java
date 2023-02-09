package com.dasmic.android.lib.calllog.Extension;


import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.dasmic.android.lib.calllog.Enum.BluetoothConstants;

/**
 * Created by Chaitanya Belwal on 4/1/2017.
 */

public class CallLogContentObserver extends ContentObserver {
    Handler mHandler;
    public CallLogContentObserver(Handler h) {
        super(h);
        mHandler =h;
    }

    @Override
    public boolean deliverSelfNotifications() {
        return false;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Message msg = mHandler.obtainMessage(0);
        //Bundle bundle = new Bundle();
        //bundle.putString(BluetoothConstants.DEVICE_NAME, devName);
        //msg.setData(bundle);
        mHandler.sendMessage(msg);
        // here you call the method to fill the list
    }
}
