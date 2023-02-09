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
public class DataAllPhone implements IDataValuePair,IExportImport {
    ArrayList<DataValuePair<Integer,String>> _dvPair;

    public DataAllPhone(){
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

    public String getFormattedString(Context context){
        String value = "";
        String type = "";

        for(DataValuePair<Integer,String> dvp:_dvPair) {
            switch (dvp.Key) {
                case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                    type = (String) context.getResources().getText(R.string.transfer_contact_home);
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                    type = (String) context.getResources().getText(R.string.transfer_contact_office);
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                    type = (String) context.getResources().getText(R.string.transfer_contact_mobile);
                    break;
                default:
                    type = (String) context.getResources().getText(R.string.transfer_contact_other);
                    break;
            }

            if(dvp.Value !="")
                value = value + type + " " + dvp.Value + "\n";
        }
        return value;
    }

    public String getVCardString(){
        String value = "";
        String type = "";

        for(DataValuePair<Integer,String> dvp:_dvPair) {
            switch (dvp.Key) {
                case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                    type = "home,voice";
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                    type = "work,voice";
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                    type = "cell,text";
                    break;
                default:
                    type = "home,voice";
                    break;
            }

            if(dvp.Value !="") {
                value = value + "TEL;TYPE="+ type + ";" +
                        "VALUE=uri:tel:"+dvp.Value + "\r\n";
            }
        }
        return value;
    }

    private void addData(String type, String value){
        Integer key=0;
        switch (type.toLowerCase()){
            case ExportImportTypeOptions.work:
                key=ContactsContract.CommonDataKinds.Phone.TYPE_WORK;
                break;
            case ExportImportTypeOptions.home:
                key=ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
                break;
            case ExportImportTypeOptions.cell:
                key=ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
                break;
            default:
                key=ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
        }

        DataValuePair<Integer,String> dvp=
                new DataValuePair<Integer,String>(key,value);
        _dvPair.add(dvp);
    }

    //TEL;TYPE=work,voice;VALUE=uri:tel:+11115551212
    // OR
    //TEL:+11115551212
    public void setFromVCard40String(String vCardString) {
        String []values=vCardString.split(";");
        String [] sTypes=null,pValues=null,types=null;
        String type="", value="";
        Integer key=0;

        type=ExportImportTypeOptions.home;//default type
        if(values.length>=2) sTypes=values[1].split(",");
        if(sTypes != null)
            if(sTypes.length>=2) types=sTypes[0].split("=");
        if(types != null)
            if(types.length>=2) type=types[1].trim();

        if(values.length>=3)
            pValues=values[2].split(":");
        else if(values.length==1) //For TEL:+11115551212
            pValues=values[0].split(":");

        if(pValues != null)
            if(pValues.length>=3)
                value=pValues[2]; //Add phone string
            else
                value=pValues[1];

        addData(type, value);
    }

    //TEL;TYPE=WORK,VOICE:(111) 555-1212
    //TEL;TYPE=WORK:707-453-5140
    public void setFromVCard30String(String vCardString) {
        String []values=vCardString.split(";");
        String [] sTypes=null,sValues=null,types=null;
        String type="", value="";

        if(values.length>=2) sValues=values[1].split(":");
        if(sValues.length>=1) sTypes=sValues[0].split(",");
        if(sTypes.length>=2) {
            types = sTypes[0].split("=");
            if (types.length >= 2) type = types[1].trim();
        }
        if(sValues.length>=2) value=sValues[1];

        addData(type, value);

    }

    //TEL;WORK;VOICE:(111) 555-1212 -OR-
    //TEL;VOICE:(111) 555-1212
    public void setFromVCard21String(String vCardString) {
        String []values=vCardString.split(";");
        String [] sTypes=null,sValues=null,types=null;
        String type="", value="";
        Integer key=0;

        if(values.length>=3) { //TEL;WORK;VOICE:(111) 555-1212
            type=values[1].trim();
            sValues=values[2].split(":");
        }
        else{ //TEL;VOICE:(111) 555-1212
            sValues=values[1].split(":");
        }
        if(sValues.length>=2) value=sValues[1];
        addData(type, value);
    }

    public String getCSVString(){
        String value = "";
        String type = "";

        for(DataValuePair<Integer,String> dvp:_dvPair) {
            switch (dvp.Key) {
                case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                    type = ExportImportTypeOptions.home;
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                    type = ExportImportTypeOptions.work;
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                    type = ExportImportTypeOptions.cell;
                    break;
                default:
                    type = ExportImportTypeOptions.other;
                    break;
            }


            value = value +type + "," + dvp.Value + ",";
        }
        //Remove , from last
        if(value != "") value=value.substring(0,value.length()-1);
        return value;
    }

    public void setFromCSVString(String csvString){
        String []values=csvString.split(",");
        for(int ii=0;ii<values.length;ii+=2)
        {
            addData(values[ii],values[ii+1]);
        }
    }
}
