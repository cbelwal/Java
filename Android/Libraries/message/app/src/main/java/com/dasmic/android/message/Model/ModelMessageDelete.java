package com.dasmic.android.lib.message.Model;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;

import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 8/19/2017.
 */

public class ModelMessageDelete {
    public static final int DELETE_COUNT_UPDATE=0;
    Context _context;
    private final Handler mHandler;
    ContentResolver _cr;

    public ModelMessageDelete(Context context
            , Handler handler){
        _context=context;
        mHandler=handler;
        _cr= _context.getContentResolver();
    }

    private void sendHandlerMessage(int messageIdx,
                                    int arg1){
        if(mHandler==null) return;
        Message msg = mHandler.obtainMessage(
                messageIdx,arg1,-1);

        mHandler.sendMessage(msg);
    }

    public int DeleteContacts(ArrayList<Long> rawContactIDList){
        Log.i("CKIT", "Model::DeleteContacts");
        int retVal=0;
        int mmCount=0;
        int idx=0;
        for(idx=0;idx<rawContactIDList.size();idx++) {
            retVal= DeleteMessage(rawContactIDList.get(idx));
            if(retVal==1) mmCount++;
            sendHandlerMessage(DELETE_COUNT_UPDATE,
                    mmCount);
        }
        return mmCount;
    }

    public int DeleteMessage(Long rawContactID){
        Log.i("CKIT", "Model::DeleteContacts");
        int retVal=0;

        String filterQuery= ContactsContract.RawContacts._ID + "=?";
        String[] idValues=new String[1];
        idValues[0] = rawContactID.toString();
        Log.i("CKIT", "Delete Operation ID:" +
                rawContactID.toString());

        //Delete one at a time
        try {
            retVal = _cr.delete(ContactsContract.RawContacts.CONTENT_URI,
                    filterQuery, idValues);
            SupportFunctions.DebugLog("ModelDelete",
                    "Delete", String.format("Retval:" + retVal));

        } catch (Exception ex) {
            Log.i("CKIT", "Delete Operation Error: " + ex.getMessage());
            //Continue deleting others
        }
        return retVal;
    }
}
