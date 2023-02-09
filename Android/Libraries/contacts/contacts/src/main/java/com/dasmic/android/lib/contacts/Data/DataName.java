package com.dasmic.android.lib.contacts.Data;

import android.content.Context;

import com.dasmic.android.lib.contacts.Interface.IExportImport;

/**
 * Created by Chaitanya Belwal on 9/23/2015.
 */
public class DataName implements IExportImport {
    private String _firstName;
    private String _middleName;
    private String _familyName;
    private String _prefix;
    private String _suffix;

    public DataName(String firstName,
                    String middleName,
                    String familyName,
                    String prefix,
                    String suffix){
        _firstName =firstName;
        _middleName =middleName;
        _familyName =familyName;
        _prefix = prefix;
        _suffix=suffix;
    }


    public String getFirstName() {
        if(_firstName==null) return "";
        return _firstName;
    }

    public String getMiddleName(){
        if(_middleName==null) return "";
        return _middleName;
    }

    public String getFamilyName()
    {
        if(_familyName==null) return "";
        return _familyName;
    }

    public String getPrefix()
    {
        if(_prefix==null) return "";
        return _prefix;
    }

    public String getSuffix()
    {
        if(_suffix==null) return "";
        return _suffix;
    }

    public String getFormattedString(Context context){
        return getFirstName() + " " +
                getMiddleName() + " " +
                getFamilyName();
    }

    public String getCSVString(){
        return getFamilyName().trim() + "," +
                getFirstName().trim() + "," +
                getMiddleName().trim();
    }

    //Will be of type Last,First,Middle
    //If middle name is empty its will be last,first,
    //LAst value is ignored
    public void setFromCSVString(String csvString){
        String []values=csvString.split(",");
        if(values == null) return;

        if(values.length > 0)
            if(!values[0].trim().toLowerCase().equals("null"))_familyName= values[0].trim();
        if(values.length > 1)
            if(!values[1].trim().toLowerCase().equals("null"))_firstName= values[1].trim();
        if(values.length > 2)
            if(!values[2].trim().toLowerCase().equals("null"))_middleName= values[2].trim();
    }

    public String getVCardString(){
        return "N:" + getFamilyName() + ";" +
                getFirstName() + ";" +
                getMiddleName()+";" +
                getPrefix()+ ";" +
                getSuffix() + ";" +
                "\r\n";
    }

    public void setFromVCard40String(String vCardString) {
        if(vCardString.equals("")) return;
        String []values=vCardString.split(":");
        if(values.length>=2) {
            values = values[1].split(";");
            if (values.length >= 1) _familyName = values[0].trim();
            if (values.length >= 2) _firstName = values[1].trim();
            if (values.length >= 3) _middleName = values[2].trim();
            if (values.length >= 4) _prefix = values[3].trim();
            if (values.length >= 5) _suffix = values[4].trim();
        }
    }

    public void setFromVCard30String(String vCardString) {
        setFromVCard40String(vCardString);
    }

    //N:Gump;Forrest
    public void setFromVCard21String(String vCardString) {
        setFromVCard40String(vCardString);
    }


    public String getVCardName(){
        String sValue="";
        if(!getFirstName().equals("")) sValue = getFirstName() + " ";
        if(!getMiddleName().equals(""))
            sValue = sValue + getMiddleName() + " ";
        if(!getFamilyName().equals(""))
            sValue = sValue + getFamilyName();
        return sValue.trim();
    }
}
