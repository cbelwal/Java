package com.dasmic.android.lib.contacts.Data;

import android.content.Context;
import android.provider.ContactsContract;

import com.dasmic.android.lib.contacts.Enum.ExportImportTypeOptions;
import com.dasmic.android.lib.contacts.Interface.IDataValueThreeTuple;
import com.dasmic.android.lib.contacts.Interface.IExportImport;
import com.dasmic.android.lib.contacts.R;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 1/7/2017.
 */

public class DataAllEvent implements IDataValueThreeTuple, IExportImport {
    ArrayList<DataValueThreeTuple<Integer,String,String>> _dvTuple;

    public DataAllEvent(){
        _dvTuple =new ArrayList<>();
    }

    public void Add(int key, String value, String label){
        _dvTuple.add(new DataValueThreeTuple(key, value,label));
    }

    public int getCount(){return _dvTuple.size();}

    public DataValueThreeTuple<Integer,String,String> getValue(int idx){
        if(idx < getCount()) return _dvTuple.get(idx);
        return null;
    }

    public String getFormattedString(Context context){
        String value = "";
        String type = "";

        for(DataValueThreeTuple<Integer,String,String> dvt: _dvTuple) {
            switch (dvt.Key) {
                case ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY:
                    type = (String) context.getResources().getText(R.string.transfer_event_birthday);
                    break;
                case ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY:
                    type = (String) context.getResources().getText(R.string.transfer_event_anniv);
                    break;
                case ContactsContract.CommonDataKinds.Event.TYPE_OTHER:
                    type = (String) context.getResources().getText(R.string.transfer_event_other);
                    break;
                case ContactsContract.CommonDataKinds.Event.TYPE_CUSTOM:
                    type = dvt.Label;
                    break;
                default:
                    type = (String) context.getResources().getText(R.string.transfer_contact_other);
                    break;
            }

            if(dvt.Value !="")
                value = value + type + " " + dvt.Value + "\n";
        }
        return value;
    }

    //BDAY:2017-08-05
    public String getVCardString(){
        String value = "";
        String type = "";

        for(DataValueThreeTuple<Integer,String,String> dvt: _dvTuple) {
                switch (dvt.Key) {
                    case ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY:
                        type = "BDAY";
                        break;
                    case ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY:
                        type = "ANNIVERSARY";
                        break;
                    default:
                        type = "BDAY";
                        break;
                }

                if(dvt.Value !="")
                    value = value + type + ":" + dvt.Value + "\r\n";
            }
            return value;

    }

    private void addData(String type, String value, String label){
        Integer key=0;
        switch (type){
            case ExportImportTypeOptions.event_birthday:
                key=ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY;
                break;
            case ExportImportTypeOptions.event_anniversary:
                key=ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY;
                break;
            case ExportImportTypeOptions.event_other:
                key=ContactsContract.CommonDataKinds.Event.TYPE_OTHER;
                break;
            case ExportImportTypeOptions.event_custom:
                key=ContactsContract.CommonDataKinds.Event.TYPE_CUSTOM;
                break;
            default:
                key=ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY;
        }

        DataValueThreeTuple<Integer,String,String> dvt=
                new DataValueThreeTuple<Integer,String,String>(key,value,label);
        _dvTuple.add(dvt);
    }

    //Will be of forms:
    // BDAY:2017-08-05
    // ANNIVERSARY:2017-08-05
    public void setFromVCard40String(String vCardString) {
        if(vCardString==null) return;
        String []values=vCardString.split(":");
        String type,value;

        if(values.length<1)
            return; //Invalid

        value=values[1].trim();

        switch (values[0].trim()){
            case "BDAY":
                type=ExportImportTypeOptions.event_birthday;
                break;
            case "ANNIVERSARY":
                type=ExportImportTypeOptions.event_anniversary;
                break;
            default:
                type=ExportImportTypeOptions.event_birthday;
        }

        addData(type, value,"");
    }

    //TEL;TYPE=WORK,VOICE:(111) 555-1212
    //TEL;TYPE=WORK:707-453-5140
    public void setFromVCard30String(String vCardString) {
        setFromVCard40String(vCardString);
    }

    //TEL;WORK;VOICE:(111) 555-1212 -OR-
    //TEL;VOICE:(111) 555-1212
    public void setFromVCard21String(String vCardString) {
        setFromVCard40String(vCardString);
    }

    public String getCSVString(){

        throw new RuntimeException("Not implemented");
    }

    public void setFromCSVString(String csvString){
        throw new RuntimeException("Not implemented");
    }
}
