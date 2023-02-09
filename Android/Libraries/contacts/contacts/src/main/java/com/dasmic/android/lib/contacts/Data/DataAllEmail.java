package com.dasmic.android.lib.contacts.Data;

import android.content.Context;
import android.provider.ContactsContract;

import com.dasmic.android.lib.contacts.Data.DataValuePair;
import com.dasmic.android.lib.contacts.Enum.ExportImportTypeOptions;
import com.dasmic.android.lib.contacts.Interface.IDataValuePair;
import com.dasmic.android.lib.contacts.Interface.IExportImport;
import com.dasmic.android.lib.contacts.R;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 9/30/2015.
 */
public class DataAllEmail implements IDataValuePair,IExportImport {
    private ArrayList<DataValuePair<Integer,String>> _dvPair;
    public DataAllEmail(){
       _dvPair=new ArrayList<>();
    }

    public void Add(int key, String value){
        _dvPair.add(new DataValuePair(key,value));
    }

    public int getCount(){return _dvPair.size();}

    public DataValuePair<Integer,String> getValue(int idx){
        if(idx < getCount()) return _dvPair.get(idx);
        return null;
    }

    public String getFormattedString(Context context) {
        String value = "";
        String type = "";


        for(DataValuePair<Integer,String> dvp:_dvPair) {
            switch (dvp.Key) {
                case ContactsContract.CommonDataKinds.Email.TYPE_HOME:
                    type = (String) context.getResources().getText(R.string.transfer_contact_home);
                    break;
                case ContactsContract.CommonDataKinds.Email.TYPE_WORK:
                    type = (String) context.getResources().getText(R.string.transfer_contact_office);
                    break;
                default:
                    type = (String) context.getResources().getText(R.string.transfer_contact_other);
                    break;
            }
            if(dvp.Value != "" && dvp.Value != null)
                value = value + type + " " + dvp.Value + "\n";
        }

        return value;
    }

    private void addData(String type, String value){
        Integer key=0;
        switch (type.toLowerCase()){
            case ExportImportTypeOptions.work:
                key=ContactsContract.CommonDataKinds.Email.TYPE_WORK;
                break;
            case ExportImportTypeOptions.home:
                key=ContactsContract.CommonDataKinds.Email.TYPE_HOME;
                break;
            default:
                key=ContactsContract.CommonDataKinds.Email.TYPE_HOME;
        }

        DataValuePair<Integer,String> dvp=
                new DataValuePair<Integer,String>(key,value);
        _dvPair.add(dvp);
    }

    public String getVCardString() {
        String value = "";
        String type = "";

        for(DataValuePair<Integer,String> dvp:_dvPair) {
            switch (dvp.Key) {
                case ContactsContract.CommonDataKinds.Email.TYPE_HOME:
                    type = ExportImportTypeOptions.home;
                    break;
                case ContactsContract.CommonDataKinds.Email.TYPE_WORK:
                    type = ExportImportTypeOptions.work;
                    break;
                default:
                    type = ExportImportTypeOptions.home;
                    break;
            }
            if(dvp.Value != "") {
                value = value +"EMAIL;TYPE="+ type + ":"
                        + dvp.Value + "\r\n";
            }
        }
        return value;
    }

    public void setFromVCard40String(String vCardString) {
        String []values=vCardString.split(";");
        String [] sTypes={""},types={""};
        String type="", value="";
        Integer key=0;

        if(values.length>=2) {
            sTypes=values[1].split(":");
            if(sTypes.length>=2) types=sTypes[0].split("=");
            if(types.length>=2) type=types[1].trim();
        }
        else{ //Email is in format EMAIL:amaindoliya@gmail.com
            sTypes=values[0].split(":");
        }
        if(sTypes.length>=2) value=sTypes[1];

        addData(type, value);
    }

    //EMAIL;TYPE=PREF,INTERNET:forrestgump@example.com
    public void setFromVCard30String(String vCardString) {
        String []values=vCardString.split(";");
        String [] sTypes={""},types={""};
        String type="", value="";
        Integer key=0;

        if(values.length>=2) {
            sTypes=values[1].split(":");
            if(sTypes.length>=2) types=sTypes[0].split("=");
            if(types.length>=2) type=types[1].trim();
        }
        else{ //Email is in format EMAIL:amaindoliya@gmail.com
            sTypes=values[0].split(":");
        }
        if(sTypes.length>=2) value=sTypes[1];
        addData(type, value);
    }

    //EMAIL;PREF;INTERNET:forrestgump@example.com
    //EMAIL;PREF:abelwal@gmail.com"
    public void setFromVCard21String(String vCardString) {
        String type="", value="";
        String []sValues=vCardString.split(":"); //Lets get values out
        if (sValues.length>=2) value=sValues[1];

        sValues = sValues[0].split(";");
        if(sValues.length>=2) type=sValues[1].trim();

        addData(type, value);
    }

    public String getCSVString() {
        String value = "";
        String type = "";

        for(DataValuePair<Integer,String> dvp:_dvPair) {
            switch (dvp.Key) {
                case ContactsContract.CommonDataKinds.Email.TYPE_HOME:
                    type = ExportImportTypeOptions.home;
                    break;
                case ContactsContract.CommonDataKinds.Email.TYPE_WORK:
                    type = ExportImportTypeOptions.work;
                    break;
                default:
                    type = ExportImportTypeOptions.home;
                    break;
            }
            //Always have the value columns in CSV files
            value = value + type + "," + dvp.Value + ",";
        }
        //Remove , from last
        if(value != "") value=value.substring(0,value.length()-1);
        return value;
    }

    public void setFromCSVString(String csvString){
        if(csvString.equals("")) return;
        String []values=csvString.split(",");
        for(int ii=0;ii<values.length;ii+=2)
        {
            addData(values[ii],values[ii+1]);
        }
    }

}
