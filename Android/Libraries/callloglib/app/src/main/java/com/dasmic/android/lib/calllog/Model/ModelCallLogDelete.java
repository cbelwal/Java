package com.dasmic.android.lib.calllog.Model;

/**
 * Created by Chaitanya Belwal on 1/22/2017.
 */

import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;
import com.dasmic.android.lib.support.Static.SupportFunctions;
import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 9/15/2015.
 */
public class ModelCallLogDelete {
    Context _context;
    private final Handler mHandler;
    public static final int DELETE_COUNT_UPDATE=0;
    ContentResolver _cr;

    public ModelCallLogDelete(Context context
            ,Handler handler){
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

    public int deleteMultiple(ArrayList<Long> rawContactIDList){
        Log.i("CKIT", "Model::DeleteContacts");
        int retVal=0;
        int mmCount=0;
        int idx=0;
        for(idx=0;idx<rawContactIDList.size();idx++) {
            retVal=deleteSingle(rawContactIDList.get(idx));
            if(retVal==1) mmCount++;
            sendHandlerMessage(DELETE_COUNT_UPDATE,
                    mmCount);
        }
        return mmCount;
    }

    public int deleteSingle(Long callLogId){
        Log.i("CKIT", "Model::DeleteContacts");
        int retVal=0;

        String filterQuery= CallLog.Calls._ID + "=?";
        String[] idValues=new String[1];
        idValues[0] = callLogId.toString();
        Log.i("CKIT", "Delete Operation ID:" +
                callLogId.toString());

        //Delete one at a time
        try {
            retVal = _cr.delete(CallLog.Calls.CONTENT_URI,
                    filterQuery, idValues);
            SupportFunctions.DebugLog("ModelDelete",
                    "Delete", String.format("Retval:" + retVal));

        }
        catch(SecurityException ex){
            Log.i("CKIT", "Delete Operation Error: " + ex.getMessage());
        }
        catch (Exception ex) {
            Log.i("CKIT", "Delete Operation Error: " + ex.getMessage());
            //Continue deleting others
        }
        return retVal;
    }
}

