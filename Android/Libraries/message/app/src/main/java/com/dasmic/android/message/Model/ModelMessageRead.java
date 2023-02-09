package com.dasmic.android.lib.message.Model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.dasmic.android.lib.message.Data.DataMessageDisplay;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 8/5/2017.
 */

public class ModelMessageRead  {
    Context _context;
    private String _sortOrder;

    public ModelMessageRead(Context context){
        _context=context;
    }

    public boolean getCurrentSortOrder() {
        return (_sortOrder.equals(" DESC")) ? true : false;
    }

    public ArrayList<DataMessageDisplay> getAllSmsData(String basicFilter,
                                                       String order ) {
        ArrayList<DataMessageDisplay> allSMS = new ArrayList<>();

        Uri URI = Uri.parse("content://sms/");
        ContentResolver cr = _context.getContentResolver();
        //Calendar cal = Calendar.getInstance(Locale.ENGLISH);

        String filter = basicFilter;
        //Cursor c = cr.query(message, null, null, null, null);
        Cursor cur = cr.query(URI,
                null,
                filter, null,order);// order);

        while (cur.moveToNext()) {
            DataMessageDisplay dmd =
                    new DataMessageDisplay(
                            cur.getString(cur.getColumnIndexOrThrow("_id")),
                            cur.getString(cur.getColumnIndexOrThrow("address")),
                            cur.getString(cur.getColumnIndexOrThrow("body")),
                            cur.getString(cur.getColumnIndexOrThrow("read")),
                            cur.getLong(cur.getColumnIndexOrThrow("date")),
                            cur.getString(cur.getColumnIndexOrThrow("type")));
                allSMS.add(dmd);
        }
        cur.close();

        return allSMS;
    }


    public ArrayList<DataMessageDisplay> getSmsData_orderby_phoneNumber(){


        return getAllSmsData(null,null);
    }

    public ArrayList<DataMessageDisplay> getSmsData_orderby_inOneMonth(){
        return getAllSmsData(null,null);
    }

    public ArrayList<DataMessageDisplay> getSmsData_orderby_sent(){
        return getAllSmsData(null,null);
    }

    public ArrayList<DataMessageDisplay> getSmsData_orderby_received(){
        return getAllSmsData(null,null);
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
}
