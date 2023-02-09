package com.dasmic.android.lib.contacts.Data;

import android.content.Context;
import android.provider.ContactsContract;

import com.dasmic.android.lib.contacts.Enum.ExportImportTypeOptions;
import com.dasmic.android.lib.contacts.Interface.IDataValuePair;
import com.dasmic.android.lib.contacts.Interface.IExportImport;
import com.dasmic.android.lib.contacts.R;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 9/30/2015.
 */
public class DataAllWhatsApp implements IDataValuePair,IExportImport {
    ArrayList<DataValuePair<Integer,String>> _dvPair;

    public DataAllWhatsApp(){
        _dvPair=new ArrayList<>();
    }

    public void Add(int key, String value){
        _dvPair.add(new DataValuePair(key, value));
    }

    public int getCount(){return _dvPair.size();}

    public DataValuePair<Integer,String> getValue(int idx){
        if(idx < getCount()) return _dvPair.get(idx);
        return null;
    }

    //Not supported for WhatsApp
    public String getFormattedString(Context context){
        String value = "";

        return value;
    }

    public String getVCardString(){
        String value = "";

         for(DataValuePair<Integer,String> dvp:_dvPair) {
            if(dvp.Value !="") {
                value = value + "X-DASMIC-WHATSAPP;" +
                        dvp.Value + "\r\n";
            }
        }
        return value;
    }

    //Assumption is that there is only one WhatsApp contact
    private void addData(String type, String value){
        Integer key=0;
        DataValuePair<Integer,String> dvp=
                new DataValuePair<Integer,String>(key,value);
        _dvPair.add(dvp);
    }

    //X-DASMIC-WHATSAPP;11115551212@whatsapp.com
    public void setFromVCard40String(String vCardString) {
        String value="";
        String []values=vCardString.split(";");
        Integer key=0;

        if(values.length>=2) value=values[1];

        addData("", value); //Type is Ignored
    }

    //Not Supported
    public void setFromVCard30String(String vCardString) {
        throw new RuntimeException("This function is not Supported");
    }

    //Not Supported
    public void setFromVCard21String(String vCardString) {
        throw new RuntimeException("This function is not Supported");
    }

    //Not supported
    public String getCSVString(){
        return "";
    }

    //Not supported
    public void setFromCSVString(String csvString){
        throw new RuntimeException("This function is not Supported");
    }
}
