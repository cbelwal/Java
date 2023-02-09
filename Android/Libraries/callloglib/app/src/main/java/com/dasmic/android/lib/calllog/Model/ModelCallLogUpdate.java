package com.dasmic.android.lib.calllog.Model;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;

import com.dasmic.android.lib.calllog.Data.DataCallLogDisplay;
import com.dasmic.android.lib.support.Static.SupportFunctions;

/**
 * Created by Chaitanya Belwal on 3/11/2017.
 */

public class ModelCallLogUpdate {
    Context _context;

    public ModelCallLogUpdate(Context context){
        _context=context;
    }

    //Update data in Rawcontacts Table
    //Currently not used
    public int updateCallLog_Single(DataCallLogDisplay oldDcld,
                                    DataCallLogDisplay newDcld){
        int count=0;

        ContentValues cValues =new ContentValues();
        cValues.put(CallLog.Calls._ID,oldDcld.getId());
        cValues.put(CallLog.Calls.NUMBER,newDcld.getNumber());
        cValues.put(CallLog.Calls.DATE,newDcld.getDate());
        cValues.put(CallLog.Calls.TYPE,newDcld.getType());
        cValues.put(CallLog.Calls.DURATION,newDcld.getDuration());


        ContentResolver cr = _context.getContentResolver();
        String filterQuery=CallLog.Calls._ID + "=?";
        String[] whereValues=new String[]{String.valueOf(oldDcld.getId())};

        //Main query
        try {
            //URI for Data and RawContacts does not work for SendToVoicemail, hence
            //using top level Contact only
            count = cr.update(CallLog.Calls.CONTENT_URI, cValues,
                    filterQuery,whereValues);
        }
        catch(SecurityException ex){

        }
        catch(Exception ex){
           throw new RuntimeException(ex.getMessage());
        }


        return count;
    }
}
