package com.dasmic.android.lib.contacts.Data;

import android.content.Context;
import android.provider.ContactsContract;

import com.dasmic.android.lib.contacts.Enum.ExportImportTypeOptions;
import com.dasmic.android.lib.contacts.Interface.IExportImport;
import com.dasmic.android.lib.contacts.R;

/**
 * Created by Chaitanya Belwal on 9/20/2015.
 */
public class DataPostalAddress implements IExportImport {
    private String _STREET;
    private String _CITY;
    private String _REGION;
    private String _COUNTRY;
    private String _POSTCODE;
    private int _TYPE;

    public DataPostalAddress(String STREET,
                             String CITY,
                             String REGION,
                             String COUNTRY,
                             String POSTCODE,
                             int TYPE){
        _STREET = STREET;
        _CITY=CITY;
        _REGION=REGION;
        _COUNTRY=COUNTRY;
        _POSTCODE=POSTCODE;
        _TYPE=TYPE;
    }

    public String getStreet(){return _STREET;}
    public String getCity(){return _CITY;}
    public String getRegion(){return _REGION;}
    public String getCountry(){return _COUNTRY;}
    public String getPostcode(){return _POSTCODE;}
    public int getType(){return _TYPE;}


    //If used to check if Address is present
    private String getAddress(){
        String address="";

        //Make sure it is \\n as per vCard specification
        //Else Readline() will terminate at \n which is treated as new line
        if(_STREET!=null)
            if(!_STREET.toLowerCase().equals("null"))
                address= address + _STREET +  "\\n";

        if(_CITY!=null)
            if(!_CITY.toLowerCase().equals("null"))
                address = address + _CITY + ", ";

        if(_REGION!=null)
            if(!_REGION.toLowerCase().equals("null"))
                address = address + _REGION +  " ";

        if(_POSTCODE!=null)
            if(!_POSTCODE.toLowerCase().equals("null"))
                address = address + _POSTCODE + "\\n";

        if(_COUNTRY!=null)
            if(!_COUNTRY.toLowerCase().equals("null"))
                address = address + _COUNTRY; //DO not add '\n' here

        return address;
    }

    private String getCSVAddress(){
        String address="";
        address=_STREET +  "," + _CITY +  "," +
                _REGION +  "," +
                _COUNTRY+  "," + _POSTCODE;
        return address;
    }

    public String getFormattedString(Context _context){
        String address="";
        if(getAddress()=="") return ""; //Return blank if no address

        if(_TYPE== ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME)
            address = (String)_context.getResources().getText(R.string.transfer_address_home);
        else
            address = (String)_context.getResources().getText(R.string.transfer_address_office);

        address= address + "\r\n" + getAddress();
        return address;
    }

    public String getVCardString(){
        String address="";
        String  type;
        if(getAddress()=="") return ""; //Return blank if no address

        if(_TYPE== ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME)
            type= ExportImportTypeOptions.home;
        else
            type=ExportImportTypeOptions.work;

        address="ADR;TYPE=" + type+";LABEL=\""+ getAddress()+ "\"" +
                ":" + ";;" + _STREET +";" + _CITY +";" + _REGION + ";"+
                _POSTCODE + ";" + _COUNTRY + "\r\n";

        return address;
    }

    private int getType(String type){
        int iType;
        switch (type.toLowerCase()){
            case ExportImportTypeOptions.work:
                iType=ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK;
                break;
            case ExportImportTypeOptions.home:
                iType=ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME;
                break;
            default:
                iType=ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME;
        }
        return iType;
    }

    //ADR;TYPE=work;LABEL="100 Waters Edge\nBaytown, LA 30314\nUnited States of A
    //merica":;;100 Waters Edge;Baytown;LA;30314;United States of America
    public void setFromVCard40String(String vCardString) {
        setFromVCard30String(vCardString);
    }

    //ADR;TYPE=WORK:;;100 Waters Edge;Baytown;LA;30314;United States of America
    public void setFromVCard30String(String vCardString) {
        String []sValues=vCardString.split(":");
        String []values=null;
        String [] types;
        if(sValues.length>=2)
            values=sValues[1].split(";");
        types=sValues[0].split(";");

        //Figure out config
        String [] sTypes=null;

        if(types.length>=2) sTypes=types[1].split("=");
        if(sTypes != null)
            if(sTypes.length>=2) _TYPE=getType(sTypes[1]);

        if(values != null) {
            if (values.length >= 3) _STREET = values[2].trim();
            if (values.length >= 4) _CITY = values[3].trim();
            if (values.length >= 5) _REGION = values[4].trim();
            if (values.length >= 6) _POSTCODE = values[5].trim();
            if (values.length >= 7) _COUNTRY = values[6].trim();
        }
    }

    //"ADR;WORK:;;100 Waters Edge;Baytown;LA;30314;United States of America"
    public void setFromVCard21String(String vCardString) {
        String []sValues=vCardString.split(":");
        String []values=null;
        String [] types;
        if(sValues.length>=2)
            values=sValues[1].split(";");
        types=sValues[0].split(";");

        //Figure out config
        if(types != null)
            if(types.length>=2)_TYPE=getType(types[1].trim());

        if(values != null) {
            if (values.length >= 3) _STREET = values[2].trim();
            if (values.length >= 4) _CITY = values[3].trim();
            if (values.length >= 5) _REGION = values[4].trim();
            if (values.length >= 6) _POSTCODE = values[5].trim();
            if (values.length >= 7) _COUNTRY = values[6].trim();
        }
    }


    public String getCSVString(){
        String address="";
        if(getAddress()=="") return ""; //Return blank if no address
        if(_TYPE== ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME)
            address = ExportImportTypeOptions.home;
        else
            address =
                    ExportImportTypeOptions.work;
        address=address + "," + getCSVAddress();
        return address;
    }

    public void setFromCSVString(String csvString){
        String []values=csvString.split(",");
        if(values == null) return;
        if(values.length < 6) return; //Some corruption happened
        if(!values[0].trim().toLowerCase().equals("null")) _TYPE = getType(values[0]);
        if(!values[1].trim().toLowerCase().equals("null")) _STREET=values[1].trim();
        if(!values[2].trim().toLowerCase().equals("null")) _CITY=values[2].trim();
        if(!values[3].trim().toLowerCase().equals("null")) _REGION=values[3].trim();
        if(!values[4].trim().toLowerCase().equals("null")) _COUNTRY=values[4].trim();
        if(!values[5].trim().toLowerCase().equals("null")) _POSTCODE=values[5].trim();
    }
}
