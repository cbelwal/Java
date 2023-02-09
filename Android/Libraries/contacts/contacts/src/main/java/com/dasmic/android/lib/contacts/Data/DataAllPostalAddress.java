package com.dasmic.android.lib.contacts.Data;

import android.content.Context;

import com.dasmic.android.lib.contacts.Interface.IExportImport;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 10/1/2015.
 */
public class DataAllPostalAddress implements IExportImport {
    private ArrayList<DataPostalAddress> _postalAddress;
    private static final String CSVAddressGroupSeparator =",~~,";
    public DataAllPostalAddress(){
        _postalAddress = new ArrayList<>();
    }

    public void Add(String STREET,
               String CITY,
               String REGION,
               String COUNTRY,
               String POSTCODE,
               int TYPE){
        _postalAddress.add(new DataPostalAddress(STREET,
                CITY,
                REGION,
                COUNTRY,
                POSTCODE,
                TYPE));
    }

    public int getCount(){return _postalAddress.size();}

    public DataPostalAddress getValue(int idx){
        if(idx < getCount()) return _postalAddress.get(idx);
        return null;
    }

    public String getFormattedString(Context _context){
        String values="";
        for (DataPostalAddress dpa :_postalAddress)
        {
            values = values + "," + dpa.getFormattedString(_context);
        }
        return values;
    }



    public String getVCardString(){
        String values="";
        for (DataPostalAddress dpa :_postalAddress)
        {
            values = values + dpa.getVCardString();
        }
        return values;
    }

    public void setFromVCard40String(String vCardString) {
        DataPostalAddress dpa =
                new DataPostalAddress("","","","","",0);
        dpa.setFromVCard40String(vCardString);
        _postalAddress.add(dpa);
    }

    public void setFromVCard30String(String vCardString) {
        DataPostalAddress dpa =
                new DataPostalAddress("","","","","",0);
        dpa.setFromVCard30String(vCardString);
        _postalAddress.add(dpa);
    }

    public void setFromVCard21String(String vCardString) {
        DataPostalAddress dpa =
                new DataPostalAddress("","","","","",0);
        dpa.setFromVCard21String(vCardString);
        _postalAddress.add(dpa);
    }

    public String getCSVString(){
        String values="";
        for (int ii=0;ii<_postalAddress.size();ii++)
        {
            DataPostalAddress dpa = _postalAddress.get(ii);
            values = values + dpa.getCSVString();
            if(ii < _postalAddress.size()-1)
                values = values + CSVAddressGroupSeparator;
        }
        //Remove , from last
        //if(values != "") values=values.substring(0,values.length()-1);
        return values;
    }

    public void setFromCSVString(String csvString){
        String []values={""};
        if(csvString.contains(CSVAddressGroupSeparator))
            values=csvString.split(CSVAddressGroupSeparator);
        else
            values[0]=csvString;
        for(String value:values)
        {
            DataPostalAddress dpa=new DataPostalAddress("",
                        "","","","",0);
            dpa.setFromCSVString(value);
            _postalAddress.add(dpa);
        }

    }

}
