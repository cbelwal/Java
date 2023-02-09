package com.dasmic.android.lib.contacts.Data;

import android.content.Context;
import android.provider.ContactsContract;

import com.dasmic.android.lib.contacts.Enum.ExportImportTypeOptions;
import com.dasmic.android.lib.contacts.R;

/**
 * Created by Chaitanya Belwal on 9/24/2016.
 */
public class DataOrganization {
    private String _company;
    private String _department;
    private String _title;

    public DataOrganization(String company,
                            String department,
                            String title)
    {
        _company=company;
        _department=department;
        _title=title;
    }

    public DataOrganization()
    {
    }

    public String getVCardString(){
        String values="";
        if(_company != null || _department != null) {
            values="ORG:";
            if(_company != null)
                values = values + _company;
            if(_department != null)
                values = values + ";"+ _department ;

            values=values + DataContactTransfer.CrLf;
        }
        if(_title != null)
            values = values  +
                     "TITLE:" + _title + DataContactTransfer.CrLf;
        return values;
    }

    public String getCSVString(){
        return _company + "," + _department + "," + _title;
    }

    public String getFormattedString(Context _context){
        if(_company == null && _department == null
                && _title==null) return "";
        String values=_context.getString(R.string.transfer_organization);
        if(_company != null && _department != null) {
            if(_company != null)
                values = values + _company + ",";
            if(_department != null)
                values = values + _department ;

            values=values + DataContactTransfer.CrLf;
        }
        if(_title != null)
            values = values  +
                    _context.getString(R.string.transfer_title) + _title + DataContactTransfer.CrLf;
        return values;
    }

    public void setFromCSVString(String csvString){
        String []values=csvString.split(",");
        if(values == null) return;
        if(values.length < 3) return;
        if(!values[0].trim().toLowerCase().equals("null")) _company= values[0].trim();
        if(!values[1].trim().toLowerCase().equals("null")) _department= values[1].trim();
        if(!values[2].trim().toLowerCase().equals("null")) _title= values[2].trim();
    }

    //ORG:Company;Department
    public void setOrgFromVCardString(String vCardString) {
        if(vCardString.equals("")) return;
        String []values=vCardString.split(":");
        if(values.length>=2) {
            values = values[1].split(";");
            if (values.length >= 1) _company = values[0].trim();
            if (values.length >= 2) _department = values[1].trim();
        }
    }

    public void setTitleFromVCardString(String vCardString) {
        if(vCardString.equals("")) return;
        String []values=vCardString.split(":");
        if(values.length>=2) {
            _title=values[1].trim();
        }
    }

}
