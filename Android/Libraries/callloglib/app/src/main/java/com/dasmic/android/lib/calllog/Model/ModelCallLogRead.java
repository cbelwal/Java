package com.dasmic.android.lib.calllog.Model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;

import com.dasmic.android.lib.calllog.Data.DataCallLogDisplay;
import com.dasmic.android.lib.calllog.ViewModel.ViewModelFilterOption;
import com.dasmic.android.lib.support.Static.DateOperations;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 7/16/2016.
 */
public class ModelCallLogRead {
    private Context _context;
    private String _sortOrder;
    private ViewModelFilterOption _vmFilterOption;

    public ModelCallLogRead(Context context,
                            ViewModelFilterOption vmFilterOption) {
        _context=context;
        _sortOrder = " DESC";
        _vmFilterOption = vmFilterOption;
        if(_vmFilterOption == null)
            _vmFilterOption = new ViewModelFilterOption();
    }

    //Changes the sort order returns true if current sort order is Descending
    public boolean changeSortOrder()
    {
        if(_sortOrder.equals(" DESC"))
            _sortOrder=" ASC";
        else
            _sortOrder=" DESC";

        return  getCurrentSortOrder();
    }

    public boolean getCurrentSortOrder() {
        return (_sortOrder.equals(" DESC")) ? true : false;
    }

