package com.dasmic.android.lib.contacts.Model;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.provider.ContactsContract;
import android.util.Log;

import com.dasmic.android.lib.support.Static.DateOperations;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 9/9/2015.
 */
public class ModelContactsUpdate {
    Context _context;

    private int updateValuesForSingleContact_Contacts_Table(Long contactID,
                                                            ContentValues cValues){
        int retVal=0;
        ContentResolver cr = _context.getContentResolver();
        String filterQuery=ContactsContract.Contacts._ID + "="+
                String.valueOf(contactID);

        //Main query
        try {
            //URI for Data and RawContacts does not work for SendToVoicemail, hence
            //using top level Contact only
            retVal = cr.update(ContactsContract.Contacts.CONTENT_URI, cValues,
                    filterQuery,null);
        }
        catch(Exception ex){
            Log.i("CKIT", "Update Operation Error. ContactID " + String.valueOf(contactID) +
                    " Message:" + ex.getMessage());
        }
        return retVal;
    }


    private int updateValuesForSingleContact_Data_Table(Long contactID, String mimeType,
                                                        ContentValues cValues){
        int retVal=0;
        ContentResolver cr = _context.getContentResolver();
        String filterQuery=ContactsContract.Data.MIMETYPE + " LIKE '" +
                mimeType + "' AND " + ContactsContract.Data.RAW_CONTACT_ID + "="+
                String.valueOf(contactID);


        //Main query
        try {
            //URI for Data and RawContacts does not work for SendToVoicemail, hence
            //using top level Contact only
            retVal = cr.update(ContactsContract.Data.CONTENT_URI, cValues,
                    filterQuery,null);
        }
        catch(Exception ex){
            SupportFunctions.DebugLog("ModelUpdate", "updateValuesForSingleContact_Data_Table",
                        "Exception: " + ex.getMessage());
        }
        return retVal;
    }

    //Do for a Single contact so that there is no limit on number of contacts that can be updated
    private int UpdateValues(ArrayList<Long> contactIDList,ContentValues cValues){
        int retVal=0;
        int idx=0;

        for(idx=0;idx<contactIDList.size();idx++){
            retVal+= updateValuesForSingleContact_Contacts_Table(contactIDList.get(idx),cValues);
        }
        return retVal;
    }

    public ModelContactsUpdate(Context context){
        _context=context;
    }

    public int undeleteContacts(ArrayList<Long> rawContactIDList){
        int retVal=0;
        for(int idx=0;idx<rawContactIDList.size();idx++){
            retVal+= unDeleteSingleContact(rawContactIDList.get(idx));
        }
        return retVal;
    }

    public int setSendtoVoiceMail(ArrayList<Long> contactIDList, boolean value){
        int fValue=value?1:0;

        Log.i("CKIT", "setSendtoVoiceMail");

        ContentValues vmValues =new ContentValues();
        vmValues.put(ContactsContract.Contacts.SEND_TO_VOICEMAIL, fValue);

        return UpdateValues(contactIDList, vmValues);
    }

    public int setStarred(ArrayList<Long> contactIDList, boolean value){
        int fValue=value?1:0;

        Log.i("CKIT", "setStarred");

        ContentValues vmValues = new ContentValues();
        vmValues.put (ContactsContract.Contacts.STARRED, fValue);

        return UpdateValues(contactIDList, vmValues);
    }

    public int setStarred(Long contactID, int value){
        int fValue=value;
        ContentValues vmValues = new ContentValues();
        vmValues.put (ContactsContract.Contacts.STARRED, fValue);

        return updateValuesForSingleContact_Contacts_Table(contactID, vmValues);
    }

    public int setTimesContacted(Long contactID,int value){
        int fValue=value;

        ContentValues vmValues = new ContentValues();
        vmValues.put (ContactsContract.Contacts.TIMES_CONTACTED, fValue);

        return updateValuesForSingleContact_Contacts_Table(contactID, vmValues);
    }

    public int setLastTimeContacted(Long contactID, long value){
        String fValue=String.valueOf(value);

        ContentValues vmValues = new ContentValues();
        vmValues.put (ContactsContract.Contacts.LAST_TIME_CONTACTED, fValue);

        return updateValuesForSingleContact_Contacts_Table(contactID, vmValues);
    }

    public int resetTimesContacted(ArrayList<Long> contactIDList){
        int fValue=0;
        Log.i("CKIT", "resetTimesContacted");

        ContentValues vmValues = new ContentValues();
        vmValues.put (ContactsContract.Contacts.TIMES_CONTACTED, fValue);

        return UpdateValues(contactIDList, vmValues);
    }

    public int setTimesContactedToNow(ArrayList<Long> contactIDList){
        String fValue=String.valueOf(DateOperations.getCurrentMilliseconds());
        Log.i("CKIT", "resetTimesContacted");

        ContentValues vmValues = new ContentValues();
        vmValues.put (ContactsContract.Contacts.LAST_TIME_CONTACTED, fValue);

        return UpdateValues(contactIDList, vmValues);
    }

    public int unDeleteSingleContact(Long rawContactID){
        int retVal=0;
        ContentValues cValues =new ContentValues();
        cValues.put(ContactsContract.RawContacts.DELETED,0);

        ContentResolver cr = _context.getContentResolver();
        String filterQuery=ContactsContract.RawContacts._ID + "="+
                String.valueOf(rawContactID);

        //Main query
        try {
            //URI for Data and RawContacts does not work for SendToVoicemail, hence
            //using top level Contact only
            retVal = cr.update(ContactsContract.RawContacts.CONTENT_URI, cValues,
                    filterQuery,null);
        }
        catch(Exception ex){
            Log.i("CKIT", "Update Operation Error. ContactID " + String.valueOf(rawContactID) +
                    " Message:" + ex.getMessage());
        }
        return retVal;
    }

    //Update data in Rawcontacts Table
    //Currently not used
    public int updateRawContactId_DataTable(long fromID, long toID){
        int count=0;

        SupportFunctions.DebugLog("ModelUpdate","updateRawContactId_DataTable",
                "From:" + fromID +",To:"+toID);


        ContentValues cValues =new ContentValues();
        cValues.put(ContactsContract.Data.RAW_CONTACT_ID,toID);
        //cValues.put(ContactsContract.Data.DATA5,0);
        cValues.put(ContactsContract.Data.IS_PRIMARY,0);
        cValues.put(ContactsContract.Data.IS_READ_ONLY,0);

        ContentResolver cr = _context.getContentResolver();
        String filterQuery=ContactsContract.Data.RAW_CONTACT_ID + "=? AND "+
                 ContactsContract.Data.MIMETYPE + "<> ?";
        String[] whereValues=new String[]{String.valueOf(fromID),ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME};

        //Main query
        try {
            //URI for Data and RawContacts does not work for SendToVoicemail, hence
            //using top level Contact only
            count = cr.update(ContactsContract.Data.CONTENT_URI, cValues,
                    filterQuery,whereValues);
        }
        catch(Exception ex){
            Log.i("CKIT", "Update RawContactId Error. ContactID " +
                    String.valueOf(fromID) +
                    " Message:" + ex.getMessage());
        }
        SupportFunctions.DebugLog("ModelUpdate","updateRawContactId_DataTable",
                "Update Count:"+count);

        return count;
    }

    public int setNote(Long rawContactID, String value){
        ContentValues vmValues = new ContentValues();
        vmValues.put (ContactsContract.CommonDataKinds.Note.NOTE, value);

        return updateValuesForSingleContact_Data_Table(rawContactID,
                ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE,
                vmValues);
    }

}
