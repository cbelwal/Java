package com.dasmic.android.lib.contacts.ViewModel;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import com.dasmic.android.lib.contacts.Data.DataContactTransfer;
import com.dasmic.android.lib.contacts.Model.ModelContactsCreate;
import com.dasmic.android.lib.contacts.Model.ModelContactsDelete;
import com.dasmic.android.lib.contacts.Model.ModelContactsRead;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 8/20/2016.
 */
public class ViewModelMergeContacts {
    private Activity _activity;
    private final Handler mHandler;
    public static final int MERGE_COUNT_UPDATE=11;

    private void sendHandlerMessage(int messageIdx,
                                    int arg1){
        if(mHandler==null) return;//Dont do anything
        Message msg = mHandler.obtainMessage(
                messageIdx,arg1,-1);
        mHandler.sendMessage(msg);
    }

    public ViewModelMergeContacts(Activity activity,
                                  Handler handler){
        _activity=activity;
        mHandler=handler;
    }

    //Merge RawContacts
    //Will merge rawContactIds to toRawContactId
    public int mergeContacts(ArrayList<Long> rawContactIds,Long toRawContactId){
        long rawContactId;
        int count=0;
        int status=0;

        if(rawContactIds.size()<=0) return count;

        ArrayList<Long>  rawContactIDList=new ArrayList<>();

        ModelContactsCreate mcc=new ModelContactsCreate(_activity);
        ModelContactsRead mcr=new ModelContactsRead(_activity);
        ModelContactsDelete mcd =  new ModelContactsDelete(_activity,null);

        DataContactTransfer toDct=
                mcr.getTransferData_Single(toRawContactId,7,true);


        for(int idx=0;idx <rawContactIds.size();idx++)
        {
            rawContactIDList.clear();
            rawContactId=rawContactIds.get(idx);
            rawContactIDList.add(rawContactId); //Have to do this as function only takes arrays

            //Store all values in temp ds
            //7 = 4+2+1 to get email, phone and address
            ArrayList<DataContactTransfer> tmpSingleDct=
                    mcr.getContactsForTransfer_RawContacts(rawContactIDList,7);


            //Delete raw record
            status=mcd.DeleteContact(rawContactId);
            SupportFunctions.DebugLog("ViewModelMergeContacts",
                                "mergeContacts",
                                "Deleted Contact:"+rawContactId+":Status:"+status);


            if(status >= 1) //Some records were deleted
            {
                DataContactTransfer dct=tmpSingleDct.get(0);
                status=mcc.createContactDetails_In_Existing_Contact(toRawContactId,
                        dct);
                //Insert record
                count += status;
                sendHandlerMessage(MERGE_COUNT_UPDATE,count);
            }
        }

        mcd=null;
        mcr=null;
        mcc=null;


        return count;
    }

}