    private long getContactId(String phoneNumber){
        long id=0;

        ArrayList<String> projection = new ArrayList<>();
        projection.add(ContactsContract.PhoneLookup._ID);//0
        Uri URI = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber));

        ContentResolver cr = _context.getContentResolver();
        String[] projectionArray = new String[projection.size()];
        projectionArray = projection.toArray(projectionArray);
        //Main query
        Cursor cur = cr.query(URI, projectionArray,null,
                null, null);

        if(cur==null) return 0;
        if (cur.moveToFirst()) {
            do {
                try {
                    id = cur.getLong(cur.getColumnIndex(ContactsContract.PhoneLookup._ID));
                }
                catch(Exception ex)
                {

                }
            }while (cur.moveToNext());
        }
        return id;
    }

    private Uri getContactPicture(String phoneNumber){
        Uri uri=null;

        ArrayList<String> projection = new ArrayList<>();
        projection.add(ContactsContract.PhoneLookup._ID);//0
        projection.add(ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI);//1
        Uri URI = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber));

        ContentResolver cr = _context.getContentResolver();
        String[] projectionArray = new String[projection.size()];
        projectionArray = projection.toArray(projectionArray);
        //Main query
        Cursor cur = cr.query(URI, projectionArray,null,
                null, null);

        if(cur==null) return null;
        if (cur.moveToFirst()) {
            do {
                try {
                    if (cur.getString(cur.getColumnIndex(
                            ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI))
                                        != null) {
                        uri = Uri.parse(
                                cur.getString(cur.getColumnIndex(
                                        ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI)));
                    }
                }
                catch(Exception ex)
                {

                }
            }while (cur.moveToNext());
        }
        return uri;
    }

    //Returns Epoch time reduced by number of days
    private String getReducedDaysInEpoch(String dayCount){
        long days =   Long.parseLong(dayCount);
        long epoch=DateOperations.getCurrentMilliseconds() -
                DateOperations.getMilliSecondForDays(days);
        return String.valueOf(epoch);
    }

    private String getFilterFromOptions(){
        String filterString="";//Default
        String prefix="";


        if(_vmFilterOption.getShowSelection())
            prefix="";
        else
            prefix = "NOT ";

        switch(_vmFilterOption.getCurrentOption()){
            case ViewModelFilterOption.OptionShowFromLocation:
                filterString = prefix + CallLog.Calls.GEOCODED_LOCATION + " Like '"+
                                _vmFilterOption.getOptionValue(ViewModelFilterOption.OptionShowFromLocation)
                                +"'";
                break;
            case ViewModelFilterOption.OptionShowLessThanDays:
                filterString = prefix + CallLog.Calls.DATE + " < " +
                        getReducedDaysInEpoch(_vmFilterOption.getOptionValue(ViewModelFilterOption.OptionShowLessThanDays));
                break;
            case ViewModelFilterOption.OptionShowMoreThanDays:
                filterString = prefix +  CallLog.Calls.DATE + " > " +
                        getReducedDaysInEpoch(_vmFilterOption.getOptionValue(ViewModelFilterOption.OptionShowMoreThanDays));
                break;
            case ViewModelFilterOption.OptionShowLessThanDuration:
                filterString = prefix + CallLog.Calls.DURATION + " < " +
                        _vmFilterOption.getOptionValue(ViewModelFilterOption.OptionShowLessThanDuration);
                break;
            case ViewModelFilterOption.OptionShowMoreThanDuration:
                filterString = prefix + CallLog.Calls.DURATION + " > " +
                        _vmFilterOption.getOptionValue(ViewModelFilterOption.OptionShowMoreThanDuration);
                break;
            default: //Dont add prefix here
                //Will always be satisfied
                filterString=CallLog.Calls._ID + " <> ''";
                break;
        }
        return filterString;

    }

    //--------- Main Function to fetch data
    private ArrayList<DataCallLogDisplay> getDisplayData_CallLog_Table(
            String basicFilter,
            String order,
            int count)
    {
        Uri URI = CallLog.Calls.CONTENT_URI;
        ArrayList<String> projection = new ArrayList<>();
        projection.add(CallLog.Calls._ID);
        projection.add(CallLog.Calls.CACHED_NAME);
        projection.add(CallLog.Calls.DATE);
        projection.add(CallLog.Calls.DURATION);
        projection.add(CallLog.Calls.TYPE);
        projection.add(CallLog.Calls.NUMBER);
        projection.add(CallLog.Calls.GEOCODED_LOCATION);
        projection.add(CallLog.Calls.VOICEMAIL_URI);


        //Filter Section --------------------
        String filter="";
        if(basicFilter != "")
            filter = basicFilter + " AND " + getFilterFromOptions();
        else
            filter=getFilterFromOptions();
         //filter = filter + CallLog.Calls.CACHED_NAME + " NOT LIKE ''";// + //Dont add blank
        //------------------------------------------------
        if(count > 0) //Add limit if valid value
            order = order + " LIMIT " + String.valueOf(count);

        ArrayList<DataCallLogDisplay> values = new ArrayList<>();

        Context context = _context;
        ContentResolver cr = context.getContentResolver();

        String[] projectionArray = new String[projection.size()];
        projectionArray = projection.toArray(projectionArray);
        //Main query
        Cursor cur;
        try {
            cur = cr.query(URI,
                    projectionArray,
                    filter, null, order);
        }
        catch(Exception ex){
            throw ex;
        }

        if(cur==null) return values;
        if (cur.moveToFirst()) {
            do {
                try {
                    Uri photoUri;
                    Uri vmUri;

                    long date = cur.getLong(cur.getColumnIndex(CallLog.Calls.DATE));
                    long duration = cur.getLong(cur.getColumnIndex(CallLog.Calls.DURATION));
                    long callLogId =  cur.getLong(cur.getColumnIndex(CallLog.Calls._ID));
                    vmUri = Uri.parse(CallLog.Calls.VOICEMAIL_URI);
                    String name = cur.getString(cur.getColumnIndex(
                            CallLog.Calls.CACHED_NAME));
                    String number = cur.getString(cur.getColumnIndex(
                            CallLog.Calls.NUMBER));
                    //Upgrade later when API 21 is min.
                    String geoLocation = cur.getString(cur.getColumnIndex(CallLog.Calls.GEOCODED_LOCATION));
                    photoUri = getContactPicture(number);

                    boolean contactAddFlag=false;
                    if(_vmFilterOption.getCurrentOption()==
                            ViewModelFilterOption.OptionShowWithStoredContacts){
                        contactAddFlag= getContactId(number)>0?true:false;
                        contactAddFlag=
                                _vmFilterOption.getShowSelection()?contactAddFlag:!contactAddFlag;
                    }
                    else
                        contactAddFlag=true;

                    int type = cur.getInt(cur.getColumnIndex(CallLog.Calls.TYPE));


                    SupportFunctions.DebugLog("ModelRead",
                            "displayData","Writing for id:"+ callLogId);

                        DataCallLogDisplay singleContact =
                                new DataCallLogDisplay(
                                        callLogId,
                                        type,
                                        date,
                                        duration,
                                        name,
                                        number,
                                        geoLocation,
                                        photoUri
                                        );
                        if(contactAddFlag)
                            values.add(singleContact);
                }
                catch(Exception ex){ //Catch exception in a single contact, move to next
                    SupportFunctions.DebugLog("ModelRead",
                            "displayData",ex.getMessage());
                }

            } while (cur.moveToNext());
        }

        cur.close();
        return values;
    }

    public ArrayList<DataCallLogDisplay> getAllCallLogs(int count) {
        String order = CallLog.Calls.DATE + _sortOrder;
        String filter="";
        return getDisplayData_CallLog_Table(filter,order,count);
    }

    public ArrayList<DataCallLogDisplay> getMissedCallLogs(int count) {
        String order = CallLog.Calls.DATE + _sortOrder;
        String filter = "(" +
                CallLog.Calls.TYPE + " LIKE '" + CallLog.Calls.MISSED_TYPE + "' OR " +
                CallLog.Calls.TYPE + " LIKE ''" + ")";

        return getDisplayData_CallLog_Table(filter,order,count);
    }

    public ArrayList<DataCallLogDisplay> getIncomingCallLogs(int count) {
        String order = CallLog.Calls.DATE + _sortOrder;
        String filter = "(" +
                CallLog.Calls.TYPE + " LIKE '" + CallLog.Calls.INCOMING_TYPE + "')";
        return getDisplayData_CallLog_Table(filter,order,count);
    }


    public ArrayList<DataCallLogDisplay> getOutgoingCallLogs(int count) {
        String order = CallLog.Calls.DATE + _sortOrder;
        String filter = "(" +
                CallLog.Calls.TYPE + " LIKE '" + CallLog.Calls.OUTGOING_TYPE + "')";
        return getDisplayData_CallLog_Table(filter,order,count);
    }

    public ArrayList<DataCallLogDisplay> getAllCallsSortedByDuration(int count) {
        String order = CallLog.Calls.DURATION + _sortOrder;
        String filter="";
        return getDisplayData_CallLog_Table(filter,order,count);
    }

    public ArrayList<DataCallLogDisplay> getAllCallsSortedByGeoLocation(int count) {
        String order = CallLog.Calls.GEOCODED_LOCATION + _sortOrder;
        String filter="";
        return getDisplayData_CallLog_Table(filter,order,count);
    }

    //Returns a string containing CLM Data for all contacts
    public String getCLMDataAllLogs(int maxCount){
        ArrayList<DataCallLogDisplay> allCallLogs = getAllCallLogs(maxCount);
        StringBuilder sb = new StringBuilder();
        //Get the CLM String from each
        for(DataCallLogDisplay dcld:allCallLogs)
        {
            sb.append(dcld.getCLMString()); //\r\n is auto added
        }

        return sb.toString();
    }

}
